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
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test
import java.time.OffsetTime

/**
 * Bulk Payment Initiation Request Payload Validation Tests
 */
class BulkPaymentInitiationRequestPayloadValidationTests extends AbstractPaymentsFlow {

    String bulkPaymentConsentPath = PaymentsConstants.BULK_PAYMENTS_PATH + "/" +
            PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401019_Initiation Request with only mandatory Data Elements in the payload"() {

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithOnlyMandatoryElements

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

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
    void "TC0401020_Initiation Request without debtorAccount in the payload"() {

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithoutDebtorAccount

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401021_Initiation Request without payments in the payload"() {

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithoutPayments

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    /**
     * This testcase only supports if AccountReferenceType is configured as 'iban' in open-banking.xml.
     */
    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401022_Initiation Request with debtorAccount in iban attribute"() {
        String accountAttributes = PaymentsConstants.accountAttributeIban
        String debtorAcc = PaymentsConstants.debtorAccount1

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, accountAttributes, debtorAcc,
                PaymentsConstants.creditorName1, PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

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
    void "TC0401027_Initiation Request with empty debtorAccount in the payload"() {

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadBuilder(PaymentsConstants.instructedAmountCurrency,
                PaymentsConstants.instructedAmount, PaymentsConstants.accountAttributeIban , "",
                PaymentsConstants.creditorName1, PaymentsConstants.creditorAccount1)

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Account reference type is missing")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401028_Initiation Request with empty array for payments"() {

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithConfigurablePayments("[]")

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Payments array cannot be empty for Bulk Payments")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401029_Initiation Request with empty array elements for payments"() {

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithConfigurablePayments("[{}]")

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    /**
     * This testcase only supports if AccountReferenceType is configured as 'iban' in open-banking.xml.
     */
    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401030_Initiation Request with payment entries having debtorAccount "() {

        String paymentArray = "[{\n" +
                "                    \"instructedAmount\": {\n" +
                "                        \"currency\": \"${PaymentsConstants.instructedAmountCurrency}\",\n" +
                "                        \"amount\": \"${PaymentsConstants.instructedAmount}\"\n" +
                "                    },\n" +
                "                     \"debtorAccount\": {\n" +
                "                        \"iban\": \"${PaymentsConstants.debtorAccount1}\"\n" +
                "                    },\n" +
                "                    \"creditorName\": \"${PaymentsConstants.creditorName2}\",\n" +
                "                    \"creditorAccount\": {\n" +
                "                        \"iban\": \"${PaymentsConstants.creditorAccount2}\"\n" +
                "                    },\n" +
                "                    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
                "                }]"

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithConfigurablePayments(paymentArray)

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "iban for Debtor Account should be equal for all payments")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401031_Initiation Request with payment entries having requestedExecutionDate"() {

        String paymentArray = "[{\n" +
                "        \"instructedAmount\": {\n" +
                "        \"currency\": \"${PaymentsConstants.instructedAmountCurrency}\",\n" +
                "        \"amount\": \"${PaymentsConstants.instructedAmount}\"\n" +
                "    },\n" +
                "        \"debtorAccount\": {\n" +
                "        \"iban\": \"${PaymentsConstants.debtorAccount1}\"\n" +
                "    },\n" +
                "        \"creditorName\": \"${PaymentsConstants.creditorName1}\",\n" +
                "        \"creditorAccount\": {\n" +
                "        \"iban\": \"${PaymentsConstants.creditorAccount1}\"\n" +
                "    },\n" +
                "        \"requestedExecutionDate\": \"${PaymentsInitiationPayloads.paymentDate}\",\n" +
                "        \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
                "    }]"

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithConfigurablePayments(paymentArray)

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Unrecognized property 'requestedExecutionDate'")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401032_Initiation Request with payment entries having requestedExecutionTime"() {

        String paymentArray = "[{\n" +
                "        \"instructedAmount\": {\n" +
                "        \"currency\": \"${PaymentsConstants.instructedAmountCurrency}\",\n" +
                "        \"amount\": \"${PaymentsConstants.instructedAmount}\"\n" +
                "    },\n" +
                "        \"debtorAccount\": {\n" +
                "        \"iban\": \"${PaymentsConstants.debtorAccount1}\"\n" +
                "    },\n" +
                "        \"creditorName\": \"${PaymentsConstants.creditorName1}\",\n" +
                "        \"creditorAccount\": {\n" +
                "        \"iban\": \"${PaymentsConstants.creditorAccount1}\"\n" +
                "    },\n" +
                "        \"requestedExecutionTime\": \"${OffsetTime.now().plusHours(1)}\",\n" +
                "        \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
                "    }]"

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithConfigurablePayments(paymentArray)

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Unrecognized property 'requestedExecutionTime'")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0401033_Initiation Request with both requestedExecutionDate and requestedExecutionTime"() {

        String bulkPaymentPayload = """{
            "batchBookingPreferred": true,
            "debtorAccount": {
                "iban": "${PaymentsConstants.debtorAccount1}"
                },
            "requestedExecutionDate": "${PaymentsInitiationPayloads.paymentDate}",
            "requestedExecutionTime": "${OffsetTime.now().plusHours(1)}",
            "payments":[
                {
                    "instructedAmount": {
                        "currency": "${PaymentsConstants.instructedAmountCurrency}",
                        "amount": "${PaymentsConstants.instructedAmount}"
                    },
                    "creditorName": "${PaymentsConstants.creditorName1}",
                    "creditorAccount": {
                        "iban": "${PaymentsConstants.creditorAccount1}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                }
            ]
        }"""
                .stripIndent()

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, bulkPaymentPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        //TODO: Uncomment the line after fixing the issue: https://github.com/wso2/financial-open-banking/issues/4437
        //Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT), "")
    }

    @Test (groups = ["1.3.6"])
    void "Initiation Request with single and future dated payment entries"() {

        String payload = PaymentsInitiationPayloads.bulkPaymentPayloadWithSingleAndFutureDatedEntries

        //Make Payment Initiation Request
        doDefaultInitiation(bulkPaymentConsentPath, payload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentResponse.getHeader("Location"))
        Assert.assertNotNull(consentResponse.getHeader("X-Request-ID"))
        Assert.assertEquals(consentResponse.getHeader("ASPSP-SCA-Approach"), "REDIRECT")

        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(paymentId, PaymentsConstants.TRANSACTION_STATUS_RECEIVED)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaOAuth.href"))
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.scaStatus.href"))
    }
}
