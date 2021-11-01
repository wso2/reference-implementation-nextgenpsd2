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

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class BulkPaymentInitiationRequestHandlerTests extends PowerMockTestCase  {

    private static final String WELL_KNOWN_ENDPOINT = "https://localhost:8243/.well-known/openid-configuration";
    private static final String PAYMENTS_PATH = "payments/sepa-credit-transfers";
    private static final String BULK_PAYMENTS_PATH = "bulk-payments/sepa-credit-transfers";
    private static final String PERIODIC_PAYMENTS_PATH = "periodic-payments/sepa-credit-transfers";

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    List<Map<String, String>> scaMethods;
    List<Map<String, String>> scaApproaches;
    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;
    String clientId;

    @BeforeMethod
    public void init() {

        clientId = UUID.randomUUID().toString();
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();

        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        doReturn(ConsentExtensionConstants.IBAN).when(commonConfigParserMock).getAccountReferenceType();
        doReturn("v1").when(commonConfigParserMock).getApiVersion(Mockito.anyString());
        doReturn(true).when(commonConfigParserMock).isScaRequired();
        doReturn(false).when(commonConfigParserMock).isTransactionFeeEnabled();
        doReturn(0).when(commonConfigParserMock).getTransactionFee();
        doReturn(StringUtils.EMPTY).when(commonConfigParserMock).getTransactionFeeCurrency();

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

    @Test (priority = 1)
    public void testHandleForBulkPaymentImplicitRedirectInitiation() throws ConsentManagementException, ParseException {

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryInitiationHeadersMap("false");
        ConsentManageData bulkPaymentConsentManageData = new ConsentManageData(validHeadersMap,
                parser.parse(TestPayloads.VALID_BULK_PAYMENTS_PAYLOAD), new HashMap(),
                BULK_PAYMENTS_PATH, mockHttpServletRequest, mockHttpServletResponse);
        bulkPaymentConsentManageData.setClientId(clientId);

        BulkPaymentInitiationRequestHandler bulkPaymentInitiationRequestHandler =
                Mockito.spy(BulkPaymentInitiationRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(bulkPaymentInitiationRequestHandler).getConsentService();
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.anyMap());

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(UUID.randomUUID().toString());
        detailedConsentResource.setCurrentStatus(TransactionStatusEnum.RCVD.name());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID(UUID.randomUUID().toString());
        authorizationResources.add(authorizationResource);

        detailedConsentResource.setAuthorizationResources(authorizationResources);

        doReturn(detailedConsentResource).when(consentCoreServiceMock).createAuthorizableConsent(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

        bulkPaymentInitiationRequestHandler.handle(bulkPaymentConsentManageData);
        TestUtil.assertImplicitConsentResponse(bulkPaymentConsentManageData, authorizationResource,
                true, mockHttpServletRequest, mockHttpServletResponse);
    }

    @Test (priority = 2)
    public void testHandleBulkPaymentExplicitRedirectInitiationFlow() throws ConsentManagementException,
            ParseException {

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryInitiationHeadersMap("true");
        ConsentManageData bulkPaymentConsentManageData = new ConsentManageData(validHeadersMap,
                parser.parse(TestPayloads.VALID_BULK_PAYMENTS_PAYLOAD), new HashMap(),
                BULK_PAYMENTS_PATH, mockHttpServletRequest, mockHttpServletResponse);
        bulkPaymentConsentManageData.setClientId(clientId);

        BulkPaymentInitiationRequestHandler bulkPaymentInitiationRequestHandler =
                Mockito.spy(BulkPaymentInitiationRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(bulkPaymentInitiationRequestHandler).getConsentService();
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.anyMap());

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(UUID.randomUUID().toString());
        detailedConsentResource.setCurrentStatus(TransactionStatusEnum.RCVD.name());

        doReturn(detailedConsentResource).when(consentCoreServiceMock).createAuthorizableConsent(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

        bulkPaymentInitiationRequestHandler.handle(bulkPaymentConsentManageData);
        TestUtil.assertImplicitConsentResponse(bulkPaymentConsentManageData, null,
                false, mockHttpServletRequest, mockHttpServletResponse);
    }
}
