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
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Get Payment Consent Request Header Validation Tests
 */
class GetPaymentConsentRequestHeaderValidationTests extends AbstractPaymentsFlow {

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304007_Get Payment Consent With Authorization Code Type Access Token"(String consentPath,
                                                                                   List<String> paymentProducts,
                                                                                   String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Authorize the Consent
            doAuthorizationFlow()
            Assert.assertNotNull(code)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)

            //Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            //Get Consent By Passing User Access Token
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)

            switch (BerlinTestUtil.solutionVersion) {
                case [TestConstants.SOLUTION_VERSION_130, TestConstants.SOLUTION_VERSION_140, TestConstants.SOLUTION_VERSION_150]:
                    Assert.assertTrue(retrievalResponse.getHeader("WWW-Authenticate").contains("error=\"invalid token\""))
                    break

                default:
                    Assert.assertTrue (retrievalResponse.getBody().xmlPath().getString("ams:description").
                            contains ("Incorrect Access Token Type is provided"))
                    break
            }
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304009_Get Payment Consent  Without X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                      String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Get Consent without X-Request-ID header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)

            switch (BerlinTestUtil.solutionVersion) {
                case [TestConstants.SOLUTION_VERSION_130, TestConstants.SOLUTION_VERSION_140]:
                    Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT),
                            "Parameter 'X-Request-ID' is required but is missing.")
                    break
                default:
                    Assert.assertTrue(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString().
                            contains("Header parameter 'X-Request-ID' is required on path"))
                    break
            }
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304010_Get Consent With Invalid X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                  String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Get Consent with invalid X-Request-ID header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "1234")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)

            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Input string \"1234\" is not a valid UUID")
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304011_Get Consent With Empty X-Request-ID Header"(String consentPath, List<String> paymentProducts,
                                                                String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Get Consent with invalid X-Request-ID header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Parameter 'X-Request-ID' is required but is missing.")
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304012_Get Consent Without Specifying Authorization Header"(String consentPath, List<String> paymentProducts,
                                                                         String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Get Consent with invalid X-Request-ID header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .filter(new BerlinSignatureFilter())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)

            switch (BerlinTestUtil.solutionVersion) {
                case [TestConstants.SOLUTION_VERSION_130, TestConstants.SOLUTION_VERSION_140, TestConstants.SOLUTION_VERSION_150]:
                    Assert.assertTrue(retrievalResponse.getHeader("WWW-Authenticate").contains("error=\"invalid token\""))
                    break

                default:
                    Assert.assertTrue (retrievalResponse.getBody().xmlPath().getString("ams:description").
                            contains ("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization"))
                    break
            }
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304013_Get Consent With Invalid Authorization Header value"(String consentPath, List<String> paymentProducts,
                                                                         String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Get Consent with invalid X-Request-ID header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                    .filter(new BerlinSignatureFilter())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)

            switch (BerlinTestUtil.solutionVersion) {
                case [TestConstants.SOLUTION_VERSION_130, TestConstants.SOLUTION_VERSION_140, TestConstants.SOLUTION_VERSION_150]:
                    Assert.assertTrue(retrievalResponse.getHeader("WWW-Authenticate").contains("error=\"invalid token\""))
                    break

                default:
                    Assert.assertTrue (retrievalResponse.getBody().xmlPath().getString("ams:description").
                            contains ("Invalid Credentials. Make sure you have provided the correct security credentials"))
                    break
            }
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0304014_Get Consent With Empty Authorization Header value"(String consentPath, List<String> paymentProducts,
                                                                       String payload) {
        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Get Consent with invalid X-Request-ID header
            def retrievalResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ")
                    .filter(new BerlinSignatureFilter())
                    .get("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)

            switch (BerlinTestUtil.solutionVersion) {
                case [TestConstants.SOLUTION_VERSION_130, TestConstants.SOLUTION_VERSION_140, TestConstants.SOLUTION_VERSION_150]:
                    Assert.assertTrue(retrievalResponse.getHeader("WWW-Authenticate").contains("error=\"invalid token\""))
                    break

                default:
                    Assert.assertTrue (retrievalResponse.getBody().xmlPath().getString("ams:description").
                            contains ("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization"))
                    break
            }
        }
    }
}
