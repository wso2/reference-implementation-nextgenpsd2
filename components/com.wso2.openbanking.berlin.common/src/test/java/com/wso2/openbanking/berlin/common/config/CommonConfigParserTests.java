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

package com.wso2.openbanking.berlin.common.config;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.util.CommonTestUtil;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

@PrepareForTest({OpenBankingConfigParser.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class CommonConfigParserTests extends PowerMockTestCase {

    private String absolutePathForTestResources;
    private OpenBankingConfigParser openBankingConfigParserMock;

    @BeforeClass
    public void beforeClass() throws ReflectiveOperationException {

        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        CommonTestUtil.injectEnvironmentVariable("CARBON_HOME", ".");
        String path = "src/test/resources";
        File file = new File(path);
        absolutePathForTestResources = file.getAbsolutePath();
    }

    //Runtime exception is thrown here because carbon home is not defined properly for an actual carbon product
    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 1)
    public void testConfigParserInitiationWithoutPath() {

        CommonConfigParser openBankingConfigParser = CommonConfigParser.getInstance();

    }

    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 2)
    public void testRuntimeExceptionInvalidConfigFile() {

        String path = absolutePathForTestResources + "/open-banking-berlin-empty.xml";
        CommonConfigParser openBankingConfigParser = CommonConfigParser.getInstance(path);

    }

    @Test(expectedExceptions = OpenBankingRuntimeException.class, priority = 3)
    public void testRuntimeExceptionNonExistentFile() {

        String path = absolutePathForTestResources + "/open-banking-berlin.xml" + "/value";
        CommonConfigParser openBankingConfigParser = CommonConfigParser.getInstance(path);

    }

    @Test(priority = 4)
    public void testConfigParserInit() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);
        Map<String, String> consentMgtConfigs = commonConfigParser.getConsentMgtConfigs();

        Assert.assertNotNull(consentMgtConfigs.get("PayableAccountsRetrieveEndpoint"));
        Assert.assertNotNull(consentMgtConfigs.get("SharableAccountsRetrieveEndpoint"));

    }

    @Test(priority = 5)
    public void testSingleton() {

        CommonConfigParser instance1 = CommonConfigParser.getInstance();
        CommonConfigParser instance2 = CommonConfigParser.getInstance();
        Assert.assertEquals(instance2, instance1);
    }

    @Test(priority = 6)
    public void testGetSupportedScaMethods() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);
        List<Map<String, String>> supportedScaMethods = commonConfigParser.getSupportedScaMethods();

        Map<String, String> supportedScaMethod = supportedScaMethods.get(0);

        Assert.assertEquals(supportedScaMethod.get("Type"), "SMS_OTP");
        Assert.assertEquals(supportedScaMethod.get("Version"), "1.0");
        Assert.assertEquals(supportedScaMethod.get("Id"), "sms-otp");
        Assert.assertEquals(supportedScaMethod.get("Name"), "SMS OTP on Mobile");
        Assert.assertEquals(supportedScaMethod.get("MappedApproach"), "REDIRECT");
        Assert.assertEquals(supportedScaMethod.get("Description"), "SMS based one time password");
        Assert.assertEquals(supportedScaMethod.get("Default"), "true");
    }

    @Test(priority = 7)
    public void testGetSupportedScaApproaches() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);
        List<Map<String, String>> supportedScaApproaches = commonConfigParser.getSupportedScaApproaches();

        Map<String, String> supportedScaApproach = supportedScaApproaches.get(0);

        Assert.assertEquals(supportedScaApproach.get("Name"), "REDIRECT");
        Assert.assertEquals(supportedScaApproach.get("Default"), "true");
    }

    @Test(priority = 8)
    public void testBerlinSpecificConfigurations() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);

        Assert.assertTrue(commonConfigParser.isScaRequired());

        Assert.assertEquals(commonConfigParser.getOauthMetadataEndpoint(),
                "https://localhost:8243/.well-known/openid-configuration");

        Assert.assertEquals(commonConfigParser.getConfiguredMinimumFreqPerDay(), 4);

        Assert.assertFalse(commonConfigParser.isValidUntilDateCapEnabled());

        Assert.assertEquals(commonConfigParser.validUntilDaysCap(), 0);

        Assert.assertEquals(commonConfigParser.getApiVersion(ConsentTypeEnum.ACCOUNTS.toString()), "v1");
        Assert.assertEquals(commonConfigParser.getApiVersion(ConsentTypeEnum.PAYMENTS.toString()), "v1");
        Assert.assertEquals(commonConfigParser.getApiVersion(ConsentTypeEnum.FUNDS_CONFIRMATION.toString()), "v2");
    }

    @Test (priority = 9)
    public void testGetMaximumFutureFuturePaymentDays() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);

        Assert.assertEquals(commonConfigParser.getMaxFuturePaymentDays(), "");
    }

    @Test (priority = 10)
    public void testGetOneSupportedCodeChallengeMethods() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);

        Assert.assertEquals(commonConfigParser.getSupportedCodeChallengeMethods().get(0), "S256");
    }

    @Test (priority = 11)
    public void testOneGetSupportedHashAlgorithm() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);

        Assert.assertEquals(commonConfigParser.getSupportedHashAlgorithms().get(0), "SHA-256");
    }

    @Test (priority = 12)
    public void testOneGetSupportedSignatureAlgorithm() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);

        Assert.assertEquals(commonConfigParser.getSupportedSignatureAlgorithms().get(0), "SHA256withRSA");
    }

    @Test (priority = 14)
    public void testGetOrgIdValidationRegex() {

        String dummyConfigFile = absolutePathForTestResources + "/open-banking-berlin.xml";
        CommonConfigParser commonConfigParser = CommonConfigParser.getInstance(dummyConfigFile);

        Assert.assertEquals(commonConfigParser.getOrgIdValidationRegex(), "^PSD[A-Z]{2}-[A-Z]{2,8}-[a-zA-Z0-9]*$");
    }
}
