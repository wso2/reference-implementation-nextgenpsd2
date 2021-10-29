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
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.utils.CommonConstants;
import com.wso2.openbanking.berlin.common.utils.ScaApproachEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import net.minidev.json.JSONObject;
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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for Payment Initiation Request Handler class.
 */
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class PaymentInitiationRequestHandlerTests extends PowerMockTestCase {

    private static final String WELL_KNOWN_ENDPOINT = "https://localhost:8243/.well-known/openid-configuration";
    private static final String PAYMENTS_PATH = "payments/sepa-credit-transfers";
    private static final String BULK_PAYMENTS_PATH = "bulk-payments/sepa-credit-transfers";
    private static final String PERIODIC_PAYMENTS_PATH = "periodic-payments/sepa-credit-transfers";

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    private PaymentInitiationRequestHandler paymentInitiationRequestHandler;
    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    List<Map<String, String>> scaMethods;
    List<Map<String, String>> scaApproaches;
    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;
    ConsentManageData paymentConsentManageData;
    ConsentManageData bulkPaymentConsentManageData;
    ConsentManageData periodicPaymentConsentManageData;
    String clientId;

    @BeforeMethod
    public void init() throws ConsentManagementException {

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

        paymentInitiationRequestHandler = Mockito.spy(PaymentInitiationRequestHandler.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(paymentInitiationRequestHandler).getConsentService();
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.anyMap());
    }

    @Test (priority = 1)
    public void testHandleForImplicitRedirectInitiation() throws ConsentManagementException, ParseException {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(UUID.randomUUID().toString());
        detailedConsentResource.setCurrentStatus(TransactionStatusEnum.RCVD.name());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setAuthorizationID(UUID.randomUUID().toString());
        authorizationResources.add(authorizationResource);

        detailedConsentResource.setAuthorizationResources(authorizationResources);

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryInitiationHeadersMap("false");

        paymentConsentManageData = new ConsentManageData(validHeadersMap,
                parser.parse(TestPayloads.VALID_PAYMENTS_PAYLOAD), new HashMap(),
                PAYMENTS_PATH, mockHttpServletRequest, mockHttpServletResponse);
        paymentConsentManageData.setClientId(clientId);

        bulkPaymentConsentManageData = new ConsentManageData(validHeadersMap,
                parser.parse(TestPayloads.VALID_BULK_PAYMENTS_PAYLOAD), new HashMap(),
                BULK_PAYMENTS_PATH, mockHttpServletRequest, mockHttpServletResponse);
        bulkPaymentConsentManageData.setClientId(clientId);

        periodicPaymentConsentManageData = new ConsentManageData(validHeadersMap,
                parser.parse(TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD), new HashMap(),
                PERIODIC_PAYMENTS_PATH, mockHttpServletRequest, mockHttpServletResponse);
        periodicPaymentConsentManageData.setClientId(clientId);

        doReturn(detailedConsentResource).when(consentCoreServiceMock).createAuthorizableConsent(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

        paymentInitiationRequestHandler.handle(paymentConsentManageData);
        assertImplicitConsentResponse(paymentConsentManageData, authorizationResource, true);

        paymentInitiationRequestHandler.handle(bulkPaymentConsentManageData);
        assertImplicitConsentResponse(bulkPaymentConsentManageData, authorizationResource, true);

        paymentInitiationRequestHandler.handle(periodicPaymentConsentManageData);
        assertImplicitConsentResponse(periodicPaymentConsentManageData, authorizationResource, true);
    }

    @Test (priority = 2)
    public void testHandleExplicitRedirectInitiationFlow() throws ConsentManagementException, ParseException {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(UUID.randomUUID().toString());
        detailedConsentResource.setCurrentStatus(TransactionStatusEnum.RCVD.name());

        Map<String, String> validHeadersMap = TestPayloads.getMandatoryInitiationHeadersMap("true");

        paymentConsentManageData = new ConsentManageData(validHeadersMap,
                parser.parse(TestPayloads.VALID_PAYMENTS_PAYLOAD), new HashMap(),
                PAYMENTS_PATH, mockHttpServletRequest, mockHttpServletResponse);
        paymentConsentManageData.setClientId(clientId);

        bulkPaymentConsentManageData = new ConsentManageData(validHeadersMap,
                parser.parse(TestPayloads.VALID_BULK_PAYMENTS_PAYLOAD), new HashMap(),
                BULK_PAYMENTS_PATH, mockHttpServletRequest, mockHttpServletResponse);
        bulkPaymentConsentManageData.setClientId(clientId);

        periodicPaymentConsentManageData = new ConsentManageData(validHeadersMap,
                parser.parse(TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD), new HashMap(),
                PERIODIC_PAYMENTS_PATH, mockHttpServletRequest, mockHttpServletResponse);
        periodicPaymentConsentManageData.setClientId(clientId);

        doReturn(detailedConsentResource).when(consentCoreServiceMock).createAuthorizableConsent(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

        paymentInitiationRequestHandler.handle(paymentConsentManageData);
        assertImplicitConsentResponse(paymentConsentManageData, null, false);

        paymentInitiationRequestHandler.handle(bulkPaymentConsentManageData);
        assertImplicitConsentResponse(bulkPaymentConsentManageData, null, false);

        paymentInitiationRequestHandler.handle(periodicPaymentConsentManageData);
        assertImplicitConsentResponse(periodicPaymentConsentManageData, null, false);
    }

    private void assertImplicitConsentResponse(ConsentManageData paymentConsentManageData,
                                               AuthorizationResource authorizationResource, boolean isImplicit) {

        Assert.assertNotNull(paymentConsentManageData.getResponsePayload());
        Assert.assertTrue(paymentConsentManageData.getResponsePayload() instanceof JSONObject);

        JSONObject response = (JSONObject) paymentConsentManageData.getResponsePayload();

        Assert.assertEquals(ResponseStatus.CREATED, paymentConsentManageData.getResponseStatus());
        Assert.assertEquals(mockHttpServletResponse.getHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH).toString(),
                ScaApproachEnum.REDIRECT.toString());
        Assert.assertNotNull(mockHttpServletResponse
                .getHeader(ConsentExtensionConstants.LOCATION_PROPER_CASE_HEADER).toString());
        Assert.assertEquals(TransactionStatusEnum.RCVD.name(),
                response.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        Assert.assertNotNull(response.get(ConsentExtensionConstants.PAYMENT_ID));
        Assert.assertNotNull(response.get(ConsentExtensionConstants.LINKS));

        JSONObject linksObject = (JSONObject) response.get(ConsentExtensionConstants.LINKS);

        Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.SELF));

        if (isImplicit) {
            Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.SCA_STATUS));
            // The same authorization ID in scaLink in links object confirms the creation of the authorization resource
            // in implicit flow
            Assert.assertTrue(StringUtils.contains(linksObject.getAsString(ConsentExtensionConstants.SCA_STATUS),
                    authorizationResource.getAuthorizationID()));
        } else {
            Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION));
        }
    }
}
