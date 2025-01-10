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
