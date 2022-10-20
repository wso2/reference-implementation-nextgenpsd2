/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import graphql.Assert;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@PrepareForTest({CommonConfigParser.class, ConsentAuthUtil.class})
@PowerMockIgnore({"jdk.internal.reflect.*",
        "com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil"})
public class PaymentCancellationBankingIntegrationStepTests extends PowerMockTestCase {

    private static PaymentCancellationBankingIntegrationStep paymentCancellationBankingIntegrationStep;
    private static String consentId;
    private static String authId;
    private static String clientId;
    List<AuthorizationResource> authResourcesList;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentAuthUtil consentAuthUtil;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);
        paymentCancellationBankingIntegrationStep = Mockito.spy(PaymentCancellationBankingIntegrationStep.class);
    }

    @BeforeMethod
    public void initMethod() throws OpenBankingException, IOException {

        authId = UUID.randomUUID().toString();
        clientId = UUID.randomUUID().toString();
        consentId = UUID.randomUUID().toString();
        authId = UUID.randomUUID().toString();
        authResourcesList = new ArrayList<>();
        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);

        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(paymentCancellationBankingIntegrationStep).getConsentService();
        consentAuthUtil = PowerMockito.mock(ConsentAuthUtil.class);
        PowerMockito.mockStatic(ConsentAuthUtil.class);
    }

    @Test
    public void testConsentPersistWithValidData() throws URISyntaxException, OpenBankingException, IOException {

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId, clientId);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.CANCELLATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);

        consentData.setType(ConsentExtensionConstants.BULK_PAYMENTS);
        consentData.setConsentId(consentId);
        consentData.setAuthResource(authorizationResource);
        consentData.setConsentResource(consentResource);
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();

        doReturn("sampleBackendUrl")
                .when(commonConfigParserMock).getPaymentsBackendURL();
        PowerMockito.when(ConsentAuthUtil.isPaymentResourceSubmitted(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        PowerMockito.when(ConsentAuthUtil.areAllOtherAuthResourcesValid(Mockito.any(), Mockito.anyString(),
                Mockito.any())).thenReturn(true);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());

        paymentCancellationBankingIntegrationStep.execute(consentPersistData);
    }

    @Test
    public void testConsentPersistWithPaymentSubmissionFailure()
            throws URISyntaxException, OpenBankingException, IOException {

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId,
                        clientId);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.CANCELLATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);

        consentData.setType(ConsentExtensionConstants.PERIODIC_PAYMENTS);
        consentData.setConsentId(consentId);
        consentData.setAuthResource(authorizationResource);
        consentData.setConsentResource(consentResource);
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();

        doReturn("sampleBackendUrl")
                .when(commonConfigParserMock).getPaymentsBackendURL();
        PowerMockito.when(ConsentAuthUtil.isPaymentResourceSubmitted(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(false);
        PowerMockito.when(ConsentAuthUtil.areAllOtherAuthResourcesValid(Mockito.any(), Mockito.anyString(),
                Mockito.any())).thenReturn(true);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());

        try {
            paymentCancellationBankingIntegrationStep.execute(consentPersistData);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ConsentException);
        }
    }

    @Test
    public void testConsentPersistWithValidData2() throws URISyntaxException, OpenBankingException, IOException {

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId,
                        clientId);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.CANCELLATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);

        consentData.setType(ConsentExtensionConstants.PERIODIC_PAYMENTS);
        consentData.setConsentId(consentId);
        consentData.setAuthResource(authorizationResource);
        consentData.setConsentResource(consentResource);
        ConsentPersistData consentPersistData = new ConsentPersistData(new JSONObject(), new HashMap<>(), true,
                consentData);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();

        doReturn("sampleBackendUrl")
                .when(commonConfigParserMock).getPaymentsBackendURL();
        PowerMockito.when(ConsentAuthUtil.isPaymentResourceSubmitted(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(true);
        PowerMockito.when(ConsentAuthUtil.areAllOtherAuthResourcesValid(Mockito.any(), Mockito.anyString(),
                Mockito.any())).thenThrow(ConsentManagementException.class);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());

        try {
            paymentCancellationBankingIntegrationStep.execute(consentPersistData);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ConsentException);
        }
    }
}
