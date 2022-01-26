package com.wso2.openbanking.toolkit.berlin.integration.test.payments.common_test.Payments_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

class MultiCurrencyValidationTests extends AbstractPaymentsFlow {

	String consentPath = PaymentsConstants.PISP_PATH

	@Test (groups = ["1.3.6"])
	void "OB-1566_Sub Account Level single payment initiation of Multi Currency Account"() {

		def initiationPayload = PaymentsInitiationPayloads.singlePaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentResponse.jsonPath().getJsonObject("debtorAccount.iban"))
	}

	@Test (groups = ["1.3.6"])
	void "OB-1567_Sub Account Level bulk payment initiation of multi currency account"() {

		def initiationPayload = PaymentsInitiationPayloads.bulkPaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentResponse.jsonPath().getJsonObject("debtorAccount.iban"))
	}

	@Test (groups = ["1.3.6"])
	void "OB-1568_Sub Account Level periodic payment initiation of multi currency account"() {

		def initiationPayload = PaymentsInitiationPayloads.periodicPaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentResponse.jsonPath().getJsonObject("debtorAccount.iban"))
	}

	@Test (groups = ["1.3.6"])
	void "OB-1569_Aggregation level specific payment initiation of multi currency account"() {

		def initiationPayload = PaymentsInitiationPayloads.singlePaymentPayloadWithoutInstructedAmount_Currency

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.FORMAT_ERROR)
	}

	@Test (groups = ["1.3.6"])
	void "OB-1570_Payment consent authorisation for multi currency account"() {

		def initiationPayload = PaymentsInitiationPayloads.singlePaymentPayload

		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(consentResponse.jsonPath().getJsonObject("debtorAccount.iban"))

		doAuthorizationFlow()
		Assert.assertNotNull(code)
	}
}
