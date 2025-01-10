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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import io.restassured.response.Response
import org.openqa.selenium.By
import org.testng.annotations.BeforeClass

import java.nio.charset.Charset
import java.text.SimpleDateFormat

/**
 * Abstract Payment Flow Methods
 */
class AbstractPaymentsFlow {

    String accessToken
    String paymentId
    String code
    String consentStatus
    String authorisationId
    String requestId
    String appClientId
    Response consentResponse
    Response retrievalResponse
    Response consentRetrievalResponse
    Response authorisationResponse
    Response deleteResponse
    BerlinOAuthAuthorization auth
    BrowserAutomation.AutomationContext automation
    final BerlinConstants.SCOPES scopes = BerlinConstants.SCOPES.PAYMENTS
    String oauthErrorCode

    @BeforeClass (alwaysRun = true)
    void setup(){

        TestSuite.init()
        def authToken = "${ConfigParser.getInstance().keyManagerAdminUsername}:" +
                "${ConfigParser.getInstance().keyManagerAdminPassword}"
        accessToken = "${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset().toString()))}"
        appClientId = ConfigParser.getInstance().getClientId().toString()
    }

    /**
     * Basic Payment Initiation Request.
     * @param consentPath
     * @param initiationPayload
     */
    void doDefaultInitiation(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, ConfigParser.instance.clientId)
                .body(initiationPayload)
                .post(consentPath)

        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")
    }

    /**
     * Basic Payment Initiation Request Without Redirect Preffered.
     * @param consentPath
     * @param initiationPayload
     */
    void doDefaultInitiationWithoutRedirectPreffered(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .body(initiationPayload)
                .post(consentPath)
    }

    /**
     * Consent Initiation Request with Explicit Auth Preferred.
     * @param consentPath
     * @param initiationPayload
     */
    void doInitiationWithExplicitAuthPreferred(String consentPath, String initiationPayload) {

        consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .body(initiationPayload)
                .post(consentPath)

        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")
    }

    /**
     * Payment Consent Status Retrieval.
     * @param consentPath
     */
    void doStatusRetrieval(String consentPath) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .get(consentPath + "/" + paymentId + "/status")

        consentStatus = TestUtil.parseResponseBody(retrievalResponse, "transactionStatus")
    }

    /**
     * Payment Consent Retrieval.
     * @param consentPath
     */
    void doConsentRetrieval(String consentPath) {

        //Consent Retrieval
        consentRetrievalResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .get(consentPath + "/" + paymentId)

        consentStatus = TestUtil.parseResponseBody(consentRetrievalResponse, "transactionStatus")
    }

    /**
     * Consent Authorisation.
     */
    void doAuthorizationFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.PAYMENTS_SUBMIT_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        //Get Code from URL
        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get())

    }

    /**
     * Consent Deny.
     */
    void doConsentDenyFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, paymentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.PAYMENTS_DENY_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        //Get Code from URL
        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get()).replace("+", " ")

    }

    /**
     * Payment Initiation for Explicit Authorisation.
     * @param consentPath
     * @param initiationPayload
     */
    void doExplicitAuthInitiation(String consentPath, String initiationPayload) {

        consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, "true")
                .body(initiationPayload)
                .post(consentPath)

        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")
    }

    /**
     * Create Explicit Authorisation Sub-Resource.
     * @param consentPath
     */
    void createExplicitAuthorization(String consentPath, String consentId = paymentId) {

        authorisationResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .body("{}")
                .post("${consentPath}/${consentId}/authorisations")

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)

    }

    /**
     * Get Authorisation Status.
     * @param consentPath
     */
    void getAuthorizationStatus(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .get("${consentPath}/${paymentId}/authorisations/${authorisationId}")
    }

    /**
     * Create Explicit Authorisation Cancellation Sub-Resource.
     * @param consentPath
     */
    void createExplicitCancellation(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .body("{}")
                .post("${consentPath}/${paymentId}/cancellation-authorisations")

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
    }

    /**
     * Get Authorisation Cancellation resource Status.
     * @param consentPath
     */
    void getAuthorisationCancellationResource(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .get("${consentPath}/${paymentId}/cancellation-authorisations/${authorisationId}")
    }

    /**
     * Get Current date.
     * @return
     */
    static String getCurrentDate() {
        return new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z").format(new Date())
    }

    /**
     * Consent Delete.
     * @param consentPath
     */
    void doConsentDelete(String consentPath, String consentId = paymentId) {

        deleteResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .delete(consentPath + "/" + consentId)
    }

    void doConsentDeleteWithExplicitAuth(String consentPath, String consentId = paymentId) {

        deleteResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .delete(consentPath + "/" + consentId)
    }

    void consentAuthorizeErrorFlow(AuthorizationRequest request){

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .execute()
    }

    void consentAuthorizeErrorFlowToValidateScopes(AuthorizationRequest request, boolean isInvalidPaymentId) {

        if (isInvalidPaymentId) {
            automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                    .addStep(new BasicAuthAutomationStep(request.toURI().toString()))
                    .execute()

            oauthErrorCode = URLDecoder.decode(automation.currentUrl.get(), "UTF8").split("=")[2]
        } else {
            automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                    .addStep(new AuthAutomationSteps(request.toURI().toString()))
                    .execute()

            oauthErrorCode = URLDecoder.decode(automation.currentUrl.get(), "UTF8").split("&")[0].split("=")[1]
                    .split(",")[1].trim()
        }
    }

    /**
     * Get List of Explicit Authorisation Resources.
     * @param consentPath
     */
    void getExplicitAuthResources(String consentPath, String consentId = paymentId) {

        authorisationResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .get("${consentPath}/${consentId}/authorisations")
    }

    /**
     * Get Explicit Authorisation Resource Status.
     * @param consentPath
     */
    void getExplicitAuthResourceStatus(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .get("${consentPath}/${paymentId}/authorisations/${authorisationId}")
    }
}
