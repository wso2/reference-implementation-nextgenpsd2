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
 * Single Payment Initiation Request Payload Validation Tests
 */
class SinglePaymentInitiationRequestPayloadValidationTests extends AbstractPaymentsFlow {

    String singlePaymentConsentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
            PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301019_Initiation Request without debtorAccount in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithoutDebtorAccount

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301020_Initiation Request with empty debtorAccount iban in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, "", PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301021_Initiation Request with invalid debtorAccount iban in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, "12345", PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301022_Initiation Request without instructedAmount element in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithoutInstructedAmount

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301023_Initiation Request without currency attribute of instructedAmount element in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithoutInstructedAmount_Currency

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301024_Initiation Request without amount attribute of instructedAmount element in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithoutInstructedAmount_Amount

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301025_Initiation Request with unsupported currency type in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder("Euro",
                PaymentsConstants.instructedAmount, PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301026_Initiation Request with unsupported amount format in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                "1,000", PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301027_Initiation Request with amount value in integer format"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                "1234", PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("paymentId"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301028_Initiation Request with amount value with one decimal place"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                "123.0", PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("paymentId"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301030_Initiation Request with negative amount value in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                "-100.00", PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("paymentId"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }
    
    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301031_Initiation Request with 14 digit amount value in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                "10000000000000.50", PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("paymentId"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301032_Initiation Request with more than 14 digit amount value in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                "1000000000001234.50", PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301033_Initiation Request without creditorAccount in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithoutCreditorAccount

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301034_Initiation Request with empty creditorAccount iban in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                "")

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301035_Initiation Request with invalid creditorAccount iban in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                "12345")

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301036_Initiation Request without creditorName in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithoutCreditorName

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    //TODO: Uncomment the method after fixing the issue: https://github.com/wso2-enterprise/financial-open-banking/issues/4813
//    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301037_Initiation Request with empty creditorName in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, PaymentsConstants.debtorAccount1, "",
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "CreditorName", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301038_Verify creditorName in Initiation Request payload"(String creditorName) {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, PaymentsConstants.debtorAccount1, creditorName,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("paymentId"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301041_Initiation Request with mandatory and optional data elements in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithOptionalData

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("paymentId"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301042_Initiation Request with unspecified additional data elements in the payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithUnspecifiedData

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("paymentId"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301043_Single Payment Initiation Request with Bulk Payment Payload"() {

        String payload = PaymentsInitiationPayloads.bulkPaymentPayload

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
    }
}
