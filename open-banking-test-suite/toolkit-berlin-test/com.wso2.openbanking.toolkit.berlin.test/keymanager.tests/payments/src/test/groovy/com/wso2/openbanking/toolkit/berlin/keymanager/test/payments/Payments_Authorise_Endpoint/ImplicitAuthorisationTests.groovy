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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.Payments_Authorise_Endpoint

import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod
import com.wso2.openbanking.berlin.common.utils.AuthAutomationSteps
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.berlin.common.utils.OAuthAuthorizationRequestBuilder
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util.PaymentsInitiationPayloads
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Implicit Authorisation Tests.
 */
class ImplicitAuthorisationTests extends AbstractPaymentsFlow {

	String consentPath = PaymentsConstants.SINGLE_PAYMENTS_CONSENT_PATH
	String initiationPayload = PaymentsInitiationPayloads.singlePaymentPayload

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1470_Implicit Authorisation when ExplicitAuthorisationPreferred param is not set in initiation"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

		doAuthorizationFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		doStatusRetrieval(consentPath)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
	}

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1471_Implicit Authorisation when ExplicitAuthorisationPreferred param set to false"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

		paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
		consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")
		Assert.assertNotNull(paymentId)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

		doAuthorizationFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertNotNull(code)

		doStatusRetrieval(consentPath)
		Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1472_Implicit Authorisation when TPP-Redirect Preferred not set in initiation request"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.body(initiationPayload)
						.post(consentPath)

		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
	}

	@Test(groups = ["SmokeTest", "1.3.3", "1.3.6"])
	void "OB-1473_Implicit Authorisation when PSU reject the auth flow"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)

		//Consent Deny
		doConsentDenyFlow()
		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertEquals(code, "User denied the consent")

		//Check consent status
		doStatusRetrieval(consentPath)
		Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_REJECTED)
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1474_Send Authorisation request without state param"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)

		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutState(scopes, paymentId)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "invalid_request, 'state' parameter is required")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1476_Send Authorisation request without client id param"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)

		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, paymentId,
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
	void "OB-1478_Send Authorisation request without scope param"() {

		//Consent Initiation
		consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
						.header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
						.header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
						.header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
						.body(initiationPayload)
						.post(consentPath)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutScope()
		consentAuthorizeErrorFlowToValidateScopes(request, false)

		Assert.assertEquals(oauthErrorCode, "Scopes are not present or invalid")
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1479_Send Authorisation request with incorrect consent append to the scope"() {

		def paymentId = "1234"

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, paymentId)
		consentAuthorizeErrorFlowToValidateScopes(request, true)

		Assert.assertEquals(oauthErrorCode, "Retrieving consent data failed")
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1480_Send Authorisation request without code_challenge param"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutCodeChallenge(scopes, paymentId)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "PKCE is mandatory for this application. PKCE " +
						"Challenge is not provided or is not upto RFC 7636 specification.")
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1482_Send Authorisation request without response_type param"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutResponseType(scopes, paymentId)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "invalid_request, Missing response_type parameter value")
	}

	@Test(groups = ["1.3.3", "1.3.6"])
	void "OB-1484_Send Authorisation request without code_challenge_method"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
		Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
		Assert.assertNotNull(paymentId)

		//Consent Authorisation
		auth = new BerlinOAuthAuthorization(scopes, paymentId, CodeChallengeMethod.PLAIN)
		automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
						.addStep(new AuthAutomationSteps(auth.authoriseUrl))
						.addStep(new WaitForRedirectAutomationStep())
						.execute()

		Assert.assertNotNull(automation.currentUrl.get().contains("state"))
		Assert.assertFalse(automation.currentUrl.get().contains("code="))
	}

	@Test (groups = ["1.3.3", "1.3.6"])
	void "OB-1486_Send Authorisation request with unsupported code_challenge_method"() {

		CodeChallengeMethod codeChallengeMethod = new CodeChallengeMethod("RS256")

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		try {
			OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, paymentId,
							AppConfigReader.getClientId(), "code", codeChallengeMethod)

		} catch (IllegalArgumentException e) {
			Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
		}
	}

	@Test (groups = ["1.3.6"])
	void "OB-1483_Send Authorisation request with incorrect response_type param"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, paymentId,
						AppConfigReader.getClientId(), "id_token&")
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

		Assert.assertEquals(oauthErrorCode, "invalid_request, Incorrect response_type provided")
	}

	@Test (groups = ["1.3.6"])
	void "OB-1485_Send Authorisation request with plain value as the code_challenge_method"() {

		//Consent Initiation
		doDefaultInitiation(consentPath, initiationPayload)

		//Do Authorization
		def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, paymentId,
						AppConfigReader.getClientId(), "code", CodeChallengeMethod.PLAIN)
		consentAuthorizeErrorFlow(request)

		String authUrl = automation.currentUrl.get()
		def code = BerlinTestUtil.getCodeFromURL(authUrl)
		Assert.assertNotNull(authUrl.contains("state"))
		Assert.assertNotNull(code)
	}
}
