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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Initiation_Tests

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

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0205004_Initiation Request with payload containing both global and dedicated consent accesses"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
               "access":{
                    "allPsd2": "allAccounts",
                    "balances":[
                     {
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ],
                  "transactions":[
                     {
                        "iban":"DE12345678901234567890"
                     }
                  ]
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
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                "Requested permissions in the Payload are invalid")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0205005_Initiation Request with unsupported access attribute"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
                "access":{
                    "allAccountsWithBalances": "allAccounts"
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
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                "\"access\" object has missing required attributes")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0205006_Initiation Request with unsupported value for the consent access attribute"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
                "access":{
                    "availableAccounts": "availableAccountsWithBalance"
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
                "Instance value (\"availableAccountsWithBalance\") not found in enum"))
    }
}
