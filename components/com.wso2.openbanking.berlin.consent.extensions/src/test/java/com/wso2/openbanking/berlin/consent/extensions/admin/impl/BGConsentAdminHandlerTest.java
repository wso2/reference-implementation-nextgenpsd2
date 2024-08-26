/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.admin.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import net.minidev.json.parser.JSONParser;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Test class for Berlin Consent Admin Handler.
 */
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({CommonConfigParser.class})
public class BGConsentAdminHandlerTest extends PowerMockTestCase {

    BGConsentAdminHandler bgConsentAdminHandler;
    @Mock
    ConsentAdminData consentAdminDataMock;
    @Mock
    CommonConfigParser commonConfigParserMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    @BeforeClass
    public void initTest() {

        bgConsentAdminHandler = Mockito.spy(BGConsentAdminHandler.class);
        consentAdminDataMock = mock(ConsentAdminData.class);
        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(bgConsentAdminHandler).getConsentService();
    }

    @BeforeMethod
    public void initMethod() {

        consentAdminDataMock = mock(ConsentAdminData.class);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeWithoutConsentId() {

        doReturn(new HashMap<>()).when(consentAdminDataMock).getQueryParams();
        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeWithConsentIdInvalidUserId() throws ConsentManagementException {

        String consentId = "fa0aaafc-1427-443d-a8a3-781b005be110";
        Map<String, Object> queryParams = new HashMap<>();
        List<String> consentIDList = new ArrayList<>();
        consentIDList.add(consentId);
        queryParams.put("consentID", consentIDList);
        queryParams.put("userID", "ann@wso2.com");

        doReturn(queryParams).when(consentAdminDataMock).getQueryParams();
        doReturn(getAccountConsentResource("valid")).when(consentCoreServiceMock)
                .getDetailedConsent(Mockito.anyString());

        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeWithPaymentConsentId() throws ConsentManagementException {

        String consentId = "fa0aaafc-1427-443d-a8a3-781b005be110";
        Map<String, Object> queryParams = new HashMap<>();
        List<String> consentIDList = new ArrayList<>();
        consentIDList.add(consentId);
        queryParams.put("consentID", consentIDList);
        DetailedConsentResource paymentConsentResource = getPaymentConsentResource();
        paymentConsentResource.setCurrentStatus("ACCP");

        doReturn(queryParams).when(consentAdminDataMock).getQueryParams();
        doReturn(paymentConsentResource).when(consentCoreServiceMock)
                .getDetailedConsent(Mockito.anyString());

        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeWithCofConsentId() throws ConsentManagementException {

        String consentId = "fa0aaafc-1427-443d-a8a3-781b005be110";
        Map<String, Object> queryParams = new HashMap<>();
        List<String> consentIDList = new ArrayList<>();
        consentIDList.add(consentId);
        queryParams.put("consentID", consentIDList);
        DetailedConsentResource cofConsentResource = getCofConsentResource();
        cofConsentResource.setCurrentStatus("valid");

        doReturn(queryParams).when(consentAdminDataMock).getQueryParams();
        doReturn(cofConsentResource).when(consentCoreServiceMock)
                .getDetailedConsent(Mockito.anyString());

        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeWithAccountAlreadyExpiredConsentId() throws ConsentManagementException {

        String consentId = "fa0aaafc-1427-443d-a8a3-781b005be110";
        Map<String, Object> queryParams = new HashMap<>();
        List<String> consentIDList = new ArrayList<>();
        consentIDList.add(consentId);
        queryParams.put("consentID", consentIDList);
        DetailedConsentResource accountConsentResource = getAccountConsentResource("expired");

        doReturn(queryParams).when(consentAdminDataMock).getQueryParams();
        doReturn(accountConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());

        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeWithAccountAlreadyRevokedConsentId() throws ConsentManagementException {

        String consentId = "fa0aaafc-1427-443d-a8a3-781b005be110";
        Map<String, Object> queryParams = new HashMap<>();
        List<String> consentIDList = new ArrayList<>();
        consentIDList.add(consentId);
        queryParams.put("consentID", consentIDList);
        DetailedConsentResource accountConsentResource = getAccountConsentResource("terminatedByTpp");

        doReturn(queryParams).when(consentAdminDataMock).getQueryParams();
        doReturn(accountConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());

        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeWithAccountAlreadyRevokedByPsuConsentId() throws ConsentManagementException {

        String consentId = "fa0aaafc-1427-443d-a8a3-781b005be110";
        Map<String, Object> queryParams = new HashMap<>();
        List<String> consentIDList = new ArrayList<>();
        consentIDList.add(consentId);
        queryParams.put("consentID", consentIDList);
        DetailedConsentResource accountConsentResource = getAccountConsentResource("revokedByPsu");

        doReturn(queryParams).when(consentAdminDataMock).getQueryParams();
        doReturn(accountConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());

        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeWithAccountValidUserId() throws ConsentManagementException {

        String consentId = "fa0aaafc-1427-443d-a8a3-781b005be110";
        Map<String, Object> queryParams = new HashMap<>();
        List<String> consentIDList = new ArrayList<>();
        consentIDList.add(consentId);
        queryParams.put("consentID", consentIDList);
        queryParams.put("userID", "admin@wso2.com");

        DetailedConsentResource accountConsentResource = getAccountConsentResource("valid");

        doReturn(queryParams).when(consentAdminDataMock).getQueryParams();
        doReturn(accountConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doThrow(ConsentManagementException.class).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());

        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleRevokeAccountWithoutUserId() throws ConsentManagementException {

        String consentId = "fa0aaafc-1427-443d-a8a3-781b005be110";
        Map<String, Object> queryParams = new HashMap<>();
        List<String> consentIDList = new ArrayList<>();
        consentIDList.add(consentId);
        queryParams.put("consentID", consentIDList);

        DetailedConsentResource accountConsentResource = getAccountConsentResource("valid");

        doReturn(queryParams).when(consentAdminDataMock).getQueryParams();
        doReturn(accountConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doThrow(ConsentManagementException.class).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());

        bgConsentAdminHandler.handleRevoke(consentAdminDataMock);
    }

    @NotNull
    private DetailedConsentResource getAccountConsentResource(String consentStatues) {

        DetailedConsentResource consentResource = new DetailedConsentResource();
        consentResource.setConsentType("accounts");

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID("fc03370d-9d8d-4a6b-bf69-dab74fde378f");
        authorizationResource.setUserID("admin@wso2.com");
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID("kc03370d-9d8d-4a6b-bf69-dab74fde378f");
        consentMappingResource.setAccountID("123456");
        consentMappingResource.setMappingStatus("active");
        consentMappingResource.setPermission("balance");
        ArrayList<ConsentMappingResource> mappingResources = new ArrayList<>();
        mappingResources.add(consentMappingResource);

        consentResource.setAuthorizationResources(authorizationResources);
        consentResource.setConsentMappingResources(mappingResources);
        consentResource.setCurrentStatus(consentStatues);
        return consentResource;
    }

    @NotNull
    private DetailedConsentResource getPaymentConsentResource() {

        DetailedConsentResource consentResource = new DetailedConsentResource();
        consentResource.setConsentType("payments");
        return consentResource;
    }

    @NotNull
    private DetailedConsentResource getCofConsentResource() {

        DetailedConsentResource consentResource = new DetailedConsentResource();
        consentResource.setConsentType("funds-confirmations");
        return consentResource;
    }

}
