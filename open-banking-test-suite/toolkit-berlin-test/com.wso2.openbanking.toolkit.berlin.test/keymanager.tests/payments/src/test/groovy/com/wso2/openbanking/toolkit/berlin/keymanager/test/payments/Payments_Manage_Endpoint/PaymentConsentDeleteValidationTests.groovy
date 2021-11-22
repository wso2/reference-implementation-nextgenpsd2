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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.Payments_Manage_Endpoint

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Payment Consent Delete Validation Tests.
 */
class PaymentConsentDeleteValidationTests extends AbstractPaymentsFlow {

	String consentPath = PaymentsConstants.PERIODIC_PAYMENTS_CONSENT_PATH
	String initiationPayload = PaymentsInitiationPayloads.periodicPaymentPayload

	// Need to config auth_cancellation.enable = false in deployment.toml
	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 1)
	void "OB-1510_Delete consent in received state"() {

		//Payment Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

		//Consent Delete
		doConsentDelete(consentPath)
		Assert.assertEquals(deleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

		//Consent Retrieval
		doConsentRetrieval(consentPath)
		Assert.assertEquals(consentRetrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)
	}

	// Need to config auth_cancellation.enable = false in deployment.toml
	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 2)
	void "OB-1511_Delete consent in authorised state"() {

		//Payment Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

		//Consent Authorisation
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Consent Retrieval
		doConsentRetrieval(consentPath)
		Assert.assertEquals(consentRetrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)

		//Consent Delete
		doConsentDelete(consentPath)
		Assert.assertEquals(deleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

		//Consent Retrieval
		doConsentRetrieval(consentPath)
		Assert.assertEquals(consentRetrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)
	}

	// Need to config auth_cancellation.enable = false in deployment.toml
	@Test (groups = ["1.3.3", "1.3.6"], priority = 3)
	void "OB-1448_Delete consent request with invalid consent id"() {

		def payment_Id = "1234"

		//Delete Consent
		doConsentDelete(consentPath, payment_Id)
		Assert.assertEquals(deleteResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
		Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_UNKNOWN)
	}

	// Need to config auth_cancellation.enable = false in deployment.toml
	@Test (groups = ["1.3.3", "1.3.6"], priority = 4)
	void "OB-1514_Send delete consent request for already terminated consent"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

		//Deny Consent
		doConsentDenyFlow()
		Assert.assertEquals(code, "User denied the consent")

		//Delete Consent
		doConsentDelete(consentPath)
		Assert.assertEquals(deleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)
	}

	// Need to config auth_cancellation.enable = false in deployment.toml
	@Test (groups = ["1.3.3", "1.3.6"], priority = 5)
	void "OB-1515_Send delete consent request without consent id"() {

		def payment_Id = ""

		//Delete Consent
		doConsentDelete(consentPath, payment_Id)
		Assert.assertEquals(deleteResponse.statusCode(), BerlinConstants.STATUS_CODE_404)
		Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.RESOURCE_UNKNOWN)
	}
}
