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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.util

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
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import io.restassured.response.Response
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.BeforeClass

import java.nio.charset.Charset

/**
 * Abstract Account Flow Methods
 */
abstract class AbstractAccountsFlow {

    String accessToken
    String accountId
    String code
    String consentStatus
    String oauthErrorCode
    Response consentResponse
    Response retrievalResponse
    Response consentDeleteResponse
    Response authorisationResponse
    String authorisationId
    String requestId
    String appClientId
    BerlinOAuthAuthorization auth
    BrowserAutomation.AutomationContext automation
    final BerlinConstants.SCOPES scopes = BerlinConstants.SCOPES.ACCOUNTS

    @BeforeClass (alwaysRun = true)
    void setup(){

        TestSuite.init()
        def authToken = "${ConfigParser.getInstance().keyManagerAdminUsername}:" +
                "${ConfigParser.getInstance().keyManagerAdminPassword}"
        accessToken = "${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset().toString()))}"
        appClientId = ConfigParser.getInstance().getClientId().toString()
    }

    /**
     * Basic Initiation Request.
     * @param consentPath
     * @param initiationPayload
     */
    void doDefaultInitiation(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .body(initiationPayload)
                .post(consentPath)

        accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
    }

    /**
     * Initiation Request without Redirect Preffered Param.
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
     * Consent Status Retrieval Request.
     * @param consentPath
     * @param accountId
     */
    void doStatusRetrieval(String consentPath, String accountId) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .get("${consentPath}/${accountId}/status")

        consentStatus = TestUtil.parseResponseBody(retrievalResponse, "consentStatus")
    }

    /**
     * Consent Status Retrieval Request.
     * @param consentPath
     * @param accountId
     */
    void doStatusRetrievalWithoutConsentId(String consentPath) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .get("${consentPath}/status")

        consentStatus = TestUtil.parseResponseBody(retrievalResponse, "consentStatus")
    }

    /**
     * Consent Retrieval Request.
     * @param consentPath
     * @param accountId
     */
    void doConsentRetrieval(String consentPath, String accountId) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .get("${consentPath}/${accountId}")

        consentStatus = TestUtil.parseResponseBody(retrievalResponse, "consentStatus")
    }

    /**
     * Consent Delete Request.
     * @param consentPath
     * @param accountId
     */
    void deleteConsent(String consentPath, String accountId) {

        // Delete the Consent
        consentDeleteResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .delete("${consentPath}/${accountId}")
    }

    /**
     * Consent Authorisation Request.
     */
    void doAuthorizationFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, accountId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_SUBMIT_XPATH)).click()
                }
                .execute()

        //Get Code from URL
        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get())
    }

    /**
     * Consent Deny.
     */
    BrowserAutomation.AutomationContext doConsentDenyFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, accountId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_DENY_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        return automation
    }

    /**
     * Validate Account Retrieval Request.
     * @param validatePath
     */
    void doValidateAccountRetrieval(String validatePath, String payload, String accessToken) {

        retrievalResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .body(payload)
                .post("${validatePath}")
    }

    /**
     * Consent Authorisation Error Scenarios.
     * @param request
     */
    void consentAuthorizeErrorFlow(AuthorizationRequest request){

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .execute()
    }

    /**
     * Validate scope in consent authorisation flow.
     * @param request
     */
    void consentAuthorizeErrorFlowToValidateScopes(AuthorizationRequest request){

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(request.toURI().toString()))
                .addStep {driver, context ->
                    Assert.assertNotNull(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim())
                }
                .execute()

        oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")
    }

    /**
     * Consent Initiation Request with Explicit Auth Preferred.
     * @param consentPath
     * @param initiationPayload
     */
    void doInitiationWithExplicitAuthPreferred(String consentPath, String initiationPayload) {

        consentResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .body(initiationPayload)
                .post(consentPath)

        accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
    }

    /**
     * Create Explicit Authorisation Resource.
     * @param consentPath
     * @param consentId
     */
    void createExplicitAuthorization(String consentPath, String consentId = accountId) {

        authorisationResponse = BerlinRequestBuilder.buildKeyManagerRequest(accessToken)
                .header(TestConstants.X_WSO2_CLIENT_ID_KEY, appClientId)
                .body("{}")
                .post("${consentPath}/${consentId}/authorisations")
    }

    /**
     * Get List of Explicit Authorisation Resources.
     * @param consentPath
     */
    void getExplicitAuthResources(String consentPath, String consentId = accountId) {

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
                .get("${consentPath}/${accountId}/authorisations/${authorisationId}")
    }
}
