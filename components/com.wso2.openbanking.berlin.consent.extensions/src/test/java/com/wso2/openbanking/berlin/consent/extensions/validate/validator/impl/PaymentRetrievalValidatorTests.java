/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class PaymentRetrievalValidatorTests extends PowerMockTestCase {

    @Mock
    CommonConfigParser commonConfigParserMock;

    PaymentRetrievalValidator paymentRetrievalValidator = new PaymentRetrievalValidator();
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

        paymentRetrievalValidator = Mockito.spy(PaymentRetrievalValidator.class);
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
        consentValidateData.setComprehensiveConsent(detailedConsentResource);
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

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        paymentRetrievalValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertTrue(consentValidationResult.isValid());
    }
}
