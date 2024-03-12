/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.common_test.Payments_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Get Payment Consent Response Validation Tests
 */
class GetPaymentResponseValidationTests extends AbstractPaymentsFlow {

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentProduct", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304001_Retrieve the Single Payment with Valid Consent Id"(String paymentProduct) {

        String consentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" + paymentProduct
        String payload = PaymentsInitiationPayloads.singlePaymentPayload

        //Payment Initiation
        doDefaultInitiation(consentPath, payload)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Get Consent
        doPaymentRetrieval(consentPath)
        Assert.assertEquals(consentRetrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("debtorAccount"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("instructedAmount"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("creditorAccount"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("creditorName"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("remittanceInformationUnstructured"))
        Assert.assertNotNull(consentRetrievalResponse.header("X-Request-ID"))
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentProduct", dataProviderClass = PaymentsDataProviders.class)
    void "TC0404001_Retrieve the Bulk Payment with Valid Consent Id"(String paymentProduct) {

        String consentPath = PaymentsConstants.BULK_PAYMENTS_PATH + "/" + paymentProduct
        String payload = PaymentsInitiationPayloads.bulkPaymentPayload

        //Payment Initiation
        doDefaultInitiation(consentPath, payload)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Get Consent
        doPaymentRetrieval(consentPath)
        Assert.assertEquals(consentRetrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("batchBookingPreferred"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("requestedExecutionDate"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("debtorAccount"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("payments.instructedAmount[0]"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("payments.creditorAccount[0]"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("payments.creditorName[0]"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("payments.remittanceInformationUnstructured[0]"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("payments.instructedAmount[1]"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("payments.creditorAccount[1]"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("payments.creditorName[1]"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("payments.remittanceInformationUnstructured[1]"))
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentProduct", dataProviderClass = PaymentsDataProviders.class)
    void "TC0504001_Retrieve the Periodic Payment Consent with Valid Consent Id"(String paymentProduct) {

        String consentPath = PaymentsConstants.PERIODIC_PAYMENTS_PATH + "/" + paymentProduct
        String payload = PaymentsInitiationPayloads.periodicPaymentPayload

        //Payment Initiation
        doDefaultInitiation(consentPath, payload)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Get Consent
        doPaymentRetrieval(consentPath)
        Assert.assertEquals(consentRetrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("debtorAccount"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("instructedAmount"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("creditorAccount"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("creditorName"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("startDate"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("executionRule"))
        Assert.assertNotNull(consentRetrievalResponse.jsonPath().getJsonObject("frequency"))
    }


    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304002_Retrieve Payment Consent With Empty Consent Id"(String consentPath, List<String> paymentProducts,
                                                               String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent
            def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                    .get("${paymentConsentPath}/")
            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304003_Get Payment Without Consent Id Parameter"(String consentPath, List<String> paymentProducts,
                                                                      String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent
            def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                    .get("${paymentConsentPath}")
            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypesForCancellation", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304004_Retrieve Already Terminated Payment Consent"(String consentPath, List<String> paymentProducts,
                                                            String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)

            //Submit the payment
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Delete Consent
            doConsentDelete(paymentConsentPath)
            Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_202)

            // Create cancellation auth resource
            createExplicitCancellation(paymentConsentPath)
            Assert.assertEquals(authorisationResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)

            //Authorise Payment cancellation
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get payment status
            doPaymentRetrieval(paymentConsentPath)
            // Not asserting the transaction status since it is for the bank to decide
            Assert.assertEquals(consentRetrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304005_Retrieve an Authorized Payment"(String consentPath, List<String> paymentProducts,
                                                       String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)

            //Authorize Consent
            doAuthorizationFlow()
            generateUserAccessToken()

            // Retrieve payment status
            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
        }
    }
}
