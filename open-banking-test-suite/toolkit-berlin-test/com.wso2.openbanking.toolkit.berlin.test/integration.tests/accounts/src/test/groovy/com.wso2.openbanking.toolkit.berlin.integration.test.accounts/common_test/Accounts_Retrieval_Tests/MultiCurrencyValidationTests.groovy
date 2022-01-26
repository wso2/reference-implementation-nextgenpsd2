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
	void "OB-1534_Sub Account Level Bulk Retrieval of Multi Currency Account"() {

		initiationPayload = AccountsInitiationPayloads.subAccLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.ACCOUNTS_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[0]"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[0].iban"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[0].currency"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[1]"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[1].iban"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[1].currency"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[2]"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[2].iban"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts[2].currency"))
	}

	@Test(groups = ["1.3.6"])
	void "OB-1535_Sub Account Level specific account retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.subAccLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("account.iban"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("account.currency"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("account.iban"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("account.currency"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("account.iban"))
		Assert.assertNotNull(response.jsonPath().getJsonObject("account.currency"))
	}

	@Test(groups = ["1.3.6"])
	void "OB-1536_Aggregation level bulk retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.aggregationLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.ACCOUNTS_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
	}

	@Test(groups = ["1.3.6"])
	void "OB-1537_Aggregation level specific retrieval of multi currency account"() {

		initiationPayload = AccountsInitiationPayloads.aggregationLevelMultiCurrencyInitiationPayload

		//Account Initiation and Authorisation Step
		preRetrievalStep(initiationPayload)

		//Account Retrieval
		def response = BerlinRequestBuilder
						.buildBasicRequest(userAccessToken)
						.header(BerlinConstants.CONSENT_ID_HEADER, accountId)
						.get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

		Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
	}

	@Test(groups = ["1.3.6"])
	void "OB-1538_Aggregation and sub account level bulk retrieval of multi currency account"() {

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
