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
 * Payment Initiation Request Validation Tests.
 */
class PaymentInitiationRequestValidationTests extends AbstractPaymentsFlow {

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1495_Single Payment Consent Initiation"() {

		String consentPath = PaymentsConstants.SINGLE_PAYMENTS_CONSENT_PATH
		String initiationPayload = PaymentsInitiationPayloads.singlePaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "chosenScaMethod[0].authenticationType"),
						"SMS_OTP")
	}

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1496_Bulk Payment Consent Initiation"() {

		String consentPath = PaymentsConstants.BULK_PAYMENTS_CONSENT_PATH
		String initiationPayload = PaymentsInitiationPayloads.bulkPaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentResponse.getHeader("Location"))
		Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
		Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
		Assert.assertNotNull(paymentId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
	}

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1497_Periodic Payment Consent Initiation"() {

		String consentPath = PaymentsConstants.PERIODIC_PAYMENTS_CONSENT_PATH
		String initiationPayload = PaymentsInitiationPayloads.periodicPaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentResponse.getHeader("Location"))
		Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
		Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
		Assert.assertNotNull(paymentId, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
	}
}
