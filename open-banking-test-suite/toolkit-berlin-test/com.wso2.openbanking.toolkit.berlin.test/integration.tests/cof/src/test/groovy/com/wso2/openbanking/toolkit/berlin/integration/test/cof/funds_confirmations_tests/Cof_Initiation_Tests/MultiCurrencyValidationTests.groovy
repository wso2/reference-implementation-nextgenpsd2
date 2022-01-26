package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

class MultiCurrencyValidationTests extends AbstractCofFlow {

	String consentPath = CofConstants.CONSENT_PATH

	@Test (groups = ["1.3.6"])
	void "OB-1572_Sub Account Level COF initiation of Multi Currency Account"() {

		def initiationPayload = CofInitiationPayloads.initiationPayloadForSubAccLevelMultiCurrency

		doDefaultCofInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
	}

	@Test (groups = ["1.3.6"])
	void "OB-1573_Aggregation Level COF initiation of multi currency account"() {

		def initiationPayload = CofInitiationPayloads.initiationPayloadForAggregateLevelMultiCurrency

		doDefaultCofInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
	}

	@Test (groups = ["1.3.6"])
	void "OB-1578_COF consent authorisation for multi currency account"() {

		def initiationPayload = CofInitiationPayloads.initiationPayloadForSubAccLevelMultiCurrency

		doDefaultCofInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)

		doCofAuthorizationFlow()
		Assert.assertNotNull(code)
	}
}
