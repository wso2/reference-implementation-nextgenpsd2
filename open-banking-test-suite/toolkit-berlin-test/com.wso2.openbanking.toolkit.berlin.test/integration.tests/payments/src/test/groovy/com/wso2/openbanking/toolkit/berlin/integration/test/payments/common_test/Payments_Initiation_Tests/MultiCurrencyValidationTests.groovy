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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.common_test.Payments_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

class MultiCurrencyValidationTests extends AbstractPaymentsFlow {

	String consentPath

	@Test (groups = ["1.3.6"])
	void "OB-1566_Sub Account Level single payment initiation of Multi Currency Account"() {

		consentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/$PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS"
		def initiationPayload = PaymentsInitiationPayloads.singlePaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)

		doAuthorizationFlow()
		Assert.assertNotNull(code)
	}

	@Test (groups = ["1.3.6"])
	void "OB-1567_Sub Account Level bulk payment initiation of multi currency account"() {

		consentPath = PaymentsConstants.BULK_PAYMENTS_PATH + "/$PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS"
		def initiationPayload = PaymentsInitiationPayloads.bulkPaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)

		doAuthorizationFlow()
		Assert.assertNotNull(code)
	}

	@Test (groups = ["1.3.6"])
	void "OB-1568_Sub Account Level periodic payment initiation of multi currency account"() {

		consentPath = PaymentsConstants.PERIODIC_PAYMENTS_PATH + "/$PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS"
		def initiationPayload = PaymentsInitiationPayloads.periodicPaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)

		doAuthorizationFlow()
		Assert.assertNotNull(code)
	}

	@Test (groups = ["1.3.6"])
	void "OB-1569_Aggregation level specific payment initiation of multi currency account"() {

		consentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/$PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS"
		def initiationPayload = PaymentsInitiationPayloads.singlePaymentPayloadWithoutInstructedAmount_Currency

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.FORMAT_ERROR)
	}

	@Test (groups = ["1.3.6"])
	void "OB-1570_Payment consent authorisation for multi currency account"() {

		consentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/$PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS"
		def initiationPayload = PaymentsInitiationPayloads.singlePaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)

		doAuthorizationFlow()
		Assert.assertNotNull(code)
	}
}
