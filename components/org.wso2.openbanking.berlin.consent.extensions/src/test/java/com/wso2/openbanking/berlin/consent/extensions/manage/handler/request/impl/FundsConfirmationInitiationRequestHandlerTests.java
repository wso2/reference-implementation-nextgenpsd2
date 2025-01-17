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

package org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
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
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import org.wso2.openbanking.berlin.consent.extensions.util.TestUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.HttpMethod;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for Funds confirmation Initiation Request Handler class.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class FundsConfirmationInitiationRequestHandlerTests extends PowerMockTestCase {

    private static final String FUNDS_CONFIRMATION_PATH = "consents/confirmation-of-funds";

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
        doReturn("v2").when(commonConfigParserMock).getApiVersion(Mockito.anyString());
        doReturn(true).when(commonConfigParserMock).isScaRequired();
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        doReturn(TestUtil.getSampleSupportedScaMethods()).when(commonConfigParserMock).getSupportedScaMethods();
        doReturn(TestUtil.getSampleSupportedScaApproaches()).when(commonConfigParserMock).getSupportedScaApproaches();
        doReturn(TestConstants.WELL_KNOWN_ENDPOINT).when(commonConfigParserMock).getOauthMetadataEndpoint();
    }

    @Test
    public void testValidFundsConfirmationHandleImplicitRedirectInitiation() throws ConsentManagementException,
            ParseException {

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryInitiationHeadersMap("false");

        ConsentManageData fundsConfirmationConsentManageData = TestUtil.getSampleConsentManageData(validHeadersMap,
                FUNDS_CONFIRMATION_PATH, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.POST,
                TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        FundsConfirmationInitiationRequestHandler fundsConfirmationInitiationRequestHandler =
                Mockito.spy(FundsConfirmationInitiationRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(fundsConfirmationInitiationRequestHandler).getConsentService();
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

        fundsConfirmationInitiationRequestHandler.handle(fundsConfirmationConsentManageData);
        TestUtil.assertConsentResponse(fundsConfirmationConsentManageData, authorizationResource,
                true, mockHttpServletRequest, mockHttpServletResponse, ConsentTypeEnum.FUNDS_CONFIRMATION);
    }

    @Test
    public void testValidFundsConfirmationHandleExplicitRedirectInitiation() throws ConsentManagementException,
            ParseException {

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryInitiationHeadersMap("true");

        ConsentManageData fundsConfirmationConsentManageData = TestUtil.getSampleConsentManageData(validHeadersMap,
                FUNDS_CONFIRMATION_PATH, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.POST,
                TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        FundsConfirmationInitiationRequestHandler fundsConfirmationInitiationRequestHandler =
                Mockito.spy(FundsConfirmationInitiationRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(fundsConfirmationInitiationRequestHandler).getConsentService();
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.anyMap());

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(UUID.randomUUID().toString());
        detailedConsentResource.setCurrentStatus(ConsentStatusEnum.RECEIVED.toString());

        doReturn(detailedConsentResource).when(consentCoreServiceMock).createAuthorizableConsent(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

        fundsConfirmationInitiationRequestHandler.handle(fundsConfirmationConsentManageData);
        TestUtil.assertConsentResponse(fundsConfirmationConsentManageData, null,
                false, mockHttpServletRequest, mockHttpServletResponse, ConsentTypeEnum.ACCOUNTS);
    }
}
