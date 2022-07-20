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
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Periodic Payment Initiation Request Payload Validation Tests
 */
class PeriodicPaymentInitiationRequestPayloadValidationTests extends AbstractPaymentsFlow {

    String periodicPaymentConsentPath = PaymentsConstants.PERIODIC_PAYMENTS_PATH + "/" +
            PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501019_Initiation Request with only mandatory Data Elements in the payload"() {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadWithOnlyMandatoryElements

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501020_Initiation Request without startDate in the payload"() {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadWithoutStartDate

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Start date is missing in periodic payments payload")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501021_Initiation Request without frequency in the payload"() {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadWithoutFrequency

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Frequency is missing in periodic payments payload")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501022_Initiation Request with past date as the startDate"() {

        LocalDate paymentDate = OffsetDateTime.now().toLocalDate().minusDays(5)

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(paymentDate,
                PaymentsConstants.executionRuleFollowing, PaymentsConstants.frequency,
                PaymentsInitiationPayloads.paymentEndDate, PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Start date must be a future date")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501023_Initiation Request with today date as the startDate"() {

        LocalDate paymentDate = OffsetDateTime.now().toLocalDate()

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(paymentDate,
                PaymentsConstants.executionRuleFollowing, PaymentsConstants.frequency,
                PaymentsInitiationPayloads.paymentEndDate, PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Start date must be a future date")
    }

    @Test (groups = ["1.3.3", "1.3.6"], dataProvider = "PaymentFrequency", dataProviderClass = PaymentsDataProviders.class)
    void "TC0501024_Initiation Request with supported frequency codes"(String frequency) {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                PaymentsConstants.executionRuleFollowing, frequency, PaymentsInitiationPayloads.paymentEndDate,
                PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501032_Initiation Request with unsupported frequency code"() {

        String frequencyCode = "Biweekly"

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                PaymentsConstants.executionRuleFollowing, frequencyCode, PaymentsInitiationPayloads.paymentEndDate,
                PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Unsupported frequency in periodic payments payload")
    }

    @Test (groups = ["1.3.3"], dataProvider = "ExecutionRule", dataProviderClass = PaymentsDataProviders.class)
    void "TC0501033_Initiation Request with supported executionRules"(String executionRule) {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                executionRule, PaymentsConstants.frequency, PaymentsInitiationPayloads.paymentEndDate,
                PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

    //TODO: Git issue: https://github.com/wso2-enterprise/financial-open-banking/issues/4717
    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501035_Initiation Request with unsupported executionRule"() {

        String executionRule = "Next"

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                executionRule, PaymentsConstants.frequency, PaymentsInitiationPayloads.paymentEndDate,
                PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Execution rule should be either \"following\" or \"preceding\"")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501036_Initiation Request with past date as the endDate"() {

        LocalDate paymentEndDate = OffsetDateTime.now().toLocalDate().minusDays(1)

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                PaymentsConstants.executionRuleFollowing, PaymentsConstants.frequency, paymentEndDate,
                PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "End date must be a future date")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501037_Initiation Request by passing same date for start and end dates"() {

        LocalDate paymentEndDate = PaymentsInitiationPayloads.paymentDate

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                PaymentsConstants.executionRuleFollowing, PaymentsConstants.frequency, paymentEndDate,
                PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "End date must be greater than start date")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501038_Initiation Request by passing an endDate previous to the startDate"() {

        LocalDate paymentEndDate = OffsetDateTime.now().toLocalDate().plusDays(2)

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                PaymentsConstants.executionRuleFollowing, PaymentsConstants.frequency, paymentEndDate,
                PaymentsConstants.dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "End date must be greater than start date")
    }

//    TODO: Uncomment the method after fixing the issue: https://github.com/wso2-enterprise/financial-open-banking/issues/4719
    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501039_Initiation Request by passing 00 for dayOfExecution value"() {

        String dayOfExecution = "00"

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                PaymentsConstants.executionRuleFollowing, PaymentsConstants.frequency,
                PaymentsInitiationPayloads.paymentEndDate, dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501040_Initiation Request by passing 31 for dayOfExecution value"() {

        String dayOfExecution = "31"

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                PaymentsConstants.executionRuleFollowing, PaymentsConstants.frequency,
                PaymentsInitiationPayloads.paymentEndDate, dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }

//    TODO: Uncomment the method after fixing the issue: https://github.com/wso2-enterprise/financial-open-banking/issues/4719
    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501041_Initiation Request by passing 32 for dayOfExecution value"() {

        String dayOfExecution = "32"

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadBuilder(PaymentsInitiationPayloads.paymentDate,
                PaymentsConstants.executionRuleFollowing, PaymentsConstants.frequency,
                PaymentsInitiationPayloads.paymentEndDate, dayOfExecution)

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501042_Initiation Request without InstructedAmount in the payload"() {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadWithoutInstructedAmount

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
//      TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501043_Initiation Request without DebtorAccount in the payload"() {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadWithoutDebtorAccount

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
//      TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501044_Initiation Request without CreditorName in the payload"() {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadWithoutCreditorName

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
//      TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0501045_Initiation Request without CreditorAccount in the payload"() {

        String payload = PaymentsInitiationPayloads.periodicPaymentPayloadWithoutCreditorAccount

        //Make Payment Initiation Request
        doDefaultInitiation(periodicPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
//      TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }
}
