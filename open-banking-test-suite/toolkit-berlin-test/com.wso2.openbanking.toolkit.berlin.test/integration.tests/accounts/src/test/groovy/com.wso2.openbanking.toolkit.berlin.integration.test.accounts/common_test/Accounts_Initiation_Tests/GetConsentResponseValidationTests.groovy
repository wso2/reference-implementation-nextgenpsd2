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
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.Reporter
import org.testng.annotations.Test

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Response Validation Tests on Get Consent Request.
 */
class GetConsentResponseValidationTests extends AbstractAccountsFlow {

    def consentPath = AccountsConstants.CONSENT_PATH
    def initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208001_Get the Consent with Valid Consent Id"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208002_Get Consent With Empty Consent Id"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208003_Get Consent Without Consent Id Parameter"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208004_Get Already Terminated Consent"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208005_Get an Authorized Consent"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent
        doAuthorizationFlow()
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    @Test(groups = ["1.3.6"],
            dataProvider = "BankOfferedConsentData", dataProviderClass = AccountsDataProviders.class)
    void "OB-1532_Bank Offered consent retrieval before authorisation"(String title, List<String> fields, String payload) {

        Reporter.log(title)

        //Consent Initiation
        doDefaultInitiation(consentPath, payload)

        //Consent Retrieval
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("access.balances.iban"), [])
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("access.accounts.iban"), [])
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("access.transactions.iban"), [])
    }

    @Test(groups = ["1.3.6"],
            dataProvider = "BankOfferedConsentData", dataProviderClass = AccountsDataProviders.class)
    void "OB-1533_Bank Offered consent retrieval after authorisation"(String title, List<String> fields,
                                                                      String payload) {

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

        //Consent Retrieval
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("access.balances.iban")[0],
                BerlinConstants.MULTICURRENCY_ACCOUNT)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access.accounts.iban")[0],
                BerlinConstants.MULTICURRENCY_ACCOUNT)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access.transactions.iban")[0],
                BerlinConstants.MULTICURRENCY_ACCOUNT)
    }
}
