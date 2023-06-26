/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework.request_builder

import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.AuthorizationCode
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant
import com.nimbusds.oauth2.sdk.AuthorizationGrant
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.TokenResponse
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import com.wso2.berlin.test.framework.configuration.AppConfigReader
import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.berlin.test.framework.configuration.ConfigParser
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.berlin.test.framework.filters.BerlinSignatureFilter
import com.wso2.berlin.test.framework.model.AccessToken
import com.wso2.berlin.test.framework.model.AccessTokenJwtDto
import com.wso2.berlin.test.framework.model.ApplicationAccessTokenDto
import com.wso2.berlin.test.framework.utility.BerlinTestUtil
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
    static String accessTokenScope
    static String refreshToken
    static int expiryTime
    private static BGConfigurationService bgConfiguration = new BGConfigurationService()


//    /**
//     * Get Application Access Token
//     *
//     * @param authMethod authentication method
//     * @param scopes scopes for token
//     * @return access token
//     */
//    static String getApplicationToken(BerlinConstants.AUTH_METHOD authMethod, BerlinConstants.SCOPES scopes) {
//
//        def tokenDTO = new ApplicationAccessTokenDto()
//        tokenDTO.setScopes(scopes.getScopes())
//
//        def tokenResponse = AccessToken.getApplicationAccessToken(tokenDTO, AppConfigReader.getClientId())
//        def accessToken = BerlinTestUtil.parseResponseBody(tokenResponse, "access_token")
//
//        log.info("Got access token $accessToken")
//
//        return accessToken
//
//    }

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
     * Get User Access Token
     *
     * @param authMethod authentication method
     * @param scopes scopes for token
     * @return access token
     */
    static String getUserToken(CodeVerifier verifier, String code) {

        def config = ConfigParser.getInstance()

        AuthorizationCode grant = new AuthorizationCode(code)
        URI callbackUri = new URI(AppConfigReader.getRedirectURL())
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri, verifier)

        ClientID clientID = new ClientID(AppConfigReader.getClientId())

        String assertionString = new AccessTokenJwtDto().getJwt()

        ClientAuthentication clientAuth = new PrivateKeyJWT(SignedJWT.parse(assertionString))

        URI tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = BGRestAsRequestBuilder.buildRequest()
                .contentType(BerlinConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
                .post(BerlinConstants.TOKEN_ENDPOINT)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        def accessToken = TokenResponse.parse(httpResponse).toSuccessResponse().tokens.accessToken
        accessTokenScope = BerlinTestUtil.parseResponseBody(response, "scope")
        refreshToken = TokenResponse.parse(httpResponse).toSuccessResponse().tokens.refreshToken

        log.info("Got user access token $accessToken")

        return accessToken
    }

    /**
     * Build Basic Request.
     *
     * @param accessToken access token for access control
     * @return RequestSpecification with basic request structure
     */
    static RequestSpecification buildBasicRequest(String accessToken, String xRequestId = null) {

        def config = ConfigParser.getInstance()

        if (xRequestId == null) {
            xRequestId = UUID.randomUUID().toString()
        }

        return BGRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(BerlinConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${accessToken}")
                .header(BerlinConstants.PSU_ID, "${ConfigParser.getUserPSUName()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(config.getBaseURL())
    }

    static String getCurrentDate() {
        return new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z").format(new Date())
    }

}

