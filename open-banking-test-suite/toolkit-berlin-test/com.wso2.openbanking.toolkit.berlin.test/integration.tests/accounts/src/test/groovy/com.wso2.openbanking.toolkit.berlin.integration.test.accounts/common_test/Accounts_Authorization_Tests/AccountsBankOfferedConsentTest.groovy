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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Authorization_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.Reporter
import org.testng.annotations.Test

/**
 * Bank Offered Consent Test.
 */
class AccountsBankOfferedConsentTest extends AbstractAccountsFlow {

    String consentPath = AccountsConstants.CONSENT_PATH

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dataProvider = "BankOfferedConsentData", dataProviderClass = AccountsDataProviders.class)
    void "TC0204001_Authorization by Bank Offered Consent"(String title, List<String> fields, String payload) {

        Reporter.log(title)

        //Consent Initiation
        doDefaultInitiation(consentPath, payload)

        //Consent Authorization
        def auth = new BerlinOAuthAuthorization(scopes, accountId)

        def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                        .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                        .addStep {driver, context ->
                            fields.forEach{ value ->
                                driver.findElement(By.xpath(value)).click()
                            }
                            driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_SUBMIT_XPATH)).click()
                        }
                        .addStep(new WaitForRedirectAutomationStep())
                        .execute()

        def code = TestUtil.getCodeFromUrl(automation.currentUrl.get())
        Assert.assertNotNull(code)
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "InvalidBankOfferedConsentData",
            dataProviderClass = AccountsDataProviders.class)
    void "TC0204002_Authorization by Bank Offered Consent"(String title, List<String> fields, String payload) {

        Reporter.log(title)

        //Consent Initiation
        doDefaultInitiation(consentPath, payload)
        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
    }
}
