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

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import net.minidev.json.parser.JSONParser;
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
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.HttpMethod;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for Account Initiation Request Handler class.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class AccountInitiationRequestHandlerTests extends PowerMockTestCase {

    private static final String ACCOUNTS_PATH = "consents";

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
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

    @Test
    public void testValidAccountHandleImplicitRedirectInitiation() throws ConsentManagementException, ParseException {

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryInitiationHeadersMap("false");

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(validHeadersMap,
                ACCOUNTS_PATH, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.POST,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        AccountInitiationRequestHandler accountInitiationRequestHandler =
                Mockito.spy(AccountInitiationRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(accountInitiationRequestHandler).getConsentService();
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.anyMap());

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(UUID.randomUUID().toString());
        detailedConsentResource.setCurrentStatus(ConsentStatusEnum.RECEIVED.toString());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID(UUID.randomUUID().toString());
        authorizationResources.add(authorizationResource);

        detailedConsentResource.setAuthorizationResources(authorizationResources);

        doReturn(detailedConsentResource).when(consentCoreServiceMock).createAuthorizableConsent(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

        accountInitiationRequestHandler.handle(accountConsentManageData);
        TestUtil.assertConsentResponse(accountConsentManageData, authorizationResource,
                true, mockHttpServletRequest, mockHttpServletResponse, ConsentTypeEnum.ACCOUNTS);
    }

    @Test
    public void testValidAccountHandleExplicitRedirectInitiation() throws ConsentManagementException, ParseException {

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryInitiationHeadersMap("true");

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(validHeadersMap,
                ACCOUNTS_PATH, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.POST,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        AccountInitiationRequestHandler accountInitiationRequestHandler =
                Mockito.spy(AccountInitiationRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(accountInitiationRequestHandler).getConsentService();
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.anyMap());

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(UUID.randomUUID().toString());
        detailedConsentResource.setCurrentStatus(ConsentStatusEnum.RECEIVED.toString());

        doReturn(detailedConsentResource).when(consentCoreServiceMock).createAuthorizableConsent(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

        accountInitiationRequestHandler.handle(accountConsentManageData);
        TestUtil.assertConsentResponse(accountConsentManageData, null,
                false, mockHttpServletRequest, mockHttpServletResponse, ConsentTypeEnum.ACCOUNTS);
    }

}
