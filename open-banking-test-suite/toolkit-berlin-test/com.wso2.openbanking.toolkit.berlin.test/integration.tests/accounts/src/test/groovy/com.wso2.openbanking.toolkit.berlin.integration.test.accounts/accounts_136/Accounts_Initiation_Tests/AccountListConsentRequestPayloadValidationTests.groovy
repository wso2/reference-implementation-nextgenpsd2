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
