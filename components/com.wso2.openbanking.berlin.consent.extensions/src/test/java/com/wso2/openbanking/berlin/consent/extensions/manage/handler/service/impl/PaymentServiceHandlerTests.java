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

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import net.minidev.json.JSONArray;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
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
    public void testHandleGetForPayments() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PAYMENTS_PAYLOAD);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.ACCP.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD, consentId, clientId);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        paymentServiceHandler.handleGet(paymentConsentManageData);

        Assert.assertNotNull(paymentConsentManageData.getResponsePayload());
        JSONObject paymentsGetResponse = (JSONObject) paymentConsentManageData.getResponsePayload();
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.CREDITOR_NAME));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.CREDITOR_ACCOUNT));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED));
        Assert.assertEquals(TransactionStatusEnum.ACCP.toString(),
                paymentsGetResponse.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), paymentConsentManageData.getResponseStatus().toString());
    }

    @Test
    public void testHandleGetForPeriodicPayments() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PAYMENTS_PAYLOAD);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.ACCP.toString(),
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD,
                        consentId, clientId);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        paymentServiceHandler.handleGet(paymentConsentManageData);

        Assert.assertNotNull(paymentConsentManageData.getResponsePayload());
        JSONObject paymentsGetResponse = (JSONObject) paymentConsentManageData.getResponsePayload();
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.CREDITOR_NAME));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.CREDITOR_ACCOUNT));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.START_DATE));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.FREQUENCY));
        Assert.assertEquals(TransactionStatusEnum.ACCP.toString(),
                paymentsGetResponse.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), paymentConsentManageData.getResponseStatus().toString());
    }

    @Test
    public void testHandleGetForBulkPayments() throws ConsentManagementException, ParseException {

        String paymentsGetPath = BULK_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PAYMENTS_PAYLOAD);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.ACCP.toString(),
                        ConsentTypeEnum.BULK_PAYMENTS.toString(), TestPayloads.VALID_BULK_PAYMENTS_PAYLOAD,
                        consentId, clientId);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        paymentServiceHandler.handleGet(paymentConsentManageData);

        Assert.assertNotNull(paymentConsentManageData.getResponsePayload());
        JSONObject paymentsGetResponse = (JSONObject) paymentConsentManageData.getResponsePayload();
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));
        Assert.assertNotNull(paymentsGetResponse.get(ConsentExtensionConstants.PAYMENTS));

        JSONArray paymentsArray = (JSONArray) paymentsGetResponse.get(ConsentExtensionConstants.PAYMENTS);
        for (Object payment : paymentsArray) {
            JSONObject paymentJson = (JSONObject) payment;
            Assert.assertNotNull(paymentJson.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT));
            Assert.assertNotNull(paymentJson.get(ConsentExtensionConstants.CREDITOR_ACCOUNT));
            Assert.assertNotNull(paymentJson.get(ConsentExtensionConstants.CREDITOR_NAME));
            Assert.assertNotNull(paymentJson.get(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED));
        }

        Assert.assertEquals(TransactionStatusEnum.ACCP.toString(),
                paymentsGetResponse.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), paymentConsentManageData.getResponseStatus().toString());
    }

    @Test
    public void testHandleGetForPaymentStatus() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId + "/status";

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PAYMENTS_PAYLOAD);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.ACCP.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD,
                        consentId, clientId);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        paymentServiceHandler.handleGet(paymentConsentManageData);

        JSONObject statusResponse = (JSONObject) paymentConsentManageData.getResponsePayload();

        Assert.assertEquals(TransactionStatusEnum.ACCP.toString(),
                statusResponse.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        Assert.assertEquals(ResponseStatus.OK.toString(), paymentConsentManageData.getResponseStatus().toString());
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleGetForPaymentsJsonParseError() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PAYMENTS_PAYLOAD);

        ConsentResource consentResource =
                TestUtil.getSampleConsentResource(TransactionStatusEnum.ACCP.toString(),
                        ConsentTypeEnum.PAYMENTS.toString(), "{\"key\":\"value\"123}",
                        consentId, clientId);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        paymentServiceHandler.handleGet(paymentConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleGetForPaymentsConsentNotFoundError() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PAYMENTS_PAYLOAD);

        doThrow(new ConsentManagementException("Error Message")).when(consentCoreServiceMock)
                .getConsent(Mockito.anyString(), Mockito.anyBoolean());
        paymentServiceHandler.handleGet(paymentConsentManageData);
    }

    @Test
    public void testHandleDeleteForPeriodicPayments() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TransactionStatusEnum.ACCP.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());
        doReturn(false).when(commonConfigParserMock).isAuthorizationRequiredForCancellation();
        paymentServiceHandler.handleDelete(paymentConsentManageData);

        Assert.assertEquals(ResponseStatus.NO_CONTENT.toString(),
                paymentConsentManageData.getResponseStatus().toString());
    }

    @Test
    public void testHandleDeleteForWithoutRedirectHeader() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TransactionStatusEnum.ACCP.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());
        doReturn(false).when(commonConfigParserMock).isAuthorizationRequiredForCancellation();
        paymentServiceHandler.handleDelete(paymentConsentManageData);

        Assert.assertEquals(ResponseStatus.NO_CONTENT.toString(),
                paymentConsentManageData.getResponseStatus().toString());
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleDeleteForAlreadyCancelledPeriodicPayment() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TransactionStatusEnum.CANC.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());
        paymentServiceHandler.handleDelete(paymentConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleDeleteForAlreadyRevokedPeriodicPayment() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TransactionStatusEnum.REVOKED.name(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deactivateAccountMappings(Mockito.any());
        paymentServiceHandler.handleDelete(paymentConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleDeletePaymentsConsent() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PAYMENTS_PAYLOAD);
        paymentServiceHandler.handleDelete(paymentConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleDeleteWithInvalidConsentId() throws ConsentManagementException, ParseException {

        String consentId = "invalid_consent_id";
        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock)
                .getDetailedConsent(Mockito.anyString());
        paymentServiceHandler.handleDelete(paymentConsentManageData);
    }

    @Test
    public void testHandleDeleteForPeriodicPaymentsWithAuthorization() throws ConsentManagementException,
            ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TransactionStatusEnum.ACCP.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentResource sampleUpdatedConsentResource = new ConsentResource();
        sampleUpdatedConsentResource.setCurrentStatus(TransactionStatusEnum.ACTC.toString());

        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doReturn(sampleUpdatedConsentResource).when(consentCoreServiceMock).updateConsentStatus(Mockito.anyString(),
                Mockito.anyString());
        doReturn(true).when(commonConfigParserMock).isAuthorizationRequiredForCancellation();
        paymentServiceHandler.handleDelete(paymentConsentManageData);

        Assert.assertEquals(ResponseStatus.ACCEPTED.toString(),
                paymentConsentManageData.getResponseStatus().toString());
        Assert.assertNotNull(paymentConsentManageData.getResponsePayload());
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleDeleteForBulkPaymentsWithAuthorizationUpdateError() throws ConsentManagementException,
            ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TransactionStatusEnum.ACCP.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ConsentResource sampleUpdatedConsentResource = new ConsentResource();
        sampleUpdatedConsentResource.setCurrentStatus(TransactionStatusEnum.ACTC.toString());

        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock)
                .updateConsentStatus(Mockito.anyString(), Mockito.anyString());
        doReturn(true).when(commonConfigParserMock).isAuthorizationRequiredForCancellation();
        paymentServiceHandler.handleDelete(paymentConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleDeleteForPeriodicPaymentsMappingDeactivationError() throws ConsentManagementException,
            ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TransactionStatusEnum.ACCP.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(true).when(consentCoreServiceMock).revokeConsent(Mockito.anyString(), Mockito.anyString());
        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock)
                .deactivateAccountMappings(Mockito.any());
        doReturn(false).when(commonConfigParserMock).isAuthorizationRequiredForCancellation();
        paymentServiceHandler.handleDelete(paymentConsentManageData);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandleDeleteForPeriodicPaymentsDecoupledError() throws ConsentManagementException, ParseException {

        String paymentsGetPath = PERIODIC_PAYMENTS_PATH + "/" + consentId;

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "false");

        ConsentManageData paymentConsentManageData = TestUtil.getSampleConsentManageData(implicitInitiationHeadersMap,
                paymentsGetPath, mockHttpServletRequest, mockHttpServletResponse, clientId, HttpMethod.GET,
                TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource =
                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TransactionStatusEnum.ACCP.toString(), authId,
                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        doReturn(detailedConsentResource).when(consentCoreServiceMock).getDetailedConsent(Mockito.anyString());
        paymentServiceHandler.handleDelete(paymentConsentManageData);
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
