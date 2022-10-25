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
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
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
 * Get Payment Consent Request Header Validation Tests
 */
class GetPaymentConsentRequestHeaderValidationTests extends AbstractPaymentsFlow {

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304007_Retrieve Payment With Application Access Token"(String consentPath,
                                                                                   List<String> paymentProducts,
                                                                                   String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            //Get Consent By Passing application Access Token
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.TOKEN_INVALID)
            Assert.assertTrue(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("Incorrect Access Token Type provided"))
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304009_Retrieve Payment Without X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                      String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent By Passing application Access Token
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertTrue(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("X-Request-ID header is missing in the request"))
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304010_Retrieve Payment With Invalid X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                  String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent By Passing application Access Token
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "1234")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)

            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Invalid X-Request-ID header. Needs to be in UUID format")
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304011_Retrieve Payment With Empty X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent with empty X-Request-ID header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "X-Request-ID header is missing in the request")
        }
    }

    //todo: fix https://github.com/wso2-enterprise/financial-open-banking/issues/7561
//    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304012_Retrieve Payment Without Specifying Authorization Header"(String consentPath, List<String> paymentProducts,
                                                                         String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent without Authorization Header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Invalid Credentials. Make sure your API invocation call has a header: 'Authorization : " +
                            "Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or 'apikey: API_KEY'")
        }
    }

    //todo: fix https://github.com/wso2-enterprise/financial-open-banking/issues/7561
//    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304013_Retrieve Payment With Invalid Authorization Header value"(String consentPath, List<String> paymentProducts,
                                                                         String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent with invalid Authorization Header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.TOKEN_INVALID)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Token is not valid")
        }
    }

    //todo: fix https://github.com/wso2-enterprise/financial-open-banking/issues/7561
//    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304014_Retrieve Payment With Empty Authorization Header value"(String consentPath, List<String> paymentProducts,
                                                                       String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent with Empty Authorization Header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Invalid Credentials. Make sure your API invocation call has a header: 'Authorization : Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or 'apikey: API_KEY'")
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1681_Payment retrieval request with same X-Request-Id and same consent"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayload
        String singlePaymentConsentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS
        def xRequestId = UUID.randomUUID().toString()

        //Payment Initiation
        doDefaultInitiation(singlePaymentConsentPath, payload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Get Consent
        def retrievalResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get("${singlePaymentConsentPath}/${paymentId}")

        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)

        //Get Consent with same X-Request-ID header
        def retrievalResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get("${singlePaymentConsentPath}/${paymentId}")

        Assert.assertEquals(retrievalResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_200)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1682_Payment retrieval request with the same X-Request-Id used for Consent Initiation"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayload
        String singlePaymentConsentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

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
        paymentId = TestUtil.parseResponseBody(consentResponse, "paymentId")

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Get Consent with same X-Request-ID header used for Payment Initiation
        def retrievalResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get("${singlePaymentConsentPath}/${paymentId}")

        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1683_Payment retrieval request with the same X-Request-Id with different Consent"() {

        String payload = PaymentsInitiationPayloads.singlePaymentPayload
        String singlePaymentConsentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

        def xRequestId = UUID.randomUUID().toString()

        //Payment Initiation
        doDefaultInitiation(singlePaymentConsentPath, payload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Get payment
        def retrievalResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get("${singlePaymentConsentPath}/${paymentId}")

        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)

        //Send Payment Initiation Again
        doDefaultInitiation(singlePaymentConsentPath, payload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Get payment
        def retrievalResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .get("${singlePaymentConsentPath}/${paymentId}")

        Assert.assertEquals(retrievalResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_200)
    }
}
