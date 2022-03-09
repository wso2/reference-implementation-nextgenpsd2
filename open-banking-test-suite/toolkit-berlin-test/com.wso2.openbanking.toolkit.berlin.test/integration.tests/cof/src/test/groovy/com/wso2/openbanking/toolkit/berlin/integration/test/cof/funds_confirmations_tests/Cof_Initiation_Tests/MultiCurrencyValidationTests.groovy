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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

class MultiCurrencyValidationTests extends AbstractCofFlow {

	String consentPath = CofConstants.COF_CONSENT_PATH

	@Test (groups = ["1.3.6"])
	void "OB-1572_Sub Account Level COF initiation of Multi Currency Account"() {

		def initiationPayload = CofInitiationPayloads.initiationPayloadForSubAccLevelMultiCurrency

		doDefaultCofInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
	}

	@Test (groups = ["1.3.6"], dependsOnMethods = "OB-1572_Sub Account Level COF initiation of Multi Currency Account")
	void "OB-1578_COF consent authorisation for multi currency account"() {

		doCofAuthorizationFlow()
		Assert.assertNotNull(code)
	}

	@Test (groups = ["1.3.6"], priority = 1)
	void "OB-1573_Aggregation Level COF initiation of multi currency account"() {

		def initiationPayload = CofInitiationPayloads.initiationPayloadForAggregateLevelMultiCurrency

		doDefaultCofInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
	}

	@Test (groups = ["1.3.6"], priority = 1, dependsOnMethods = "OB-1573_Aggregation Level COF initiation of multi currency account")
	void "OB-1591_COF consent authorisation for aggregation level multi currency account"() {

		doCofAuthorizationFlow()
		Assert.assertNotNull(code)
	}
}
