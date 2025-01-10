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
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Accounts Initiation Request Validation Tests.
 */
class AccountsInitiationRequestValidationTests extends AbstractAccountsFlow {

	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH
	String initiationPayload = AccountsPayloads.defaultInitiationPayload

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1430_Account consent initiation on Account List of Available Accounts"() {

		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllAccounts)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"), AccountsConstants
				.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "chosenScaMethod[0].authenticationType"),
						"SMS_OTP")
	}

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1431_Account consent initiation on dedicated accounts"() {

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"),
				AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "chosenScaMethod[0].authenticationType"),
						"SMS_OTP")
	}

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1432_Account consent initiation on Bank Offered Consent"() {

		doDefaultInitiation(consentPath, AccountsPayloads.AllAccessBankOfferedConsentPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"),
				AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "chosenScaMethod[0].authenticationType"),
						"SMS_OTP")
	}

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1433_Account consent initiation on Global Consent"() {

		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllPsd2)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"),
				AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "chosenScaMethod[0].authenticationType"),
						"SMS_OTP")
	}
}
