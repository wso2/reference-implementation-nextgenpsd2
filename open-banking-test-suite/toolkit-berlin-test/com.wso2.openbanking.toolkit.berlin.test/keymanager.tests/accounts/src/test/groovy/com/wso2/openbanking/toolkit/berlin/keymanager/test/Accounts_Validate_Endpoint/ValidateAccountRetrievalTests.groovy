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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.Accounts_Validate_Endpoint

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Validate Account Retrieval Tests.
 */
class ValidateAccountRetrievalTests extends AbstractAccountsFlow {

	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH
	String initiationPayload = AccountsPayloads.defaultInitiationPayload

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1458_Validate Bulk Account retrieval"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Authorise Consent
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Account Retrieval
		def validationPayload = AccountsPayloads.buildValidationPayload(AccountsConstants.ACCOUNT_ID,
						accountId, "accounts",
						"${ConfigParser.getInstance().getTppUserName()}@${ConfigParser.getInstance().getTenantDomain()}")

		def accountValidationPath = AccountsConstants.BULK_ACCOUNT_PATH

		doValidateAccountRetrieval(accountValidationPath, validationPayload, accessToken)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(Boolean.parseBoolean(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.IS_VALID)),
						true)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1459_Validate Specific Account retrieval"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Authorise Consent
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Account Retrieval
		def validationPayload = AccountsPayloads.buildValidationPayload(AccountsConstants.ACCOUNT_ID,
						accountId, "accounts",
						"${ConfigParser.getInstance().getTppUserName()}@${ConfigParser.getInstance().getTenantDomain()}")

		def accountValidationPath = AccountsConstants.SPECIFIC_ACCOUNT_PATH

		doValidateAccountRetrieval(accountValidationPath, validationPayload, accessToken)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(Boolean.parseBoolean(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.IS_VALID)),
						true)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1460_Validate Bulk Balances retrieval"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Authorise Consent
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Account Retrieval
		def validationPayload = AccountsPayloads.buildValidationPayload(AccountsConstants.ACCOUNT_ID,
						accountId, "accounts",
						"${ConfigParser.getInstance().getTppUserName()}@${ConfigParser.getInstance().getTenantDomain()}")

		def accountValidationPath = AccountsConstants.BALANCES_PATH

		doValidateAccountRetrieval(accountValidationPath, validationPayload, accessToken)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(Boolean.parseBoolean(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.IS_VALID)),
						true)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1461_Validate Bulk Transactions retrieval"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Authorise Consent
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Account Retrieval
		def validationPayload = AccountsPayloads.buildValidationPayload(AccountsConstants.ACCOUNT_ID,
						accountId, "accounts",
						"${ConfigParser.getInstance().getTppUserName()}@${ConfigParser.getInstance().getTenantDomain()}")

		def accountValidationPath = AccountsConstants.TRANSACTIONS_PATH

		doValidateAccountRetrieval(accountValidationPath, validationPayload, accessToken)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(Boolean.parseBoolean(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.IS_VALID)),
						true)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1462_Validate Specific Transaction retrieval"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Authorise Consent
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Account Retrieval
		def validationPayload = AccountsPayloads.buildValidationPayload(AccountsConstants.ACCOUNT_ID,
						accountId, "accounts",
						"${ConfigParser.getInstance().getTppUserName()}@${ConfigParser.getInstance().getTenantDomain()}")

		def accountValidationPath = AccountsConstants.SPECIFIC_TRANSACTIONS_PATH

		doValidateAccountRetrieval(accountValidationPath, validationPayload, accessToken)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(Boolean.parseBoolean(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.IS_VALID)),
						true)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1463_Validate Bulk Card Accounts retrieval"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Authorise Consent
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Account Retrieval
		def validationPayload = AccountsPayloads.buildValidationPayload(AccountsConstants.ACCOUNT_ID,
						accountId, "accounts",
						"${ConfigParser.getInstance().getTppUserName()}@${ConfigParser.getInstance().getTenantDomain()}")

		def accountValidationPath = AccountsConstants.CARD_ACCOUNTS_PATH

		doValidateAccountRetrieval(accountValidationPath, validationPayload, accessToken)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(Boolean.parseBoolean(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.IS_VALID)),
						true)
	}
}
