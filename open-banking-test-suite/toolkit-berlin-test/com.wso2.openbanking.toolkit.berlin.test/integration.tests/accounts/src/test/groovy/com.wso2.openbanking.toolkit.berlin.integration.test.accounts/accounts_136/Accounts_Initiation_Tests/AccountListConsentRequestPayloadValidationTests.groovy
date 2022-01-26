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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.accounts_136.Accounts_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Tests of Consent Request on Account List of Available Accounts.
 * Tests of Global Consents.
 */
class AccountListConsentRequestPayloadValidationTests extends AbstractAccountsFlow {

    @Test (groups = "1.3.6")
    void "TC0205008_Initiation Request with availableAccounts parameter set to allAccountsWithBalances"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
            "access":{
                "availableAccounts": "allAccountsWithBalances"
            },
            "recurringIndicator":false,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay":1,
            "combinedServiceIndicator":false
        }"""
                .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_PATH),
                "access.availableAccounts")
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim().contains(
                "Instance value (\"allAccountsWithBalances\") not found in enum "))
    }

    @Test (groups = "1.3.6")
    void "TC0205009_Initiation Request with availableAccounts parameter set to allAccountsWithOwnerName"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
            "access":{
                "availableAccounts": "allAccountsWithOwnerName"
            },
            "recurringIndicator":false,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay":1,
            "combinedServiceIndicator":false
        }"""
                .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.CONSENT_STATUS),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "consentId"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
    }

    @Test (groups = "1.3.6")
    void "TC0205010_Initiation Request with availableAccountsWithBalance parameter set to allAccountsWithOwnerName"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
            "access":{
                "availableAccountsWithBalance": "allAccountsWithOwnerName"
            },
            "recurringIndicator":false,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay":1,
            "combinedServiceIndicator":false
        }"""
                .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.CONSENT_STATUS),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "consentId"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
    }
}
