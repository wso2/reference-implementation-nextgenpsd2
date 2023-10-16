/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
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
    String paymentType

    /**
     * Data Factory to Retrieve Explicit Authorization Data from the Data Provider
     * @param maps
     *
     * Note: The auth_cancellation.enable attribute should set to true in deployment.toml file
     */
    @Factory(dataProvider = "ExplicitAuthCancellationData", dataProviderClass = PaymentsDataProviders.class)
    CancellationAuthorisationTest(Map<String, String> maps) {
        this.map = maps

        consentPath = map.get("consentPath")
        initiationPayload = map.get("initiationPayload")
        paymentType = map.get("paymentType")

    }

    @Test (groups = ["SmokeTest", "1.3.6"])
    void "BS-451 Create Payment Consent for Cancellation"() {

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId)
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "BS-451 Create Payment Consent for Cancellation")
    void "BS-531_Authorise and submit the Payment"() {

        doAuthorizationFlow()
        Assert.assertNotNull(code)

        // Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check the Authorisation status
        getExplicitAuthResources(consentPath, paymentId)
        Assert.assertNotNull(TestUtil.parseResponseBody(authorisationResponse, "authorisationIds[0]"))

        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_FINALISED)

    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = "BS-531_Authorise and submit the Payment")
    void "OB-1518_Delete Payment after Submission"() {

        def cancellationResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                .delete("${consentPath}/${paymentId}")

        Assert.assertEquals(cancellationResponse.statusCode(), BerlinConstants.STATUS_CODE_202)
        Assert.assertEquals(cancellationResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_ACTC)
        Assert.assertNotNull(cancellationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"))
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1518_Delete Payment after Submission")
    void "OB-1519_Create Explicit Cancellation Sub-Resource"() {

        createExplicitCancellation(consentPath)
        authorisationId = authorisationResponse.jsonPath().get("authorisationId")

        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1519_Create Explicit Cancellation Sub-Resource")
    void "OB-1522_PSU Authorise Payment Cancellation"() {

        def auth = new BerlinOAuthAuthorization(scopes, paymentId)
        def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertTrue(driver.findElement(By.xpath(BerlinConstants.PAYMENTS_INTENT_TEXT_XPATH))
                            .getText().contains("requests consent to cancel a ${paymentType} payment transaction"))
                    driver.findElement(By.xpath(BerlinConstants.PAYMENTS_SUBMIT_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        code = TestUtil.getCodeFromUrl(automation.currentUrl.get())

        //Check the Cancellation Status
        getCancellationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_FINALISED)

        //Check whether the payment is cancelled
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                .get("${consentPath}/${paymentId}")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1522_PSU Authorise Payment Cancellation")
    void "OB-1520_Get List of Authorisation Cancellation sub-resources"() {

        def authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .get("${consentPath}/${paymentId}/cancellation-authorisations")

        def authorisationsList = authorisationResponse.jsonPath().getList("authorisationIds")

        Assert.assertTrue(authorisationsList.contains(authorisationId))
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1522_PSU Authorise Payment Cancellation")
    void "OB-1521_Get Authorisation Cancellation sub-resource status"() {

        getCancellationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_FINALISED)
    }
}
