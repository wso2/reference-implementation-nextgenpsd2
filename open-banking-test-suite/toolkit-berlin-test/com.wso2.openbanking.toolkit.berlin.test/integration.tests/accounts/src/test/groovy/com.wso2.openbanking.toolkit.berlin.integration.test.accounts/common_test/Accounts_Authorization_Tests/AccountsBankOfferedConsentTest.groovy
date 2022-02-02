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
import org.openqa.selenium.support.ui.Select
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
