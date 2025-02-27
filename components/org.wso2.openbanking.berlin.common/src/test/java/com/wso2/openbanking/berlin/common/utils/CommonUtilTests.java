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

package org.wso2.openbanking.berlin.common.utils;

import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.CommonConstants;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.common.enums.ScaApproachEnum;
import org.wso2.openbanking.berlin.common.models.ScaApproach;
import org.wso2.openbanking.berlin.common.models.ScaMethod;
import org.wso2.openbanking.berlin.common.models.TPPMessage;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.doReturn;

/**
 * This contains unit tests for CommonUtil class.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CommonConfigParser.class})
public class CommonUtilTests {

    private static List<Map<String, String>> supportedScaMethods;
    private static List<Map<String, String>> supportedScaApproaches;
    private CommonConfigParser commonConfigParserMock;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);

        supportedScaMethods = new ArrayList<>();
        supportedScaApproaches = new ArrayList<>();

        Map<String, String> scaApproach1 = new HashMap<>();
        scaApproach1.put(CommonConstants.SCA_NAME, "REDIRECT");
        scaApproach1.put(CommonConstants.SCA_DEFAULT, "true");

        Map<String, String> scaApproach2 = new HashMap<>();
        scaApproach2.put(CommonConstants.SCA_NAME, "DECOUPLED");
        scaApproach2.put(CommonConstants.SCA_DEFAULT, "false");

        Map<String, String> scaMethod1 = new HashMap<>();
        scaMethod1.put(CommonConstants.SCA_TYPE, "SMS_OTP");
        scaMethod1.put(CommonConstants.SCA_VERSION, "1.0");
        scaMethod1.put(CommonConstants.SCA_ID, "sms-otp");
        scaMethod1.put(CommonConstants.SCA_NAME, "SMS OTP on Mobile");
        scaMethod1.put(CommonConstants.SCA_MAPPED_APPROACH, "REDIRECT");
        scaMethod1.put(CommonConstants.SCA_DESCRIPTION, "SMS based one time password");
        scaMethod1.put(CommonConstants.SCA_DEFAULT, "true");

        Map<String, String> scaMethod2 = new HashMap<>();
        scaMethod2.put(CommonConstants.SCA_TYPE, "PUSH_OTP");
        scaMethod2.put(CommonConstants.SCA_VERSION, "1.0");
        scaMethod2.put(CommonConstants.SCA_ID, "push-otp");
        scaMethod2.put(CommonConstants.SCA_NAME, "PUSH OTP on Mobile app");
        scaMethod2.put(CommonConstants.SCA_MAPPED_APPROACH, "DECOUPLED");
        scaMethod2.put(CommonConstants.SCA_DESCRIPTION, "Mobile push notification");
        scaMethod2.put(CommonConstants.SCA_DEFAULT, "false");

        supportedScaMethods.add(scaMethod1);
        supportedScaMethods.add(scaMethod2);

        supportedScaApproaches.add(scaApproach1);
        supportedScaApproaches.add(scaApproach2);
    }

    @BeforeMethod
    public void initMethod() {

        commonConfigParserMock = Mockito.mock(CommonConfigParser.class);
        Mockito.doReturn(supportedScaMethods).when(commonConfigParserMock).getSupportedScaMethods();
        Mockito.doReturn(supportedScaApproaches).when(commonConfigParserMock).getSupportedScaApproaches();

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testGetScaApproachAndMethods() {

        Map<String, Object> scaApproachAndMethods;
        ScaApproach currentScaApproach;
        List<ScaMethod> currentScaMethods;

        // isTppRedirectPreferred = true; isScaRequired = true
        scaApproachAndMethods = CommonUtil.getScaApproachAndMethods(true, true);

        currentScaApproach = (ScaApproach) scaApproachAndMethods.get(CommonConstants.SCA_APPROACH_KEY);
        currentScaMethods = (List<ScaMethod>) scaApproachAndMethods.get(CommonConstants.SCA_METHODS_KEY);

        Assert.assertEquals(currentScaApproach.getApproach(), ScaApproachEnum.REDIRECT);
        Assert.assertEquals(currentScaMethods.size(), 1);
        Assert.assertEquals(currentScaMethods.get(0).getMappedApproach(), ScaApproachEnum.REDIRECT);

        // isTppRedirectPreferred = true; isScaRequired = false
        scaApproachAndMethods = CommonUtil.getScaApproachAndMethods(true, false);

        currentScaApproach = (ScaApproach) scaApproachAndMethods.get(CommonConstants.SCA_APPROACH_KEY);
        currentScaMethods = (List<ScaMethod>) scaApproachAndMethods.get(CommonConstants.SCA_METHODS_KEY);

        Assert.assertEquals(currentScaApproach.getApproach(), ScaApproachEnum.REDIRECT);
        Assert.assertEquals(currentScaMethods.size(), 0);

        // isTppRedirectPreferred = false; isScaRequired = true
        scaApproachAndMethods = CommonUtil.getScaApproachAndMethods(false, true);

        currentScaApproach = (ScaApproach) scaApproachAndMethods.get(CommonConstants.SCA_APPROACH_KEY);
        currentScaMethods = (List<ScaMethod>) scaApproachAndMethods.get(CommonConstants.SCA_METHODS_KEY);

        Assert.assertEquals(currentScaApproach.getApproach(), ScaApproachEnum.DECOUPLED);
        Assert.assertEquals(currentScaMethods.size(), 1);
        Assert.assertEquals(currentScaMethods.get(0).getMappedApproach(), ScaApproachEnum.DECOUPLED);

        // isTppRedirectPreferred = false; isScaRequired = false
        scaApproachAndMethods = CommonUtil.getScaApproachAndMethods(false, false);

        currentScaApproach = (ScaApproach) scaApproachAndMethods.get(CommonConstants.SCA_APPROACH_KEY);
        currentScaMethods = (List<ScaMethod>) scaApproachAndMethods.get(CommonConstants.SCA_METHODS_KEY);

        Assert.assertEquals(currentScaApproach.getApproach(), ScaApproachEnum.DECOUPLED);
        Assert.assertEquals(currentScaMethods.size(), 0);

        // isTppRedirectPreferred = null; isScaRequired = true
        // (if default SCA method is configured the approach and method will be that of the default SCA method)
        scaApproachAndMethods = CommonUtil.getScaApproachAndMethods(null, true);

        currentScaApproach = (ScaApproach) scaApproachAndMethods.get(CommonConstants.SCA_APPROACH_KEY);
        currentScaMethods = (List<ScaMethod>) scaApproachAndMethods.get(CommonConstants.SCA_METHODS_KEY);

        Assert.assertEquals(currentScaApproach.getApproach(), ScaApproachEnum.REDIRECT);
        Assert.assertEquals(currentScaMethods.size(), 1);
        Assert.assertEquals(currentScaMethods.get(0).getMappedApproach(), ScaApproachEnum.REDIRECT);

        // isTppRedirectPreferred = null; isScaRequired = false
        // (if default SCA approach is configured it will be selected as the approach)
        scaApproachAndMethods = CommonUtil.getScaApproachAndMethods(null, false);

        currentScaApproach = (ScaApproach) scaApproachAndMethods.get(CommonConstants.SCA_APPROACH_KEY);
        currentScaMethods = (List<ScaMethod>) scaApproachAndMethods.get(CommonConstants.SCA_METHODS_KEY);

        Assert.assertEquals(currentScaApproach.getApproach(), ScaApproachEnum.REDIRECT);
        Assert.assertEquals(currentScaMethods.size(), 0);
    }

    @Test
    public void testConvertObjectToJson() {

        ScaMethod scaMethod = new ScaMethod();
        scaMethod.setAuthenticationType("SMS_OTP");
        scaMethod.setAuthenticationVersion("1.0");
        scaMethod.setAuthenticationMethodId("sms-otp");
        scaMethod.setName("SMS OTP on Mobile");
        scaMethod.setMappedApproach(ScaApproachEnum.REDIRECT);
        scaMethod.setDescription("SMS based one time password");
        scaMethod.setDefault(true);

        JSONObject scaMethodObject = CommonUtil.convertObjectToJson(scaMethod);

        Assert.assertEquals(scaMethodObject.get("authenticationType"), "SMS_OTP");
        Assert.assertEquals(scaMethodObject.get("authenticationMethodId"), "sms-otp");
        Assert.assertEquals(scaMethodObject.get("name"), "SMS OTP on Mobile");
        Assert.assertEquals(scaMethodObject.get("explanation"), "SMS based one time password");
    }

    @Test
    public void testIsValidUuid() {

        Assert.assertFalse(CommonUtil.isValidUuid(""));
        Assert.assertFalse(CommonUtil.isValidUuid("abc"));
        Assert.assertTrue(CommonUtil.isValidUuid("1b91e649-3d06-4e16-ada7-bf5af2136b44"));
    }

    @Test
    public void isJsonPayloadSimilarTest() throws IOException {

        String samplePayload1 = "{\n" +
                "    \"instructedAmount\": {\n" +
                "        \"currency\": \"EUR\",\n" +
                "        \"amount\": \"123.50\"\n" +
                "    },\n" +
                "    \"debtorAccount\": {\n" +
                "        \"iban\": \"DE12345678901234567890\"\n" +
                "    },\n" +
                "    \"creditorName\": \"Merchant123\",\n" +
                "    \"creditorAccount\": {\n" +
                "        \"iban\": \"DE98765432109876543210\"\n" +
                "    },\n" +
                "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
                "}";

        Assert.assertTrue(CommonUtil.isJSONPayloadSimilar(samplePayload1, samplePayload1));
        Assert.assertFalse(CommonUtil.isJSONPayloadSimilar(samplePayload1, "{}"));
    }

    @Test
    public void isRequestReceivedWithinAllowedTimeTest() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(CommonConstants.IDEMPOTENCY_ALLOWED_TIME, "1");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("GMT"));
        doReturn(configMap).when(commonConfigParserMock).getConfiguration();
        Assert.assertTrue(CommonUtil.isRequestReceivedWithinAllowedTime(DateTimeFormatter.RFC_1123_DATE_TIME
                .format(zonedDateTime)));
        Assert.assertTrue(CommonUtil.isRequestReceivedWithinAllowedTime(null));
    }

    @Test
    public void isRequestReceivedWithinAllowedTimeTestNull() {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(CommonConstants.IDEMPOTENCY_ALLOWED_TIME, null);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("GMT"));
        doReturn(configMap).when(commonConfigParserMock).getConfiguration();
        Assert.assertFalse(CommonUtil.isRequestReceivedWithinAllowedTime(DateTimeFormatter.RFC_1123_DATE_TIME
                .format(zonedDateTime)));
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testFromValueOfCommonEnums() {

        ScaApproachEnum.fromValue("");
        ConsentTypeEnum.fromValue("");
        TPPMessage.CodeEnum.fromValue("");

        ScaApproachEnum.fromValue("ABC");
        ConsentTypeEnum.fromValue("ABC");
        TPPMessage.CodeEnum.fromValue("ABC");
    }
}
