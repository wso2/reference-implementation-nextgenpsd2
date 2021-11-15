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
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "scaMethods[0].authenticationType"),
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
