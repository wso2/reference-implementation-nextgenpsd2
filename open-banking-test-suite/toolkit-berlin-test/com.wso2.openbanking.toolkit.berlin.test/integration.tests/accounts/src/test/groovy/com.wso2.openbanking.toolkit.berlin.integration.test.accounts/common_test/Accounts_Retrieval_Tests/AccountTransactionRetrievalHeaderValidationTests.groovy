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
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Factory
import org.testng.annotations.Test

class AccountTransactionRetrievalHeaderValidationTests extends AbstractAccountsFlow {

    Map<String, String> map
    String resourcePath
    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    /**
     * Data Factory to Retrieve Account Retrieval Resource Paths Data Provider.
     * @param maps
     */
    @Factory(dataProvider = "TransactionRetrievalResourcePaths", dataProviderClass = AccountsDataProviders.class)
    AccountTransactionRetrievalHeaderValidationTests(Map<String, String> maps) {
        this.map = maps

        resourcePath = map.get("resourcePath")
    }

    @BeforeClass (groups = ["1.3.3", "1.3.6"])
    void "Get User Access Token"() {

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
    void "TC0211002_Retrieval Request With Client Credential Type Access Token"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE), BerlinConstants
                .TOKEN_INVALID)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains ("Incorrect Access Token Type provided"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211003_Retrieval Request without Authorization header"() {

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains ("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211004_Retrieval Request With invalid Authorization header"() {

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE), BerlinConstants
                .TOKEN_INVALID)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains("Token is not valid"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211005_Retrieval Request Without X-Request-ID Header"() {

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains("X-Request-ID header is missing in the request"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211006_Retrieval Request With Invalid X-Request-ID Header"() {

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, "1234")
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)

        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "Invalid X-Request-ID header. Needs to be in UUID format")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211007_Retrieval Request Without Consent Id Header"() {

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .queryParam("bookingStatus", "booked")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains("Consent-ID header is missing in the request"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211008_Retrieval Request With Invalid Consent Id Header"() {

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .header(BerlinConstants.CONSENT_ID_HEADER, UUID.randomUUID().toString())
                .queryParam("bookingStatus", "booked")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_404)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.RESOURCE_UNKNOWN)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains("No valid consent found for given client id"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211009_Retrieval Request With Empty Consent Id Header"() {

        //Make Account Retrieval Request with the deleted Consent
        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .header(BerlinConstants.CONSENT_ID_HEADER, "")
                .queryParam("bookingStatus", "booked")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "Consent-ID header is missing in the request")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211010_Retrieval Request if associate consent is not in authorized state"() {

        //Do Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent and Extract the Code
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

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
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
    }

    @Test (groups = ["1.3.3"])
    void "TC0211024_Get Accounts with an invalid PSU_IP_Address" () {

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
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .queryParam("bookingStatus", "booked")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get(resourcePath)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "String \"823.121.123.142\" is not a valid IPv4 address")
    }
}
