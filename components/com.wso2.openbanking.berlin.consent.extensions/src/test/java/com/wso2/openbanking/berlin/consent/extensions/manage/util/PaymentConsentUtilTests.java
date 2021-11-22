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
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
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

        configMap.put(CommonConstants.ACCOUNT_REFERENCE_TYPE_PATH, ConsentExtensionConstants.IBAN);
        configMap.put(CommonConstants.MAX_FUTURE_PAYMENT_DAYS, StringUtils.EMPTY);

        commonConfigParserMock = mock(CommonConfigParser.class);
        doReturn(configMap).when(commonConfigParserMock).getConsentMgtConfigs();

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateDebtorAccount() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITHOUT_DEBTOR_ACCOUNT);
        PaymentConsentUtil.validateDebtorAccount(payload, "iban");
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateDebtorAccountWithBban() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.VALID_PAYMENTS_PAYLOAD);
        PaymentConsentUtil.validateDebtorAccount(payload, "bban");
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateDebtorAccountWithPan() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.VALID_PAYMENTS_PAYLOAD);
        PaymentConsentUtil.validateDebtorAccount(payload, "pan");
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testValidateDebtorAccountWithInvalidReference() throws ParseException {

        JSONObject payload =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(TestPayloads.PAYMENTS_PAYLOAD_WITH_INVALID_REFERENCE);
        PaymentConsentUtil.validateDebtorAccount(payload, "iban");
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


}
