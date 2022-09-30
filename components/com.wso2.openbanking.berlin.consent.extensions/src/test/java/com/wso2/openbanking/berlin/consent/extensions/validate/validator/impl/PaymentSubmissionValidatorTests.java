///**
// * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
// *
// * This software is the property of WSO2 LLC. and its suppliers, if any.
// * Dissemination of any information or reproduction of any material contained
// * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
// * You may not alter or remove any copyright or other notice from copies of this content.
// */
//
//package com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl;
//
//import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
//import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
//import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
//import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
//import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
//import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
//import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
//import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
//import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
//import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
//import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
//import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
//import net.minidev.json.JSONObject;
//import net.minidev.json.parser.JSONParser;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.testng.PowerMockTestCase;
//import org.testng.Assert;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//import java.util.HashMap;
//import java.util.UUID;
//
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.mock;
//
//@PowerMockIgnore("jdk.internal.reflect.*")
//@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
//public class PaymentSubmissionValidatorTests extends PowerMockTestCase {
//
//    @Mock
//    CommonConfigParser commonConfigParserMock;
//
//    @Mock
//    ConsentCoreServiceImpl consentCoreServiceMock;
//
//    PaymentSubmissionValidator paymentSubmissionValidator = new PaymentSubmissionValidator();
//    String clientId;
//    String consentId;
//    String futureDate;
//
//    @BeforeMethod
//    public void init() {
//
//        consentId = UUID.randomUUID().toString();
//        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
//        PowerMockito.mockStatic(CommonConfigParser.class);
//        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
//
//        paymentSubmissionValidator = Mockito.spy(PaymentSubmissionValidator.class);
//        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
//        doReturn(consentCoreServiceMock).when(paymentSubmissionValidator).getConsentService();
//    }
//
//    @Test
//    public void testPaymentsRetrievalValidatorValidScenarios() throws ConsentManagementException {
//
//        String authId = UUID.randomUUID().toString();
//        DetailedConsentResource detailedConsentResource =
//                TestUtil.getSampleDetailedStoredTestConsentResource(consentId, clientId,
//                        ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.RCVD.toString(), authId,
//                        AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
//
//        ConsentValidateData consentValidateData = new ConsentValidateData(new JSONObject(), new JSONObject(),
//                null, detailedConsentResource.getConsentID(), null, null, new HashMap<>());
//
//        doReturn(detailedConsentResource.getAuthorizationResources()).when(consentCoreServiceMock)
//                .searchAuthorizations(Mockito.anyString());
//
//        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
//
//        paymentSubmissionValidator.validate(consentValidateData, consentValidationResult);
//        Assert.assertTrue(consentValidationResult.isValid());
//    }
//
//    @Test(dataProvider = "SingleAccountSubmissionTestDataProvider", dataProviderClass = TestDataProvider.class)
//    public void testSingleAccountSubmissionValidatorWithAccountIdValidation(String payload, String requestPath)
//            throws ParseException {
//
//        doReturn(true).when(commonConfigParserMock).isAccountIdValidationEnabled();
//        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);
//
//        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
//                (JSONObject) parser.parse(payload), requestPath,
//                consentId, TestConstants.USER_ID, clientId, new HashMap<>());
//
//        Map<String, String> resourceParams = new HashMap<>();
//        resourceParams.put("ResourcePath", requestPath);
//        consentValidateData.setResourceParams(resourceParams);
//
//        String authId = UUID.randomUUID().toString();
//        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
//                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
//                authId, AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
//        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
//        detailedConsentResource.setUpdatedTime(AccountConsentUtil
//                .convertToUtcTimestamp(TestUtil.getCurrentDate(2)));
//        detailedConsentResource.setConsentMappingResources(TestUtil
//                .getSampleTestConsentMappingResourcesWithPermissions(authId));
//
//        consentValidateData.setComprehensiveConsent(detailedConsentResource);
//
//        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
//
//        paymentSubmissionValidator.validate(consentValidateData, consentValidationResult);
//        Assert.assertTrue(consentValidationResult.isValid());
//    }
//
//    @Test(dataProvider = "SingleAccountSubmissionTestDataProvider", dataProviderClass = TestDataProvider.class)
//    public void testSingleAccountSubmissionValidatorWithoutAccountIdValidation(String payload, String requestPath)
//            throws ParseException {
//
//        doReturn(false).when(commonConfigParserMock).isAccountIdValidationEnabled();
//        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);
//
//        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
//                (JSONObject) parser.parse(payload), requestPath,
//                consentId, TestConstants.USER_ID, clientId, new HashMap<>());
//
//        Map<String, String> resourceParams = new HashMap<>();
//        resourceParams.put("ResourcePath", requestPath);
//        consentValidateData.setResourceParams(resourceParams);
//
//        String authId = UUID.randomUUID().toString();
//        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
//                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
//                authId, AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
//        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
//        detailedConsentResource.setUpdatedTime(AccountConsentUtil
//                .convertToUtcTimestamp(TestUtil.getCurrentDate(2)));
//        detailedConsentResource.setConsentMappingResources(TestUtil
//                .getSampleTestConsentMappingResourcesWithPermissions(authId));
//
//        consentValidateData.setComprehensiveConsent(detailedConsentResource);
//
//        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
//
//        paymentSubmissionValidator.validate(consentValidateData, consentValidationResult);
//        Assert.assertTrue(consentValidationResult.isValid());
//    }
//
//    @Test
//    public void testBulkAccountsSubmissionValidatorExpiredScenario() throws ParseException {
//
//        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);
//
//        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
//                (JSONObject) parser.parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2), "accounts",
//                consentId, TestConstants.USER_ID, clientId, new HashMap<>());
//
//        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
//                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
//                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
//        detailedConsentResource.setValidityPeriod(AccountConsentUtil
//                .convertToUtcTimestamp(TestUtil.getCurrentDate(-2)));
//        detailedConsentResource.setUpdatedTime(AccountConsentUtil.convertToUtcTimestamp("2021-12-20"));
//
//        consentValidateData.setComprehensiveConsent(detailedConsentResource);
//
//        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
//
//        paymentSubmissionValidator.validate(consentValidateData, consentValidationResult);
//        Assert.assertFalse(consentValidationResult.isValid());
//    }
//
//    @Test
//    public void testBulkAccountsSubmissionValidatorInvalidStatusScenario() throws ParseException {
//
//        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);
//
//        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
//                (JSONObject) parser.parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2), "accounts",
//                consentId, TestConstants.USER_ID, clientId, new HashMap<>());
//
//        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
//                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
//                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
//        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
//        detailedConsentResource.setUpdatedTime(AccountConsentUtil.convertToUtcTimestamp("2021-12-20"));
//
//        consentValidateData.setComprehensiveConsent(detailedConsentResource);
//
//        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
//
//        paymentSubmissionValidator.validate(consentValidateData, consentValidationResult);
//        Assert.assertFalse(consentValidationResult.isValid());
//    }
//}
