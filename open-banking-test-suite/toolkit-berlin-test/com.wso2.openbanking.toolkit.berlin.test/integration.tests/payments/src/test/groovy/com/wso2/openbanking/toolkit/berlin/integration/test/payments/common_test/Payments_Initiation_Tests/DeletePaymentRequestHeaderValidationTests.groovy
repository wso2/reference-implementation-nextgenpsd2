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
 * Payment Direct Cancellation Header Validation Tests
 */
class DeletePaymentRequestHeaderValidationTests extends AbstractPaymentsFlow {

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303006_Delete Payment Consent With Authorization Code Type Access Token"(String consentPath, List<String>
            paymentProducts, String payload) {

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

            //Delete Consent By Passing User Access Token
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
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
    void "TC0303008_Delete Payment Consent Without X-Request-ID Header"(String consentPath, List<String>
            paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Delete Consent without X-Request-ID header
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
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
    void "TC0303009_Delete Payment Consent With Invalid X-Request-ID Header"(String consentPath, List<String>
            paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Delete Consent with invalid X-Request-ID header
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "1234")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                    .filter(new BerlinSignatureFilter())
                    .baseUri(ConfigParser.getInstance().getBaseURL())
                    .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.FORMAT_ERROR)

            Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                    "Input string \"1234\" is not a valid UUID")
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303010_Delete Payment Consent With Empty X-Request-ID Header"(String consentPath, List<String>
            paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

            //Delete Consent with invalid X-Request-ID header
            def consentDeleteResponse = TestSuite.buildRequest()
                    .contentType(ContentType.JSON)
                    .header(BerlinConstants.X_REQUEST_ID, "")
                    .header(BerlinConstants.Date, getCurrentDate())
                    .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
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

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303011_Delete Payment Consent Without Specifying Authorization Header"(String consentPath, List<String>
            paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

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

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303012_Delete Payment Consent With Invalid Authorization Header value"(String consentPath, List<String>
            paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

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

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0303013_Delete Payment Consent With Empty Authorization Header value"(String consentPath, List<String>
            paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            doStatusRetrieval(paymentConsentPath)
            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

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

    @Test(groups = ["1.3.3", "1.3.6"],
      dataProvider = "PaymentsTypesForCancellation", dataProviderClass = PaymentsDataProviders.class)
    void "OB-1681_Payment consent delete request with same X-Request-Id and same consent"(String consentPath, List<String>
      paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Payment Initiation
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

            def xRequestId = UUID.randomUUID().toString()

            //Delete Consent
            def consentDeleteResponse = TestSuite.buildRequest()
              .contentType(ContentType.JSON)
              .header(BerlinConstants.X_REQUEST_ID, xRequestId)
              .header(BerlinConstants.Date, getCurrentDate())
              .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
              .filter(new BerlinSignatureFilter())
              .baseUri(ConfigParser.getInstance().getBaseURL())
              .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)

            //Get Consent with same X-Request-ID header
            def consentDeleteResponse2 = TestSuite.buildRequest()
              .contentType(ContentType.JSON)
              .header(BerlinConstants.X_REQUEST_ID, xRequestId)
              .header(BerlinConstants.Date, getCurrentDate())
              .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
              .filter(new BerlinSignatureFilter())
              .baseUri(ConfigParser.getInstance().getBaseURL())
              .delete("${paymentConsentPath}/${paymentId}")

            Assert.assertEquals(consentDeleteResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        }
    }

        @Test(groups = ["1.3.3", "1.3.6"],
          dataProvider = "PaymentsTypesForCancellation", dataProviderClass = PaymentsDataProviders.class)
    void "OB-1683_Payment consent retrieval request with the same X-Request-Id with different Consent"(String consentPath,
                List<String> paymentProducts, String payload) {

            paymentProducts.each { value ->
                String paymentConsentPath = consentPath + "/" + value

                //Payment Initiation
                doDefaultInitiation(paymentConsentPath, payload)
                Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

                def xRequestId = UUID.randomUUID().toString()

                //Delete Consent
                def retrievalResponse = TestSuite.buildRequest()
                  .contentType(ContentType.JSON)
                  .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                  .header(BerlinConstants.Date, getCurrentDate())
                  .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                  .filter(new BerlinSignatureFilter())
                  .baseUri(ConfigParser.getInstance().getBaseURL())
                  .delete("${paymentConsentPath}/${paymentId}")

                Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)

                //Payment Initiation
                doDefaultInitiation(paymentConsentPath, payload)
                Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

                //Delete Consent
                def retrievalResponse2 = TestSuite.buildRequest()
                  .contentType(ContentType.JSON)
                  .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                  .header(BerlinConstants.Date, getCurrentDate())
                  .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                  .filter(new BerlinSignatureFilter())
                  .baseUri(ConfigParser.getInstance().getBaseURL())
                  .delete("${paymentConsentPath}/${paymentId}")

                Assert.assertEquals(retrievalResponse2.statusCode(), BerlinConstants.STATUS_CODE_400)
                Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse2, BerlinConstants.TPPMESSAGE_CODE),
                  BerlinConstants.FORMAT_ERROR)
                Assert.assertTrue (TestUtil.parseResponseBody (retrievalResponse2, BerlinConstants.TPPMESSAGE_TEXT).
                  contains ("Idempotency check failed."))
            }
        }
}
