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
                "Unrecognized property 'allAccountsWithBalances'")
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
