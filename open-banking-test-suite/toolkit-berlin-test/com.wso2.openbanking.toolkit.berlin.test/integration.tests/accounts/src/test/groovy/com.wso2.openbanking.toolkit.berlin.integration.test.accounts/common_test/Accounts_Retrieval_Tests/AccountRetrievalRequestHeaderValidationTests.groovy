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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Retrieval_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Factory
import org.testng.annotations.Test

/**
 * Header Validation Tests on Account Retrieval Request.
 */
class AccountRetrievalRequestHeaderValidationTests extends AbstractAccountsFlow {

    Map<String, String> map
    String resourcePath
    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    /**
     * Data Factory to Retrieve Account Retrieval Resource Paths Data Provider.
     * @param maps
     */
    @Factory(dataProvider = "AccountRetrievalResourcePaths", dataProviderClass = AccountsDataProviders.class)
    AccountRetrievalRequestHeaderValidationTests(Map<String, String> maps) {
        this.map = maps

        resourcePath = map.get("resourcePath")
    }

    void preRetreivalFlow() {

        //Do Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent and Extract the Code
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209002_Retrieval Request With Client Credential Type Access Token"() {

        preRetreivalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains("Incorrect Access Token Type provided"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209003_Retrieval Request without Authorization header"() {

        preRetreivalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertTrue (TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains ("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209004_Retrieval Request With invalid Authorization header"() {

        preRetreivalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue (TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains ("Token is not valid"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209005_Retrieval Request Without X-Request-ID Header"() {

        preRetreivalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains("X-Request-ID header is missing in the request"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209006_Retrieval Request With Invalid X-Request-ID Header"() {

        preRetreivalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, "1234")
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)

        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "Input string \"1234\" is not a valid UUID")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209007_Retrieval Request Without Consent Id Header"() {

        preRetreivalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains("Header parameter 'Consent-ID' is required on path"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209008_Retrieval Request With Invalid Consent Id Header"() {

        preRetreivalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, "1234")
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.CONSENT_UNKNOWN)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209009_Retrieval Request With Empty Consent Id Header"() {

        preRetreivalFlow()

        //Make Account Retrieval Request with the deleted Consent
        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, "")
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "Parameter 'Consent-ID' is required but is missing.")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209010_Retrieval Request if associate consent is not in authorized state"() {

        preRetreivalFlow()

        //Delete Consent
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)

        //Check Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)

        //Make Account Retrieval Request with the deleted Consent

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.CONSENT_UNKNOWN)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "Consent is not in a valid state")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209016_Get Accounts with an invalid PSU_IP_Address" () {

        preRetreivalFlow()

        def config = ConfigParser.getInstance()
        def xRequestId = UUID.randomUUID().toString()

        //do the GET call with a invalid IP address
        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, BerlinRequestBuilder.getCurrentDate())
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .header(BerlinConstants.PSU_IP_ADDRESS, "823.121.123.142")
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "String \"823.121.123.142\" is not a valid IPv4 address")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1688_Account retrieval with user access token generated from refresh token grant"() {

        //Authorise consent
        preRetreivalFlow()

        //Account Retrieval
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH + "/")

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))

        //Generate User Access Token from refresh token
        generateRefreshTokenUserAccessToken(BerlinRequestBuilder.refreshToken, null)
        Assert.assertNotNull(userAccessToken)

        //Account Retrieval with user access token generated from refresh token
        def response2 = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH + "/")

        Assert.assertEquals(response2.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response2.jsonPath().getJsonObject("accounts"))

    }
}
