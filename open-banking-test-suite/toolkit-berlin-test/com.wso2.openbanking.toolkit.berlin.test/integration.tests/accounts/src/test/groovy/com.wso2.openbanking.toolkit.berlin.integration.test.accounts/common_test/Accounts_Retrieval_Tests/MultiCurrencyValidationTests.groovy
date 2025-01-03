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
