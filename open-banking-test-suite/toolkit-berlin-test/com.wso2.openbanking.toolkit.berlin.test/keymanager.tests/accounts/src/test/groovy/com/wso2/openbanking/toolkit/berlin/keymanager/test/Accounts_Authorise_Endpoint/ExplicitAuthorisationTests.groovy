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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.Accounts_Authorise_Endpoint

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Explicit Authorisation Request.
 */
class ExplicitAuthorisationTests extends AbstractAccountsFlow {

	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH
	String initiationPayload = AccountsPayloads.defaultInitiationPayload

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1426_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true"() {

		//Consent Initiation
		doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisation"))

		//Create Explicit Authorisation Resources
		createExplicitAuthorization(consentPath)

		authorisationId = authorisationResponse.jsonPath().get("authorisationId")
		requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
		Assert.assertNotNull(requestId)
		Assert.assertNotNull(authorisationId)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

		//Do Authorisation
		doAuthorizationFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		//Check Consent Status
		doStatusRetrieval(consentPath)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
	}

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
					dependsOnMethods = "OB-1426_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true")
	void "OB-1455_Get list of all authorisation sub-resource IDs"() {

		getExplicitAuthResources(consentPath)
		Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(authorisationId, authorisationResponse.jsonPath().get("authorisationId"))
	}

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
					dependsOnMethods = "OB-1426_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true")
	void "OB-1457_Get SCA status of consent authorisation sub-resource"() {

		getExplicitAuthResourceStatus(consentPath)
		Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(authorisationResponse.jsonPath().get(BerlinConstants.SCA_STATUS_PARAM),
						AccountsConstants.CONSENT_STATUS_PSUAUTHENTICATED)
		Assert.assertEquals(authorisationResponse.jsonPath().get(BerlinConstants.TRUSTED_BENEFICIARY_FLAG),
						false)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1427_Consent Initiation when TPP-Redirect Preferred not set in initiation request"() {

		//Consent Initiation
		doDefaultInitiationWithoutRedirectPreffered(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
						"Decoupled Approach is not supported.")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1428_Explicit Authorisation when TPP-Redirect Preferred not set in initiation request "() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
						.body(initiationPayload)
						.post(consentPath)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
						"Decoupled Approach is not supported.")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1429_Explicit Authorisation when PSU reject the consent"() {

		//Consent Initiation
		doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisation"))

		//Create Explicit Authorisation Resources
		createExplicitAuthorization(consentPath)
		authorisationId = authorisationResponse.jsonPath().get("authorisationId")
		requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
		Assert.assertNotNull(requestId)
		Assert.assertNotNull(authorisationId)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

		//Do Authorisation
		doConsentDenyFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertEquals(code, "User denied the consent")

		//Check consent status
		doStatusRetrieval(consentPath)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1454_Create consent authorisation resource for incorrect consent id"() {

		//Consent Initiation
		doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisation"))

		//Create Explicit Authorisation Resources
		def consentId = "12345"
		createExplicitAuthorization(consentPath, consentId)

		Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_UNKNOWN)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1456_Send Get list of all authorisation sub-resource request with invalid consent id"() {

		def accountId = "1234"

		getExplicitAuthResources(consentPath, accountId)
		Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_UNKNOWN)
	}
}