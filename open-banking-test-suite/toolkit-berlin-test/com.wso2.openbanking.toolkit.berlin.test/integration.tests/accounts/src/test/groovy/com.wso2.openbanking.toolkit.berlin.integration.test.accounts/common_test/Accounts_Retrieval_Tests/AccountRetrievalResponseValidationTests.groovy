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
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Accounts Retrieval Tests.
 */
class AccountRetrievalResponseValidationTests extends AbstractAccountsFlow {

    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @BeforeClass (groups = ["SmokeTest", "1.3.3", "1.3.6"])
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

    //Account Retrieval - Accounts

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209011_Retrieval Request to get the Account List"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH + "/")

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0209012_Bulk Account Retrieval Request with 'withBalance' query parameter"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("withBalance")
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts._links.balances"))

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0210012_Retrieval Request to get the details of a specific Account with Balances"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("withBalance")
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("account._links.balances"))
    }

    //Account Retrieval - Transaction

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211011_Retrieval Request to Get Transactions List with booked booking status"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211012_Retrieval Request to Get Transactions List without booking status"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_PATH),
                BerlinConstants.QUERY_BOOKINGSTATUS)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.PARAMETER_NOT_CONSISTENT)

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211013_Retrieval Request to Get Transactions List with invalid booking status"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "cat")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_PATH),
                BerlinConstants.QUERY_BOOKINGSTATUS)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.PARAMETER_NOT_CONSISTENT)

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211014_Retrieval Request to Get Transactions List with both booking status"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211015_Retrieval Request to Get Transactions List with valid booking status and valid date range"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", "2018-05-11")
                .queryParam("dateTo", "2018-07-11")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211016_Retrieval Request to Get Transactions List with valid booking status and partial date range"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", "2018-05-11")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211017_Retrieval Request to Get Transactions List with valid booking status and invalid date expecting error"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", "2018-Jan-11")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_PATH),
                BerlinConstants.QUERY_DATEFROM)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TIMESTAMP_INVALID)

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211018_Retrieval Request to Get Transactions List with valid booking status with balances param"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .queryParam("withBalance")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))

    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0211019_Retrieval Request to Get Transactions List with valid booking status with entryReferenceFrom param"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "pending")
                .queryParam("entryReferenceFrom")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0212011_Retrieval Request to Get transaction details from a given transactionId on a given account"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .get(AccountsConstants.SPECIFIC_TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactionsDetails"))
    }

    //Note: In a Production Server the error should be 404. [https://github.com/wso2/financial-open-banking/issues/3947]
    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0212012_Retrieval Request to Get Transaction Details without specifying the Account Reference Id"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .get("$AccountsConstants.ACCOUNTS_PATH/transactions")

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0212013_Retrieval Request to Get Transaction Details of an Account with valid booking status and deltaList"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .queryParam("deltaList", "true")
                .get(AccountsConstants.SPECIFIC_TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactionsDetails"))
    }

    //Account Retrieval - Balances

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0213011_Retrieval Request to Get Balances Details from a given Account"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.BALANCES_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("balances"))
    }

    //Note: In a Production Server the error should be 404. [https://github.com/wso2/financial-open-banking/issues/3947]
    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0213012_Retrieval Request to Get Balances Details without specifying the Account Reference Id"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get("$AccountsConstants.ACCOUNTS_PATH/balances")

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0213013_Get Accounts Details by calling incorrect resource path"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH + "/fixed")

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_404)
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "TC0211020_Get Transaction Details of an Account without transactions permission"() {

        //Do Account Initiation
        doDefaultInitiation(consentPath, AccountsInitiationPayloads.initiationPayloadWithoutTransactionsPermission)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent and Extract the Code
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_403)
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "TC0213014_Get Balances Details of an Account without balance permission"() {

        //Do Account Initiation
        doDefaultInitiation(consentPath,AccountsInitiationPayloads.initiationPayloadWithoutBalancesPermission)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent and Extract the Code
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.BALANCES_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_403)
    }

    @Test (groups = ["1.3.6"])
    void "TC0202013_Accounts Authorization for SCA implicit accept scenario"() {

        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertNotNull(accountId)

        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)
    }

    /**
     * This testcase only supports if AccountReferenceType is configured as 'bban' in deployment.toml.
     */
    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], enabled = false)
    void "OB-1663_Accounts retrieval for consent with pan account reference"() {

        String initiationPayload = AccountsInitiationPayloads.initiationPayloadWithPan

        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertNotNull(accountId)

        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
    }

    /**
     * This testcase only supports if AccountReferenceType is configured as 'bban' in deployment.toml.
     */
    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], enabled = false)
    void "OB-1664_Accounts retrieval for consent with bban account reference"() {

        String initiationPayload = AccountsInitiationPayloads.initiationPayloadWithBban

        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertNotNull(accountId)

        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
        Assert.assertNotNull(refreshToken)

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
    }
}
