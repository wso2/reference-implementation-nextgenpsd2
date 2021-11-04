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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.Accounts_Authorise_Endpoint

import com.wso2.openbanking.berlin.common.utils.AuthAutomationSteps
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.berlin.common.utils.OAuthAuthorizationRequestBuilder
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
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

	String consentPath = AccountsConstants.ACCOUNTS_CONSENT_PATH
	String initiationPayload = AccountsPayloads.defaultInitiationPayload

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1409_Implicit Authorisation when ExplicitAuthorisationPreferred param is not set in initiation"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		doAuthorizationFlow()

		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		doStatusRetrieval(consentPath)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
	}

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1410_Implicit Authorisation when ExplicitAuthorisationPreferred param set to false"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
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
		doStatusRetrieval(consentPath)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1411_Implicit Authorisation when TPP-Redirect Preferred not set in initiation request"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_400)
		Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
						"Decoupled Approach is not supported.")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1412_Implicit Authorisation when PSU reject the auth flow "() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Consent Deny
		doConsentDenyFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertEquals(code, "User denied the consent")

		//Check consent status
		doStatusRetrieval(consentPath)
		Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1413_Send Authorisation request without state param"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
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
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithInvalidClientId(scopes, accountId)
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
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutScope()
		consentAuthorizeErrorFlowToValidateScopes(request)

		Assert.assertEquals(oauthErrorCode, "Scopes are not present or invalid")
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1418_Send Authorisation request with incorrect consent append to the scope"() {

		def accountId = "1234"

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedScope(scopes, accountId)
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

		accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(accountId)

		//Consent Authorisation
		auth = new BerlinOAuthAuthorization(scopes, accountId)
		automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
						.addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
						.addStep { driver, context ->
							driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_SUBMIT_XPATH)).click()
						}
						.addStep(new WaitForRedirectAutomationStep())
						.execute()

		//Get Code from URL
		code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get())

		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1425_Send Authorisation request with unsupported code_challenge_method"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		try {
			OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedCodeChallengeMethod(scopes, accountId)

		} catch (IllegalArgumentException e) {
			Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
		}
	}
}
