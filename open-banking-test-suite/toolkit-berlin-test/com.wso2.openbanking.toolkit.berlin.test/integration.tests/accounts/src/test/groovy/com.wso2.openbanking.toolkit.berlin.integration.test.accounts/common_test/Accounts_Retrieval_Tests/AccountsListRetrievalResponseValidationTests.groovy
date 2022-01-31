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
import com.wso2.openbanking.test.framework.util.ConfigParser
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

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_403)
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

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_403)
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
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
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

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_403)
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
}
