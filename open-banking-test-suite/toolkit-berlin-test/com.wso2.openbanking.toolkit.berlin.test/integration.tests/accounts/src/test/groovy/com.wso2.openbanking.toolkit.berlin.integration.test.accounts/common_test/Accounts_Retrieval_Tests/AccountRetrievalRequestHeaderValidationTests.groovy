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
                "Invalid X-Request-ID header. Needs to be in UUID format")
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
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT),
          ("Consent-ID header is missing in the request"))
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
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT),
          "Invalid Consent-ID header. Needs to be in UUID format")
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
                "Consent-ID header is missing in the request")
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

    @Test (groups = ["1.3.6"])
    void "OB-355_Account Retrieval Request with same user-access token and the same x-request-id"() {

        preRetreivalFlow()

        def xReqId = UUID.randomUUID().toString()

        //Account Retrieval Request = 1st Time
        def response = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, xReqId)
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
          .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))

        //Account Retrieval Request = 2nd Time
        def response2 = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, xReqId)
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
          .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response2.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response2.jsonPath().getJsonObject("accounts"))
    }

    @Test (groups = ["1.3.6"])
    void "OB-356_Account Retrieval Request with different user-access token and the same x-request-id"() {

        preRetreivalFlow()

        def xReqId = UUID.randomUUID().toString()

        //Account Retrieval Request = 1st Time
        def response = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, xReqId)
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
          .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))

        preRetreivalFlow()

        //Account Retrieval Request = 1st Time
        def response2 = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, xReqId)
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
          .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response2.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response2.jsonPath().getJsonObject("accounts"))
    }
}
