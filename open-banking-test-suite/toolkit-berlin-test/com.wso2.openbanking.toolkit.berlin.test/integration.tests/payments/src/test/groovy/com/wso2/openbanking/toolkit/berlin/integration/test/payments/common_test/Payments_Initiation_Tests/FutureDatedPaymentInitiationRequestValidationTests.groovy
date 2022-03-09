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
