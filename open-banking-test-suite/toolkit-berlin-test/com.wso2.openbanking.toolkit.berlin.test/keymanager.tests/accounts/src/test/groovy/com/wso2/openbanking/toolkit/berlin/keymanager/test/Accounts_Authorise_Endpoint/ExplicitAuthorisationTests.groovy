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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.Accounts_Authorise_Endpoint

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Explicit Authorisation Request.
 */
class ExplicitAuthorisationTests extends AbstractAccountsFlow {

	String url
	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH
	String initiationPayload = AccountsPayloads.defaultInitiationPayload

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 1)
	void "OB-1426_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true"() {

		//Consent Initiation
		doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"))

		//Create Explicit Authorisation Resources
		createExplicitAuthorization(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		authorisationId = authorisationResponse.jsonPath().get("authorisationId")
		requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
		Assert.assertNotNull(requestId)
		Assert.assertNotNull(authorisationId)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

		//Do Authorisation
		doAuthorizationFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		//Check Consent Status
		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
	}

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
					dependsOnMethods = "OB-1426_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true", priority = 2)
	void "OB-1455_Get list of all authorisation sub-resource IDs"() {

		getExplicitAuthResources(consentPath)
		Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(authorisationId, authorisationResponse.jsonPath().get("authorisationIds[0]"))
	}

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
					dependsOnMethods = "OB-1426_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true", priority = 3)
	void "OB-1457_Get SCA status of consent authorisation sub-resource"() {

		getExplicitAuthResourceStatus(consentPath)
		Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(authorisationResponse.jsonPath().get(BerlinConstants.SCA_STATUS_PARAM),
						AccountsConstants.CONSENT_STATUS_PSUAUTHENTICATED)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1427_Consent Initiation when TPP-Redirect Preferred not set in initiation request"() {

		//Consent Initiation
		doDefaultInitiationWithoutRedirectPreffered(consentPath, initiationPayload)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"), AccountsConstants
				.CONSENT_STATUS_RECEIVED)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"),
				"${ConfigParser.getInstance().getBaseURL()}/.well-known/openid-configuration")
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "chosenScaMethod" +
				".authenticationType"), "SMS_OTP")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1428_Explicit Authorisation when TPP-Redirect Preferred not set in initiation request "() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"), AccountsConstants
				.CONSENT_STATUS_RECEIVED)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "_links.self.href"), "/v1/consents/"
				+ accountId)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "_links.status.href"), "/v1/consents/" +
				accountId + "/status")
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "chosenScaMethod" +
				".authenticationType"), "SMS_OTP")
		Assert.assertEquals(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"),
				"/v1/consents/" + accountId +"/authorisations")

		//Create Explicit Authorisation Resources
		createExplicitAuthorization(consentPath)
		authorisationId = authorisationResponse.jsonPath().get("authorisationId")
		requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
		Assert.assertNotNull(requestId)
		Assert.assertNotNull(authorisationId)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
				AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1429_Explicit Authorisation when PSU reject the consent"() {

		//Consent Initiation
		doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"))

		//Create Explicit Authorisation Resources
		createExplicitAuthorization(consentPath)
		authorisationId = authorisationResponse.jsonPath().get("authorisationId")
		requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
		Assert.assertNotNull(requestId)
		Assert.assertNotNull(authorisationId)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

		//Do Authorisation
		BrowserAutomation.AutomationContext responseConsentDeny = doConsentDenyFlow()
		url = responseConsentDeny.currentUrl.get()
		def errorMessage = url.split("error_description=")[1].split("&")[0].replaceAll("\\+"," ")
		Assert.assertEquals(errorMessage, "User denied the consent")
		Assert.assertNotNull(url.contains("state"))

		//Check consent status
		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1454_Create consent authorisation resource for incorrect consent id"() {

		//Consent Initiation
		doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"))

		//Create Explicit Authorisation Resources
		def consentId = "12345"
		createExplicitAuthorization(consentPath, consentId)

		Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
		Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_UNKNOWN)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1456_Send Get list of all authorisation sub-resource request with invalid consent id"() {

		def accountId = "1234"

		getExplicitAuthResources(consentPath, accountId)
		Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
		Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse, BerlinConstants.TPPMESSAGE_CODE),
						BerlinConstants.CONSENT_UNKNOWN)
	}

	@Test(groups = ["1.3.6"])
	void "OB-1531_Authorisation with undifed PSU_ID when TPP-ExplicitAuthorisationPreferred set to true"() {

		//Consent Initiation
		consentResponse = TestSuite.buildRequest()
						.contentType(ContentType.JSON)
						.header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
						.header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
						.header(TestConstants.AUTHORIZATION_HEADER_KEY, "Basic ${accessToken}")
						.header(BerlinConstants.PSU_ID, "psu1@wso2.com")
						.header(BerlinConstants.PSU_TYPE, "email")
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.filter(new BerlinSignatureFilter())
						.baseUri(ConfigParser.instance.authorisationServerURL)
						.body(initiationPayload)
						.post(consentPath)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)
		Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"))

		//Create Explicit Authorisation Resources
		createExplicitAuthorization(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		authorisationId = authorisationResponse.jsonPath().get("authorisationId")
		requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
		Assert.assertNotNull(requestId)
		Assert.assertNotNull(authorisationId)
		Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
						AccountsConstants.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

		//Do Authorisation
		doAuthorizationFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		//Check Consent Status
		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
	}
}
