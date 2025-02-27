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
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Payment Direct Cancellation Header Validation Tests
 */
class DeletePaymentRequestHeaderValidationTests extends AbstractPaymentsFlow {

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303006_Delete Payment With Client Credentials Type Access Token"(String consentPath,
                                                                              List<String>paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            //Delete Consent By Passing User Access Token
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE),
                    BerlinConstants.TOKEN_INVALID)
            Assert.assertTrue (TestUtil.parseResponseBody (consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).
                    contains ("Incorrect Access Token Type provided"))
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303008_Delete Payment Without X-Request-ID Header"(String consentPath,
                                                                 List<String>paymentProducts, String payload) {

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

            //Delete Consent without X-Request-ID header
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertTrue(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("X-Request-ID header is missing in the request"))
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303009_Delete Payment With Invalid X-Request-ID Header"(String consentPath, List<String>paymentProducts,
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

            //Delete Consent with invalid X-Request-ID header
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "1234")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)

            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Invalid X-Request-ID header. Needs to be in UUID format")
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303010_Delete Payment With Empty X-Request-ID Header"(String consentPath, List<String>paymentProducts,
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

            //Delete Consent with invalid X-Request-ID header
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "X-Request-ID header is missing in the request")
        }
    }

    //todo: fix https://github.com/wso2-enterprise/financial-open-banking/issues/7561
//    @Test(groups = ["1.3.3", "1.3.6"],
//            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303011_Delete Payment Without Specifying Authorization Header"(String consentPath,
                                                                            List<String>paymentProducts, String payload) {

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

            //Delete Consent
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Invalid Credentials. Make sure your API invocation call has a header: 'Authorization : " +
                            "Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or 'apikey: API_KEY'")
        }
    }

    //todo: fix https://github.com/wso2-enterprise/financial-open-banking/issues/7561
//    @Test(groups = ["1.3.3", "1.3.6"],
//            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303012_Delete Payment With Invalid Authorization Header value"(String consentPath,
                                                                            List<String>paymentProducts, String payload) {

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

            //Delete Consent
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.TOKEN_INVALID)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Token is not valid")
        }
    }

    //todo: fix https://github.com/wso2-enterprise/financial-open-banking/issues/7561
//    @Test(groups = ["1.3.3", "1.3.6"],
//            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303013_Delete Payment Consent With Empty Authorization Header value"(String consentPath,
                                                                                  List<String>paymentProducts, String payload) {

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

            //Delete Consent
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Invalid Credentials. Make sure your API invocation call has a header: 'Authorization : " +
                            "Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or 'apikey: API_KEY'")
        }
    }

    //todo: fix issue https://github.com/wso2-enterprise/financial-open-banking/issues/7865
//    @Test(groups = ["1.3.3", "1.3.6"],
//      dataProvider = "PaymentsTypesForCancellation", dataProviderClass = PaymentsDataProviders.class)
    void "OB-1681_Payment delete request with same X-Request-Id and same consent"(String consentPath,
                                                              List<String>paymentProducts, String payload) {

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

            def xRequestId = UUID.randomUUID().toString()

            //Delete Consent
            def consentDeleteResponse = TestSuite.buildRequest()
              .contentType(ContentType.JSON)
              .header(BerlinConstants.X_REQUEST_ID, xRequestId)
              .header(BerlinConstants.Date, getCurrentDate())
              .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
              .filter(new BerlinSignatureFilter())
              .baseUri(ConfigParser.getInstance().getBaseURL())
              .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_202)

            //Get Consent with same X-Request-ID header
            def consentDeleteResponse2 = TestSuite.buildRequest()
              .contentType(ContentType.JSON)
              .header(BerlinConstants.X_REQUEST_ID, xRequestId)
              .header(BerlinConstants.Date, getCurrentDate())
              .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
              .filter(new BerlinSignatureFilter())
              .baseUri(ConfigParser.getInstance().getBaseURL())
              .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
      dataProvider = "PaymentsTypesForCancellation", dataProviderClass = PaymentsDataProviders.class)
    void "OB-1683_Payment delete request with the same X-Request-Id with different Consent"(String consentPath,
                List<String> paymentProducts, String payload) {

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

                def xRequestId = UUID.randomUUID().toString()

                //Delete Consent
                def retrievalResponse = TestSuite.buildRequest()
                  .contentType(ContentType.JSON)
                  .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                  .header(BerlinConstants.Date, getCurrentDate())
                  .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                  .filter(new BerlinSignatureFilter())
                  .baseUri(ConfigParser.getInstance().getBaseURL())
                  .delete("${paymentConsentPath}/${paymentId}")

                Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_202)

                //Payment Initiation
                doDefaultInitiation(paymentConsentPath, payload)
                Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

                //Authorize the Consent
                doAuthorizationFlow()
                Assert.assertNotNull(code)

                //Get User Access Token
                generateUserAccessToken()
                Assert.assertNotNull(userAccessToken)

                //Delete Consent
                def retrievalResponse2 = TestSuite.buildRequest()
                  .contentType(ContentType.JSON)
                  .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                  .header(BerlinConstants.Date, getCurrentDate())
                  .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                  .filter(new BerlinSignatureFilter())
                  .baseUri(ConfigParser.getInstance().getBaseURL())
                  .delete("${paymentConsentPath}/${paymentId}")

                Assert.assertEquals(retrievalResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_202)
            }
        }
}
