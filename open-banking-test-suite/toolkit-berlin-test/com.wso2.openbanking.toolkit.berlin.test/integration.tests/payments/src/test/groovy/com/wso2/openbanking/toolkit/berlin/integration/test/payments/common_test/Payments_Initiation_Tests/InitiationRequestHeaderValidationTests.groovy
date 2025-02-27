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
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
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
 * Payment Initiation Request Header Validation Tests
 */
class InitiationRequestHeaderValidationTests extends AbstractPaymentsFlow{

    def config = ConfigParser.getInstance()
    AppConfigReader appConfigReader = new AppConfigReader()

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsPayloads", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301004_Initiation Request without payment product attribute"(String consentPath, String payload) {

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_403)
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsPayloads", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301005_Initiation Request with unsupported payment product attribute"(String consentPath, String payload) {

        //Make Payment Initiation Request
        def consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(payload)
                .post(consentPath + "/credit-payments")

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_404)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.PRODUCT_UNKNOWN)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, "tppMessages[0].text"),
                "Instance value (\"credit-payments\") not found in enum (possible values: [\"sepa-credit-transfers\",\"" +
                        "instant-sepa-credit-transfers\",\"target-2-payments\",\"cross-border-credit-transfers\",\"" +
                        "pain.001-sepa-credit-transfers\",\"pain.001-instant-sepa-credit-transfers\",\"" +
                        "pain.001-target-2-payments\",\"pain.001-cross-border-credit-transfers\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301006_Initiation Request without Content-Type Header"(String consentPath, List<String> paymentProducts,
                                                                    String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            //Note: Return 401 due to limitation in test framework. It check content type when creating signature
            // header.
            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301007_Initiation Request with invalid Content-Type Header"(String consentPath, List<String> paymentProducts,
                                                                         String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.HTML)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            //Note: Return 401 due to limitation in test framework. It check content type when creating signature
            // header.
            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301008_Initiation Request without X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                    String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                    "X-Request-ID header is missing in the request")
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301009_Initiation Request with invalid X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                         String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "1234")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                    "Input string \"1234\" is not a valid UUID")
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301011_Initiation Request without PSU-IP-Address Header"(String consentPath, List<String> paymentProducts,
                                                                      String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Header parameter 'psu-ip-address' is required on " +
                            "path '/{payment-service}/{payment-product}' but not found in request.")
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301012_Initiation Request with invalid PSU-IP-Address Header"(String consentPath, List<String> paymentProducts,
                                                                           String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, "174327080")
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                    "String \"174327080\" is not a valid IPv4 address")
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301013_Initiation Request with without PSU-ID Header"(String consentPath, List<String> paymentProducts,
                                                                   String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
            Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                    PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
            Assert.assertNotNull(consentResponse.jsonPath().get("paymentId"))
            Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
            Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301014_Initiation Request without Authorization Header"(String consentPath, List<String> paymentProducts,
                                                                     String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertTrue (TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).
                    contains ("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization"))
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301015_Initiation Request with invalid Authorization Header"(String consentPath, List<String> paymentProducts,
                                                                          String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                    BerlinConstants.TOKEN_INVALID)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                    "Token is not valid")
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301017_Initiation Request with Authorization Code Grant Access Token"(String consentPath,
                                                                                   List<String> paymentProducts,
                                                                                   String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertNotNull(paymentId)

            //Authorize the consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Generate User Access token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                    BerlinConstants.TOKEN_INVALID)
            Assert.assertTrue (TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).
                    contains ("Incorrect Access Token Type provided"))
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301018_Initiation Request with access token not bound to the payment scope"(String consentPath,
                                                                                         List<String> paymentProducts,
                                                                                         String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            def applicationAccessToken = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT,
                    BerlinConstants.SCOPES.ACCOUNTS)

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_403)
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "SinglePayments", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301019_Initiation Request with TPP-Brand-LoggingInformation header"(String consentPath, List<String> paymentProducts,
                                                                                 String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                    .header(BerlinConstants.PSU_TYPE, "email")
                    .filter(new BerlinSignatureFilter())
                    .body(payload)
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0301010_Initiation Request with duplicate X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                           String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            doDefaultInitiation(paymentConsentPath, payload)
            def xRequestId = consentResponse.getHeader(BerlinConstants.X_REQUEST_ID).toString()

            //Make Payment Initiation Request
            def secondConsentResponse = TestSuite.buildRequest()
              .contentType(ContentType.JSON)
              .header(BerlinConstants.X_REQUEST_ID, xRequestId)
              .header(BerlinConstants.Date, getCurrentDate())
              .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
              .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
              .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
              .header(BerlinConstants.PSU_TYPE, "email")
              .filter(new BerlinSignatureFilter())
              .body(payload)
              .baseUri(ConfigParser.getInstance().getBaseURL())
              .post(paymentConsentPath)

            Assert.assertEquals(secondConsentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
            Assert.assertEquals(paymentId, TestUtil.parseResponseBody(secondConsentResponse, "paymentId").toString())
            Assert.assertEquals(xRequestId, secondConsentResponse.getHeader(BerlinConstants.X_REQUEST_ID).toString())
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "OB-218_Initiation Request with same X-Request-ID and different payload"(String consentPath,
                                                                          List<String>paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            doDefaultInitiation(paymentConsentPath, payload)
            def xRequestId = consentResponse.getHeader(BerlinConstants.X_REQUEST_ID).toString()

            def payload2 = PaymentsInitiationPayloads.singlePaymentPayloadWithOptionalData
            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
              .contentType(ContentType.JSON)
              .header(BerlinConstants.X_REQUEST_ID, xRequestId)
              .header(BerlinConstants.Date, getCurrentDate())
              .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
              .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
              .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
              .header(BerlinConstants.PSU_TYPE, "email")
              .filter(new BerlinSignatureFilter())
              .body(payload2)
              .baseUri(ConfigParser.getInstance().getBaseURL())
              .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
              BerlinConstants.FORMAT_ERROR)
            Assert.assertTrue (TestUtil.parseResponseBody (consentResponse, BerlinConstants.TPPMESSAGE_TEXT).
              contains ("Payloads are not similar. Hence this is not a valid idempotent request"))
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "OB-217_Initiation Request with different X-Request-ID and same payload"(String consentPath,
                                                        List<String>paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            doDefaultInitiation(paymentConsentPath, payload)

            //Make Payment Initiation Request
            def consentResponse = TestSuite.buildRequest()
              .contentType(ContentType.JSON)
              .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
              .header(BerlinConstants.Date, getCurrentDate())
              .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
              .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
              .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
              .header(BerlinConstants.PSU_TYPE, "email")
              .filter(new BerlinSignatureFilter())
              .body(payload)
              .baseUri(ConfigParser.getInstance().getBaseURL())
              .post(paymentConsentPath)

            Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
            Assert.assertNotEquals(paymentId, TestUtil.parseResponseBody(consentResponse, "paymentId").toString())
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "BG-547_Payment Initiation Request with same x-request-id given in accounts request"() {

        String paymentConsentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS
        def xRequestId = UUID.randomUUID().toString()

        //Account Consent Initiation Request
        def applicationAccessToken = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT,
                    BerlinConstants.SCOPES.ACCOUNTS)

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(PaymentsInitiationPayloads.accountsInitiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(PaymentsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Make Accounts Initiation Request
        def applicationAccessToken2 = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT,
                BerlinConstants.SCOPES.PAYMENTS)

        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken2}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(PaymentsInitiationPayloads.singlePaymentPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(paymentConsentPath)

        Assert.assertEquals(consentResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse2, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse2, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Payloads are not similar. Hence this is not a valid idempotent request"))
    }
}
