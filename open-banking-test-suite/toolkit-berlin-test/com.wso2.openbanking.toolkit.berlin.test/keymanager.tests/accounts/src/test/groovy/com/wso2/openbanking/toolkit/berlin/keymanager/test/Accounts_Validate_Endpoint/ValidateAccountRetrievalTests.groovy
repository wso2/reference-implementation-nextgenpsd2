/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
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
