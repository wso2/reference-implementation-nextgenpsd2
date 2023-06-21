/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.berlin.test.framework

import com.wso2.berlin.test.framework.constant.BerlinConstants

import com.wso2.berlin.test.framework.request_builder.BGRestAsRequestBuilder
import com.wso2.berlin.test.framework.request_builder.BerlinRequestBuilder
import com.wso2.berlin.test.framework.utility.BerlinOAuthAuthorization
import com.wso2.berlin.test.framework.utility.BerlinTestUtil
import com.wso2.openbanking.test.framework.OBTest
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import io.restassured.response.Response
import org.openqa.selenium.By
import org.testng.annotations.BeforeClass

class BGTest extends OBTest{

    String applicationAccessToken
    String accountId
    String code
    String consentStatus
    String userAccessToken
    Response consentResponse
    Response retrievalResponse
    BerlinOAuthAuthorization auth
    BrowserAutomation.AutomationContext automation
    final BerlinConstants.SCOPES scopes = BerlinConstants.SCOPES.ACCOUNTS
    @BeforeClass (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void setup(){

        BGRestAsRequestBuilder.init()
        applicationAccessToken = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT,
                scopes)
    }


    void doDefaultInitiation(String consentPath, String initiationPayload) {

        //initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .post(consentPath)

        accountId = BerlinTestUtil.parseResponseBody(consentResponse, "consentId")
    }

    void doStatusRetrieval(String consentPath) {

        //Status Retrieval
        retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${accountId}/status")

        consentStatus = BerlinTestUtil.parseResponseBody(retrievalResponse, "consentStatus")
    }

    void doAuthorizationFlow() {

        // Initiate SCA flow.
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
    }


    void generateUserAccessToken() {

        // Get User Access Token
        userAccessToken = BerlinRequestBuilder
                .getUserToken(auth.getVerifier(), code)
    }

    void doConsentDenyFlow() {

        // Initiate SCA flow.
        auth = new BerlinOAuthAuthorization(scopes, accountId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
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
