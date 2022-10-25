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

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.HttpMethod;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class AccountServiceHandlerTests extends PowerMockTestCase {

    private static final String ACCOUNTS_PATH = "consents";

    @Mock
    ConsentManageData consentManageDataMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private AccountServiceHandler accountServiceHandler;
    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;
    String clientId;
    String consentId;

    @BeforeClass
    public void initClass() {

        accountServiceHandler = Mockito.spy(AccountServiceHandler.class);
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
        doReturn("v1").when(commonConfigParserMock).getApiVersion(Mockito.anyString());
        doReturn(true).when(commonConfigParserMock).isScaRequired();

        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(accountServiceHandler).getConsentService();

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
        accountServiceHandler.handlePost(consentManageDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidPayload() {

        String invalidJson = "{\"key\":\"value\",}";
        doReturn(invalidJson).when(consentManageDataMock).getPayload();
        accountServiceHandler.handlePost(consentManageDataMock);
    }

    @Test
    public void testHandleGetForAccounts() throws ConsentManagementException, ParseException {

        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                        ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2, consentId,
                        clientId);

        consentResource.setValidityPeriod(LocalDate.parse(TestUtil.getCurrentDate(2))
                .atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        consentResource.setUpdatedTime(LocalDate.parse(TestUtil.getCurrentDate(3))
                .atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        accountServiceHandler.handleGet(accountConsentManageData);

        Assert.assertNotNull(accountConsentManageData.getResponsePayload());
        JSONObject accountsGetResponse = (JSONObject) accountConsentManageData.getResponsePayload();
        Assert.assertNotNull(accountsGetResponse.get(ConsentExtensionConstants.ACCESS));
        Assert.assertNotNull(accountsGetResponse.get(ConsentExtensionConstants.LAST_ACTION_DATE));
        Assert.assertNotNull(accountsGetResponse.get(ConsentExtensionConstants.RECURRING_INDICATOR));
        Assert.assertNotNull(accountsGetResponse.get(ConsentExtensionConstants.FREQUENCY_PER_DAY));

        JSONObject links = (JSONObject) accountsGetResponse.get(ConsentExtensionConstants.LINKS);
        Assert.assertNotNull(links.get(ConsentExtensionConstants.ACCOUNT));

        Assert.assertNotNull(accountsGetResponse.get(ConsentExtensionConstants.COMBINED_SERVICE_INDICATOR));
        Assert.assertNotNull(accountsGetResponse.get(ConsentExtensionConstants.VALID_UNTIL));
        Assert.assertEquals(ConsentStatusEnum.RECEIVED.toString(),
                accountsGetResponse.get(ConsentExtensionConstants.CONSENT_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), accountConsentManageData.getResponseStatus().toString());
    }

    @Test
    public void testHandleGetForExpiredAccountConsent() throws ConsentManagementException, ParseException {

        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                        ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2, consentId,
                        clientId);

        consentResource.setValidityPeriod(LocalDate.parse(TestUtil.getCurrentDate(0))
                .atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        consentResource.setUpdatedTime(LocalDate.parse(TestUtil.getCurrentDate(3))
                .atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        consentResource.setRecurringIndicator(true);
        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        consentResource.setCurrentStatus(ConsentStatusEnum.EXPIRED.toString());
        doReturn(consentResource).when(consentCoreServiceMock).updateConsentStatus(Mockito.anyString(),
                Mockito.anyString());
        accountServiceHandler.handleGet(accountConsentManageData);

        Assert.assertNotNull(accountConsentManageData.getResponsePayload());
        JSONObject accountsGetResponse = (JSONObject) accountConsentManageData.getResponsePayload();
        Assert.assertEquals(ConsentStatusEnum.EXPIRED.toString(),
                accountsGetResponse.get(ConsentExtensionConstants.CONSENT_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), accountConsentManageData.getResponseStatus().toString());
    }

    @Test
    public void testHandleGetForAccountStatus() throws ConsentManagementException, ParseException {

        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId + "/status";

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(ConsentStatusEnum.VALID.toString(),
                        ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        consentId, clientId);

        consentResource.setValidityPeriod(LocalDate.parse(TestUtil.getCurrentDate(2))
                .atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        consentResource.setUpdatedTime(LocalDate.parse(TestUtil.getCurrentDate(3))
                .atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        accountServiceHandler.handleGet(accountConsentManageData);

        JSONObject statusResponse = (JSONObject) accountConsentManageData.getResponsePayload();

        Assert.assertEquals(ConsentStatusEnum.VALID.toString(),
                statusResponse.get(ConsentExtensionConstants.CONSENT_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), accountConsentManageData.getResponseStatus().toString());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetForAccountsJsonParseError() throws ConsentManagementException, ParseException {

        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(ConsentStatusEnum.VALID.toString(),
                        ConsentTypeEnum.ACCOUNTS.toString(), "{\"key\":\"value\"123}",
                        consentId, clientId);

        ConsentResource updatedConsentResource =
                TestUtil.getSampleConsentResource(ConsentStatusEnum.EXPIRED.toString(),
                        ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        consentId, clientId);

        doReturn(updatedConsentResource).when(consentCoreServiceMock).updateConsentStatus(Mockito.anyString(),
                Mockito.anyString());
        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        accountServiceHandler.handleGet(accountConsentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGetForPaymentsConsentNotFoundError() throws ConsentManagementException, ParseException {

        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        doThrow(new ConsentManagementException("Error Message")).when(consentCoreServiceMock)
                .getConsent(Mockito.anyString(), Mockito.anyBoolean());
        accountServiceHandler.handleGet(accountConsentManageData);
    }

    @Test
    public void testHandleDeleteForAccounts() throws ConsentManagementException, ParseException {

        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.DELETE,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());
        accountServiceHandler.handleDelete(accountConsentManageData);

        Assert.assertEquals(ResponseStatus.NO_CONTENT.toString(),
                accountConsentManageData.getResponseStatus().toString());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteForAlreadyDeletedAccountConsent() throws ConsentManagementException, ParseException {

        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.DELETE,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.TERMINATED_BY_TPP.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());
        accountServiceHandler.handleDelete(accountConsentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithInvalidConsentId() throws ConsentManagementException, ParseException {

        String consentId = "invalid_consent_id";
        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.DELETE,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock)
                .getDetailedConsent(Mockito.anyString());
        accountServiceHandler.handleDelete(accountConsentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDeleteForAccountsMappingDeactivationError() throws ConsentManagementException,
            ParseException {

        String accountsGetPath = ACCOUNTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData accountConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                accountsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.DELETE,
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock)
                .deactivateAccountMappings(Mockito.any());
        accountServiceHandler.handleDelete(accountConsentManageData);
    }

}
