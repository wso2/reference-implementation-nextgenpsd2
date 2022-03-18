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
    void "Create Payment Consent for Cancellation"() {

        doExplicitAuthInitiation(consentPath, initiationPayload)

        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(authorisationResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))
    }

    @Test(groups = ["SmokeTest", "1.3.6"], dependsOnMethods = "Create Payment Consent for Cancellation")
    void "OB-1517_Consent Explicit Authorisation for Payment Cancellation"() {

        createExplicitAuthorization(consentPath)

        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Check the Authorisation status
        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1517_Consent Explicit Authorisation for Payment Cancellation")
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

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = "Authenticate PSU on SCA Flow")
    void "OB-1518_Delete payment Consent after Explicit Authorisation"() {

        def cancellationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}/${paymentId}")

        Assert.assertEquals(cancellationResponse.statusCode(), BerlinConstants.STATUS_CODE_202)
        Assert.assertEquals(cancellationResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_ACTC)
        Assert.assertNotNull(cancellationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"))
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1518_Delete payment Consent after Explicit Authorisation")
    void "OB-1519_Create Explicit Cancellation Sub-Resource"() {

        createExplicitCancellation(consentPath)
        authorisationId = authorisationResponse.jsonPath().get("authorisationId")

        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1519_Create Explicit Cancellation Sub-Resource")
    void "OB-1522_PSU Authorise Cancellation for Explicit Auth Consent"() {

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
                PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)

        //Check whether the payment is cancelled
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}")

        def consentStatus = TestUtil.parseResponseBody(retrievalResponse, "transactionStatus")
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1522_PSU Authorise Cancellation for Explicit Auth Consent")
    void "OB-1520_Get List of Authorisation Cancellation sub-resources"() {

        def authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .get("${consentPath}/${paymentId}/cancellation-authorisations")

        def authorisationsList = authorisationResponse.jsonPath().getList("authorisationIds")

        Assert.assertTrue(authorisationsList.contains(authorisationId))
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "OB-1522_PSU Authorise Cancellation for Explicit Auth Consent")
    void "OB-1521_Get Authorisation Cancellation sub-resource status"() {

        getCancellationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)
    }

    @Test (groups = ["1.3.6"], priority = 1)
    void "OB-1523_Consent Implicit Authorisation for Payment Cancellation"() {

        //Consent Initiation
        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body(initiationPayload)
                .post(consentPath)

        paymentId = TestUtil.parseResponseBody(authorisationResponse, "paymentId")

        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(authorisationResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth"))

        //Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(code)
    }

    @Test(groups = ["1.3.6"], priority = 1,
            dependsOnMethods = "OB-1523_Consent Implicit Authorisation for Payment Cancellation")
    void "OB-1524_Delete payment Consent after Implicit Authorisation"() {

        def cancellationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}/${paymentId}")

        Assert.assertEquals(cancellationResponse.statusCode(), BerlinConstants.STATUS_CODE_202)
        Assert.assertEquals(cancellationResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_ACTC)
        Assert.assertNotNull(cancellationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification.href"))
    }

    @Test(groups = ["1.3.6"], priority = 1,
            dependsOnMethods = "OB-1524_Delete payment Consent after Implicit Authorisation")
    void "OB-1525_Create Cancellation Sub-Resource for Implicit Auth Consent"() {

        createExplicitCancellation(consentPath)
        authorisationId = authorisationResponse.jsonPath().get("authorisationId")

        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test(groups = ["1.3.6"], priority = 1,
            dependsOnMethods = "OB-1525_Create Cancellation Sub-Resource for Implicit Auth Consent")
    void "OB-1526_PSU Authorise Cancellation for Implicit Auth Consent"() {

        def auth = new BerlinOAuthAuthorization(scopes, paymentId)
        def automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertTrue(driver.findElement(By.xpath(BerlinConstants.PAYMENTS_INTENT_TEXT_XPATH))
                            .getText().contains("requests consent to cancel"))
                    driver.findElement(By.xpath(BerlinConstants.PAYMENTS_SUBMIT_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        // Get Code From URL
        code = TestUtil.getCodeFromUrl(automation.currentUrl.get())

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
}
