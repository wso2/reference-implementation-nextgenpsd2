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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.Payments_Authorise_Endpoint

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.PaymentsInitiationPayloads
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Cancellation Authorisation Tests.
 */
class CancellationAuthorisationTests extends AbstractPaymentsFlow {

	String consentPath = PaymentsConstants.PERIODIC_PAYMENTS_CONSENT_PATH
	String initiationPayload = PaymentsInitiationPayloads.periodicPaymentPayload

	//Authorisation Cancellation For Explicit Authorisation Flow

	// Need to config auth_cancellation.enable = false in deployment.toml
	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 1)
	void "OB-1517_Consent Explicit Authorisation for Payment Cancellation"() {

		//Consent Initiation
		doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

		//Create Explicit Authorisation Resources
		createExplicitAuthorization(consentPath)

		authorisationId = authorisationResponse.jsonPath().get("authorisationId")
		requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
		Assert.assertNotNull(requestId)
		Assert.assertNotNull(authorisationId)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						PaymentsConstants.SCA_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

		//Do Authorisation
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Check Consent Status
		doStatusRetrieval(consentPath)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
	}

	// Need to config auth_cancellation.enable = false in deployment.toml
	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 2,
					dependsOnMethods = "OB-1517_Consent Explicit Authorisation for Payment Cancellation")
	void "OB-1518_Delete payment Consent after Explicit Authorisation"() {

		//Consent Delete
		doConsentDelete(consentPath)
		Assert.assertEquals(deleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

		//Consent Retrieval
		doConsentRetrieval(consentPath)
		Assert.assertEquals(consentRetrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)
	}

	// Need to config auth_cancellation.enable = true in deployment.toml
	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 3)
	void "OB-1519_Create Explicit Cancellation Sub-Resource"() {

		//Payment Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorisation
		doAuthorizationFlow()

		//Consent Delete
		doConsentDeleteWithExplicitAuth(consentPath)
		Assert.assertEquals(deleteResponse.statusCode(), BerlinConstants.STATUS_CODE_202)
		Assert.assertEquals(deleteResponse.jsonPath().get("transactionStatus"), PaymentsConstants.TRANSACTION_STATUS_ACTC)

		createExplicitCancellation(consentPath)
		Assert.assertNotNull(authorisationId)

		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))
	}

	// Need to config auth_cancellation.enable = true in deployment.toml
	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 4,
					dependsOnMethods = ["OB-1519_Create Explicit Cancellation Sub-Resource"])
	void "OB-1520_Get List of Authorisation Cancellation sub-resources"() {

		authorisationResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.get("${consentPath}/${paymentId}/cancellation-authorisations")

		def authIds = authorisationResponse.jsonPath().get("authorisationIds")
		Assert.assertNotNull(authIds)
	}

	// Need to config auth_cancellation.enable = true in deployment.toml
	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 5,
					dependsOnMethods = ["OB-1519_Create Explicit Cancellation Sub-Resource"])
	void "OB-1521_Get Authorisation Cancellation sub-resource Details"() {

		getAuthorisationCancellationResource(consentPath)

		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						PaymentsConstants.SCA_STATUS_RECEIVED)
	}

	// Need to config auth_cancellation.enable = true in deployment.toml
	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 6,
					dependsOnMethods = ["OB-1519_Create Explicit Cancellation Sub-Resource"])
	void "OB-1522_PSU Authorise Cancellation for Explicit Auth Consent"() {

		def auth = new BerlinOAuthAuthorization(scopes, paymentId)
		def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
						.addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
						.addStep {driver, context ->
							Assert.assertTrue(driver.findElement(By.xpath(BerlinConstants.PAYMENTS_INTENT_TEXT_XPATH))
											.getText().contains("cancel a periodic payment"))
							driver.findElement(By.xpath(BerlinConstants.PAYMENTS_SUBMIT_XPATH)).click()
						}
						.addStep(new WaitForRedirectAutomationStep())
						.execute()

		// Get Code From URL
		code = TestUtil.getCodeFromUrl(automation.currentUrl.get())

		//Check the Cancellation Status
		getAuthorisationCancellationResource(consentPath)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)

		//Check whether the payment is cancelled
		doConsentRetrieval(consentPath)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)
	}

	//Authorisation Cancellation For Implicit Authorisation Flow

	// Need to config auth_cancellation.enable = true in deployment.toml
	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 7)
	void "OB-1523_Consent Implicit Authorisation for Payment Cancellation"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)

		//Do Authorisation
		doAuthorizationFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		//Check Consent Status
		doStatusRetrieval(consentPath)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
	}

	// Need to config auth_cancellation.enable = true in deployment.toml
	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 8,
					dependsOnMethods = "OB-1523_Consent Implicit Authorisation for Payment Cancellation")
	void "OB-1524_Delete payment Consent after Implicit Authorisation"() {

		//Consent Delete
		doConsentDeleteWithExplicitAuth(consentPath)
		Assert.assertEquals(deleteResponse.statusCode(), BerlinConstants.STATUS_CODE_202)
		Assert.assertEquals(deleteResponse.jsonPath().get("transactionStatus"),
						PaymentsConstants.TRANSACTION_STATUS_ACTC)
		Assert.assertNotNull(deleteResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"))

		//Consent Retrieval
		doConsentRetrieval(consentPath)
		Assert.assertEquals(consentRetrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACTC)
	}

	// Need to config auth_cancellation.enable = true in deployment.toml
	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 9,
					dependsOnMethods = ["OB-1524_Delete payment Consent after Implicit Authorisation"])
	void "OB-1525_Create Cancellation Sub-Resource for Implicit Auth Consent"() {

		createExplicitCancellation(consentPath)
		Assert.assertNotNull(authorisationId)

		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))
	}

	// Need to config auth_cancellation.enable = true in deployment.toml
	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 10,
					dependsOnMethods = ["OB-1525_Create Cancellation Sub-Resource for Implicit Auth Consent"])
	void "OB-1526_PSU Authorise Cancellation for Implicit Auth Consent"() {

		def auth = new BerlinOAuthAuthorization(scopes, paymentId)
		def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
						.addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
						.addStep {driver, context ->
							Assert.assertTrue(driver.findElement(By.xpath(BerlinConstants.PAYMENTS_INTENT_TEXT_XPATH))
											.getText().contains("cancel a periodic payment"))
							driver.findElement(By.xpath(BerlinConstants.PAYMENTS_SUBMIT_XPATH)).click()
						}
						.addStep(new WaitForRedirectAutomationStep())
						.execute()

		// Get Code From URL
		code = TestUtil.getCodeFromUrl(automation.currentUrl.get())

		//Check the Cancellation Status
		getAuthorisationCancellationResource(consentPath)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)

		//Check whether the payment is cancelled
		doConsentRetrieval(consentPath)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)
	}
}
