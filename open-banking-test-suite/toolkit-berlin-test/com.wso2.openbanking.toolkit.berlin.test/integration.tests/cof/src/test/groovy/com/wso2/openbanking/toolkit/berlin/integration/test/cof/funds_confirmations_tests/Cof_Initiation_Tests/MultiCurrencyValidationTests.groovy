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
