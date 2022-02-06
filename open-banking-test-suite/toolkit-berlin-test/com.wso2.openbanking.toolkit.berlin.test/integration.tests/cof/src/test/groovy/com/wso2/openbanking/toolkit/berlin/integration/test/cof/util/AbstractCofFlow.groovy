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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.util

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
import org.testng.Assert
import org.testng.annotations.BeforeClass

import java.text.SimpleDateFormat

/**
 * Abstract COF Flow Methods
 */
abstract class AbstractCofFlow {

    String applicationAccessToken
    String consentId
    String code
    String consentStatus
    String authorisationId
    String requestId
    Response consentResponse
    Response authorisationResponse
    Response retrievalResponse
    String oauthErrorCode
    Response consentDeleteResponse
    BerlinOAuthAuthorization auth
    String userAccessToken
    BrowserAutomation.AutomationContext automation
    final BerlinConstants.SCOPES scopes = BerlinConstants.SCOPES.COF

    @BeforeClass (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void setup(){

        TestSuite.init()
        applicationAccessToken = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT, scopes)
    }

    static String getCurrentDate() {
        return new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z").format(new Date())
    }

    void doDefaultCofInitiation(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "consentStatus")
    }

    void doCofAuthorizationFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, consentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.COF_SUBMIT_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        //Get Code from URL
        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get())
    }

    void consentAuthorizeErrorFlow(AuthorizationRequest request){

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .execute()
    }

    void consentAuthorizeErrorFlowValidation(AuthorizationRequest request){

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(request.toURI().toString()))
                .execute()

        oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")
    }

    void doConsentDenyFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, consentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.COF_DENY_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        //Get Code from URL
        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get()).split("=")[0]
                .replace("+", " ")
    }

    void generateUserAccessToken() {

        // Get User Access Token
        userAccessToken = BerlinRequestBuilder
                .getUserToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT, scopes, auth.getVerifier(), code)
    }

    void doStatusRetrieval(String consentPath) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${consentId}/status")

        consentStatus = TestUtil.parseResponseBody(retrievalResponse, "consentStatus")
    }

    void doConsentRetrieval(String consentPath) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${consentId}")

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        consentStatus = TestUtil.parseResponseBody(retrievalResponse, "consentStatus")
    }

    void deleteCofConsent(String consentPath) {

        // Delete the Consent
        consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}/${consentId}")
    }

    void getConsentStatus(String consentPath, String consent_Id) {
        retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${consent_Id}/status")
        consentStatus = TestUtil.parseResponseBody(retrievalResponse, "consentStatus")
    }

    void doExplicitAuthInitiation(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "consentStatus")
    }

    void createExplicitAuthorization(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body("{}")
                .post("${consentPath}/${consentId}/authorisations")

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
    }

    void getAuthorizationStatus(String consentPath) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${consentId}/authorisations/${authorisationId}")
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
     * Get List of Explicit Authorisation Resources.
     * @param consentPath
     */
    void getExplicitAuthResources(String consentPath, String consent_Id = consentId) {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${consent_Id}/authorisations")
    }
}
