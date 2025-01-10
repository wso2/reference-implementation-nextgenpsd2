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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
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
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import org.wso2.openbanking.berlin.consent.extensions.util.TestUtil;

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
        "org.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil"})
public class PaymentSubmissionBankingIntegrationStepTests extends PowerMockTestCase {

    private static PaymentSubmissionBankingIntegrationStep paymentSubmissionBankingIntegrationStep;
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
        paymentSubmissionBankingIntegrationStep = Mockito.spy(PaymentSubmissionBankingIntegrationStep.class);
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
        doReturn(consentCoreServiceMock).when(paymentSubmissionBankingIntegrationStep).getConsentService();
        consentAuthUtil = PowerMockito.mock(ConsentAuthUtil.class);
        PowerMockito.mockStatic(ConsentAuthUtil.class);
    }

    @Test
    public void testConsentPersistWithValidData() throws URISyntaxException, OpenBankingException, IOException {

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId, clientId);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);

        consentData.setType(ConsentExtensionConstants.PAYMENTS);
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

        paymentSubmissionBankingIntegrationStep.execute(consentPersistData);
    }

    @Test
    public void testConsentPersistWithPaymentSubmissionFailure()
            throws URISyntaxException, OpenBankingException, IOException {

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId, clientId);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);

        consentData.setType(ConsentExtensionConstants.PAYMENTS);
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
            paymentSubmissionBankingIntegrationStep.execute(consentPersistData);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ConsentException);
        }
    }

    @Test
    public void testConsentPersistWithValidData2() throws URISyntaxException, OpenBankingException, IOException {

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId, clientId);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        ConsentData consentData = TestUtil.getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, "pis:" + consentId, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);

        consentData.setType(ConsentExtensionConstants.PAYMENTS);
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
            paymentSubmissionBankingIntegrationStep.execute(consentPersistData);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ConsentException);
        }
    }
}
