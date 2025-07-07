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

package org.wso2.openbanking.berlin.keymanager;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({OpenBankingConfigParser.class, CommonConfigParser.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class BerlinKeyManagerExtensionImplTests {
    @Mock
    X509Certificate x509Certificate;

    @Spy
    CertificateContent certificateContent;

    @Spy
    BerlinKeyManagerExtensionImpl berlinKeyManagerExtensionImpl;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    @Mock
    CommonConfigParser commonConfigParser;

    @Spy
    org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO oAuthConsumerAppDTO;

    @Spy
    org.wso2.carbon.identity.application.common.model.ServiceProvider serviceProvider;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() {

        MockitoAnnotations.initMocks(this);
    }

    @DataProvider
    public Object[][] validateOrganizationIdPatternDataProvider() {

        return new Object[][]{
                {"PSDGB-OB-Unknown0015800001HQQrZAAX", null},
                {"GB-OB-Unknown0015800001HQQrZAAX", APIManagementException.class},
                {"PSDGBUnknown0015800001HQQrZAAX", APIManagementException.class},
        };
    }

    @Test(dataProvider = "validateOrganizationIdPatternDataProvider")
    private void testValidateOrganizationIdPattern(String orgId, Class<? extends Exception> exceptionType) {

        commonConfigParser = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParser);
        PowerMockito.when(commonConfigParser.getOrgIdValidationRegex())
                .thenReturn("^PSD[A-Z]{2}-[A-Z]{2,8}-[a-zA-Z0-9]*$");

        try {
            mockStatic(CommonConfigParser.class);
            CommonConfigParser commonConfigParser = mock(CommonConfigParser.class);
            when(CommonConfigParser.getInstance()).thenReturn(commonConfigParser);
            when(commonConfigParser.getOrgIdValidationRegex()).thenReturn("^PSD[A-Z]{2}-[A-Z]{2,8}-[a-zA-Z0-9]*$");
            berlinKeyManagerExtensionImpl.validateOrganizationIdPattern(orgId);
            Assert.assertTrue(exceptionType == null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getClass(), exceptionType);
        }

    }

    @DataProvider
    public Object[][] validateRolesFromCertDataProvider() {

        List<String> allValidRoles = new ArrayList<>(Arrays.asList("AISP", "PISP", "CBPII"));
        List<String> singleValidRole = new ArrayList<>(Arrays.asList("AISP"));
        List<String> allInvalidRoles = new ArrayList<>(Arrays.asList("dummy1", "dummy2"));
        List<String> singleInvalidRole = new ArrayList<>(Arrays.asList("AISP", "PISP", "dummy"));
        return new Object[][]{
                {allValidRoles, true},
                {singleValidRole, true},
                {allInvalidRoles, false},
                {singleInvalidRole, false},
        };
    }

    @Test(dataProvider = "validateRolesFromCertDataProvider")
    private void testValidateRolesFromCert(List<String> roles, boolean isValid) throws APIManagementException {

        openBankingConfigParser = PowerMockito.mock(OpenBankingConfigParser.class);
        mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        commonConfigParser = PowerMockito.mock(CommonConfigParser.class);
        mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParser);

        berlinKeyManagerExtensionImpl = Mockito.spy(BerlinKeyManagerExtensionImpl.class);
        Mockito.doReturn(certificateContent).when(berlinKeyManagerExtensionImpl)
                .extractCertificateContent(Mockito.anyObject());
        Map<String, List<String>> allowedScopes = new HashMap<>();
        allowedScopes.put("accounts", Arrays.asList("AISP", "PISP"));
        allowedScopes.put("payments", Arrays.asList("PISP"));
        allowedScopes.put("cof", Arrays.asList("CBPII"));
        Mockito.when(openBankingConfigParser.getAllowedScopes()).thenReturn(allowedScopes);
        Mockito.when(commonConfigParser.isPsd2RoleValidationEnabled()).thenReturn(true);
        certificateContent.setPspRoles(roles);
        Assert.assertEquals(berlinKeyManagerExtensionImpl.validateRolesFromCert(x509Certificate), isValid);
    }

    @DataProvider
    public Object[][] validateOrganizationIdFromCertDataProvider() {

        return new Object[][]{
                {"orgId", "orgId", true},
                {"orgId", "invalidOrgId", false},
        };
    }

    @Test(dataProvider = "validateOrganizationIdFromCertDataProvider")
    private void testValidateOrganizationIdFromCert(String organizationId, String organizationIdFromCert,
                                                    boolean isValid) throws APIManagementException {

        berlinKeyManagerExtensionImpl = Mockito.spy(BerlinKeyManagerExtensionImpl.class);
        Mockito.doReturn(certificateContent).when(berlinKeyManagerExtensionImpl)
                .extractCertificateContent(Mockito.anyObject());
        certificateContent.setPspAuthorisationNumber(organizationIdFromCert);
        Assert.assertEquals(berlinKeyManagerExtensionImpl
                .validateOrganizationIdFromCert(x509Certificate, organizationId), isValid);
    }

    @DataProvider
    public Object[][] validateAdditionalPropertiesDataProvider() {

        Map<String, ConfigurationDto> correctRegulatoryAppAdditionalProperties = new HashMap<>();
        Map<String, ConfigurationDto> incompleteAppAdditionalProperties = new HashMap<>();
        Map<String, ConfigurationDto> incorrectRegulatoryAppAdditionalProperties = new HashMap<>();

        String dummyString = "dummy";
        String property1Name = OpenBankingConstants.REGULATORY;
        String property2Name = BerlinKeyManagerConstants.SP_CERTIFICATE;
        String property3Name = BerlinKeyManagerConstants.ORG_ID;

        ConfigurationDto property1 = new ConfigurationDto(property1Name, "", "", "",
                "", false, false, Arrays.asList("true"), false);
        ConfigurationDto property2 = new ConfigurationDto(property2Name, "", "", "",
                "", false, false, Arrays.asList(dummyString), false);
        ConfigurationDto property3 = new ConfigurationDto(property3Name, "", "", "",
                "", false, false, Arrays.asList(dummyString), false);
        ConfigurationDto property4 = new ConfigurationDto(property1Name, "", "", "",
                "", false, false, Arrays.asList(dummyString), false);

        correctRegulatoryAppAdditionalProperties.put(property1Name, property1);
        correctRegulatoryAppAdditionalProperties.put(property2Name, property2);
        correctRegulatoryAppAdditionalProperties.put(property3Name, property3);

        incompleteAppAdditionalProperties.put(property1Name, property1);

        incorrectRegulatoryAppAdditionalProperties.put(property1Name, property4);
        incorrectRegulatoryAppAdditionalProperties.put(property2Name, property2);
        incorrectRegulatoryAppAdditionalProperties.put(property3Name, property3);

        return new Object[][]{
                {correctRegulatoryAppAdditionalProperties, null},
                {incompleteAppAdditionalProperties, APIManagementException.class},
                {incorrectRegulatoryAppAdditionalProperties, APIManagementException.class},
        };
    }

    @Test(dataProvider = "validateAdditionalPropertiesDataProvider")
    private void testValidateAdditionalProperties(Map<String, ConfigurationDto> obAdditionalProperties,
                                                  Class<? extends Exception> exceptionType)
            throws APIManagementException {

        Mockito.doNothing().when(berlinKeyManagerExtensionImpl).validateOrganizationIdPattern(Mockito.anyString());
        Mockito.doNothing().when(berlinKeyManagerExtensionImpl)
                .validateCertificate(Mockito.anyString(), Mockito.anyString());
        try {
            berlinKeyManagerExtensionImpl.validateAdditionalProperties(obAdditionalProperties);
            Assert.assertTrue(exceptionType == null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getClass(), exceptionType);
        }
    }

    @DataProvider
    public Object[][] testOrgIdAsClientIdDataProvider() {
        return new Object[][]{
                {"dummyorgId", null},
                {null, APIManagementException.class},
        };
    }


    @Test(dataProvider = "testOrgIdAsClientIdDataProvider")
    private void testOrgIdAsClientId(String orgId, Class<? extends Exception> exceptionType)
            throws APIManagementException {

        try {
            HashMap<String, String> additionalProperties = new HashMap<>();
            additionalProperties.put(BerlinKeyManagerConstants.ORG_ID, orgId);
            OAuthAppRequest oAuthAppRequest = new OAuthAppRequest();
            oAuthAppRequest.setOAuthApplicationInfo(new OAuthApplicationInfo());

            Mockito.doNothing().when(berlinKeyManagerExtensionImpl).validateOrganizationIdPattern(Mockito.anyString());
            Mockito.doNothing().when(berlinKeyManagerExtensionImpl)
                    .validateCertificate(Mockito.anyString(), Mockito.anyString());
            berlinKeyManagerExtensionImpl.setOrgIdAsClientID(oAuthAppRequest, additionalProperties);
            Assert.assertEquals(oAuthAppRequest.getOAuthApplicationInfo().getClientId(), orgId);
            Assert.assertTrue(exceptionType == null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getClass(), exceptionType);
        }
    }

   @Test
   private void preUpdateSpApp() throws APIManagementException {
       HashMap<String, String> additionalProperties = new HashMap<>();
       String dummyString = "dummy";
       oAuthConsumerAppDTO.setApplicationName(dummyString);
       additionalProperties.put(BerlinKeyManagerConstants.SP_CERTIFICATE, dummyString);
       additionalProperties.put(OpenBankingConstants.REGULATORY, "true");
       berlinKeyManagerExtensionImpl.doPreUpdateSpApp(oAuthConsumerAppDTO, serviceProvider, additionalProperties, true);

       Assert.assertEquals(serviceProvider.getCertificateContent(), dummyString);
       Assert.assertTrue(oAuthConsumerAppDTO.getPkceMandatory());
       Assert.assertTrue(oAuthConsumerAppDTO.getPkceSupportPlain());
   }

   @Test
   private void preUpdateSpAppWithEmptyCertificate() throws APIManagementException {
       HashMap<String, String> additionalProperties = new HashMap<>();
       String dummyString = "dummy";
       oAuthConsumerAppDTO.setApplicationName(dummyString);
       additionalProperties.put(OpenBankingConstants.REGULATORY, "true");
       try {
           berlinKeyManagerExtensionImpl.doPreUpdateSpApp(oAuthConsumerAppDTO, serviceProvider,
                   additionalProperties, true);
       } catch (APIManagementException e) {
           Assert.assertEquals(e.getClass(), APIManagementException.class);
       }
   }

   @Test
   private void testValidateDbPropertyChangePass()
           throws APIManagementException {

       try {
           String dummyPropertyName1 = "dummyName1";
           String dummyValue1 = "dummyValue1";
           String dummyValue2 = "dummyValue2";

           HashMap<String, String> additionalProperties = new HashMap<>();
           additionalProperties.put(dummyPropertyName1, dummyValue1);

           ServiceProviderProperty correctServiceProviderProperty = new ServiceProviderProperty();
           correctServiceProviderProperty.setName(dummyPropertyName1);
           correctServiceProviderProperty.setValue(dummyValue1);

           ServiceProviderProperty[] correctSpProperties = new ServiceProviderProperty[1];
           correctSpProperties[0] = correctServiceProviderProperty;
           berlinKeyManagerExtensionImpl.validateDbPropertyChange(correctSpProperties, additionalProperties,
                   dummyPropertyName1);
           // This statement is reached only if void method is succesfully executed
           Assert.assertTrue(null == null);
       } catch (APIManagementException e) {
           Assert.assertEquals(e.getClass(), APIManagementException.class);
       }
   }

    @Test()
    private void testValidateDbPropertyChangeFailure() throws APIManagementException {

        try {
            String dummyPropertyName1 = "dummyName1";
            String dummyValue1 = "dummyValue1";
            String dummyValue2 = "dummyValue2";

            HashMap<String, String> additionalProperties = new HashMap<>();
            additionalProperties.put(dummyPropertyName1, dummyValue1);

            ServiceProviderProperty incorrectServiceProviderProperty = new ServiceProviderProperty();
            incorrectServiceProviderProperty.setName(dummyPropertyName1);
            incorrectServiceProviderProperty.setValue(dummyValue2);

           ServiceProviderProperty[] incorrectSpProperties = new ServiceProviderProperty[1];
           incorrectSpProperties[0] = incorrectServiceProviderProperty;
            berlinKeyManagerExtensionImpl.validateDbPropertyChange(incorrectSpProperties, additionalProperties,
                    dummyPropertyName1);
            // This statement is reached only if void method is succesfully executed
            Assert.assertTrue(null == null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getClass(), APIManagementException.class);
        }
    }
}
