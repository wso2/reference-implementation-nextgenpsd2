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

package com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.Non_Regulatory_Token_Generation_Tests

import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.nimbusds.oauth2.sdk.id.ClientID
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util.AbstractNonRegulatoryFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util.NonRegulatoryConstants
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset
import java.text.SimpleDateFormat

/**
 * Test Class for Token Generation With Authorization Code Validation
 */
class TokenGenWithAuthorizationCodeValidationTests extends AbstractNonRegulatoryFlow {

    URI callbackUri
    ClientID clientID
    URI tokenEndpoint
    def authToken
    def basicHeader
    HTTPRequest httpRequest

    @BeforeClass
    void initClass() {
        callbackUri = new URI(config.getNonRegulatoryRedirectURL())
        clientID = new ClientID(config.getNonRegulatoryClientId())
        tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")
    }

    void preTokenGenerationStep() {
        doAuthorization()
        Assert.assertNotNull(code)

        AuthorizationCode grant = new AuthorizationCode(code)

        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri)

        TokenRequest request = new TokenRequest(tokenEndpoint, clientID, codeGrant)

        httpRequest = request.toHTTPRequest()
    }

    void generateBasicheader() {
        authToken = "${ConfigParser.getInstance().getNonRegulatoryClientId()}:${config.getNonRegulatoryClientSecret()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"
    }

    @Test
    void "TC1801001_Access Token Generation using Authorization Code grant Type"() {

        preTokenGenerationStep()
        generateBasicheader()

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(httpRequest.query)
                .post(tokenEndpoint.toString())

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        userAccessToken = TokenResponse.parse(httpResponse).toSuccessResponse().tokens.accessToken

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_200)

        Assert.assertNotNull(userAccessToken)
    }

    @Test
    void "TC1801002_Access Token Generation without Authorization: Basic header"() {

        preTokenGenerationStep()

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(httpRequest.query)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Unsupported Client Authentication Method!")
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    @Test
    void "TC1801003_Access Token Generation with Authorization: Bearer header"() {

        preTokenGenerationStep()

        basicHeader = "Bearer ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(httpRequest.query)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Unsupported Client Authentication Method!")
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    @Test
    void "TC1801004_Access Token Generation without basic Auth token"() {

        preTokenGenerationStep()

        authToken = "${config.getNonRegulatoryClientSecret()}:${config.getNonRegulatoryClientId()}"
        basicHeader = "Basic "

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(httpRequest.query)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
    }

    @Test
    void "TC1801005_Access Token Generation with invalid basic Auth token"() {

        preTokenGenerationStep()

        authToken = "${config.getNonRegulatoryClientSecret()}:${config.getNonRegulatoryClientId()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(httpRequest.query)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR_DESCRIPTION)
                .split(":")[0], "A valid OAuth client could not be found for client_id")
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    //Note: Error getting from apim side. Need to check whether we can give a meaningful error other than 500.
    @Test
    void "TC1801006_Access Token Generation with empty client_id in Auth Token"() {

        preTokenGenerationStep()
        authToken = ":${config.getNonRegulatoryClientSecret()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(httpRequest.query)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_500)
    }

    @Test
    void "TC1801007_Access Token Generation without code parameter"() {

        generateBasicheader()
        String query = "redirect_uri=" + callbackUri + "&grant_type=authorization_code&client_id=" + clientID

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(query)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Missing parameters: code")
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR), "invalid_request")
    }


    @Test
    void "TC1801008_Access Token Generation with expired code"() {

        generateBasicheader()

        AuthorizationCode grant = new AuthorizationCode("8c4517ee-f02b-34de-b7de-4aef1e7c53dd")

        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri)

        TokenRequest request = new TokenRequest(tokenEndpoint, clientID, codeGrant)

        httpRequest = request.toHTTPRequest()

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(httpRequest.query)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Invalid authorization code received from token request")
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR), "invalid_grant")
    }

    @Test
    void "TC1801009_Access Token Generation without redirect url"() {

        preTokenGenerationStep()
        generateBasicheader()

        String query = "code=" + code + "&grant_type=authorization_code&client_id=" + clientID

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(query)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(response.statusCode(), NonRegulatoryConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Missing parameters: redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(response, NonRegulatoryConstants.ERROR), "invalid_request")
    }

    @Test
    void "TC1401010_Invoke Berlin Regulatory API using a token generated from Non-Regulatory Application"() {

        doAuthorization()
        Assert.assertNotNull(code)

        getUserTokenFromAuthorizationCode()
        Assert.assertNotNull(userAccessToken)

        String baseURI = config.getBaseURL()

        URI apiPath = new URI(baseURI.concat(NonRegulatoryConstants.BERLIN_ACCOUNTS_PATH))
        def date = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z").format(new Date())

        apiResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(TestConstants.DATE, date)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(apiPath)

        Assert.assertEquals(apiResponse.statusCode(), 401)
        Assert.assertEquals(TestUtil.parseResponseBody(apiResponse, BerlinConstants.TPPMESSAGE_CODE),
               "CERTIFICATE_INVALID")
        Assert.assertTrue (TestUtil.parseResponseBody (apiResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Organization ID mismatch with Client ID"))
    }
}
