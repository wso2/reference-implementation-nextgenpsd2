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
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestDataProvider;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.doReturn;

/**
 * Test class for AccountConsentUtil class.
 */
@PrepareForTest({CommonConfigParser.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class AccountConsentUtilTests extends PowerMockTestCase {

    private static final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    @Mock
    CommonConfigParser commonConfigParserMock;

    @BeforeMethod
    public void init() {

        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
    }

    @Test(expectedExceptions = ConsentException.class, dataProvider = "InvalidAccountInitiationPayloadTestDataProvider",
            dataProviderClass = TestDataProvider.class)
    public void testInvalidValidateAccountInitiationPayloadScenarios(String payload, int configuredMinFreqPerDay,
                                                                     boolean isValidUntilDateCapEnabled,
                                                                     int validUntilDaysCap) throws ParseException {

        JSONObject payloadJsonObject = (JSONObject) parser.parse(payload);
        AccountConsentUtil.validateAccountInitiationPayload(payloadJsonObject, configuredMinFreqPerDay,
                isValidUntilDateCapEnabled, validUntilDaysCap);
    }

    @Test(dataProvider = "ValidAccReferencePayloadTestDataProvider", dataProviderClass = TestDataProvider.class)
    public void testValidAccRefObjects(String accountRefs) throws ParseException {

        JSONArray accountRefsJsonArray = (JSONArray) parser.parse(accountRefs);
        AccountConsentUtil.validateAccountRefObjects(accountRefsJsonArray);
    }

    @Test(expectedExceptions = ConsentException.class, dataProvider = "InvalidAccReferencePayloadTestDataProvider",
            dataProviderClass = TestDataProvider.class)
    public void testInvalidAccRefObjects(String accountRefs) throws ParseException {

        JSONArray accountRefsJsonArray = (JSONArray) parser.parse(accountRefs);
        AccountConsentUtil.validateAccountRefObjects(accountRefsJsonArray);
    }

    @Test(dataProvider = "ValidAccountInitiationPayloadTestDataProvider",
            dataProviderClass = TestDataProvider.class)
    public void testValidValidateAccountInitiationPayloadScenarios(String payload, int configuredMinFreqPerDay,
                                                                   boolean isValidUntilDateCapEnabled,
                                                                   int validUntilDaysCap) throws ParseException {

        JSONObject payloadJsonObject = (JSONObject) parser.parse(payload);
        AccountConsentUtil.validateAccountInitiationPayload(payloadJsonObject, configuredMinFreqPerDay,
                isValidUntilDateCapEnabled, validUntilDaysCap);
    }

    @Test(dataProvider = "PermissionTestDataProvider", dataProviderClass = TestDataProvider.class)
    public void testGetPermissionByValidatingAccountAccessAttribute(String payload, String expectedPermission)
            throws ParseException {

        JSONObject payloadJsonObject = (JSONObject) parser.parse(payload);
        String actualPermission = AccountConsentUtil.getPermissionByValidatingAccountAccessAttribute((JSONObject)
                payloadJsonObject.get(ConsentExtensionConstants.ACCESS));
        Assert.assertEquals(actualPermission, expectedPermission);
    }

    @Test(dataProvider = "ValidatedValidUntilTestDataProvider", dataProviderClass = TestDataProvider.class)
    public void testGetValidatedValidUntil(String validUntil, boolean isValidUntilDateCapEnabled,
                                           int validUntilDaysCap, String expectedValidUntil) {

        String actualValidUntil = AccountConsentUtil.getValidatedValidUntil(validUntil, isValidUntilDateCapEnabled,
                validUntilDaysCap);
        Assert.assertEquals(actualValidUntil, expectedValidUntil);
    }

    @Test
    public void testConvertToUtcTimestamp() {

        Assert.assertEquals(AccountConsentUtil.convertToUtcTimestamp("2021-12-31"), 1640908800);
        Assert.assertEquals(AccountConsentUtil.convertToUtcTimestamp("2023-03-01"), 1677628800);
    }

    @Test
    public void testIsConsentExpired() {

        OffsetDateTime validUntilDateTime;
        OffsetDateTime updatedDateTime;
        validUntilDateTime = OffsetDateTime.now().plusDays(2);
        updatedDateTime = OffsetDateTime.now().plusDays(2);
        Assert.assertFalse(AccountConsentUtil.isConsentExpired(validUntilDateTime.toEpochSecond(),
                updatedDateTime.toEpochSecond()));

        validUntilDateTime = OffsetDateTime.now().plusDays(10);
        updatedDateTime = OffsetDateTime.now().plusDays(8);
        Assert.assertFalse(AccountConsentUtil.isConsentExpired(validUntilDateTime.toEpochSecond(),
                updatedDateTime.toEpochSecond()));

        validUntilDateTime = OffsetDateTime.now().plusDays(-2);
        updatedDateTime = OffsetDateTime.now().plusDays(-2);
        Assert.assertTrue(AccountConsentUtil.isConsentExpired(validUntilDateTime.toEpochSecond(),
                updatedDateTime.toEpochSecond()));

        validUntilDateTime = OffsetDateTime.now().plusDays(100);
        updatedDateTime = OffsetDateTime.now().plusDays(-100);
        Assert.assertTrue(AccountConsentUtil.isConsentExpired(validUntilDateTime.toEpochSecond(),
                updatedDateTime.toEpochSecond()));
    }

}
