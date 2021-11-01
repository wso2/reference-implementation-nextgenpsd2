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
 * Card Accounts Retrieval Tests.
 */
class CardAccountsRetrievalRequestBodyValidationTests extends AbstractAccountsFlow {

    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

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

    //Account Retrieval - Card Accounts

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0214011_Retrieval Request to Get a List of Available Card Accounts"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.CARD_ACCOUNTS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("cardAccounts"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0215011_Retrieval Request to Get Details about a Card Account"(){

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.SPECIFIC_CARD_ACCOUNTS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("cardAccount"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0217011_Retrieval Request to Get Transaction Details of a Card Account"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "booked")
                .get(AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("cardAccount"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0217012_Retrieval Request to Get Transactions List without booking status"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_PATH),
                BerlinConstants.QUERY_BOOKINGSTATUS)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.PARAMETER_NOT_CONSISTENT)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0217013_Retrieval Request to Get Transactions List with invalid booking status"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "cat")
                .get(AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_PATH),
                BerlinConstants.QUERY_BOOKINGSTATUS)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.PARAMETER_NOT_CONSISTENT)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0217014_Retrieval Request to Get Transactions List with both booking status"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .get(AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0217015_Retrieval Request to Get Transactions List with valid booking status and valid date range"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", "2018-05-11")
                .queryParam("dateTo", "2018-07-11")
                .get(AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("cardAccount"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0217016_Retrieval Request to Get Transactions List with valid booking status and partial date range"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", "2018-05-11")
                .get(AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("cardAccount"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0217017_Retrieval Request to Get Transactions List with valid booking status and invalid date"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", "2018-60-11")
                .get(AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_PATH),
                BerlinConstants.QUERY_DATEFROM)
        Assert.assertEquals(response.jsonPath().getString(BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TIMESTAMP_INVALID)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0217018_Retrieval Request to Get Transactions Details of Card Account with valid booking status and deltaList"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("bookingStatus", "both")
                .queryParam("deltaList", "true")
                .get(AccountsConstants.SPECIFIC_CARD_ACCOUNTS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("cardAccount"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0216011_Retrieval Request to Get Balance Details of a Card Account"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.CARD_ACCOUNTS_BALANCES_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("cardAccount"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("balances"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0216012_Retrieval Request to Get Balances Details without specifying the Account Reference Id"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get("$AccountsConstants.CARD_ACCOUNTS_PATH/balances")

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0216013_Get Card Accounts Details by calling incorrect resource path"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.SPECIFIC_CARD_ACCOUNTS_PATH + "/fixed")

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_404)
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "TC0216014_Get Balances Details of a Card Account without balance permission"() {

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
                .get(AccountsConstants.CARD_ACCOUNTS_BALANCES_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_403)
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "TC0217019_Get Transaction Details of a Card Account without transactions permission"() {

        //Do Account Initiation
        doDefaultInitiation(consentPath,AccountsInitiationPayloads.initiationPayloadWithoutTransactionsPermission)
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
                .get(AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_403)
    }
}
