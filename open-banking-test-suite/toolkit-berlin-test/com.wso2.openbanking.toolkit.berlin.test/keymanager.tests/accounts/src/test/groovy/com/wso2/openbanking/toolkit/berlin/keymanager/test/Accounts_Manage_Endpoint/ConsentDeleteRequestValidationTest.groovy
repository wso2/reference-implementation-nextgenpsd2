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
 * Consent Delete Request Validation Test.
 */
class ConsentDeleteRequestValidationTest extends AbstractAccountsFlow {

	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1446_Delete consent in received state"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllAccounts)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertEquals(consentResponse, AccountsConstants.CONSENT_STATUS_RECEIVED)

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
		Assert.assertEquals(consentResponse, AccountsConstants.CONSENT_STATUS_RECEIVED)

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
		Assert.assertEquals(consentResponse, AccountsConstants.CONSENT_STATUS_RECEIVED)

		//Deny Consent
		doConsentDenyFlow()
		Assert.assertEquals(code, "User denied the consent")

		//Delete Consent
		deleteConsent(consentPath, accountId)
		Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_401)
		Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_INVALID)
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
