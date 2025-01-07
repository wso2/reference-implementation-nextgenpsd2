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

package org.wso2.openbanking.berlin.consent.extensions.validate.validator.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Contains tests related to PaymentConsentValidator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class PaymentConsentValidatorTests extends PowerMockTestCase {

    @Mock
    CommonConfigParser commonConfigParserMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;
    PaymentConsentValidator paymentRetrievalValidator = new PaymentConsentValidator();
    String clientId;
    String consentId;
    String authId;

    @BeforeMethod
    public void init() {

        consentId = UUID.randomUUID().toString();
        authId = UUID.randomUUID().toString();
        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);

        paymentRetrievalValidator = Mockito.spy(PaymentConsentValidator.class);

        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(paymentRetrievalValidator).getConsentService();
    }

    @Test
    public void testPaymentsRetrievalValidatorValidScenarios() throws ConsentManagementException {

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentValidateData consentValidateData = new ConsentValidateData(new JSONObject(), new JSONObject(),
                null, detailedConsentResource.getConsentID(), null, null, new HashMap<>());
        consentValidateData.setComprehensiveConsent(detailedConsentResource);
        Map<String, String> resourceParams = new HashMap<>();
        resourceParams.put("ResourcePath", "/payments/sepa-credit-transfers/" + consentId);
        consentValidateData.setResourceParams(resourceParams);

        doReturn(TestUtil.getSampleConsentResource("testStatus", "payments",
                "testReceipt")).when(consentCoreServiceMock).getConsent(Mockito.anyString(),
                Mockito.anyBoolean());

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        paymentRetrievalValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertTrue(consentValidationResult.isValid());
    }

    @Test
    public void testPaymentsRetrievalValidatorWithInvalidConsentStatus() throws ConsentManagementException {

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.RCVD.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentValidateData consentValidateData = new ConsentValidateData(new JSONObject(), new JSONObject(),
                null, detailedConsentResource.getConsentID(), null, null, new HashMap<>());
        Map<String, String> resourceParams = new HashMap<>();
        resourceParams.put("ResourcePath", "/payments/sepa-credit-transfers/" + consentId);
        consentValidateData.setResourceParams(resourceParams);
        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        doReturn(TestUtil.getSampleConsentResource("testStatus", "payments",
                "testReceipt")).when(consentCoreServiceMock).getConsent(Mockito.anyString(),
                Mockito.anyBoolean());

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        paymentRetrievalValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertFalse(consentValidationResult.isValid());
    }

    @Test
    public void testPaymentsRetrievalValidatorWithInvalidAuthStatus() throws ConsentManagementException {

        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.ACCP.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);

        detailedConsentResource.setAuthorizationResources(authorizationResources);

        ConsentValidateData consentValidateData = new ConsentValidateData(new JSONObject(), new JSONObject(),
                null, detailedConsentResource.getConsentID(), null, null, new HashMap<>());
        consentValidateData.setComprehensiveConsent(detailedConsentResource);
        Map<String, String> resourceParams = new HashMap<>();
        resourceParams.put("ResourcePath", "/payments/sepa-credit-transfers/" + consentId);
        consentValidateData.setResourceParams(resourceParams);

        doReturn(TestUtil.getSampleConsentResource("testStatus", "payments",
                "testReceipt")).when(consentCoreServiceMock).getConsent(Mockito.anyString(),
                Mockito.anyBoolean());

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        paymentRetrievalValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertTrue(consentValidationResult.isValid());
    }
}
