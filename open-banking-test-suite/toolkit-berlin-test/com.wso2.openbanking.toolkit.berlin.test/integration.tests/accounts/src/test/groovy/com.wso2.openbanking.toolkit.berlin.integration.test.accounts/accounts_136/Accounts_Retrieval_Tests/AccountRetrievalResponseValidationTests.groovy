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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.accounts_136.Accounts_Retrieval_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Account Retrieval Test for Multiple Recurring Consents Service
 */
class AccountRetrievalResponseValidationTests extends AbstractAccountsFlow {

    String oldConsentId
    String newConsentId
    Response oldConsentResponse
    Response newConsentResponse
    String userAccessTokenForOldConsent
    boolean isMultipleConsentServiceSupported

    @BeforeClass
    void initiateMultipleRecurringConsentResources() {

        String consentPath = AccountsConstants.CONSENT_PATH
        String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

        //initiate first recurring consent
        oldConsentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .post(consentPath)

        oldConsentId = TestUtil.parseResponseBody(oldConsentResponse, "consentId")

        //authorize the first recurring consent
        String oldAuthorizationCode = getAuthorizationCode(oldConsentId)
        Assert.assertNotNull(oldAuthorizationCode)

        //get userAccessToken for first recurring consent
        userAccessTokenForOldConsent = getUserAccessToken(oldAuthorizationCode)

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))

        //initiate second recurring consent
        newConsentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .post(consentPath)

        newConsentId = TestUtil.parseResponseBody(newConsentResponse, "consentId")

        //to check if multiple recurring consent support is enabled or not
        isMultipleConsentServiceSupported = oldConsentResponse.
                getHeader("ASPSP-Multiple-Consent-Support").contains("true")

        //authorize the second recurring consent
        String newAuthorizationCode = getAuthorizationCode(newConsentId)
        Assert.assertNotNull(newAuthorizationCode)

    }

    @Test (groups = "1.3.6")
    void "TC0209017_Retrieval Request to get the Account List with multiple recurring consents"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        if (isMultipleConsentServiceSupported) {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
        } else {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CONSENT_EXPIRED)
            Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("The consent is expired"))
        }
    }

    @Test (groups = "1.3.6")
    void "TC0210020_Retrieval Request to get the details of a specific Account with multiple recurring consents"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

        if (isMultipleConsentServiceSupported) {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
        } else {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CONSENT_EXPIRED)
            Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("The consent is expired"))
        }
    }

    @Test (groups = "1.3.6")
    void "TC0211025_Retrieval Request for Transactions List with information booking status with multiple recurring consent"() {
        //access resources from first recurring consent
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .queryParam("bookingStatus", "information")
                .get(AccountsConstants.TRANSACTIONS_PATH)

        if (isMultipleConsentServiceSupported) {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
        } else {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CONSENT_EXPIRED)
            Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("The consent is expired"))
        }
    }

    @Test (groups = "1.3.6")
    void "TC0212015_Get transaction details from a given transactionId on a given account with multiple recurring consents"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .queryParam("bookingStatus", "both")
                .get(AccountsConstants.SPECIFIC_TRANSACTIONS_PATH)

        if (isMultipleConsentServiceSupported) {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertNotNull(response.jsonPath().getJsonObject("transactionsDetails"))
        } else {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CONSENT_EXPIRED)
            Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("The consent is expired"))
        }
    }

    @Test (groups = "1.3.6")
    void "TC0213019_Retrieval Request to Get Balances Details from a given Account with multiple recurring consents"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .get(AccountsConstants.BALANCES_PATH)

        if (isMultipleConsentServiceSupported) {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertNotNull(response.jsonPath().getJsonObject("balances"))
        } else {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CONSENT_EXPIRED)
            Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("The consent is expired"))
        }
    }

    @Test (groups = "1.3.6")
    void "TC0214013_Retrieval Request to Get a List of Available Card Accounts with multiple recurring consents"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .get(AccountsConstants.CARD_ACCOUNTS_PATH)

        if (isMultipleConsentServiceSupported) {
            Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertNotNull(response.jsonPath().getJsonObject("cardAccounts"))
        } else {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CONSENT_EXPIRED)
            Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("The consent is expired"))
        }
    }
}
