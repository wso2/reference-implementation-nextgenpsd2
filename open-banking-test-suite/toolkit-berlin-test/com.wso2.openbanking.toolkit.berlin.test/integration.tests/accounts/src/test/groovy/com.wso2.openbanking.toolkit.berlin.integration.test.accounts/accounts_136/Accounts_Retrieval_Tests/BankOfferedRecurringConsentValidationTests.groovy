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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.accounts_136.Accounts_Retrieval_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Account Retrieval Test for Multiple Recurring Consents Service
 */
class BankOfferedRecurringConsentValidationTests extends AbstractAccountsFlow {

    String oldConsentId
    String newConsentId
    String userAccessTokenForOldConsent
    boolean isMultipleConsentServiceSupported

    @Test(groups = ["1.3.6"],
            dataProvider = "BankOfferedConsentData", dataProviderClass = AccountsDataProviders.class)
    void "OB-1671_Retrieval Request to get the Account List with multiple recurring bank offered consents"(String title,
                                                                                                   List<String> fields,
                                                                                                   String payload) {

        String consentPath = AccountsConstants.CONSENT_PATH

        //Consent Initiation - 1st time
        doDefaultInitiation(consentPath, payload)
        oldConsentId = accountId

        //Consent Authorization - 1st time
        auth = new BerlinOAuthAuthorization(scopes, oldConsentId)

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    fields.forEach{ value ->
                        driver.findElement(By.xpath(value)).click()
                    }
                    driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_SUBMIT_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get())
        Assert.assertNotNull(code)
        getUserAccessToken(code)
        userAccessTokenForOldConsent = userAccessToken

        //to check if multiple recurring consent support is enabled or not
        isMultipleConsentServiceSupported = consentResponse.
                getHeader("ASPSP-Multiple-Consent-Support").contains("true")

        //Retrieve Account Details - 1st time
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))

        //Consent Initiation - 2nd Time
        doDefaultInitiation(consentPath, payload)
        newConsentId = accountId

        //Consent Authorization - 2nd Time
        auth = new BerlinOAuthAuthorization(scopes, newConsentId)

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    fields.forEach{ value ->
                        driver.findElement(By.xpath(value)).click()
                    }
                    driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_SUBMIT_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get())
        Assert.assertNotNull(code)

        //Retrieve Account Details After New Consent Authorisation
        def response2 = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        if (isMultipleConsentServiceSupported) {
            Assert.assertEquals(response2.statusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertNotNull(response2.jsonPath().getJsonObject("accounts"))
        } else {
            Assert.assertEquals(response2.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(response2, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CONSENT_EXPIRED)
            Assert.assertTrue(TestUtil.parseResponseBody(response2, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("The consent is expired"))
        }
    }
}
