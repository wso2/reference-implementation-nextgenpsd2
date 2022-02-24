/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Common_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.Test

/**
 * End to End Bank Offered Consent Flow.
 */
class BankOfferedConsentEndToEndFlow extends AbstractAccountsFlow {

	String consentPath = AccountsConstants.CONSENT_PATH

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
					dataProvider = "BankOfferedConsentData", dataProviderClass = AccountsDataProviders.class)
	void "TC0204001_Authorization by Bank Offered Consent"(String title, List<String> fields, String payload) {

		//Consent Initiation
		doDefaultInitiation(consentPath, payload)

		//Consent Authorization
		auth = new BerlinOAuthAuthorization(scopes, accountId)

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

		//Retrieve Account Details
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.ACCOUNTS_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
	}
}
