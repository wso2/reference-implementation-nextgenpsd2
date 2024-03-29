/**
 * Copyright (c) 2021-2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import net.minidev.json.JSONObject;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import javax.ws.rs.HttpMethod;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class, CommonConsentUtil.class})
public class AuthorisationServiceHandlerTests extends PowerMockTestCase {

    private static final String PAYMENTS_PATH = "payments/sepa-credit-transfers";

    @Mock
    ConsentManageData consentManageDataMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private String clientId;
    private AuthorisationServiceHandler authorisationServiceHandler;
    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;

    @BeforeClass
    public void initClass() {

        authorisationServiceHandler = new AuthorisationServiceHandler();
        consentManageDataMock = mock(ConsentManageData.class);
        clientId = UUID.randomUUID().toString();
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();
    }

    @BeforeMethod
    public void initMethod() {
        PowerMockito.mockStatic(CommonConsentUtil.class);
        PowerMockito.when(CommonConsentUtil.isIdempotent(Mockito.any())).thenReturn(false);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePostWithValidConsentData() {

        String invalidRequestPath = "invalid request path";
        doReturn(invalidRequestPath).when(consentManageDataMock).getRequestPath();

        authorisationServiceHandler.handlePost(consentManageDataMock);
    }

    @Test
    public void testHandleGetForAuthorisations() throws ConsentManagementException {

        String consentId = UUID.randomUUID().toString();
        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId + "/authorisations";

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentManageData authorisationConsentManageData = new ConsentManageData(new HashMap<>(), new JSONObject(),
                new HashMap(), paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse);
        authorisationConsentManageData.setClientId(clientId);

        mockHttpServletRequest.setMethod(HttpMethod.GET);
        doReturn(clientId).when(consentManageDataMock).getClientId();

        AuthorisationServiceHandler authorisationServiceHandler = Mockito.spy(AuthorisationServiceHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(authorisationServiceHandler).getConsentService();
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        authorisationServiceHandler.handleGet(authorisationConsentManageData);

        Assert.assertNotNull(authorisationConsentManageData.getResponsePayload());
        Assert.assertEquals(ResponseStatus.OK.toString(),
                authorisationConsentManageData.getResponseStatus().toString());
    }

    @Test
    public void testHandleGetForAuthorisationsForCancellation() throws ConsentManagementException {

        String consentId = UUID.randomUUID().toString();
        String cancellationPath = PAYMENTS_PATH + "/" + consentId + "/cancellation-authorisations";

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), authId,
                        AuthTypeEnum.CANCELLATION.toString(), TestConstants.USER_ID);

        ConsentManageData authorisationConsentManageData = new ConsentManageData(new HashMap<>(), new JSONObject(),
                new HashMap(), cancellationPath, mockHttpServletRequest, mockHttpServletResponse);
        authorisationConsentManageData.setClientId(clientId);

        mockHttpServletRequest.setMethod(HttpMethod.GET);
        doReturn(clientId).when(consentManageDataMock).getClientId();

        AuthorisationServiceHandler authorisationServiceHandler = Mockito.spy(AuthorisationServiceHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(authorisationServiceHandler).getConsentService();
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        authorisationServiceHandler.handleGet(authorisationConsentManageData);

        Assert.assertNotNull(authorisationConsentManageData.getResponsePayload());
        Assert.assertEquals(ResponseStatus.OK.toString(),
                authorisationConsentManageData.getResponseStatus().toString());
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleGetForAuthorisationsGetConsentError() throws ConsentManagementException {

        String consentId = UUID.randomUUID().toString();
        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId + "/authorisations";

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentManageData authorisationConsentManageData = new ConsentManageData(new HashMap<>(), new JSONObject(),
                new HashMap(), paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse);
        authorisationConsentManageData.setClientId(clientId);

        mockHttpServletRequest.setMethod(HttpMethod.GET);
        doReturn(clientId).when(consentManageDataMock).getClientId();

        AuthorisationServiceHandler authorisationServiceHandler = Mockito.spy(AuthorisationServiceHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(authorisationServiceHandler).getConsentService();
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock)
                .getDetailedConsent(Mockito.anyString());
        authorisationServiceHandler.handleGet(authorisationConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleGetForAuthorisationsForConsentWithNullAuthorisations() throws ConsentManagementException {

        String consentId = UUID.randomUUID().toString();
        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId + "/authorisations";

        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), null,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentManageData authorisationConsentManageData = new ConsentManageData(new HashMap<>(), new JSONObject(),
                new HashMap(), paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse);
        authorisationConsentManageData.setClientId(clientId);

        mockHttpServletRequest.setMethod(HttpMethod.GET);
        doReturn(clientId).when(consentManageDataMock).getClientId();

        AuthorisationServiceHandler authorisationServiceHandler = Mockito.spy(AuthorisationServiceHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(authorisationServiceHandler).getConsentService();
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        authorisationServiceHandler.handleGet(authorisationConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleGetForAuthorisationsForConsentWithoutAuthorisations() throws ConsentManagementException {

        String consentId = UUID.randomUUID().toString();
        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId + "/authorisations";

        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), null,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
        detailedConsentResource.setAuthorizationResources(new ArrayList<>());

        ConsentManageData authorisationConsentManageData = new ConsentManageData(new HashMap<>(), new JSONObject(),
                new HashMap(), paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse);
        authorisationConsentManageData.setClientId(clientId);

        mockHttpServletRequest.setMethod(HttpMethod.GET);
        doReturn(clientId).when(consentManageDataMock).getClientId();

        AuthorisationServiceHandler authorisationServiceHandler = Mockito.spy(AuthorisationServiceHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(authorisationServiceHandler).getConsentService();
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        authorisationServiceHandler.handleGet(authorisationConsentManageData);
    }

    @Test
    public void testHandleGetForAuthorisationsStatus() throws ConsentManagementException {

        String authId = UUID.randomUUID().toString();
        String consentId = UUID.randomUUID().toString();
        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId + "/authorisations/" + authId;

        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentManageData authorisationConsentManageData = new ConsentManageData(new HashMap<>(), new JSONObject(),
                new HashMap(), paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse);
        authorisationConsentManageData.setClientId(clientId);

        mockHttpServletRequest.setMethod(HttpMethod.GET);
        doReturn(clientId).when(consentManageDataMock).getClientId();

        AuthorisationServiceHandler authorisationServiceHandler = Mockito.spy(AuthorisationServiceHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(authorisationServiceHandler).getConsentService();
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        authorisationServiceHandler.handleGet(authorisationConsentManageData);

        Assert.assertNotNull(authorisationConsentManageData.getResponsePayload());
        Assert.assertEquals(ResponseStatus.OK.toString(),
                authorisationConsentManageData.getResponseStatus().toString());
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleGetForAuthorisationsStatusWithInvalidAuthId() throws ConsentManagementException {

        String authId = UUID.randomUUID().toString();
        String consentId = UUID.randomUUID().toString();
        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId + "/authorisations/invalid_auth_id";

        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentManageData authorisationConsentManageData = new ConsentManageData(new HashMap<>(), new JSONObject(),
                new HashMap(), paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse);
        authorisationConsentManageData.setClientId(clientId);

        mockHttpServletRequest.setMethod(HttpMethod.GET);
        doReturn(clientId).when(consentManageDataMock).getClientId();

        AuthorisationServiceHandler authorisationServiceHandler = Mockito.spy(AuthorisationServiceHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(authorisationServiceHandler).getConsentService();
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        authorisationServiceHandler.handleGet(authorisationConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleDelete() {

        authorisationServiceHandler.handleDelete(consentManageDataMock);
    }
}
