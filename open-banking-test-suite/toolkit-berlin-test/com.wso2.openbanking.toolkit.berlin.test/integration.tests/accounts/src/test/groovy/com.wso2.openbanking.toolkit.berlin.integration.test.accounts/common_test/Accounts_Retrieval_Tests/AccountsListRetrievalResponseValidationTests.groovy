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
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Response Validation Tests of Consent Request on Account List of Available Accounts.
 * Response Validation Tests of Global Consents.
 */
class AccountsListRetrievalResponseValidationTests extends AbstractAccountsFlow {

    String apiVersion = ConfigParser.getInstance().getApiVersion()

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211021_Retrieve Transactions from Consent on Account List of Available Accounts"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = AccountsInitiationPayloads.initiationPayloadForAllAccounts

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Transaction Retrieval
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_INVALID)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_TEXT),
                "Provided account Id does not have requested permissions")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211022_Retrieve Transactions from Consent on Account List of Available Accounts with Balances"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload

        if (apiVersion.equalsIgnoreCase("1.3.3")) {
            initiationPayload = AccountsInitiationPayloads.initiationPayloadForAvailableAccountsWithBalances
        } else {
            initiationPayload = AccountsInitiationPayloads.initiationPayloadForAvailableAccountsWithBalance
        }

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Transaction Retrieval
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_INVALID)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_TEXT),
                "Provided account Id does not have requested permissions")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211023_Retrieve Transactions from Consent on Account List of All Psd2 Accounts"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = AccountsInitiationPayloads.initiationPayloadForAllPsd2

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Transaction Retrieval
        def response = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0213015_Retrieve Balances from Consent on Account List of Available Accounts"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = AccountsInitiationPayloads.initiationPayloadForAllAccounts

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Balances Retrieval
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.BALANCES_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_INVALID)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_TEXT),
                "Provided account Id does not have requested permissions")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0213016_Retrieve Transactions from Consent on Account List of Available Accounts with Balances"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload

        if (apiVersion.equalsIgnoreCase("1.3.3")) {
            initiationPayload = AccountsInitiationPayloads.initiationPayloadForAvailableAccountsWithBalances
        } else {
            initiationPayload = AccountsInitiationPayloads.initiationPayloadForAvailableAccountsWithBalance
        }

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Balances Retrieval
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.BALANCES_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("balances"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0213017_Retrieve Balances from Consent on Account List of All Psd2 Accounts"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = AccountsInitiationPayloads.initiationPayloadForAllPsd2

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Balances Retrieval
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.BALANCES_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("balances"))
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "OB-1667_Retrieve accounts more than one time with one-off all available account consent"() {

        def consentPath = AccountsConstants.CONSENT_PATH

        def initiationPayload = """
          {  
               "access":{
                    "availableAccounts": "allAccounts"
               },
               "recurringIndicator":false,
               "validUntil":"${BerlinTestUtil.getDateAndTime(0)}",
               "frequencyPerDay":1,
               "combinedServiceIndicator":false
               
        }
        """.stripIndent()

        //Do Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent and Extract the Code
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Account Retrieval
        for(int i=1; i<=2; i++) {
            def response = BerlinRequestBuilder
                    .buildBasicRequest(userAccessToken)
                    .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                    .get(AccountsConstants.ACCOUNTS_PATH + "/")

            if(i < 2) {
                Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
                Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
            } else {
                Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
                Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                        BerlinConstants.CONSENT_EXPIRED)
                Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                        contains("The consent is expired"))
            }
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "OB-1666_Retrieve accounts more than one time with one-off global account consent"() {

        def consentPath = AccountsConstants.CONSENT_PATH

        def initiationPayload = """
          {  
               "access":{
                    "allPsd2": "allAccounts"
               },
               "recurringIndicator":false,
               "validUntil":"${BerlinTestUtil.getDateAndTime(0)}",
               "frequencyPerDay":1,
               "combinedServiceIndicator":false
               
        }
        """.stripIndent()

        //Do Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent and Extract the Code
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Account Retrieval
        for(int i=1; i<=2; i++) {
            def response = BerlinRequestBuilder
                    .buildBasicRequest(userAccessToken)
                    .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                    .get(AccountsConstants.ACCOUNTS_PATH + "/")

            if(i < 2) {
                Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
                Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
            } else {
                Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
                Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                        BerlinConstants.CONSENT_EXPIRED)
                Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                        contains("The consent is expired"))
            }
        }
    }
}
