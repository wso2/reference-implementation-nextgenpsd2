/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
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
