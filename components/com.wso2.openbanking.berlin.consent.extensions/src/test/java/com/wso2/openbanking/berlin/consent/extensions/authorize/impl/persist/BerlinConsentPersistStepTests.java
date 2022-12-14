/**
 * Copyright (c) 2021 - 2022, WSO2 LLC. (https://www.wso2.com/). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.authorize.common.impl.PaymentsStateChangeHook;
import com.wso2.openbanking.berlin.consent.extensions.authorize.factory.AuthorizationHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.PaymentConsentPersistHandler;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@PrepareForTest({CommonConfigParser.class, AuthorizationHandlerFactory.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class BerlinConsentPersistStepTests extends PowerMockTestCase {

    private static BerlinConsentPersistStep berlinConsentPersistStep;
    private static String consentId;
    private static String authId;
    private static String clientId;
    List<AuthorizationResource> authResourcesList;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    AuthorizationHandlerFactory authorizationHandlerFactory;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);
        berlinConsentPersistStep = Mockito.spy(BerlinConsentPersistStep.class);
    }

    @BeforeMethod
    public void initMethod()  {

        authId = UUID.randomUUID().toString();
        clientId = UUID.randomUUID().toString();
        consentId = UUID.randomUUID().toString();
        authId = UUID.randomUUID().toString();
        authResourcesList = new ArrayList<>();
        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);

        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(berlinConsentPersistStep).getConsentService();

        authorizationHandlerFactory = PowerMockito.mock(AuthorizationHandlerFactory.class);
        PowerMockito.mockStatic(AuthorizationHandlerFactory.class);
        PowerMockito.when(AuthorizationHandlerFactory.getConsentService()).thenReturn(consentCoreServiceMock);
    }

    @Test
    public void testConsentPersistWithValidData() throws URISyntaxException, ConsentManagementException {

        PowerMockito.when(AuthorizationHandlerFactory.getConsentPersistHandler(Mockito.anyString()))
                .thenReturn(new PaymentConsentPersistHandler(consentCoreServiceMock));
        PowerMockito.when(AuthorizationHandlerFactory.getAuthorisationStateChangeHook(Mockito.anyString()))
                .thenReturn(new PaymentsStateChangeHook());

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId, clientId);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.FINALISED.toString(), authId,
                TestConstants.USER_ID);

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);

        consentData.setConsentId(consentId);
        consentData.setAuthResource(authorizationResource);
        consentData.setConsentResource(consentResource);
        consentData.addData(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, TestUtil
                .getSampleAccountRefObject("iban", "123", null));
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        doReturn(new AuthorizationResource()).when(consentCoreServiceMock)
                .updateAuthorizationStatus(Mockito.anyString(), Mockito.anyString());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);
        doReturn(authorizationResource).when(consentCoreServiceMock).getAuthorizationResource(Mockito.anyString());
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).bindUserAccountsToConsent(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.anyString());

        berlinConsentPersistStep.execute(consentPersistData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutConsentId() throws URISyntaxException, ConsentManagementException {

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), "{\"key\":\"value\"123}", consentId, clientId);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.PSU_AUTHENTICATED.toString(), authId,
                TestConstants.USER_ID);

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);
        consentData.setAuthResource(authorizationResource);
        consentData.setConsentResource(consentResource);
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);
        berlinConsentPersistStep.execute(consentPersistData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentPersistWithoutConsentData() throws URISyntaxException, ConsentManagementException {

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);
        consentData.setConsentId(consentId);
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);
        berlinConsentPersistStep.execute(consentPersistData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentPersistWithMismatchingConsentIdsData() throws URISyntaxException,
            ConsentManagementException {

        PowerMockito.when(AuthorizationHandlerFactory.getConsentPersistHandler(Mockito.anyString()))
                .thenReturn(new PaymentConsentPersistHandler(consentCoreServiceMock));

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId, clientId);

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID(authId);
        authorizationResource.setAuthorizationType(AuthTypeEnum.AUTHORISATION.toString());
        authorizationResource.setConsentID(UUID.randomUUID().toString());
        authorizationResource.setAuthorizationStatus(ScaStatusEnum.PSU_AUTHENTICATED.toString());

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);
        consentData.setConsentId(consentId);
        consentData.setAuthResource(authorizationResource);
        consentData.setConsentResource(consentResource);
        consentData.addData(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, TestUtil
                .getSampleAccountRefObject("iban", "123", null));
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        doReturn(new AuthorizationResource()).when(consentCoreServiceMock)
                .updateAuthorizationStatus(Mockito.anyString(), Mockito.anyString());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);
        doReturn(authorizationResource).when(consentCoreServiceMock).getAuthorizationResource(Mockito.anyString());
        berlinConsentPersistStep.execute(consentPersistData);
    }

    @Test
    public void testConsentPersistWithFailedAuthData() throws URISyntaxException,
            ConsentManagementException {

        PowerMockito.when(AuthorizationHandlerFactory.getConsentPersistHandler(Mockito.anyString()))
                .thenReturn(new PaymentConsentPersistHandler(consentCoreServiceMock));
        PowerMockito.when(AuthorizationHandlerFactory.getAuthorisationStateChangeHook(Mockito.anyString()))
                .thenReturn(new PaymentsStateChangeHook());

        ConsentResource consentResource = new ConsentResource(clientId, TestPayloads.VALID_PAYMENTS_PAYLOAD,
                ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.RCVD.toString());
        consentResource.setConsentID(consentId);

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID(authId);
        authorizationResource.setAuthorizationType(AuthTypeEnum.AUTHORISATION.toString());
        authorizationResource.setConsentID(consentId);
        authorizationResource.setAuthorizationStatus(ScaStatusEnum.FAILED.toString());

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);
        consentData.setConsentId(consentId);
        consentData.setAuthResource(authorizationResource);
        consentData.setConsentResource(consentResource);
        consentData.addData(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, TestUtil
                .getSampleAccountRefObject("iban", "123", null));
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        doReturn(new AuthorizationResource()).when(consentCoreServiceMock)
                .updateAuthorizationStatus(Mockito.anyString(), Mockito.anyString());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);
        doReturn(authorizationResource).when(consentCoreServiceMock).getAuthorizationResource(Mockito.anyString());
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).bindUserAccountsToConsent(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.anyString());

        berlinConsentPersistStep.execute(consentPersistData);
    }

    @Test
    public void testConsentPersistPartialAuthData() throws URISyntaxException,
            ConsentManagementException {

        PowerMockito.when(AuthorizationHandlerFactory.getConsentPersistHandler(Mockito.anyString()))
                .thenReturn(new PaymentConsentPersistHandler(consentCoreServiceMock));
        PowerMockito.when(AuthorizationHandlerFactory.getAuthorisationStateChangeHook(Mockito.anyString()))
                .thenReturn(new PaymentsStateChangeHook());

        ConsentResource consentResource = new ConsentResource(clientId, TestPayloads.VALID_PAYMENTS_PAYLOAD,
                ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.RCVD.toString());
        consentResource.setConsentID(consentId);

        AuthorizationResource authorizationResource1 = new AuthorizationResource();
        authorizationResource1.setAuthorizationID(authId);
        authorizationResource1.setAuthorizationType(AuthTypeEnum.AUTHORISATION.toString());
        authorizationResource1.setConsentID(consentId);
        authorizationResource1.setAuthorizationStatus(ScaStatusEnum.FINALISED.toString());

        AuthorizationResource authorizationResource2 = new AuthorizationResource();
        authorizationResource2.setAuthorizationID(authId);
        authorizationResource2.setAuthorizationType(AuthTypeEnum.AUTHORISATION.toString());
        authorizationResource2.setConsentID(consentId);
        authorizationResource2.setAuthorizationStatus(ScaStatusEnum.RECEIVED.toString());

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);
        consentData.setConsentId(consentId);
        consentData.setAuthResource(authorizationResource1);
        consentData.setConsentResource(consentResource);
        consentData.addData(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, TestUtil
                .getSampleAccountRefObject("iban", "123", null));
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        doReturn(new AuthorizationResource()).when(consentCoreServiceMock)
                .updateAuthorizationStatus(Mockito.anyString(), Mockito.anyString());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource1);
        authorizationResources.add(authorizationResource2);
        doReturn(authorizationResource1).when(consentCoreServiceMock).getAuthorizationResource(Mockito.anyString());
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).bindUserAccountsToConsent(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.anyString());

        berlinConsentPersistStep.execute(consentPersistData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentPersistEmptyAggregatedStatusData() throws URISyntaxException,
            ConsentManagementException {

        PowerMockito.when(AuthorizationHandlerFactory.getConsentPersistHandler(Mockito.anyString()))
                .thenReturn(new PaymentConsentPersistHandler(consentCoreServiceMock));

        ConsentResource consentResource = new ConsentResource(clientId, TestPayloads.VALID_PAYMENTS_PAYLOAD,
                ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.RCVD.toString());
        consentResource.setConsentID(consentId);

        AuthorizationResource authorizationResource1 = new AuthorizationResource();
        authorizationResource1.setAuthorizationID(authId);
        authorizationResource1.setAuthorizationType(AuthTypeEnum.AUTHORISATION.toString());
        authorizationResource1.setConsentID(consentId);
        authorizationResource1.setAuthorizationStatus(ScaStatusEnum.RECEIVED.toString());

        AuthorizationResource authorizationResource2 = new AuthorizationResource();
        authorizationResource2.setAuthorizationID(authId);
        authorizationResource2.setAuthorizationType(AuthTypeEnum.AUTHORISATION.toString());
        authorizationResource2.setConsentID(consentId);
        authorizationResource2.setAuthorizationStatus(ScaStatusEnum.RECEIVED.toString());

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);
        consentData.setConsentId(consentId);
        consentData.setAuthResource(authorizationResource1);
        consentData.setConsentResource(consentResource);
        consentData.addData(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, TestUtil
                .getSampleAccountRefObject("iban", "123", null));
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        doReturn(new AuthorizationResource()).when(consentCoreServiceMock)
                .updateAuthorizationStatus(Mockito.anyString(), Mockito.anyString());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource1);
        authorizationResources.add(authorizationResource2);
        doReturn(authorizationResource1).when(consentCoreServiceMock).getAuthorizationResource(Mockito.anyString());
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).bindUserAccountsToConsent(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.anyString());

        berlinConsentPersistStep.execute(consentPersistData);
    }
}
