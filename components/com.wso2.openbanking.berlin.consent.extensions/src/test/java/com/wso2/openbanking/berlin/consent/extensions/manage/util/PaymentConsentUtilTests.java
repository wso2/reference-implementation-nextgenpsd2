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

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for PaymentConsentUtil class.
 */
@PrepareForTest({CommonConfigParser.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
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

        configMap.put(CommonConstants.SUPPORTED_ACCOUNT_REFERENCE_TYPES_PATH, ConsentExtensionConstants.IBAN);
        configMap.put(CommonConstants.MAX_FUTURE_PAYMENT_DAYS, StringUtils.EMPTY);

        commonConfigParserMock = mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        doReturn(configMap).when(commonConfigParserMock).getConsentMgtConfigs();

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateDebtorAccount() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITHOUT_DEBTOR_ACCOUNT);
        PaymentConsentUtil.validateDebtorAccount(payload);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateDebtorAccountWithBban() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.VALID_PAYMENTS_PAYLOAD);
        PaymentConsentUtil.validateDebtorAccount(payload);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateDebtorAccountWithPan() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.VALID_PAYMENTS_PAYLOAD);
        PaymentConsentUtil.validateDebtorAccount(payload);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateDebtorAccountWithInvalidReference() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITH_INVALID_REFERENCE);
        PaymentConsentUtil.validateDebtorAccount(payload);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateCommonPaymentElementsWithoutInstructedAmount() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITH_NULL_INSTRUCTED_AMOUNT);
        PaymentConsentUtil.validateCommonPaymentElements(payload);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateCommonPaymentElementsWithoutCurrencyInInstructedAmount() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITHOUT_CURRENCY);
        PaymentConsentUtil.validateCommonPaymentElements(payload);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateCommonPaymentElementsWithoutAmountInInstructedAmount() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITHOUT_AMOUNT);
        PaymentConsentUtil.validateCommonPaymentElements(payload);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateCommonPaymentElementsWithoutCreditorAccount() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITHOUT_CREDITOR_ACCOUNT);
        PaymentConsentUtil.validateCommonPaymentElements(payload);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateCommonPaymentElementsWithoutCreditorName() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITHOUT_CREDITOR_NAME);
        PaymentConsentUtil.validateCommonPaymentElements(payload);
    }

    @Test
    public void testGetPaymentProduct() {

        String samplePath = "payments/sepa-credit-transfers";
        Assert.assertEquals(ConsentExtensionConstants.SEPA_CREDIT_TRANSFERS,
                PaymentConsentUtil.getPaymentProduct(samplePath));
    }

    @Test
    public void testGetConstructedPaymentsGetResponse() throws ParseException {

        ConsentResource sampleConsentResource = new ConsentResource();
        sampleConsentResource.setReceipt(TestPayloads.VALID_PAYMENTS_PAYLOAD);
        sampleConsentResource.setCurrentStatus(TransactionStatusEnum.ACCP.name());

        JSONObject paymentGetResponse = PaymentConsentUtil.getConstructedPaymentsGetResponse(sampleConsentResource);
        Assert.assertEquals(paymentGetResponse.get(ConsentExtensionConstants.TRANSACTION_STATUS),
                TransactionStatusEnum.ACCP.name());
        Assert.assertNotNull(paymentGetResponse.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));
        Assert.assertNotNull(paymentGetResponse.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT));
        Assert.assertNotNull(paymentGetResponse.get(ConsentExtensionConstants.CREDITOR_ACCOUNT));
        Assert.assertNotNull(paymentGetResponse.get(ConsentExtensionConstants.CREDITOR_NAME));
    }

    @Test
    public void testGetConstructedPeriodicPaymentGetResponse() throws ParseException {

        ConsentResource sampleConsentResource = new ConsentResource();
        sampleConsentResource.setReceipt(TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);
        sampleConsentResource.setCurrentStatus(TransactionStatusEnum.ACCP.name());

        JSONObject periodicPaymentGetResponse =
                PaymentConsentUtil.getConstructedPeriodicPaymentGetResponse(sampleConsentResource);
        Assert.assertNotNull(periodicPaymentGetResponse.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));
        Assert.assertNotNull(periodicPaymentGetResponse.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT));
        Assert.assertNotNull(periodicPaymentGetResponse.get(ConsentExtensionConstants.CREDITOR_ACCOUNT));
        Assert.assertNotNull(periodicPaymentGetResponse.get(ConsentExtensionConstants.CREDITOR_NAME));
        Assert.assertNotNull(periodicPaymentGetResponse.get(ConsentExtensionConstants.START_DATE));
        Assert.assertNotNull(periodicPaymentGetResponse.get(ConsentExtensionConstants.FREQUENCY));
    }

    @Test
    public void testGetConstructedBulkPaymentGetResponse() throws ParseException {

        ConsentResource sampleConsentResource = new ConsentResource();
        sampleConsentResource.setReceipt(TestPayloads.VALID_BULK_PAYMENTS_PAYLOAD);
        sampleConsentResource.setCurrentStatus(TransactionStatusEnum.ACCP.name());

        JSONObject bulkPaymentGetResponse =
                PaymentConsentUtil.getConstructedBulkPaymentGetResponse(sampleConsentResource);
        Assert.assertNotNull(bulkPaymentGetResponse.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));
        Assert.assertNotNull(bulkPaymentGetResponse.get(ConsentExtensionConstants.PAYMENTS));
        JSONArray paymentsArray = (JSONArray) bulkPaymentGetResponse.get(ConsentExtensionConstants.PAYMENTS);

        for (Object payment : paymentsArray) {
            JSONObject paymentJson = (JSONObject) payment;
            Assert.assertNotNull(paymentJson.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT));
            Assert.assertNotNull(paymentJson.get(ConsentExtensionConstants.CREDITOR_ACCOUNT));
            Assert.assertNotNull(paymentJson.get(ConsentExtensionConstants.CREDITOR_NAME));
            Assert.assertNotNull(paymentJson.get(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED));
        }
    }

    @Test
    public void testGetPaymentCancellationResponse() {

        List<Map<String, String>> scaMethods = new ArrayList<>();
        Map<String, String> scaMethod = new HashMap<>();
        scaMethod.put(CommonConstants.SCA_TYPE, "SMS_OTP");
        scaMethod.put(CommonConstants.SCA_VERSION, "1.0");
        scaMethod.put(CommonConstants.SCA_ID, "sms-otp");
        scaMethod.put(CommonConstants.SCA_NAME, "SMS OTP on Mobile");
        scaMethod.put(CommonConstants.SCA_MAPPED_APPROACH, "REDIRECT");
        scaMethod.put(CommonConstants.SCA_DESCRIPTION, "SMS based one time password");
        scaMethod.put(CommonConstants.SCA_DEFAULT, "true");
        scaMethods.add(scaMethod);
        doReturn(scaMethods).when(commonConfigParserMock).getSupportedScaMethods();
        doReturn("v1").when(commonConfigParserMock).getApiVersion(Mockito.anyString());

        ConsentResource consentResource = new ConsentResource();
        consentResource.setCurrentStatus(TransactionStatusEnum.CANC.name());
        String consentId = UUID.randomUUID().toString();
        consentResource.setConsentID(consentId);

        String sampleRequestPath = "/payments/sepa-credit-transfers";
        JSONObject paymentCancellationResponse = PaymentConsentUtil.getPaymentCancellationResponse(consentResource,
                sampleRequestPath, true);
        Assert.assertEquals(TransactionStatusEnum.CANC.name(),
                paymentCancellationResponse.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        Assert.assertNotNull(paymentCancellationResponse.get(ConsentExtensionConstants.CHOSEN_SCA_METHOD));
        Assert.assertNotNull(paymentCancellationResponse.get(ConsentExtensionConstants.LINKS));
        JSONObject chosenScaMethod = (JSONObject) paymentCancellationResponse
                .get(ConsentExtensionConstants.CHOSEN_SCA_METHOD);
        Assert.assertEquals(chosenScaMethod.get(TestConstants.SCA_TYPE), scaMethod.get(CommonConstants.SCA_TYPE));
        Assert.assertEquals(chosenScaMethod.get(TestConstants.SCA_VERSION),
                scaMethod.get(CommonConstants.SCA_VERSION));
        Assert.assertEquals(chosenScaMethod.get(TestConstants.SCA_ID), scaMethod.get(CommonConstants.SCA_ID));
        Assert.assertEquals(chosenScaMethod.get(TestConstants.SCA_NAME), scaMethod.get(CommonConstants.SCA_NAME));
        Assert.assertEquals(chosenScaMethod.get(TestConstants.SCA_DESCRIPTION),
                scaMethod.get(CommonConstants.SCA_DESCRIPTION));

        JSONObject links = (JSONObject) paymentCancellationResponse.get(ConsentExtensionConstants.LINKS);
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
    }
}
