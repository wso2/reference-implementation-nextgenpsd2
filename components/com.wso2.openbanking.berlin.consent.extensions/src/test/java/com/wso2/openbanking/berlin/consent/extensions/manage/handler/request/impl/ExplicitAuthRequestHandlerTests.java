/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestDataProvider;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.HttpMethod;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for Explicit Auth Request Handler class.
 */
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class ExplicitAuthRequestHandlerTests extends PowerMockTestCase {

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;
    String clientId;
    String consentId;

    @BeforeMethod
    public void init() {

        clientId = UUID.randomUUID().toString();
        consentId = UUID.randomUUID().toString();
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();

        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        doReturn("v1").when(commonConfigParserMock).getApiVersion(Mockito.anyString());
        doReturn(true).when(commonConfigParserMock).isScaRequired();
        doReturn(TestUtil.getSampleSupportedScaMethods()).when(commonConfigParserMock).getSupportedScaMethods();
        doReturn(TestUtil.getSampleSupportedScaApproaches()).when(commonConfigParserMock).getSupportedScaApproaches();
        doReturn(TestConstants.WELL_KNOWN_ENDPOINT).when(commonConfigParserMock).getOauthMetadataEndpoint();
    }

    @Test(dataProvider = "HandleValidStartAuthTestDataProvider", dataProviderClass = TestDataProvider.class)
    public void testValidHandleRedirectStartAuthorisation(String path, String consentType, String status,
                                                          String authType, boolean isIdempotent)
            throws ConsentManagementException, ParseException {

        String startAuthPath = String.format(path, consentId);

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryStartAuthHeadersMap();

        ConsentManageData consentManageData = TestUtil.getSampleConsentManageData(validHeadersMap,
                startAuthPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.POST,
                String.valueOf(new JSONObject()));

        ExplicitAuthRequestHandler explicitAuthRequestHandler =
                Mockito.spy(ExplicitAuthRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(explicitAuthRequestHandler).getConsentService();
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.anyMap());

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(consentId);
        detailedConsentResource.setClientID(clientId);
        detailedConsentResource.setConsentType(consentType);
        detailedConsentResource.setCurrentStatus(status);

        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER, "true");
        detailedConsentResource.setConsentAttributes(consentAttributes);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID(UUID.randomUUID().toString());
        authorizationResource.setAuthorizationStatus(ScaStatusEnum.RECEIVED.toString());
        authorizationResource.setConsentID(consentId);
        authorizationResource.setAuthorizationType(authType);

        if (isIdempotent) {
            authorizationResource.setUserID(TestConstants.USER_ID);
            mockHttpServletRequest.addHeader(ConsentExtensionConstants.PSU_ID_HEADER, TestConstants.USER_ID);
        }

        authorizationResources.add(authorizationResource);

        detailedConsentResource.setAuthorizationResources(authorizationResources);

        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(authorizationResource).when(consentCoreServiceMock).createConsentAuthorization(Mockito.anyObject());

        explicitAuthRequestHandler.handle(consentManageData);
        TestUtil.assertStartAuthResponse(consentManageData, authorizationResource, mockHttpServletRequest,
                mockHttpServletResponse);
    }

    @Test(expectedExceptions = ConsentException.class, dataProvider = "HandleInvalidStartAuthTestDataProvider",
            dataProviderClass = TestDataProvider.class)
    public void testInvalidHandleRedirectStartAuthorisation(String path, String consentType, String status,
                                                            String authType, boolean isIdempotent)
            throws ConsentManagementException, ParseException {

        String startAuthPath = String.format(path, consentId);

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryStartAuthHeadersMap();

        ConsentManageData consentManageData = TestUtil.getSampleConsentManageData(validHeadersMap,
                startAuthPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.POST,
                String.valueOf(new JSONObject()));

        ExplicitAuthRequestHandler explicitAuthRequestHandler =
                Mockito.spy(ExplicitAuthRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(explicitAuthRequestHandler).getConsentService();
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.anyMap());

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(consentId);
        detailedConsentResource.setClientID(clientId);
        detailedConsentResource.setConsentType(consentType);
        detailedConsentResource.setCurrentStatus(status);

        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER, "true");
        detailedConsentResource.setConsentAttributes(consentAttributes);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID(UUID.randomUUID().toString());
        authorizationResource.setAuthorizationStatus(ScaStatusEnum.RECEIVED.toString());
        authorizationResource.setConsentID(consentId);
        authorizationResource.setAuthorizationType(authType);

        if (isIdempotent) {
            authorizationResource.setUserID(TestConstants.USER_ID);
            mockHttpServletRequest.addHeader(ConsentExtensionConstants.PSU_ID_HEADER, TestConstants.USER_ID);
        }

        authorizationResources.add(authorizationResource);

        detailedConsentResource.setAuthorizationResources(authorizationResources);

        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(authorizationResource).when(consentCoreServiceMock).createConsentAuthorization(Mockito.anyObject());

        explicitAuthRequestHandler.handle(consentManageData);
    }
}
