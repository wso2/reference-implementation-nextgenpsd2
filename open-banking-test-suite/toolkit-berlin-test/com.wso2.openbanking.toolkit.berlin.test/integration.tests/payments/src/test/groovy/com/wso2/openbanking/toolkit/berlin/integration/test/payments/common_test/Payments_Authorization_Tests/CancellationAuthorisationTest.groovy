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

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.Factory
import org.testng.annotations.Test

/**
 * Tests Cancellation Authorisation API with Berlin Payments.
 */
class CancellationAuthorisationTest extends AbstractPaymentsFlow {

    Map<String, String> map
    String consentPath
    String initiationPayload

    /**
     * Data Factory to Retrieve Explicit Authorization Data from the Data Provider
     * @param maps
     */
    @Factory(dataProvider = "ExplicitAuthorizationData", dataProviderClass = PaymentsDataProviders.class)
    CancellationAuthorisationTest(Map<String, String> maps) {
        this.map = maps

        consentPath = map.get("consentPath")
        initiationPayload = map.get("initiationPayload")

    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void "Create Payment Consent for Cancellation"() {

        doExplicitAuthInitiation(consentPath, initiationPayload)

        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(authorisationResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisation"))
    }


    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Create Payment Consent for Cancellation"])
    void "Create Explicit Authorisation For Cancellation"() {

        createExplicitAuthorization(consentPath)

        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Check the Authorisation status
        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Create Explicit Authorisation For Cancellation"])
    void "Authenticate PSU on SCA Flow"() {

        doAuthorizationFlow()
        Assert.assertNotNull(code)

        // Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check the Authorisation status
        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)

    }

//    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Authenticate PSU on SCA Flow"])
    void "Delete Payment Consent"() {

        def cancellationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}/${paymentId}")

        if (cancellationResponse.contentType().isEmpty()) {
            Assert.assertEquals(cancellationResponse.statusCode(), BerlinConstants.STATUS_CODE_204)
        } else {
            Assert.assertEquals(cancellationResponse.jsonPath().get("transactionStatus"),
                    PaymentsConstants.TRANSACTION_STATUS_ACTC)
            Assert.assertNotNull(cancellationResponse.jsonPath().get("_links.startAuthorisation.href"))
        }
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Authenticate PSU on SCA Flow"])
    void "Create Explicit Cancellation"() {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .post("${consentPath}/${paymentId}/cancellation-authorisations")

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")

        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

    }

    // TODO : Uncomment this after fixing https://github.com/wso2-enterprise/financial-open-banking/issues/4878
//    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Create Explicit Cancellation"])

    void "Authorise Cancellation with PSU"() {

        def auth = new BerlinOAuthAuthorization(scopes, paymentId)
        def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertTrue(driver.findElement(By.xpath(BerlinConstants.PAYMENTS_INTENT_TEXT_XPATH))
                            .getText().contains("cancel payment"))
                    driver.findElement(By.xpath(BerlinConstants.PAYMENTS_SUBMIT_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        code = TestUtil.getCodeFromURL(automation.currentUrl.get())

        // Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check the Cancellation Status
        getCancellationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)

        //Check whether the payment is cancelled
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}")

        def consentStatus = TestUtil.parseResponseBody(retrievalResponse, "transactionStatus")

        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)

    }

 //   @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Authorise Cancellation with PSU"])
    void "Validate Cancellation IDs Are Listed"() {

        def authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}/cancellation-authorisations/")

        def authorisationsList = authorisationResponse.jsonPath().getList("authorisationIds")

        Assert.assertTrue(authorisationsList.contains(authorisationId))
    }
}
