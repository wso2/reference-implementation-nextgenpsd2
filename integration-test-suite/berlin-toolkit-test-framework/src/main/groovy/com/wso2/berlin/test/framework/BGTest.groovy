/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework

import com.wso2.berlin.test.framework.automation.BGBasicAuthAutomationStep
import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.berlin.test.framework.request_builder.BGRestAsRequestBuilder
import com.wso2.berlin.test.framework.request_builder.BerlinRequestBuilder
import com.wso2.berlin.test.framework.utility.BerlinOAuthAuthorization
import com.wso2.berlin.test.framework.utility.BerlinTestUtil
import com.wso2.openbanking.test.framework.OBTest
import com.wso2.openbanking.test.framework.automation.OBBrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.configuration.OBConfigParser
import io.restassured.response.Response
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.openqa.selenium.By
import org.testng.annotations.BeforeClass

/**
 * Class for defining common methods that needed in test classes.
 * Every test class in Test layer should extended from this.
 * Execute test framework initialization process
 */
class BGTest extends OBTest{

    protected static Logger log = LogManager.getLogger(BGTest.class.getName());
    BGConfigurationService bgConfiguration
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
    public String accessToken
    private List<String> AccountScopes


    @BeforeClass(alwaysRun = true)
    void "Initialize Test Suite"() {
        OBConfigParser.getInstance(BerlinConstants.CONFIG_FILE_LOCATION)
        BGRestAsRequestBuilder.init()
        bgConfiguration = new BGConfigurationService()
    }

    /**
     * Get Application access token
     * @param clientId
     */
    String getApplicationAccessToken(String clientId = bgConfiguration.getAppInfoClientID()) {
        String token = BerlinRequestBuilder.getApplicationAccessToken(getApplicationScope(), clientId)
        if (token != null) {
            addToContext(BerlinConstants.APP_ACCESS_TKN, token)
        } else {
            log.error("Application access Token Cannot be generated")
        }
        return token
    }

    /**
     * Default Initiation to build the request and retrieve account id.
     *  @param consentPath
     *  @param initiationPayload
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
     * @param consentPath
     */
    void doStatusRetrieval(String consentPath) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${accountId}/status")

        consentStatus = BerlinTestUtil.parseResponseBody(retrievalResponse, BerlinConstants.CONSENT_STATUS)
    }

    /**
     * Consent Authorization Flow
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

    /**
     * Get Scopes
     * @return
     */
    List<String> getApplicationScope() {
        if (this.AccountScopes == null) {
            this.AccountScopes = [
                    BerlinConstants.SCOPES.ACCOUNTS.getScopes(),
            ] as List<String>
        }
        return this.AccountScopes
    }
}
