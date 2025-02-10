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

package com.wso2.openbanking.berlin.common.utils

import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import com.nimbusds.oauth2.sdk.token.RefreshToken
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.model.AccessTokenJwtDto
import com.wso2.openbanking.test.framework.model.ApplicationAccessTokenDto
import com.wso2.openbanking.test.framework.request.AccessToken
import com.wso2.openbanking.test.framework.util.*
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.logging.Logger

/**
 * Berlin Automation Helper
 */
class BerlinRequestBuilder {

    static log = Logger.getLogger(BerlinRequestBuilder.class.toString())
    static String accessTokenScope
    static String refreshToken
    static int expiryTime

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

        def tokenResponse = AccessToken.getApplicationAccessToken(tokenDTO, AppConfigReader.getClientId())
        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        log.info("Got access token $accessToken")

        return accessToken

    }

    /**
     * Get Application Access Token
     *
     * @param authMethod authentication method
     * @param scopes scopes for token
     * @param clientId
     * @return access token
     */
    static String getApplicationToken(BerlinConstants.AUTH_METHOD authMethod, BerlinConstants.SCOPES scopes,
                                      String clientId) {

        def tokenDTO = new ApplicationAccessTokenDto()
        tokenDTO.setScopes(scopes.getScopes())

        def tokenResponse = AccessToken.getApplicationAccessToken(tokenDTO, clientId)
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
    static String getUserToken(CodeVerifier verifier, String code) {

        def config = ConfigParser.getInstance()

        AuthorizationCode grant = new AuthorizationCode(code)
        URI callbackUri = new URI(AppConfigReader.getRedirectURL())
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri, verifier)

        ClientID clientID = new ClientID(AppConfigReader.getClientId())

        URI tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientID, codeGrant);

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = TestSuite.buildRequest()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
                .post(TestConstants.TOKEN_ENDPOINT)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        def accessToken = TokenResponse.parse(httpResponse).toSuccessResponse().tokens.accessToken
        accessTokenScope = TestUtil.parseResponseBody(response, "scope")
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

        return TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${accessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(config.getBaseURL())
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

    /**
     * Get Refresh Token Grant User Access Token
     * @param refreshToken - refresh token of the previous user access token request
     * @param scopes - scopes
     * @return response - response of the access token call
     */
    static Response getRefreshTokenGrantAccessToken(refreshToken, BerlinConstants.SCOPES scopes) {

        def config = ConfigParser.getInstance()

        RefreshToken refreshTokenValue = new RefreshToken(refreshToken)
        AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(refreshTokenValue)

        ClientID clientID = new ClientID(AppConfigReader.getClientId())

        URI tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientID, refreshTokenGrant)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = TestSuite.buildRequest()
                .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .post(TestConstants.TOKEN_ENDPOINT)

        return response
    }

    /**
     * Get User Access Token Without PKCE Verifier
     *
     * @param authMethod authentication method
     * @param scopes scopes for token
     * @return access token
     */
    static Response getUserTokenWithoutCodeVerifier(BerlinConstants.AUTH_METHOD authMethod, BerlinConstants.SCOPES scopes,
                                                  String code) {

        def config = ConfigParser.getInstance()

        AuthorizationCode grant = new AuthorizationCode(code)
        URI callbackUri = new URI(AppConfigReader.getRedirectURL())
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri)

        ClientID clientID = new ClientID(AppConfigReader.getClientId())

        String assertionString = new AccessTokenJwtDto().getJwt()

        ClientAuthentication clientAuth = new PrivateKeyJWT(SignedJWT.parse(assertionString))

        URI tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def response = TestSuite.buildRequest()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
                .post(TestConstants.TOKEN_ENDPOINT)

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        return response
    }

}
