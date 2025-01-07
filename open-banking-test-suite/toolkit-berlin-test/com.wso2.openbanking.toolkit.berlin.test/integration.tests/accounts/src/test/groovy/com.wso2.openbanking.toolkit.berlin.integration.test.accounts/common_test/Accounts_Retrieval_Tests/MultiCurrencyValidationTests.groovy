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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Retrieval_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

class MultiCurrencyValidationTests extends AbstractAccountsFlow {

	String consentPath = AccountsConstants.CONSENT_PATH
	String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

	void preRetrievalStep(String initiationPayload) {

		//Do Account Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

		//Authorize the Consent and Extract the Code
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Get User Access Token
		generateUserAccessToken()
		Assert.assertNotNull(userAccessToken)
	}

	@Test(groups = ["1.3.6"])
	void "BG-373_Sub Account Level Bulk Retrieval of Multi Currency Account"() {

		initiationPayload = AccountsInitiationPayloads.subAccLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.ACCOUNTS_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[3]"))
		Assert.assertEquals(response.jsonPath().getJsonObject("accounts[3].iban"), BerlinConstants.MULTICURRENCY_ACCOUNT)
		Assert.assertEquals(response.jsonPath().getJsonObject("accounts[3].currency"), BerlinConstants.CURRENCY2)
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[4]"))
		Assert.assertEquals(response.jsonPath().getJsonObject("accounts[4].iban"), BerlinConstants.MULTICURRENCY_ACCOUNT)
		Assert.assertEquals(response.jsonPath().getJsonObject("accounts[4].currency"), BerlinConstants.CURRENCY1)
	}

	@Test(groups = ["1.3.6"])
	void "BG-374_Sub Account Level transaction retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.subAccLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.queryParam("bookingStatus", "booked")
						.get(AccountsConstants.TRANSACTIONS_PATHT_MULTICURRENCY)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
		Assert.assertEquals(response.jsonPath().getJsonObject("account.iban"), BerlinConstants.MULTICURRENCY_ACCOUNT)
	}

	@Test(groups = ["1.3.6"])
	void "BG-375_Sub Account Level balances retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.subAccLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.BALANCES_PATH_MULTICURRENCY)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("balances"))
		Assert.assertEquals(response.jsonPath().getJsonObject("account.iban"), BerlinConstants.MULTICURRENCY_ACCOUNT)
	}

	@Test(groups = ["1.3.6"])
	void "BG-376_Aggregation level bulk retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.aggregationLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.ACCOUNTS_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[3]"))
		Assert.assertEquals(response.jsonPath().getJsonObject("accounts[3].iban"), BerlinConstants.MULTICURRENCY_ACCOUNT)
	}

	@Test(groups = ["1.3.6"])
	void "BG-377_Aggregation level transaction retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.aggregationLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.TRANSACTIONS_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("transactions"))
		Assert.assertEquals(response.jsonPath().getJsonObject("account.iban"), BerlinConstants.MULTICURRENCY_ACCOUNT)
	}

	@Test(groups = ["1.3.6"])
	void "BG-379_Aggregation level balances retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.aggregationLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.BALANCES_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("balances"))
		Assert.assertEquals(response.jsonPath().getJsonObject("account.iban"), BerlinConstants.MULTICURRENCY_ACCOUNT)
	}

	@Test(groups = ["1.3.6"])
	void "BG-378_Aggregation and sub account level bulk retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.subAndAggregartionMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
	}
}
