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
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import io.restassured.http.ContentType
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
        authorisationId = TestUtil.parseResponseBody(authorisationResponse, "authorisationIds[0]")
        Assert.assertNotNull(TestUtil.parseResponseBody(authorisationResponse, "authorisationIds[0]"))

        getAuthorizationStatus(consentPath)

        String scaStatus = authorisationResponse.jsonPath().get("scaStatus")
        Assert.assertTrue(scaStatus.equalsIgnoreCase(PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED) ||
                scaStatus.equalsIgnoreCase(PaymentsConstants.SCA_STATUS_FINALISED))
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

        String scaStatus = authorisationResponse.jsonPath().get("scaStatus")
        Assert.assertTrue(scaStatus.equalsIgnoreCase(PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED) ||
                scaStatus.equalsIgnoreCase(PaymentsConstants.SCA_STATUS_FINALISED))

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

        String scaStatus = authorisationResponse.jsonPath().get("scaStatus")
        Assert.assertTrue(scaStatus.equalsIgnoreCase(PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED) ||
                scaStatus.equalsIgnoreCase(PaymentsConstants.SCA_STATUS_FINALISED))
    }

    @Test (groups = ["1.3.6"])
    void "BG-461_Explicit Cancellation Authorisation with same x-request-id and same consent id"() {

        //Initiate and authorise payment consent for cancellation
        paymentId = doConsentAuthorisationForCancellation()

        //Create Explicit Authorisation Sub Resource
        createExplicitCancellation(consentPath)
        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)

        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Create Explicit Authorisation Sub Resource 2
        def authorisationResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, requestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body("{}")
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post("${consentPath}/${paymentId}/cancellation-authorisations")

        def authorisationId2 = authorisationResponse2.jsonPath().get("authorisationId")
        def requestId2 = authorisationResponse2.getHeader(BerlinConstants.X_REQUEST_ID)

        Assert.assertEquals(requestId, requestId2)
        Assert.assertEquals(authorisationId, authorisationId2)
        Assert.assertEquals(authorisationResponse2.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse2.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test (groups = ["1.3.6"])
    void "BG-462_Explicit Cancellation Authorisation with same x-request-id and different consent id"() {

        //Initiate and authorise payment consent for cancellation
        paymentId = doConsentAuthorisationForCancellation()

        //Create Explicit Authorisation Sub Resource
        createExplicitCancellation(consentPath)
        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)

        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Initiate and authorise second payment consent for cancellation
        def paymentId2 = doConsentAuthorisationForCancellation()

        //Create Explicit Authorisation Sub Resource with same x-request-id and different consent
        def authorisationResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, requestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body("{}")
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post("${consentPath}/${paymentId2}/cancellation-authorisations")

        def authorisationId2 = authorisationResponse2.jsonPath().get("authorisationId")
        def requestId2 = authorisationResponse2.getHeader(BerlinConstants.X_REQUEST_ID)

        Assert.assertEquals(requestId, requestId2)
        Assert.assertNotEquals(authorisationId, authorisationId2)
        Assert.assertEquals(authorisationResponse2.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse2.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test (groups = ["1.3.6"])
    void "BG-534_Explicit Cancellation Authorisation with different x-request-id and same consent id"() {

        //Initiate and authorise payment consent for cancellation
        paymentId = doConsentAuthorisationForCancellation()

        //Create Explicit Authorisation Sub Resource
        createExplicitCancellation(consentPath)
        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)

        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Create Explicit Authorisation Sub Resource with different x-request-id and same consent
        def authorisationResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body("{}")
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post("${consentPath}/${paymentId}/cancellation-authorisations")

        Assert.assertEquals(authorisationResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse2, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue (TestUtil.parseResponseBody (authorisationResponse2, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Cannot use different unique identifier for the same consent ID when the request does not " +
                        "contain a payload."))
    }

    @Test (groups = ["1.3.6"])
    void "BG-535_Explicit Cancellation Authorisation without x-request-id"() {

        //Initiate and authorise payment consent for cancellation
        paymentId = doConsentAuthorisationForCancellation()

        //Create Explicit Authorisation Sub Resource
        authorisationResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body("{}")
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post("${consentPath}/${paymentId}/cancellation-authorisations")

        Assert.assertEquals(authorisationResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "X-Request-ID header is missing in the request")
    }

    /**
     * Common Steps required to create a resource for authorisation cancellation.
     */
     String doConsentAuthorisationForCancellation() {

        //Create Payment Consent for Cancellation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(paymentId)

        //Authorise Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        // Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check the Authorisation status
        getExplicitAuthResources(consentPath, paymentId)
        authorisationId = TestUtil.parseResponseBody(authorisationResponse, "authorisationIds[0]")
        Assert.assertNotNull(TestUtil.parseResponseBody(authorisationResponse, "authorisationIds[0]"))

        //Delete Payment After Submission
        def cancellationResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                .delete("${consentPath}/${paymentId}")
        Assert.assertEquals(cancellationResponse.statusCode(), BerlinConstants.STATUS_CODE_202)

        return paymentId
    }
}
