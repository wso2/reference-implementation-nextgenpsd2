/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.keymanager;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
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

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        try {
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
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        commonConfigParser = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
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
        String property1Name = BerlinKeyManagerConstants.REGULATORY;
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
        berlinKeyManagerExtensionImpl.doPreUpdateSpApp(oAuthConsumerAppDTO, serviceProvider, additionalProperties);

        Assert.assertEquals(serviceProvider.getCertificateContent(), dummyString);
        Assert.assertTrue(oAuthConsumerAppDTO.getPkceMandatory());
        Assert.assertTrue(oAuthConsumerAppDTO.getPkceSupportPlain());
    }

    @Test
    private void preUpdateSpAppWithEmptyCertificate() throws APIManagementException {
        HashMap<String, String> additionalProperties = new HashMap<>();
        String dummyString = "dummy";
        oAuthConsumerAppDTO.setApplicationName(dummyString);
        try {
            berlinKeyManagerExtensionImpl.doPreUpdateSpApp(oAuthConsumerAppDTO, serviceProvider, additionalProperties);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getClass(), APIManagementException.class);
        }
    }
}