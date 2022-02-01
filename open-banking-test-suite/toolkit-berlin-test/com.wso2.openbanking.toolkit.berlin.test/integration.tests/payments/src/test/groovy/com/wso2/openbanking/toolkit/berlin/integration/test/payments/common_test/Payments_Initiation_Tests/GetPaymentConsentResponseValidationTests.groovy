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
class GetPaymentConsentResponseValidationTests extends AbstractPaymentsFlow {

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentProduct", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304001_Get the Single Payment Consent with Valid Consent Id"(String paymentProduct) {

        String consentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" + paymentProduct
        String payload = PaymentsInitiationPayloads.singlePaymentPayload

        //Payment Initiation
        doDefaultInitiation(consentPath, payload)

        //Get Consent
        doConsentRetrieval(consentPath)
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
    void "TC0404001_Get the Bulk Payment Consent with Valid Consent Id"(String paymentProduct) {

        String consentPath = PaymentsConstants.BULK_PAYMENTS_PATH + "/" + paymentProduct
        String payload = PaymentsInitiationPayloads.bulkPaymentPayload

        //Payment Initiation
        doDefaultInitiation(consentPath, payload)

        //Get Consent
        doConsentRetrieval(consentPath)
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
    void "TC0504001_Get the Periodic Payment Consent with Valid Consent Id"(String paymentProduct) {

        String consentPath = PaymentsConstants.PERIODIC_PAYMENTS_PATH + "/" + paymentProduct
        String payload = PaymentsInitiationPayloads.periodicPaymentPayload

        //Payment Initiation
        doDefaultInitiation(consentPath, payload)

        //Get Consent
        doConsentRetrieval(consentPath)
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
    void "TC0304002_Get Payment Consent With Empty Consent Id"(String consentPath, List<String> paymentProducts,
                                                               String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)

            //Get Consent
            def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                    .get("${paymentConsentPath}/")
            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304003_Get Payment Consent Without Consent Id Parameter"(String consentPath, List<String> paymentProducts,
                                                                      String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)

            //Get Consent
            def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                    .get("${paymentConsentPath}")
            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        }
    }

    //Note: The <AuthorizeCancellation> tag should set to false in openbanking.xml
    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypesForCancellation", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304004_Get Already Terminated Payment Consent"(String consentPath, List<String> paymentProducts,
                                                            String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Delete Consent
            doConsentDelete(paymentConsentPath)
            Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)

            //Get Consent
            doConsentRetrieval(paymentConsentPath)
            Assert.assertEquals(consentRetrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_CANC)
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304005_Get an Authorized Payment Consent"(String consentPath, List<String> paymentProducts,
                                                       String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Delete Consent
            doAuthorizationFlow()
            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)

            //Get Consent
            doConsentRetrieval(paymentConsentPath)
            Assert.assertEquals(consentRetrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
        }
    }
}
