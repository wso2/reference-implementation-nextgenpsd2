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
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Payment Direct Cancellation Response Validation Tests
 */
class DeletePaymentResponseValidationTest extends AbstractPaymentsFlow {

    //todo: this cannot be tested because of the default mock backend implementation
    //Note: The auth_cancellation.enable attribute should set to false in deployment.toml file
//    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
//            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303001_Direct Payment Cancellation"(String consentPath, List<String> paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertNotNull(paymentId)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
            Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            // Delete Flow
            doConsentDelete(paymentConsentPath)
            Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303002_Delete Payment With Empty Payment Id"(String consentPath, List<String> paymentProducts,
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

            //Delete Payment
            def consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                    .delete("${paymentConsentPath}/")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
            Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303003_Delete Payment Without Payment Id Parameter"(String consentPath, List<String> paymentProducts,
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

            //Delete Consent
            def consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                    .delete("${paymentConsentPath}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
            Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypesForCancellation", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303004_Delete Already Deleted Payment"(String consentPath, List<String> paymentProducts,
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

            //Delete Consent
            doConsentDelete(paymentConsentPath)
            Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_202)
            Assert.assertNotNull(deleteResponse.getHeader("X-Request-ID"))

            // Create cancellation auth resource
            createExplicitCancellation(paymentConsentPath)
            Assert.assertEquals(authorisationResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
            Assert.assertNotNull(authorisationResponse.getHeader("X-Request-ID"))

            // Cancel the payment
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Delete Consent which is already deleted
            doConsentDelete(paymentConsentPath)
            Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_CODE),
              BerlinConstants.CONSENT_INVALID)
            Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "The requested consent is already deleted")
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypesForCancellation", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303014_Delete an Authorised Payment Consent"(String consentPath, List<String> paymentProducts,
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

            //Delete Consent
            doConsentDelete(paymentConsentPath)
            Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_202)
            Assert.assertNotNull(deleteResponse.getHeader("X-Request-ID"))

            // Create cancellation auth resource
            createExplicitCancellation(paymentConsentPath)
            Assert.assertEquals(authorisationResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
            Assert.assertNotNull(authorisationResponse.getHeader("X-Request-ID"))

            // Cancel the payment
            doAuthorizationFlow()
            Assert.assertNotNull(code)
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0403015_Bulk Payment Cancellation Request by passing a single payment consent"() {

        String singlePaymentConsentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

        String bulkPaymentConsentPath = PaymentsConstants.BULK_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

        //Single Payment Initiation
        doDefaultInitiation(singlePaymentConsentPath, PaymentsInitiationPayloads.singlePaymentPayload)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check Status
        doStatusRetrieval(singlePaymentConsentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)

        //Delete Consent
        doConsentDelete(bulkPaymentConsentPath)
        Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.CONSENT_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "The provided consent ID valid but belongs to a different consent type")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0503015_Periodic Payment Cancellation Request by passing a single payment Id"() {

        String singlePaymentConsentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

        String periodicPaymentConsentPath = PaymentsConstants.PERIODIC_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

        //Single Payment Initiation
        doDefaultInitiation(singlePaymentConsentPath, PaymentsInitiationPayloads.singlePaymentPayload)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check Status
        doStatusRetrieval(singlePaymentConsentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)

        //Delete Consent
        doConsentDelete(periodicPaymentConsentPath)
        Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.CONSENT_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "The provided consent ID valid but belongs to a different consent type")
    }

    @Test(groups = ["1.3.6"],
            dataProvider = "SinglePayments", dataProviderClass = PaymentsDataProviders.class)
    void "OB-1592_Send Single payment delete request"(String consentPath, List<String> paymentProducts, String
            payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertNotNull(paymentId)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            // Check consent received status
            doStatusRetrieval(paymentConsentPath)

            Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
            Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))

            // Delete Flow
            doConsentDelete(paymentConsentPath)
            Assert.assertEquals(deleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
            Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CANCELLATION_INVALID)
            Assert.assertEquals(TestUtil.parseResponseBody(deleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Cancellation not applicable for Single payments")
        }
    }
}
