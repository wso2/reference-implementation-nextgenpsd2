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

import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod
import com.wso2.openbanking.berlin.common.utils.AuthAutomationSteps
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.berlin.common.utils.OAuthAuthorizationRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import io.restassured.http.ContentType
import io.restassured.response.Response
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

        def consentPath = PaymentsConstants.BULK_PAYMENTS_PATH + "/" + PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS
        def initiationPayload = PaymentsInitiationPayloads.bulkPaymentPayload

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete the created Consent
        doConsentDelete(consentPath)

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .execute()

        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")

        Assert.assertEquals(oauthErrorCode,"The consent is not in an applicable status for authorization")
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
                .baseUri(ConfigParser.getInstance().baseURL)
                .post(consentPath)

        def paymentId = TestUtil.parseResponseBody(response, "paymentId")

        //Do Authorization
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .execute()

        //Verify the Error
        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")
        Assert.assertEquals(oauthErrorCode, "The logged in user does not match with the user who initiated the consent")
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
                .baseUri(ConfigParser.getInstance().baseURL)
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
                .execute()

        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")

        Assert.assertEquals(oauthErrorCode,"This consent has already been authorised by " +
                "${PsuConfigReader.getPSU()}@carbon.super")
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
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, paymentId,
                UUID.randomUUID().toString())

        new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .addStep {driver, context ->

                    WebElement lblErrorResponse = driver.findElement(By.xpath(BerlinConstants
                            .LBL_AUTH_PAGE_CLIENT_INVALID_ERROR_200))
                    Assert.assertTrue(lblErrorResponse.getText().trim().contains("Cannot find an application associated " +
                            "with the given consumer key"))
                }
                .execute()
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302010_Send the Authorisation Request without scope attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutScope()
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Scopes are not present or invalid")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302011_Send the Authorisation Request with unsupported scope value"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(BerlinConstants.SCOPES.ACCOUNTS,
                paymentId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "The provided consent Id mismatches with the scope type (\"ais, pis, piis\")")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302012_Send the Authorisation Request with invalid paymentId in the scope parameter"() {

        def paymentId = "1234"

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, paymentId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Matching consent not found for provided Id")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302013_Send the Authorisation Request without paymentId in the scope parameter"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, " ")
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Error while retrieving consent data. No consent Id provided with scope")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0302014_Send the Authorisation Request without state attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutState(scopes, paymentId)
        consentAuthorizeErrorFlowValidation(request)

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
    void "OB-1470_Implicit Authorisation when ExplicitAuthorisationPreferred param is not set in initiation"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
        Assert.assertNotNull(paymentId)

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        //Consent Status Retrieval
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1471_Implicit Authorisation when ExplicitAuthorisationPreferred param set to false"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
        Assert.assertNotNull(paymentId)

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        //Consent Status Retrieval
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1540_Authorisation with undefined PSU_ID when TPP-ExplicitAuthorisationPreferred set to false"() {

        String psuId = "psu1@wso2.com"

        //Consent Initiation
        Response consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, psuId)
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
        Assert.assertNotNull(paymentId)

        //Do Implicit Authorisation
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                            "The logged in user does not match with the user who initiated the consent")
                }
                .execute()

        //Verify the Error
        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")
        Assert.assertEquals(oauthErrorCode, "The logged in user does not match with the user who initiated the consent")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1541_Authorisation when PSU_ID not define in initiation when TPP-ExplicitAuthorisationPreferred set false"() {

        //Consent Initiation
        Response consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
        Assert.assertNotNull(paymentId)

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test(groups = ["1.3.6"])
    void "OB-1472_Implicit Authorisation when TPP-Redirect Preferred not set in initiation request"() {

        //Consent Initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test(groups = ["1.3.6"])
    void "OB-1473_Implicit Authorisation when PSU reject the auth flow"() {

        //Consent Initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
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

    @Test(groups = ["1.3.6"])
    void "OB-1476_Send Authorisation request without client id param"() {

        //Consent Initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
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

    @Test (groups = ["1.3.6"])
    void "OB-1479_Send Authorisation request with incorrect consent append to the scope"() {

        def paymentId = "1234"

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, paymentId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Matching consent not found for provided Id")
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

        Assert.assertEquals(oauthErrorCode, "invalid_request, Invalid response_type parameter value")
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

    /**
     * This testcase only supports if AccountReferenceType is configured as 'bban' in deployment.toml.
     */
    @Test (groups = ["1.3.3", "1.3.6"], enabled = false)
    void "TC0401023_Initiation Request with debtorAccount in bban attribute"() {

        String bulkPaymentConsentPath = PaymentsConstants.BULK_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS
        String accountAttributes = PaymentsConstants.accountAttributeBban
        String debtorAcc = PaymentsConstants.bbanAccount

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, accountAttributes, debtorAcc,
                PaymentsConstants.creditorName1, PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))

        doAuthorizationFlow()
        Assert.assertNotNull(code)
    }

    /**
     * This testcase only supports if AccountReferenceType is configured as 'pan' in deployment.toml.
     */
    @Test (groups = ["1.3.3", "1.3.6"], enabled = false)
    void "TC0401024_Initiation Request with debtorAccount in pan attribute"() {

        String bulkPaymentConsentPath = PaymentsConstants.BULK_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS
        String accountAttributes = PaymentsConstants.accountAttributePan
        String debtorAcc = PaymentsConstants.panAccount

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, accountAttributes, debtorAcc,
                PaymentsConstants.creditorName1, PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))

        doAuthorizationFlow()
        Assert.assertNotNull(code)
    }
}
