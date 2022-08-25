/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.Non_Regulatory_Token_Generation_Tests

import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.model.ApplicationAccessTokenDto
import com.wso2.openbanking.test.framework.request.AccessToken
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.model.UserAccessTokenForClientCredentialGrantTypeDTO
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util.AbstractNonRegulatoryFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util.NonRegulatoryConstants
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset
import java.text.SimpleDateFormat

/**
 * Test Class for Token Generation With Client Credentials Validation
 */
class TokenGenWithClientCredentialsValidationTests extends AbstractNonRegulatoryFlow {

    def userTokenDTO
    URI tokenEndpoint
    def authToken
    def basicHeader

    @BeforeClass
    void initClass() {
        tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")
    }

    void preTokenGenerationStep(List<String> tokenScope, String tokenGrant) {
        userTokenDTO = new UserAccessTokenForClientCredentialGrantTypeDTO()
        userTokenDTO.setScope(tokenScope)
        userTokenDTO.setGrantType(tokenGrant)

        tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")
    }

    void generateBasicHeader() {
        authToken = "${config.getNonRegulatoryClientId()}:${config.getNonRegulatoryClientSecret()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"
    }

    @Test
    void "TC1802001_Access Token Generation using Client Credentials grant Type"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, TestConstants.CLIENT_CREDENTIALS)
        generateBasicHeader()

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        HTTPResponse httpResponse = new HTTPResponse(tokenResponse.statusCode())
        httpResponse.setContentType(tokenResponse.contentType())
        httpResponse.setContent(tokenResponse.getBody().print())

        userAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_200)
        Assert.assertNotNull(userAccessToken)
    }

    @Test
    void "TC1802002_Access Token Generation Without scope open-id"() {

        String payload = "client_id=" + ConfigParser.getInstance().getNonRegulatoryClientId() + "&grant_type=" +
                "client_credentials&redirect_uri=" +  ConfigParser.getInstance().getNonRegulatoryRedirectURL()
        tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")
        generateBasicHeader()

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(payload)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_200)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "access_token"))
        Assert.assertFalse(TestUtil.parseResponseBody(tokenResponse, "scope").contains("openid"))
    }

    @Test
    void "TC1802003_Access Token Generation without Authorization: Basic header "() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.CLIENT_CREDENTIALS)

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Unsupported Client Authentication Method!")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    @Test
    void "TC1802004_Access Token Generation with Authorization: Bearer header"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.CLIENT_CREDENTIALS)
        generateBasicHeader()

        basicHeader = "Bearer ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Unsupported Client Authentication Method!")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    @Test
    void "TC1802005_Access Token Generation without basic Auth token"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.CLIENT_CREDENTIALS)
        generateBasicHeader()

        basicHeader = NonRegulatoryConstants.BASIC_HEADER_WITHOUT_VALUE

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
    }

    @Test
    void "TC1802006_Access Token Generation with invalid basic Auth token"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.CLIENT_CREDENTIALS)

        authToken = "${config.getNonRegulatoryClientSecret()}:${config.getNonRegulatoryClientId()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION)
                .split(":")[0],"A valid OAuth client could not be found for client_id")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    //Note: Error getting from apim side. Need to check whether we can give a meaningful error other than 500.
    @Test
    void "TC1802007_Access Token Generation with empty client_id in Auth Token"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.CLIENT_CREDENTIALS)

        authToken = ":${config.getNonRegulatoryClientId()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_500)
    }

    @Test
    void "TC1802008_Access Token Generation using incorrect grant_type=Client_credential"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, "client_credential")
        generateBasicHeader()

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Unsupported grant_type value")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR),
                "invalid_request")
    }

    @Test
    void "TC1802009_Access Token Generation using without grant_type"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, "client_credential")
        generateBasicHeader()

        String payload = "client_id=" + config.getNonRegulatoryClientId() + "&scope=openid&redirect_uri=" +
                config.getNonRegulatoryRedirectURL()

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(payload)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Missing grant_type parameter value")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_request")
    }

    @Test
    void "TC1802011_Invoke Non Regulatory API using a token generated from Regulatory Application"() {

        def tokenDTO = new ApplicationAccessTokenDto()
        tokenDTO.setScopes(NonRegulatoryConstants.SCOPES_OPEN_ID)

        def tokenResponse = AccessToken.getApplicationAccessToken(tokenDTO)
        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertNotNull(accessToken)

        doApiInvocation()

        Assert.assertEquals(apiResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_201)
    }

    @Test
    void "TC1802012_Access Token Generation using Client Credentials grant Type with different scopes"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_ACCOUNTS, NonRegulatoryConstants.CLIENT_CREDENTIALS)
        generateBasicHeader()

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        HTTPResponse httpResponse = new HTTPResponse(tokenResponse.statusCode())
        httpResponse.setContentType(tokenResponse.contentType())
        httpResponse.setContent(tokenResponse.getBody().print())

        userAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        doApiInvocation()

        Assert.assertEquals(apiResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_201)
    }

    @Test
    void "TC1402010_Invoke Berlin Regulatory API using a token generated from Non-Regulatory Application"() {

        getUserTokenFromClientCredentialsGrantType()
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

        Assert.assertEquals(apiResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(apiResponse, BerlinConstants.TPPMESSAGE_CODE),
          "CERTIFICATE_INVALID")
        Assert.assertTrue (TestUtil.parseResponseBody (apiResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Organization ID mismatch with Client ID"))
    }
}
