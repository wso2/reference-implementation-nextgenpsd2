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
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Future Dated Payment Initiation Request Validation Tests
 */
class FutureDatedPaymentInitiationRequestValidationTests extends AbstractPaymentsFlow {

    String singlePaymentConsentPath = PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
            PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301044_Future Dated Payment Initiation Request valid inputs"() {

        String payload = PaymentsInitiationPayloads.futureDatedPaymentPayload

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
    void "TC0301045_Future Dated Payment Initiation by passing past date for requestedExecutionDate"() {

        LocalDate requestedExecutionDate = OffsetDateTime.now().toLocalDate().minusDays(5)

        String payload = PaymentsInitiationPayloads.futureDatedPaymentPayloadBuilder(requestedExecutionDate)

        //Make Payment Initiation Request
        doDefaultInitiation(singlePaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.EXECUTION_DATE_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "The execution date must be a future date")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0301046_Future Dated Payment Initiation by passing today date for requestedExecutionDate"() {

        LocalDate requestedExecutionDate = OffsetDateTime.now().toLocalDate()

        String payload = PaymentsInitiationPayloads.futureDatedPaymentPayloadBuilder(requestedExecutionDate)

        //Make Payment Initiation Request
        doDefaultInitiation(singlePaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.EXECUTION_DATE_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "The execution date must be a future date")
    }
}
