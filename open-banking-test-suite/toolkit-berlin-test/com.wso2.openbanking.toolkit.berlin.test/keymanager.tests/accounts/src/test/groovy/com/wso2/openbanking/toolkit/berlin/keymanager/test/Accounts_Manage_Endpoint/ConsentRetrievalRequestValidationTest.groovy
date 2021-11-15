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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.Accounts_Manage_Endpoint

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsPayloads
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Consent Retrieval Request Validation Test.
 */
class ConsentRetrievalRequestValidationTest extends AbstractAccountsFlow {

	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH

	@BeforeClass (alwaysRun = true)
	void "Consent Initiation"() {
		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllAccounts)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)
		Assert.assertEquals(consentResponse, AccountsConstants.CONSENT_STATUS_RECEIVED)
	}

	@Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1434_Retrieve consent details"() {

		doConsentRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
		Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
						LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
	}

	@Test (groups = ["1.3.3", "1.3.6"], dependsOnMethods = "OB-1434_Retrieve consent details")
	void "OB-1437_Retrieve status of a valid consent"() {

		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1435_Consent detail retrieval for invalid consent id"() {

		def accountId = "1234"

		doConsentRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
		Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_UNKNOWN)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1436_Consent details retrieval for a deleted consent"() {

		//Delete the consent
		deleteConsent(consentPath, accountId)
		Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

		//Retrieve consent
		doConsentRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
		Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
						LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
	}

	@Test (groups = ["1.3.3", "1.3.6"],
					dependsOnMethods = "OB-1436_Consent details retrieval for a deleted consent")
	void "OB-1439_Retrieve status of a deleted consent"() {

		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1443_Consent details retrieval for terminated consent"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllAccounts)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

		//Deny the consent
		doConsentDenyFlow()
		Assert.assertEquals(code, "User denied the consent")

		//Retrieve consent
		doConsentRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
		Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
						LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
	}

	@Test (groups = ["1.3.3", "1.3.6"],
					dependsOnMethods = "OB-1443_Consent details retrieval for terminated consent")
	void "OB-1440_Retrieve status of a terminated consent"() {

		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1444_Consent details retrieval for authorised consent"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, AccountsPayloads.initiationPayloadForAllAccounts)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

		//Authorised the consent
		doAuthorizationFlow()
		Assert.assertNotNull(code)

		//Retrieve consent
		doConsentRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
		Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
		Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
						LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
	}

	@Test (groups = ["1.3.3", "1.3.6"],
					dependsOnMethods = "OB-1444_Consent details retrieval for authorised consent")
	void "OB-1441_Retrieve status of an authorised consent"() {

		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1438_Retrieve status of a invalid consent"() {

		def accountId = "1234"

		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
		Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_UNKNOWN)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1451_Consent detail retrieval without consent id"() {

		def accountId = ""

		doConsentRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_404)
		Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.RESOURCE_UNKNOWN)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1452_Retrieve consent status without consent id"() {

		def accountId = ""

		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_404)
		Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.RESOURCE_UNKNOWN)
	}
}
