/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework

import com.wso2.berlin.test.framework.automation.BGBasicAuthAutomationStep
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.berlin.test.framework.request_builder.BGRestAsRequestBuilder
import com.wso2.berlin.test.framework.request_builder.BerlinRequestBuilder
import com.wso2.berlin.test.framework.utility.BerlinOAuthAuthorization
import com.wso2.berlin.test.framework.utility.BerlinTestUtil
import com.wso2.openbanking.test.framework.OBTest
import com.wso2.openbanking.test.framework.automation.OBBrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import io.restassured.response.Response
import org.openqa.selenium.By
import org.testng.annotations.BeforeClass

/**
 * Class for defining common methods that needed in test classes.
 * Every test class in Test layer should extended from this.
 * Execute test framework initialization process
 */
class BGTest extends OBTest{

    String applicationAccessToken
    String accountId
    String code
    String consentStatus
    String userAccessToken
    Response consentResponse
    Response retrievalResponse
    BerlinOAuthAuthorization auth
    OBBrowserAutomation.AutomationContext automation
    final BerlinConstants.SCOPES scopes = BerlinConstants.SCOPES.ACCOUNTS
    @BeforeClass (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void setup(){

        BGRestAsRequestBuilder.init()
        applicationAccessToken = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT,
                scopes)
    }


    /**
     * Default Initiation
     */
    void doDefaultInitiation(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .post(consentPath)

        accountId = BerlinTestUtil.parseResponseBody(consentResponse, BerlinConstants.CONSENT_ID)
    }

    /**
     * Status Retrieval
     */
    void doStatusRetrieval(String consentPath) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${accountId}/status")

        consentStatus = BerlinTestUtil.parseResponseBody(retrievalResponse, BerlinConstants.CONSENT_STATUS)
    }

    /**
     * Authorization Flow
     */
    void doAuthorizationFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, accountId)
        automation = new OBBrowserAutomation(OBBrowserAutomation.DEFAULT_DELAY)
                .addStep(new BGBasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_SUBMIT_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        //Get Code from URL
        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get())
    }

    /**
     * Generate User Access Token
     */
    void generateUserAccessToken() {

        // Get User Access Token
        userAccessToken = BerlinRequestBuilder
                .getUserToken(auth.getVerifier(), code)
    }

    /**
     * Consent Deny Flow
     */
    void doConsentDenyFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, accountId)
        automation = new OBBrowserAutomation(OBBrowserAutomation.DEFAULT_DELAY)
                .addStep(new BGBasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_DENY_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        //Get Code from URL
        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get()).split("=")[0]
                .replace("+", " ")
    }
}
