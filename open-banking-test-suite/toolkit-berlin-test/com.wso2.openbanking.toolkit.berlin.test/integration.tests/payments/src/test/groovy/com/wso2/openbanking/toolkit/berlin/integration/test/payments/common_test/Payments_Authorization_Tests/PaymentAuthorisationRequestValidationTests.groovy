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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.common_test.Payments_Authorization_Tests

import com.wso2.openbanking.berlin.common.utils.AuthAutomationSteps
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.berlin.common.utils.OAuthAuthorizationRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import io.restassured.http.ContentType
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Payment Authorisation Request Validation Tests
 */
class PaymentAuthorisationRequestValidationTests extends AbstractPaymentsFlow {

    def consentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" + PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS
    def initiationPayload = PaymentsInitiationPayloads.singlePaymentPayload

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302003_Authorize a Revoked consent"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete the created Consent
        doConsentDelete(consentPath)

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
            Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                    "Current Consent State is not valid for authorisation")
        }
        .execute()

        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")

        Assert.assertEquals(oauthErrorCode,"Current Consent State is not valid for authorisation")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302004_Authorize the consent by a different PSU when the PSU-ID is defined in the initiation request"() {

        def xRequestId = UUID.randomUUID().toString()

        //Consent Initiation
        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, BerlinConstants.PSU_EMAIL_ID)
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .post(consentPath)

        def paymentId = TestUtil.parseResponseBody(response, "paymentId")

        //Do Authorization
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
            Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                    "PSU-ID of the consent does not match with the logged in user")
        }
        .execute()

        //Verify the Error
        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")
        Assert.assertEquals(oauthErrorCode, "PSU-ID of the consent does not match with the logged in user")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302005_Authorize the consent by a different PSU when the PSU-ID is not defined in the initiation request"() {

        def xRequestId = UUID.randomUUID().toString()

        //Consent Initiation
        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .post(consentPath)

        paymentId = TestUtil.parseResponseBody(response, "paymentId")

        //Do Authorization
        doAuthorizationFlow()

        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302006_Authorize a Rejected consent"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete the created Consent
        doConsentDenyFlow()
        Assert.assertEquals(code, "User denied the consent")
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_REJECTED)

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
            Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                    "Unauthenticated authorization not found for Consent")
        }
        .execute()

        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")

        Assert.assertEquals(oauthErrorCode,"Unauthenticated authorization not found for Consent")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302007_Re Authorize a payment consent"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete the created Consent
        doAuthorizationFlow()
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
            Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                    "Unauthenticated authorization not found for Consent")
        }
        .execute()

        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")

        Assert.assertEquals(oauthErrorCode,"Unauthenticated authorization not found for Consent")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302008_Send the Authorisation Request without response_type attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutResponseType(scopes, paymentId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "invalid_request, Missing response_type parameter value")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302009_Send the Authorisation Request with invalid client_id value"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithInvalidClientId(scopes, paymentId)

        new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .addStep {driver, context ->

            switch (BerlinTestUtil.solutionVersion) {
                case [TestConstants.SOLUTION_VERSION_130, TestConstants.SOLUTION_VERSION_140, TestConstants.SOLUTION_VERSION_150]:

                    WebElement lblErrorResponse = driver.findElement(By.xpath(BerlinConstants.LBL_AUTH_PAGE_CLIENT_INVALID_ERROR))
                    Assert.assertTrue(lblErrorResponse.getText().trim().contains("{\"error_description\":\"A valid OAuth client " +
                            "could not be found for client_id: "))
                    break
                default:
                    WebElement lblErrorResponse = driver.findElement(By.xpath(BerlinConstants.LBL_AUTH_PAGE_CLIENT_INVALID_ERROR_200))
                    Assert.assertTrue(lblErrorResponse.getText().trim().contains("Cannot find an application associated " +
                            "with the given consumer key"))
                    break
            }
        }
        .execute()
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302010_Send the Authorisation Request without scope attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutScope()
        consentAuthorizeErrorFlowToValidateScopes(request)

        Assert.assertEquals(oauthErrorCode, "Scopes are not present or invalid")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302011_Send the Authorisation Request with unsupported scope value"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedScope(BerlinConstants.SCOPES.ACCOUNTS,
                paymentId)
        consentAuthorizeErrorFlowToValidateScopes(request)

        Assert.assertEquals(oauthErrorCode, "Requested consent not found for this TPP-Unique-ID")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302012_Send the Authorisation Request with invalid paymentId in the scope parameter"() {

        def paymentId = "1234"

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedScope(scopes, paymentId)
        consentAuthorizeErrorFlowToValidateScopes(request)

        Assert.assertEquals(oauthErrorCode, "Requested consent not found for this TPP-Unique-ID")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302013_Send the Authorisation Request without paymentId in the scope parameter"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedScope(scopes, " ")
        consentAuthorizeErrorFlowToValidateScopes(request)

        Assert.assertEquals(oauthErrorCode, "Error while retrieving payment data. No payment ID provided with " +
                "scope.")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302014_Send the Authorisation Request without state attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutState(scopes, paymentId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "invalid_request, 'state' parameter is required")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302015_Send the Authorisation Request without redirect_uri attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutRedirectionURI(scopes, paymentId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = URLDecoder.decode(authUrl.split("&")[1].split("=")[1].toString(), "UTF8")

        Assert.assertEquals(oauthErrorCode, "Redirect URI is not present in the authorization request")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302016_Send the Authorisation Request without code_challenge attribute"() {

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
    void "TC0302019_Send the Authorisation Request with unsupported code_challenge_method value"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        try {
            OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedCodeChallengeMethod(scopes, paymentId)

        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
        }
    }
}
