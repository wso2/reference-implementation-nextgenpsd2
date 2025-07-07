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

package org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
import org.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import org.wso2.openbanking.berlin.consent.extensions.util.TestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.HttpMethod;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class, CommonConsentUtil.class})
public class FundsConfirmationServiceHandlerTests extends PowerMockTestCase  {

    private static final String FUNDS_CONFIRMATION_PATH = "consents/confirmation-of-funds";

    @Mock
    ConsentManageData consentManageDataMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private FundsConfirmationServiceHandler fundsConfirmationServiceHandler;
    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;
    String clientId;
    String consentId;

    @BeforeClass
    public void initClass() {

        fundsConfirmationServiceHandler = Mockito.spy(FundsConfirmationServiceHandler.class);
        consentManageDataMock = mock(ConsentManageData.class);
        clientId = UUID.randomUUID().toString();
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
    }

    @BeforeMethod
    public void initMethod() {

        consentId = UUID.randomUUID().toString();
        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        doReturn("v2").when(commonConfigParserMock).getApiVersion(Mockito.anyString());
        doReturn(true).when(commonConfigParserMock).isScaRequired();

        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(fundsConfirmationServiceHandler).getConsentService();
        PowerMockito.mockStatic(CommonConsentUtil.class);
        PowerMockito.when(CommonConsentUtil.isIdempotent(Mockito.any())).thenReturn(false);

        doReturn(TestUtil.getSampleSupportedScaMethods()).when(commonConfigParserMock).getSupportedScaMethods();
        doReturn(TestUtil.getSampleSupportedScaApproaches()).when(commonConfigParserMock).getSupportedScaApproaches();
        doReturn(TestConstants.WELL_KNOWN_ENDPOINT).when(commonConfigParserMock).getOauthMetadataEndpoint();
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithValidConsentData() {

        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        JSONObject samplePayload = new JSONObject();
        samplePayload.put("key", "value");
        doReturn(samplePayload).when(consentManageDataMock).getPayload();
        fundsConfirmationServiceHandler.handlePost(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidPayload() {

        String invalidJson = "{\"key\":\"value\",}";
        doReturn(invalidJson).when(consentManageDataMock).getPayload();
        fundsConfirmationServiceHandler.handlePost(consentManageDataMock);
    }

    @Test
    public void testHandleGetForFundsConfirmation() throws ConsentManagementException, ParseException {

        String fundsConfirmationGetPath = FUNDS_CONFIRMATION_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData fundsConfirmationConsentManageData = TestUtil.getSampleConsentManageData(
                implicitInitiationHeadersMap, fundsConfirmationGetPath, mockHttpServletRequest, mockHttpServletResponse,
                clientId, HttpMethod.GET, TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                        ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD,
                        consentId, clientId);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        fundsConfirmationServiceHandler.handleGet(fundsConfirmationConsentManageData);

        Assert.assertNotNull(fundsConfirmationConsentManageData.getResponsePayload());
        JSONObject fundsConfirmationGetResponse = (JSONObject) fundsConfirmationConsentManageData.getResponsePayload();
        Assert.assertNotNull(fundsConfirmationGetResponse.get(ConsentExtensionConstants.ACCOUNT));
        Assert.assertNotNull(fundsConfirmationGetResponse.get(ConsentExtensionConstants.CARD_NUMBER));
        Assert.assertNotNull(fundsConfirmationGetResponse.get(ConsentExtensionConstants.CARD_EXPIRY_DATE));
        Assert.assertNotNull(fundsConfirmationGetResponse.get(ConsentExtensionConstants.CARD_INFORMATION));
        Assert.assertEquals(ConsentStatusEnum.RECEIVED.toString(),
                fundsConfirmationGetResponse.get(ConsentExtensionConstants.CONSENT_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), fundsConfirmationConsentManageData.getResponseStatus()
                .toString());
    }

    @Test
    public void testHandleGetForFundsConfirmationStatus() throws ConsentManagementException, ParseException {

        String fundsConfirmationGetPath = FUNDS_CONFIRMATION_PATH + "/" + consentId + "/status";

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData fundsConfirmationConsentManageData = TestUtil
                .getSampleConsentManageData(implicitInitiationHeadersMap, fundsConfirmationGetPath,
                        mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                        TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(ConsentStatusEnum.VALID.toString(),
                        ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD,
                        consentId, clientId);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        fundsConfirmationServiceHandler.handleGet(fundsConfirmationConsentManageData);

        JSONObject statusResponse = (JSONObject) fundsConfirmationConsentManageData.getResponsePayload();

        Assert.assertEquals(ConsentStatusEnum.VALID.toString(),
                statusResponse.get(ConsentExtensionConstants.CONSENT_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), fundsConfirmationConsentManageData.getResponseStatus()
                .toString());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetForFundsConfirmationJsonParseError() throws ConsentManagementException, ParseException {

        String fundsConfirmationGetPath = FUNDS_CONFIRMATION_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData fundsConfirmationConsentManageData = TestUtil
                .getSampleConsentManageData(implicitInitiationHeadersMap, fundsConfirmationGetPath,
                        mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                        TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(ConsentStatusEnum.VALID.toString(),
                        ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), "{\"key\":\"value\"123}",
                        consentId, clientId);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        fundsConfirmationServiceHandler.handleGet(fundsConfirmationConsentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetForFundsConfirmationConsentNotFoundError() throws ConsentManagementException,
            ParseException {

        String fundsConfirmationGetPath = FUNDS_CONFIRMATION_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData fundsConfirmationConsentManageData = TestUtil
                .getSampleConsentManageData(implicitInitiationHeadersMap, fundsConfirmationGetPath,
                        mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                        TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        doThrow(new ConsentManagementException("Error Message")).when(consentCoreServiceMock)
                .getConsent(Mockito.anyString(), Mockito.anyBoolean());
        fundsConfirmationServiceHandler.handleGet(fundsConfirmationConsentManageData);
    }

    @Test
    public void testHandleDeleteForFundsConfirmation() throws ConsentManagementException, ParseException {

        String fundsConfirmationGetPath = FUNDS_CONFIRMATION_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData fundsConfirmationConsentManageData = TestUtil
                .getSampleConsentManageData(implicitInitiationHeadersMap, fundsConfirmationGetPath,
                        mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.DELETE,
                        TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), ConsentStatusEnum.VALID.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());
        fundsConfirmationServiceHandler.handleDelete(fundsConfirmationConsentManageData);

        Assert.assertEquals(ResponseStatus.NO_CONTENT.toString(),
                fundsConfirmationConsentManageData.getResponseStatus().toString());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteForAlreadyDeletedFundsConfirmationConsent() throws ConsentManagementException,
            ParseException {

        String fundsConfirmationGetPath = FUNDS_CONFIRMATION_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData fundsConfirmationConsentManageData = TestUtil
                .getSampleConsentManageData(implicitInitiationHeadersMap, fundsConfirmationGetPath,
                        mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.DELETE,
                        TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                        authId, AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());
        fundsConfirmationServiceHandler.handleDelete(fundsConfirmationConsentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithInvalidConsentId() throws ConsentManagementException, ParseException {

        String consentId = "invalid_consent_id";
        String fundsConfirmationGetPath = FUNDS_CONFIRMATION_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData fundsConfirmationConsentManageData = TestUtil
                .getSampleConsentManageData(implicitInitiationHeadersMap, fundsConfirmationGetPath,
                        mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.DELETE,
                        TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock)
                .getDetailedConsent(Mockito.anyString());
        fundsConfirmationServiceHandler.handleDelete(fundsConfirmationConsentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteForFundsConfirmationMappingDeactivationError() throws ConsentManagementException,
            ParseException {

        String fundsConfirmationGetPath = FUNDS_CONFIRMATION_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData fundsConfirmationConsentManageData = TestUtil
                .getSampleConsentManageData(implicitInitiationHeadersMap, fundsConfirmationGetPath,
                        mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.DELETE,
                        TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), ConsentStatusEnum.VALID.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock)
                .deactivateAccountMappings(Mockito.any());
        fundsConfirmationServiceHandler.handleDelete(fundsConfirmationConsentManageData);
    }
}
