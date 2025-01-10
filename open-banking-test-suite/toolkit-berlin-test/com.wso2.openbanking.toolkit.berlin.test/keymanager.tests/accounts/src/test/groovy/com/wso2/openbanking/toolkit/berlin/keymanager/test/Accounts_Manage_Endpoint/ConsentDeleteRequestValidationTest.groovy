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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.Accounts_Manage_Endpoint

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Consent Delete Request Validation Test.
 */
class ConsentDeleteRequestValidationTest extends AbstractAccountsFlow {

	String url
	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "BG-364_Delete consent in received state"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllAccounts)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"),
				AccountsConstants.CONSENT_STATUS_RECEIVED)

		//Delete Consent
		deleteConsent(consentPath, accountId)
		Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

		//Consent Status
		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1447_Delete consent in authorised state"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllAccounts)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"),
				AccountsConstants.CONSENT_STATUS_RECEIVED)

		//Authorise Consent
		doAuthorizationFlow()

		//Delete Consent
		deleteConsent(consentPath, accountId)
		Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

		//Consent Status
		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1448_Delete consent request with invalid consent id"() {

		def accountId = "1234"

		//Delete Consent
		deleteConsent(consentPath, accountId)
		Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
		Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_UNKNOWN)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1450_Send delete consent request for already terminated consent"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllAccounts)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"),
				AccountsConstants.CONSENT_STATUS_RECEIVED)

		//Deny Consent
		BrowserAutomation.AutomationContext responseConsentDeny = doConsentDenyFlow()
		url = responseConsentDeny.currentUrl.get()
		def errorMessage = url.split("error_description=")[1].split("&")[0].replaceAll("\\+"," ")
		Assert.assertEquals(errorMessage, "User denied the consent")

		//Delete Consent
		deleteConsent(consentPath, accountId)
		Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

		deleteConsent(consentPath, accountId)
		Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE),
				BerlinConstants.INVALID_STATUS_VALUE)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1453_Send delete consent request without consent id"() {

		def accountId = ""

		//Delete Consent
		deleteConsent(consentPath, accountId)
		Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_404)
		Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.RESOURCE_UNKNOWN)
	}
}
