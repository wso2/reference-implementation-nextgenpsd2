/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.common_test.Payments_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import io.restassured.http.ContentType
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "Object has missing required properties ([\"debtorAccount\"])"))
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "ECMA 262 regex \"[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}\" does not match input string"))
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
         "ECMA 262 regex \"[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}\" does not match input string \"12345\""))
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "Object has missing required properties ([\"instructedAmount\"])"))
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "Object has missing required properties ([\"currency\"])"))
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "Object has missing required properties ([\"amount\"])"))
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
          "ECMA 262 regex \"[A-Z]{3}\" does not match input string \"Euro\""))
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
                "1000000000001234567.50", PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "Object has missing required properties ([\"creditorAccount\"])"))
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
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "Object has missing required properties ([\"creditorName\"])"))
    }

    //TODO: Uncomment the method after fixing the issue: https://github.com/wso2-enterprise/financial-open-banking/issues/4813
    @Test (groups = ["1.3.3", "1.3.6"])
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

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1588_Payment initiation request with mismatching currency types in payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadWithMismatchingCurrency

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "OB-1679_Initiation request with same x-request-id and same payload"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayload
        def xRequestId = UUID.randomUUID().toString()

        //Make Payment Initiation Request - 1st time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        def paymentId1 = TestUtil.parseResponseBody(consentResponse, "paymentId")
        def consentStatus1 = TestUtil.parseResponseBody(consentResponse, "transactionStatus")

        //Make Payment Initiation Request - 2st time
        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        def paymentId2 = TestUtil.parseResponseBody(consentResponse2, "paymentId")
        def consentStatus2 = TestUtil.parseResponseBody(consentResponse2, "transactionStatus")

        Assert.assertEquals(paymentId1, paymentId2)
        Assert.assertEquals(consentStatus1, consentStatus2)
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "OB-1680_Initiation request with same x-request-id and different payload"() {

        def xRequestId = UUID.randomUUID().toString()

        //Make Payment Initiation Request - 1st time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(PaymentsInitiationPayloads.singlePaymentPayload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)

        String payload = PaymentsInitiationPayloads.singlePaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency2,
                PaymentsConstants.instructedAmount, PaymentsConstants.debtorAccount1, PaymentsConstants.creditorName1,
                PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request - 2st time
        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse2, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse2, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Payloads are not similar. Hence this is not a valid idempotent request"))
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "OB-1684_Initiation request with same X-Request-Id and different access token"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayload
        def xRequestId = UUID.randomUUID().toString()

        //Make Payment Initiation Request - 1st time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        def paymentId1 = TestUtil.parseResponseBody(consentResponse, "paymentId")
        def consentStatus1 = TestUtil.parseResponseBody(consentResponse, "transactionStatus")

        //Create new Application Access Token
        def applicationAccessToken2 = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD
                .PRIVATE_KEY_JWT, scopes)

        //Make Payment Initiation Request - 2st time
        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken2}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(payload)
                .post(singlePaymentConsentPath)

        Assert.assertEquals(consentResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_201)
    }
}
