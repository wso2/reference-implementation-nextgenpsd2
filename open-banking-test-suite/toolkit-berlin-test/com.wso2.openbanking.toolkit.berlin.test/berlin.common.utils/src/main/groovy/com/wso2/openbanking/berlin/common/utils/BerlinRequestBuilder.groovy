/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.common.utils

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
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.model.AccessTokenJwtDto
import com.wso2.openbanking.test.framework.model.ApplicationAccessTokenDto
import com.wso2.openbanking.test.framework.request.AccessToken
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification

import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.logging.Logger

/**
 * Berlin Automation Helper
 */
class BerlinRequestBuilder {

    static log = Logger.getLogger(BerlinRequestBuilder.class.toString())

    /**
     * Get Application Access Token
     *
     * @param authMethod authentication method
     * @param scopes scopes for token
     * @return access token
     */
    static String getApplicationToken(BerlinConstants.AUTH_METHOD authMethod, BerlinConstants.SCOPES scopes) {

        def tokenDTO = new ApplicationAccessTokenDto()
        tokenDTO.setScopes(scopes.getScopes())

        def tokenResponse = AccessToken.getApplicationAccessToken(tokenDTO)
        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

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
    static String getUserToken(BerlinConstants.AUTH_METHOD authMethod, BerlinConstants.SCOPES scopes, CodeVerifier verifier, String code) {

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

        def response = TestSuite.buildRequest()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .post(TestConstants.TOKEN_ENDPOINT)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        def accessToken = TokenResponse.parse(httpResponse).toSuccessResponse().tokens.accessToken

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

        return TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${accessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}@${config.getTenantDomain()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
    }

    static String getCurrentDate() {
        return new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z").format(new Date())
    }

    /**
     * Build Request Specification for KeyManager Requests.
     *
     * @param accessToken
     * @param xRequestId
     * @return RequestSpecification with basic request structure
     */
    static RequestSpecification buildKeyManagerRequest(String accessToken = null, String xRequestId = null) {

        def config = ConfigParser.getInstance()

        if(accessToken == null) {
            def authToken = "${config.keyManagerAdminUsername}:${config.keyManagerAdminPassword}"
            accessToken = "${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset().toString()))}"
        }

        if (xRequestId == null) {
            xRequestId = UUID.randomUUID().toString()
        }

        return TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Basic ${accessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.instance.authorisationServerURL)
    }
}
