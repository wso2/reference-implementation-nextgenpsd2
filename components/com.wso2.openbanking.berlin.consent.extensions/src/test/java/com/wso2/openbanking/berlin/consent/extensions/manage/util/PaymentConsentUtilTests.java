/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.manage.util;

import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for PaymentConsentUtil class.
 */
@PrepareForTest({CommonConfigParser.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*"})
public class PaymentConsentUtilTests extends PowerMockTestCase {

    private static JSONParser parser;
    private static Map<String, String> configMap;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @BeforeClass
    public void init() {
        configMap = new HashMap<>();
        parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    }

    @BeforeMethod
    public void initMethod() {

        configMap.put(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH, ConsentExtensionConstants.IBAN);
        configMap.put(CommonConstants.MAX_FUTURE_PAYMENT_DAYS, StringUtils.EMPTY);

        commonConfigParserMock = mock(CommonConfigParser.class);
        doReturn(configMap).when(commonConfigParserMock).getConsentMgtConfigs();

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
    }

//    @Test
//    public void testValidatePaymentsPayload() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads.VALID_PAYMENTS_PAYLOAD),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithoutDebtorAccount() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITHOUT_DEBTOR_ACCOUNT), configMap.get(CommonConstants
//                .ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithNullIban() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_NULL_IBAN), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithNullBban() throws ParseException {
//        configMap.put(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH, ConsentExtensionConstants.BBAN);
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_NULL_BBAN), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithNullPan() throws ParseException {
//
//        configMap.put(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH, ConsentExtensionConstants.PAN);
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_NULL_PAN), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithEmptyIban() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_EMPTY_IBAN), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithEmptyBban() throws ParseException {
//        configMap.put(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH, ConsentExtensionConstants.BBAN);
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_EMPTY_BBAN), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithEmptyPan() throws ParseException {
//
//        configMap.put(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH, ConsentExtensionConstants.PAN);
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_EMPTY_PAN), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithInvalidAccountReference() throws ParseException {
//
//        configMap.put(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH, "invalid account reference");
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_INVALID_IBAN), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithoutInstructedAmount() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_NULL_INSTRUCTED_AMOUNT),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithEmptyInstructedAmount() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_EMPTY_INSTRUCTED_AMOUNT),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithoutAmount() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITHOUT_AMOUNT), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithEmptyAmount() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_EMPTY_AMOUNT), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithoutCurrency() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITHOUT_CURRENCY), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithEmptyCurrency() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_EMPTY_CURRENCY), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithoutCreditorAccount() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITHOUT_CREDITOR_ACCOUNT),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithoutCreditorName() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITHOUT_CREDITOR_NAME), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePaymentsPayloadWithEmptyCreditorName() throws ParseException {
//
//        PaymentConsentUtil.validatePaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PAYMENTS_PAYLOAD_WITH_EMPTY_CREDITOR_NAME),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test
//    public void testValidatePeriodicalPaymentsPayload() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .VALID_PERIODICAL_PAYMENT_PAYLOAD), configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithoutStartDate() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITHOUT_START_DATE),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithEmptyStartDate() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITH_EMPTY_START_DATE),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithoutFrequency() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITHOUT_FREQUENCY),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithEmptyFrequency() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITH_EMPTY_FREQUENCY),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test
//    public void testValidatePeriodicalPaymentsPayloadWithoutExecutionRule() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITHOUT_EXECUTION_RULE),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithInvalidExecutionRule() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITH_INVALID_EXECUTION_RULE),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithPastEndDate() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITH_PAST_END_DATE),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithInvalidStartDate() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .VALID_PERIODICAL_PAYMENT_PAYLOAD_WITH_INVALID_START_DATE),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithInvalidEndDate() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITH_INVALID_END_DATE),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidatePeriodicalPaymentsPayloadWithInconsistentDates() throws ParseException {
//
//        PaymentConsentUtil.validatePeriodicPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .PERIODICAL_PAYMENT_PAYLOAD_WITH_INCONSISTENT_DATES),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (priority = 1)
//    public void testValidateBulkPaymentsPayloadWithValidPayload() throws ParseException {
//
//        PaymentConsentUtil.validateBulkPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .VALID_BULK_PAYMENTS_PAYLOAD), configMap.get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test
//    public void testValidateBulkPaymentsPayloadWithoutExecutionDate() throws ParseException {
//
//        PaymentConsentUtil.validateBulkPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .BULK_PAYMENTS_PAYLOAD_WITHOUT_EXECUTION_DATE),
//                configMap.get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test
//    public void testValidateBulkPaymentsPayloadWithEmptyExecutionDate() throws ParseException {
//
//        PaymentConsentUtil.validateBulkPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .BULK_PAYMENTS_PAYLOAD_WITH_EMPTY_EXECUTION_DATE),
//                configMap.get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidateBulkPaymentsPayloadWithInvalidExecutionDate() throws ParseException {
//
//        PaymentConsentUtil.validateBulkPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .BULK_PAYMENTS_PAYLOAD_WITH_INVALID_EXECUTION_DATE),
//                configMap.get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidateBulkPaymentsPayloadWithPastExecutionDate() throws ParseException {
//
//        PaymentConsentUtil.validateBulkPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .BULK_PAYMENTS_PAYLOAD_WITH_PAST_EXECUTION_DATE),
//                configMap.get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class, priority = 2)
//    public void testValidateBulkPaymentsPayloadWithAnExecutionDateOutOfRange() throws ParseException {
//
//        configMap.put(CommonConstants.MAX_FUTURE_PAYMENT_DAYS, "2");
//        PaymentConsentUtil.validateBulkPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .VALID_BULK_PAYMENTS_PAYLOAD),
//                configMap.get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidateBulkPaymentsPayloadWithNoPaymentElement() throws ParseException {
//
//        PaymentConsentUtil.validateBulkPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .BULK_PAYMENTS_PAYLOAD_WITHOUT_PAYMENTS), configMap.get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }
//
//    @Test (expectedExceptions = ConsentException.class)
//    public void testValidateBulkPaymentsPayloadWithEmptyPaymentElement() throws ParseException {
//
//        PaymentConsentUtil.validateBulkPaymentsPayload((JSONObject) parser.parse(TestPayloads
//                .BULK_PAYMENTS_PAYLOAD_WITH_EMPTY_PAYMENTS), configMap.get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS),
//                configMap.get(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH));
//    }

    @Test
    public void testGetPaymentProduct() {

        String samplePath = "payments/sepa-credit-transfers";
        Assert.assertEquals(ConsentExtensionConstants.SEPA_CREDIT_TRANSFERS,
                PaymentConsentUtil.getPaymentProduct(samplePath));
    }


}
