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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.util

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.wso2.openbanking.berlin.common.utils.AuthAutomationSteps
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.util.TestUtil
import io.restassured.response.Response
import org.openqa.selenium.By
import org.testng.annotations.BeforeClass

import java.text.SimpleDateFormat

/**
 * Abstract Payment Flow Methods
 */
class AbstractPaymentsFlow {

    String applicationAccessToken
    String paymentId
    String code
    String consentStatus
    String userAccessToken
    String authorisationId
    String requestId
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
        applicationAccessToken = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT, scopes)
    }

    void doDefaultInitiation(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .post(consentPath)

        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")
    }

    void doStatusRetrieval(String consentPath) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                .get(consentPath + "/" + paymentId + "/status")

        consentStatus = TestUtil.parseResponseBody(retrievalResponse, "transactionStatus")
    }

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

    void generateUserAccessToken() {

        // Get User Access Token
        userAccessToken = BerlinRequestBuilder
                .getUserToken(auth.getVerifier(), code)
    }

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
        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get()).split("=")[0]
                .replace("+", " ")

    }

    void doExplicitAuthInitiation(String consentPath, String initiationPayload) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body(initiationPayload)
                .post(consentPath)

        paymentId = TestUtil.parseResponseBody(authorisationResponse, "paymentId")

    }

    void createExplicitAuthorization(String consentPath, String xRequestId = null) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken, xRequestId)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body("{}")
                .post("${consentPath}/${paymentId}/authorisations")

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)

    }

    void getAuthorizationStatus(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}/authorisations/${authorisationId}")
    }

    void createExplicitCancellation(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body("{}")
                .post("${consentPath}/${paymentId}/cancellation-authorisations")

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)

    }

    void getCancellationStatus(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}/cancellation-authorisations/${authorisationId}")
    }

    static String getCurrentDate() {
        return new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z").format(new Date())
    }

    void doConsentDelete(String consentPath) {

        deleteResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                .delete(consentPath + "/" + paymentId)
    }

    void consentAuthorizeErrorFlow(AuthorizationRequest request){

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .execute()
    }

    void consentAuthorizeErrorFlowValidation(AuthorizationRequest request) {

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(request.toURI().toString()))
                .execute()

        oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")
    }

    void doPaymentRetrieval(String consentPath) {

        //Status Retrieval
        consentRetrievalResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                .get(consentPath + "/" + paymentId)

        consentStatus = TestUtil.parseResponseBody(consentRetrievalResponse, "transactionStatus")
    }

    /**
     * Initiation Request without Redirect Preffered Param.
     * @param consentPath
     * @param initiationPayload
     */
    void doDefaultInitiationWithoutRedirectPreffered(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .body(initiationPayload)
                .post(consentPath)
    }

    /**
     * Consent Initiation Request with Explicit Auth Preferred.
     * @param consentPath
     * @param initiationPayload
     */
    void doInitiationWithExplicitAuthPreferred(String consentPath, String initiationPayload) {

        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .body(initiationPayload)
                .post(consentPath)

        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")
    }

    /**
     * Get List of Explicit Authorisation Resources.
     * @param consentPath
     */
    void getExplicitAuthResources(String consentPath, String paymentId = paymentId) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}/authorisations")
    }

    /**
     * Get Explicit Authorisation Resource Status.
     * @param consentPath
     */
    void getExplicitAuthResourceStatus(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}/authorisations/${authorisationId}")
    }
}
