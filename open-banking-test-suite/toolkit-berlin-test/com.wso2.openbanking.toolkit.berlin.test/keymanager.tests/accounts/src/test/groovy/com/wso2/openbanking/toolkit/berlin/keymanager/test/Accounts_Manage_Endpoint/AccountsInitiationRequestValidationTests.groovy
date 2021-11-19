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
