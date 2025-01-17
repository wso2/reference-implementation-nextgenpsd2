/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.berlin.test.framework.request_builder

import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.berlin.test.framework.filters.BerlinSignatureFilter
import com.wso2.berlin.test.framework.utility.BerlinTestUtil
import com.wso2.openbanking.test.framework.configuration.OBConfigParser
import com.wso2.openbanking.test.framework.request_builder.SignedObject
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

import java.text.SimpleDateFormat
import java.util.logging.Logger

/**
 * Berlin Automation Helper
 */
class BerlinRequestBuilder  {

    static log = Logger.getLogger(BerlinRequestBuilder.class.toString())
    private static BGConfigurationService bgConfiguration = new BGConfigurationService()


    /**
     * Method for get application access token
     * @param scopes
     * @param clientId
     * @return
     */
    static String getApplicationAccessToken(List<String> scopes, String clientId) {
        BGJWTGenerator auJwtGenerator = new BGJWTGenerator()
        auJwtGenerator.setScopes(scopes)
        String jwt = auJwtGenerator.getAppAccessTokenJwt(clientId)

        RestAssured.baseURI = bgConfiguration.getServerAuthorisationServerURL()
        Response response = BGRestAsRequestBuilder.buildRequest().contentType(BerlinConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(jwt)
                .post(BerlinConstants.TOKEN_ENDPOINT)

        def accessToken = BerlinTestUtil.parseResponseBody(response, "access_token")
        log.info("Got access token $accessToken")

        return accessToken

    }

    /**
     * Get User Access Token From Authorization Code.
     *
     * @param code authorisation code
     * @param client_id
     * @return token response
     */
    static AccessTokenResponse getUserToken(String code, String clientId = null) {

        AuthorizationCode grant = new AuthorizationCode(code)
        URI callbackUri = new URI(bgConfiguration.getAppInfoRedirectURL())
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri)

        String assertionString = new SignedObject().getJwt(clientId)

        ClientAuthentication clientAuth = new PrivateKeyJWT(SignedJWT.parse(assertionString))

        URI tokenEndpoint = new URI
                ("${bgConfiguration.getServerAuthorisationServerURL()}${BerlinConstants.TOKEN_ENDPOINT}")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = BGRestAsRequestBuilder.buildRequest()
                .contentType(BerlinConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .post(tokenEndpoint)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        return TokenResponse.parse(httpResponse).toSuccessResponse()

    }
    /**
     * Build Basic Request.
     *
     * @param accessToken access token for access control
     * @return RequestSpecification with basic request structure
     */
    static RequestSpecification buildBasicRequest(String accessToken, String xRequestId = null) {

        def config = OBConfigParser.getInstance()

        if (xRequestId == null) {
            xRequestId = UUID.randomUUID().toString()
        }

        return BGRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(BerlinConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${accessToken}")
                .header(BerlinConstants.PSU_ID, "${bgConfiguration.getUserPSUName()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(bgConfiguration.getServerBaseURL())
    }

    static String getCurrentDate() {
        return new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z").format(new Date())
    }

}

