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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.PermissionEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@PrepareForTest({CommonConfigParser.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class BerlinConsentRetrievalStepTests extends PowerMockTestCase {

    private static BerlinConsentRetrievalStep berlinConsentRetrievalStep;
    private static JSONObject jsonObject;
    private static String consentId;
    private static String authId;
    List<AuthorizationResource> authResourcesList;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @BeforeClass
    public void initClass() {

        berlinConsentRetrievalStep = Mockito.spy(BerlinConsentRetrievalStep.class);
    }

    @BeforeMethod
    public void initMethod()  {

        consentId = UUID.randomUUID().toString();
        authId = UUID.randomUUID().toString();
        jsonObject = new JSONObject();
        authResourcesList = new ArrayList<>();
        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);

        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(berlinConsentRetrievalStep).getConsentService();
    }

    @Test
    public void testGetConsentDataSetForNonRegulatory() {

        ConsentData consentDataMock = Mockito.mock(ConsentData.class);
        Mockito.doReturn(false).when(consentDataMock).isRegulatory();
        berlinConsentRetrievalStep.execute(consentDataMock, jsonObject);
        Assert.assertTrue(jsonObject.isEmpty());
    }

    @Test
    public void testConsentRetrievalWithValidData() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.PAYMENTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidDataBBAN() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD_BBAN))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.PAYMENTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidDataPAN() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD_PAN))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());

        List<String> supportedAccRefTypes = Arrays.asList(ConsentExtensionConstants.IBAN,
                ConsentExtensionConstants.BBAN, ConsentExtensionConstants.MASKED_PAN, ConsentExtensionConstants.PAN);
        doReturn(supportedAccRefTypes).when(commonConfigParserMock).getSupportedAccountReferenceTypes();

        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.PAYMENTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidDataMaskedPan() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD_MASKED_PAN))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.PAYMENTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidDataMsisdn() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD_MSISDN))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());

        List<String> supportedAccRefTypes = Arrays.asList(ConsentExtensionConstants.IBAN,
                ConsentExtensionConstants.BBAN, ConsentExtensionConstants.MASKED_PAN, ConsentExtensionConstants.MSISDN);
        doReturn(supportedAccRefTypes).when(commonConfigParserMock).getSupportedAccountReferenceTypes();

        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.PAYMENTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidDedicatedAccountsData() throws URISyntaxException,
            ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        ConsentResource consentResource = TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_DEDICATED_ACCOUNTS_CONSENT);

        HashMap<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.PERMISSION, PermissionEnum.DEDICATED_ACCOUNTS.toString());
        consentResource.setConsentAttributes(consentAttributes);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.ACCOUNTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidBankOfferedAccountsData() throws URISyntaxException,
            ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        ConsentResource consentResource = TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_BANK_OFFERED_CONSENT);

        HashMap<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.PERMISSION, PermissionEnum.BANK_OFFERED.toString());
        consentResource.setConsentAttributes(consentAttributes);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.ACCOUNTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidAvailableAccountsData() throws URISyntaxException,
            ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        ConsentResource consentResource = TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS);

        HashMap<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.PERMISSION, PermissionEnum.AVAILABLE_ACCOUNTS.toString());
        consentResource.setConsentAttributes(consentAttributes);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.ACCOUNTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidAvailableAccountsWithBalanceData() throws URISyntaxException,
            ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        ConsentResource consentResource = TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                ConsentTypeEnum.ACCOUNTS.toString(),
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS_WITH_BALANCE);

        HashMap<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.PERMISSION,
                PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString());
        consentResource.setConsentAttributes(consentAttributes);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.ACCOUNTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidAllPsd2Data() throws URISyntaxException,
            ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        ConsentResource consentResource = TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        HashMap<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.PERMISSION, PermissionEnum.ALL_PSD2.toString());
        consentResource.setConsentAttributes(consentAttributes);

        doReturn(consentResource).when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.ACCOUNTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidFundsConfirmationData() throws URISyntaxException,
            ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("piis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(ConsentStatusEnum.RECEIVED.toString(),
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.FUNDS_CONFIRMATION.toString());
    }

    @Test
    public void testConsentRetrievalWithValidPeriodicPaymentData() throws URISyntaxException,
            ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.PERIODIC_PAYMENTS.toString());
    }

    @Test
    public void testConsentRetrievalWithValidBulkPaymentData() throws URISyntaxException,
            ConsentManagementException {

        ConsentData consentDataWithoutScopesString =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.BULK_PAYMENTS.toString(), TestPayloads.VALID_BULK_PAYMENTS_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataWithoutScopesString, jsonObject);

        Assert.assertNotNull(jsonObject.get(ConsentExtensionConstants.CONSENT_DATA));
        JSONObject consentData = (JSONObject) jsonObject.get(ConsentExtensionConstants.CONSENT_DATA);
        JSONArray consentDetails = (JSONArray) consentData.get(ConsentExtensionConstants.CONSENT_DETAILS);
        for (Object element : consentDetails) {
            JSONObject jsonElement = (JSONObject) element;
            if (jsonElement.containsKey(ConsentExtensionConstants.DATA_SIMPLE)) {
                Assert.assertNotNull(jsonElement.get(ConsentExtensionConstants.DATA_SIMPLE));
                Assert.assertNotNull(jsonElement.get(ConsentExtensionConstants.TITLE));
                JSONArray data = (JSONArray) jsonElement.get(ConsentExtensionConstants.DATA_SIMPLE);
                Assert.assertNotNull(data);
            }
        }
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentRetrievalWithAuthorizedData() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataObject =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentDataObject.setState(UUID.randomUUID().toString());
        consentDataObject.setRedirectURI(new URI(TestConstants.REDIRECT_URI));
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.PSU_AUTHENTICATED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataObject, jsonObject);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentRetrievalWithEmptyScopesString() throws URISyntaxException {

        ConsentData consentDataObject =
                TestUtil.getSampleRegulatoryConsentDataResource("");
        berlinConsentRetrievalStep.execute(consentDataObject, jsonObject);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentRetrievalWithEmptyConsentIdInScope() throws URISyntaxException {

        ConsentData consentDataObject =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:");
        berlinConsentRetrievalStep.execute(consentDataObject, jsonObject);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentRetrievalError() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataObject =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doThrow(new ConsentManagementException("error")).when(consentCoreServiceMock).getConsent(Mockito.anyString(),
                Mockito.anyBoolean());
        berlinConsentRetrievalStep.execute(consentDataObject, jsonObject);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentRetrievalWithInvalidAuthType() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataObject =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentDataObject.setState(UUID.randomUUID().toString());
        consentDataObject.setRedirectURI(new URI(TestConstants.REDIRECT_URI));
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.ACCP.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.PSU_AUTHENTICATED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataObject, jsonObject);
    }

    @Test
    public void testConsentRetrievalWithMismatchingUser() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataObject =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentDataObject.setState(UUID.randomUUID().toString());
        consentDataObject.setRedirectURI(new URI(TestConstants.REDIRECT_URI));
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.DIFFERENT_USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataObject, jsonObject);
        Assert.assertTrue(jsonObject.containsKey(ConsentExtensionConstants.IS_ERROR));
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testConsentRetrievalWithInvalidConsentStatus() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataObject =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentDataObject.setState(UUID.randomUUID().toString());
        consentDataObject.setRedirectURI(new URI(TestConstants.REDIRECT_URI));
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.ACCP.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataObject, jsonObject);
    }

    @Test
    public void testConsentRetrievalWithFullPayloadData() throws URISyntaxException, ConsentManagementException {

        ConsentData consentDataObject =
                TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        doReturn(TestUtil.getSampleConsentResource(TransactionStatusEnum.RCVD.name(),
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.FULL_VALID_PAYMENTS_PAYLOAD))
                .when(consentCoreServiceMock).getConsent(Mockito.anyString(), Mockito.anyBoolean());
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        authResourcesList.add(TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID));
        doReturn(authResourcesList).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        berlinConsentRetrievalStep.execute(consentDataObject, jsonObject);
        TestUtil.assertConsentRetrieval(jsonObject, ConsentTypeEnum.PAYMENTS.toString());
    }
}
