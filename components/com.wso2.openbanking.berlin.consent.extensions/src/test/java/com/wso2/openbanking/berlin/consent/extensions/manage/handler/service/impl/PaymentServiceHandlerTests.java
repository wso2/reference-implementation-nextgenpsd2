/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.HttpMethod;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class, CommonConsentUtil.class})
public class PaymentServiceHandlerTests extends PowerMockTestCase {

    private static final String WELL_KNOWN_ENDPOINT = "https://localhost:8243/.well-known/openid-configuration";
    private static final String PAYMENTS_PATH = "payments/sepa-credit-transfers";
    private static final String PERIODIC_PAYMENTS_PATH = "periodic-payments/sepa-credit-transfers";
    private static final String BULK_PAYMENTS_PATH = "bulk-payments/sepa-credit-transfers";

    @Mock
    ConsentManageData consentManageDataMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private PaymentServiceHandler paymentServiceHandler;
    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    List<Map<String, String>> scaMethods;
    List<Map<String, String>> scaApproaches;
    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;
    String clientId;
    String consentId;

    @BeforeClass
    public void initClass() {

        paymentServiceHandler = Mockito.spy(PaymentServiceHandler.class);
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
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        doReturn("v1").when(commonConfigParserMock).getApiVersion(Mockito.anyString());
        doReturn(true).when(commonConfigParserMock).isScaRequired();

        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(paymentServiceHandler).getConsentService();
        PowerMockito.mockStatic(CommonConsentUtil.class);
        PowerMockito.when(CommonConsentUtil.isIdempotent(Mockito.any())).thenReturn(false);

        scaMethods = new ArrayList<>();
        Map<String, String> scaMethod = new HashMap<>();
        scaMethod.put(CommonConstants.SCA_TYPE, "SMS_OTP");
        scaMethod.put(CommonConstants.SCA_VERSION, "1.0");
        scaMethod.put(CommonConstants.SCA_ID, "sms-otp");
        scaMethod.put(CommonConstants.SCA_NAME, "SMS OTP on Mobile");
        scaMethod.put(CommonConstants.SCA_MAPPED_APPROACH, "REDIRECT");
        scaMethod.put(CommonConstants.SCA_DESCRIPTION, "SMS based one time password");
        scaMethod.put(CommonConstants.SCA_DEFAULT, "true");
        scaMethods.add(scaMethod);
        doReturn(scaMethods).when(commonConfigParserMock).getSupportedScaMethods();

        scaApproaches = new ArrayList<>();
        Map<String, String> scaApproach = new HashMap<>();
        scaApproach.put(CommonConstants.SCA_NAME, "REDIRECT");
        scaApproach.put(CommonConstants.SCA_DEFAULT, "true");
        scaApproaches.add(scaApproach);
        doReturn(scaApproaches).when(commonConfigParserMock).getSupportedScaApproaches();
        doReturn(WELL_KNOWN_ENDPOINT).when(commonConfigParserMock).getOauthMetadataEndpoint();
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandlePostWithValidConsentData() {

        doReturn(TestConstants.INVALID_REQUEST_PATH).when(consentManageDataMock).getRequestPath();
        JSONObject samplePayload = new JSONObject();
        samplePayload.put("key", "value");
        doReturn(samplePayload).when(consentManageDataMock).getPayload();
        paymentServiceHandler.handlePost(consentManageDataMock);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidPayload() {

        String invalidJson = "{\"key\":\"value\",}";
        doReturn(invalidJson).when(consentManageDataMock).getPayload();
        paymentServiceHandler.handlePost(consentManageDataMock);
    }

    @Test
    public void testHandlePutWith202ResponseCode() throws ParseException, ConsentManagementException {

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "false");
        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                ConsentExtensionConstants.PAYMENT_CONSENT_UPDATE_PATH, mockHttpServletRequest, mockHttpServletResponse,
                clientId, HttpMethod.PUT,
                TestPayloads.getTestConsentUpdatePayload("202"));
        when(consentCoreServiceMock.updateConsentStatus(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new ConsentResource());
        paymentServiceHandler.handlePut(paymentConsentManageData);
        Assert.assertEquals(paymentConsentManageData.getResponseStatus(), ResponseStatus.OK);
    }

    @Test
    public void testHandlePutWith204ResponseCode() throws ParseException, ConsentManagementException {

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "false");
        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                ConsentExtensionConstants.PAYMENT_CONSENT_UPDATE_PATH, mockHttpServletRequest, mockHttpServletResponse,
                clientId, HttpMethod.PUT,
                TestPayloads.getTestConsentUpdatePayload("204"));
        when(consentCoreServiceMock.updateConsentStatus(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new ConsentResource());
        paymentServiceHandler.handlePut(paymentConsentManageData);
        Assert.assertEquals(paymentConsentManageData.getResponseStatus(), ResponseStatus.OK);
    }

    @Test
    public void testHandlePutWithConsentUpdateError() throws ParseException, ConsentManagementException {

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "false");
        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                ConsentExtensionConstants.PAYMENT_CONSENT_UPDATE_PATH, mockHttpServletRequest, mockHttpServletResponse,
                clientId, HttpMethod.PUT,
                TestPayloads.getTestConsentUpdatePayload("204"));
        when(consentCoreServiceMock.updateConsentStatus(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new ConsentManagementException(Mockito.anyString()));
        paymentServiceHandler.handlePut(paymentConsentManageData);
    }
}
