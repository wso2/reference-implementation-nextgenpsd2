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
import com.wso2.finance.open.banking.integration.test.framework.TestSuite
import com.wso2.finance.open.banking.integration.test.framework.util.TestConstants
import com.wso2.finance.open.banking.integration.test.framework.util.TestUtil
import com.wso2.finance.open.banking.nonregulatory.test.model.UserAccessTokenForPasswordGrantTypeDTO
import com.wso2.finance.open.banking.nonregulatory.test.util.AbstractNonRegulatoryFlow
import com.wso2.finance.open.banking.nonregulatory.test.util.NonRegulatoryConstants
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.model.UserAccessTokenForPasswordGrantTypeDTO
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util.AbstractNonRegulatoryFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util.NonRegulatoryConstants
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.nio.charset.Charset
import java.text.SimpleDateFormat

/**
 * Test Class for Token Generation With Password Validation
 */
class TokenGenWithPasswordGrantValidationTests extends AbstractNonRegulatoryFlow {

    def tokenDTO
    URI tokenEndpoint
    def authToken
    def basicHeader

    @BeforeClass
    void initClass() {
        tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")
    }

    void preTokenGenerationStep(List<String> tokenScope, String tokenGrant) {
        tokenDTO = new UserAccessTokenForPasswordGrantTypeDTO()
        tokenDTO.setScope(tokenScope)
        tokenDTO.setGrantType(tokenGrant)

        tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")
    }

    void generateBasicHeader() {
        authToken = "${config.getNonRegulatoryClientId()}:${config.getNonRegulatoryClientSecret()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"
    }

    @Test
    void "TC1803001_Access Token Generation using Password Grant Type"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.PASSWORD_GRANT)
        generateBasicHeader()

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        HTTPResponse httpResponse = new HTTPResponse(tokenResponse.statusCode())
        httpResponse.setContentType(tokenResponse.contentType())
        httpResponse.setContent(tokenResponse.getBody().print())

        userAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
    }

    @Test
    void "TC1803002_Access Token Generation Without scope open-id"() {

        String payload = "grant_type=password&&username=" + config.getPSU() + "&password=" + config.getPSUPassword() +
                "&redirect_uri=" + config.getNonRegulatoryRedirectURL()

        tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")
        generateBasicHeader()

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(payload)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_200)
    }

    @Test
    void "TC1803003_Access Token Generation without Authorization: Basic header"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.PASSWORD_GRANT)

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Unsupported Client Authentication Method!")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    @Test
    void "TC1803004_Access Token Generation with Authorization: Bearer header"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.PASSWORD_GRANT)
        generateBasicHeader()

        basicHeader = "Bearer ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Unsupported Client Authentication Method!")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    @Test
    void "TC1803005_Access Token Generation without basic Auth token"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.PASSWORD_GRANT)
        generateBasicHeader()

        basicHeader = NonRegulatoryConstants.BASIC_HEADER_WITHOUT_VALUE

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
    }

    @Test
    void "TC1803006_Access Token Generation with invalid basic Auth token"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.PASSWORD_GRANT)

        authToken = "${config.getNonRegulatoryClientSecret()}:${config.getNonRegulatoryClientId()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_401)
        Assert.assertTrue(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION)
                .contains("A valid OAuth client could not be found for client_id"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_client")
    }

    //Note: Error getting from apim side. Need to check whether we can give a meaningful error other than 500.
    @Test
    void "TC1803007_Access Token Generation with empty client_id in Auth Token"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, NonRegulatoryConstants.PASSWORD_GRANT)

        authToken = ":${config.getNonRegulatoryClientSecret()}"
        basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_500)
    }

    @Test
    void "TC1803008_Access Token Generation using incorrect grant_type=pass"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, "pass")
        generateBasicHeader()

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Unsupported grant_type value")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_request")
    }

    @Test
    void "TC1803009_Access Token Generation using without grant_type"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_OPEN_ID, "client_credential")
        generateBasicHeader()

        String payload = "scope=openid&username=" + config.getPSU() + "&password=" + config.getPSUPassword() +
                "&redirect_uri=" + config.getNonRegulatoryRedirectURL()

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(payload)
                .post(tokenEndpoint.toString())

        Assert.assertEquals(tokenResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR_DESCRIPTION),
                "Missing grant_type parameter value")
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, NonRegulatoryConstants.ERROR), "invalid_request")
    }

    @Test
    void "TC1803011_Access Token Generation using Password grant Type with different scopes"() {

        preTokenGenerationStep(NonRegulatoryConstants.SCOPES_ACCOUNTS, NonRegulatoryConstants.PASSWORD_GRANT)
        generateBasicHeader()

        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, TestConstants.CLIENTID_NON_REGULATORY_APP)
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        HTTPResponse httpResponse = new HTTPResponse(tokenResponse.statusCode())
        httpResponse.setContentType(tokenResponse.contentType())
        httpResponse.setContent(tokenResponse.getBody().print())

        userAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        doApiInvocation()

        Assert.assertEquals(apiResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_201)
    }

    @Test
    void "TC1403010_Invoke Berlin Regulatory API using a token generated from Non-Regulatory Application"() {

        getUserTokenFromPasswordGrantType()
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

        Assert.assertEquals(apiResponse.statusCode(), 403)
        Assert.assertEquals(TestUtil.parseResponseBody(apiResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue (TestUtil.parseResponseBody (apiResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Token does not consist of the required permissions for this resource"))
    }

}
