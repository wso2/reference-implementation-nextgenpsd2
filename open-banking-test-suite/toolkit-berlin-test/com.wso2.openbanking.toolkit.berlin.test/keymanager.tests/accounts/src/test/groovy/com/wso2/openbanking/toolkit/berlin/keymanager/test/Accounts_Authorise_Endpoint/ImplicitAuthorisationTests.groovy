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

import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod
import com.wso2.openbanking.berlin.common.utils.*
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.util.AccountsPayloads
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Implicit Authorisation Tests.
 */
class ImplicitAuthorisationTests extends AbstractAccountsFlow {

	String url
	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH
	String initiationPayload = AccountsPayloads.defaultInitiationPayload

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1409_Implicit Authorisation when ExplicitAuthorisationPreferred param is not set in initiation"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		doAuthorizationFlow()

		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
	}

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1410_Implicit Authorisation when ExplicitAuthorisationPreferred param set to false"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Consent Authorisation
		doAuthorizationFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		//Check Consent Status
		doStatusRetrieval(consentPath, accountId)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1411_Implicit Authorisation when TPP-Redirect Preferred not set in initiation request"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "consentStatus"), AccountsConstants
				.CONSENT_STATUS_RECEIVED)
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
		Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "chosenScaMethod[0].authenticationType"),
				"SMS_OTP")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1412_Implicit Authorisation when PSU reject the auth flow "() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Consent Deny
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
	void "OB-1413_Send Authorisation request without state param"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutState(scopes, accountId)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "invalid_request, 'state' parameter is required")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1416_Send Authorisation request with client id not bound to the consent"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, "1234")
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
						UUID.randomUUID().toString())
		new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
						.addStep(new AuthAutomationSteps(request.toURI().toString()))
						.addStep { driver, context ->
							WebElement lblErrorResponse = driver.findElement(By.xpath(BerlinConstants.LBL_AUTH_PAGE_CLIENT_INVALID_ERROR_200))
							Assert.assertTrue(lblErrorResponse.getText().trim().contains("Cannot find an application associated " +
											"with the given consumer key"))
						}
						.execute()
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1417_Send Authorisation request without scope param"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutScope()
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "invalid_request, Scopes are not present or invalid")
	}

//	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1418_Send Authorisation request with incorrect consent append to the scope"() {

		def accountId = "1234"

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId)
		consentAuthorizeErrorFlowToValidateScopes(request)

		Assert.assertEquals(oauthErrorCode, "Requested consent not found for this TPP-Unique-ID")
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1419_Send Authorisation request without code_challenge param"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutCodeChallenge(scopes, accountId)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "PKCE is mandatory for this application. PKCE " +
						"Challenge is not provided or is not upto RFC 7636 specification.")
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1421_Send Authorisation request without response_type param"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutResponseType(scopes, accountId)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "invalid_request, Missing response_type parameter value")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1423_Send Authorisation request without code_challenge_method"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutCodeChallenge(scopes, accountId)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "PKCE is mandatory for this application. PKCE Challenge is not provided " +
				"or is not upto RFC 7636 specification.")
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1425_Send Authorisation request with unsupported code_challenge_method"() {

		CodeChallengeMethod codeChallengeMethod = new CodeChallengeMethod("RS256")

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		try {
			OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
							AppConfigReader.getClientId(), "code", codeChallengeMethod)

		} catch (IllegalArgumentException e) {
			Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
		}
	}

	@Test (groups = ["1.3.6"])
	void "OB-1422_Send Authorisation request with incorrect response_type param"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
						AppConfigReader.getClientId(), "id_token&")
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "invalid_request, Incorrect response_type provided")
	}

	@Test (groups = ["1.3.6"])
	void "OB-1424_Send Authorisation request with plain value as the code_challenge_method"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
						AppConfigReader.getClientId(), "code", CodeChallengeMethod.PLAIN)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def code = BerlinTestUtil.getCodeFromURL(authUrl)
		Assert.assertNotNull(authUrl.contains("state"))
		Assert.assertNotNull(code)
	}
}
