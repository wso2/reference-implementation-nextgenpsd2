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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.PISAccountListRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.PermissionEnum;
import org.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import org.wso2.openbanking.berlin.consent.extensions.util.TestUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;

@PrepareForTest({CommonConfigParser.class, HttpClients.class, HTTPClientUtils.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class BerlinAccountListRetrievalStepTests extends PowerMockTestCase {

    private static BerlinAccountListRetrievalStep berlinAccountListRetrievalStep;
    private static JSONObject jsonObject;
    private static String consentId;
    private static String clientId;
    private static String authId;
    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    @Mock
    CommonConfigParser commonConfigParserMock;

    @BeforeClass
    public void initClass() {

        berlinAccountListRetrievalStep = Mockito.spy(BerlinAccountListRetrievalStep.class);
    }

    @BeforeMethod
    public void initMethod() {

        consentId = UUID.randomUUID().toString();
        clientId = UUID.randomUUID().toString();
        authId = UUID.randomUUID().toString();
        jsonObject = new JSONObject();
        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();
        doReturn(TestConstants.PAYABLE_ACCOUNT_RETRIEVAL_ENDPOINT).when(commonConfigParserMock)
                .getPayableAccountsRetrieveEndpoint();
        doReturn(TestConstants.SHAREABLE_ACCOUNT_RETRIEVAL_ENDPOINT).when(commonConfigParserMock)
                .getShareableAccountsRetrieveEndpoint();
    }

    @Test
    public void testAccountDataSetForNonRegulatory() {

        ConsentData consentDataMock = Mockito.mock(ConsentData.class);
        Mockito.doReturn(false).when(consentDataMock).isRegulatory();
        berlinAccountListRetrievalStep.execute(consentDataMock, jsonObject);
        Assert.assertTrue(jsonObject.isEmpty());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testAccountDataSetWithNullConsentData() throws URISyntaxException {

        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("");
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);
    }

    // PIISAccountListRetrievalHandler related tests
    @Test
    public void testValidAccountDataSetForFundsConfirmation() throws URISyntaxException, OpenBankingException,
            IOException {

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("piis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSampleFundsConfirmationConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, TestConstants.SINGLE_CURRENCY_ACC_NUMBER,
                        null));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountRefObjects = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
        Assert.assertNotNull(accountRefObjects);
        Assert.assertEquals(accountRefObjects.size(), 1);

        JSONObject accountRefObject = (JSONObject) accountRefObjects.get(0);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.IBAN),
                TestConstants.SINGLE_CURRENCY_ACC_NUMBER);
        Assert.assertFalse(accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY));
    }

    @Test
    public void testValidAccountDataSetForFundsConfirmationMultiCurrencyAccount() throws URISyntaxException,
            OpenBankingException, IOException {

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("piis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSampleFundsConfirmationConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, TestConstants.MULTI_CURRENCY_ACC_NUMBER,
                        "GBP"));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountRefObjects = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
        Assert.assertNotNull(accountRefObjects);
        Assert.assertEquals(accountRefObjects.size(), 1);

        JSONObject accountRefObject = (JSONObject) accountRefObjects.get(0);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.IBAN),
                TestConstants.MULTI_CURRENCY_ACC_NUMBER);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.CURRENCY),
                "GBP");
    }

    @Test
    public void testValidAccountDataSetForDefaultFundsConfirmationMultiCurrencyAccount() throws URISyntaxException,
            OpenBankingException, IOException {

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("piis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSampleFundsConfirmationConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, TestConstants.MULTI_CURRENCY_ACC_NUMBER,
                        null));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountRefObjects = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
        Assert.assertNotNull(accountRefObjects);
        Assert.assertEquals(accountRefObjects.size(), 1);

        JSONObject accountRefObject = (JSONObject) accountRefObjects.get(0);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.IBAN),
                TestConstants.MULTI_CURRENCY_ACC_NUMBER);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.CURRENCY),
                "USD");
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testAccountDataSetForFundsConfirmationWithoutAccounts() throws URISyntaxException,
            OpenBankingException, IOException {

        mockBackend("src/test/resources/empty-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("piis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSampleFundsConfirmationConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, TestConstants.MULTI_CURRENCY_ACC_NUMBER,
                        null));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testGetAccountDataSetForFundsConfirmationWithInvalidAccountId() throws URISyntaxException,
            OpenBankingException, IOException {

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("piis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), TestPayloads.VALID_FUNDS_CONFIRMATION_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSampleFundsConfirmationConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, "DE000000",
                        "GBP"));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);
    }

    // PISAccountListRetrievalHandler related test
    @Test
    public void testValidAccountDataSetForPayments() throws URISyntaxException, OpenBankingException,
            IOException {

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSamplePaymentConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, TestConstants.SINGLE_CURRENCY_ACC_NUMBER,
                        null));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountRefObjects = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
        Assert.assertNotNull(accountRefObjects);
        Assert.assertEquals(accountRefObjects.size(), 1);

        JSONObject accountRefObject = (JSONObject) accountRefObjects.get(0);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.IBAN),
                TestConstants.SINGLE_CURRENCY_ACC_NUMBER);
        Assert.assertFalse(accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY));
    }

    @Test
    public void testValidAccountDataSetForBulkPayments() throws URISyntaxException, OpenBankingException,
            IOException {

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.BULK_PAYMENTS.toString(), TestPayloads.VALID_BULK_PAYMENTS_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSamplePaymentConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, TestConstants.SINGLE_CURRENCY_ACC_NUMBER,
                        null));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountRefObjects = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
        Assert.assertNotNull(accountRefObjects);
        Assert.assertEquals(accountRefObjects.size(), 1);

        JSONObject accountRefObject = (JSONObject) accountRefObjects.get(0);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.IBAN),
                TestConstants.SINGLE_CURRENCY_ACC_NUMBER);
        Assert.assertFalse(accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY));
    }

    @Test
    public void testValidAccountDataSetForPaymentMultiCurrencyAccount() throws URISyntaxException,
            OpenBankingException, IOException {

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSamplePaymentConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, TestConstants.MULTI_CURRENCY_ACC_NUMBER,
                        "GBP"));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountRefObjects = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
        Assert.assertNotNull(accountRefObjects);
        Assert.assertEquals(accountRefObjects.size(), 1);

        JSONObject accountRefObject = (JSONObject) accountRefObjects.get(0);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.IBAN),
                TestConstants.MULTI_CURRENCY_ACC_NUMBER);
        Assert.assertEquals(accountRefObject.getAsString(ConsentExtensionConstants.CURRENCY),
                "GBP");
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testAccountDataSetForPaymentWithoutAccounts() throws URISyntaxException,
            OpenBankingException, IOException {

        mockBackend("src/test/resources/empty-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD,
                consentId, clientId));

        jsonObject = TestUtil.getSamplePaymentConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, TestConstants.MULTI_CURRENCY_ACC_NUMBER,
                        null));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testGetAccountDataSetForPaymentWithInvalidAccountId() throws URISyntaxException,
            OpenBankingException, IOException {

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("pis:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.PAYMENTS.toString(), TestPayloads.VALID_PAYMENTS_PAYLOAD,
                consentId, clientId));
        doReturn(true).when(commonConfigParserMock)
                .isPaymentDebtorAccountCurrencyValidationEnabled();
        jsonObject = TestUtil.getSamplePaymentConsentDataJSONObject(TestUtil
                .getSampleAccountRefObject(ConsentExtensionConstants.IBAN, "DE000000",
                        "GBP"));
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);
    }

    // AISAccountListRetrievalHandler related tests
    @Test
    public void testValidAccountDataSetForDedicatedAccounts() throws URISyntaxException, OpenBankingException,
            IOException, ParseException {

        JSONObject payloadJSON = (JSONObject) parser
                .parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_DEDICATED_ACCOUNTS_CONSENT);

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_DEDICATED_ACCOUNTS_CONSENT,
                consentId, clientId));

        jsonObject = TestUtil.getSampleAccountsConsentDataJSONObject((JSONObject) payloadJSON
                .get(ConsentExtensionConstants.ACCESS), PermissionEnum.DEDICATED_ACCOUNTS.toString());
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountDetailsList = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_DETAILS_LIST);
        Assert.assertNotNull(accountDetailsList);

        JSONObject accountDetailList1 = (JSONObject) accountDetailsList.get(0);
        JSONArray accessMethods1 = new JSONArray().appendElement(AccessMethodEnum.ACCOUNTS.toString())
                .appendElement(AccessMethodEnum.BALANCES.toString());
        Assert.assertTrue(accessMethods1.containsAll(((JSONArray) accountDetailList1
                .get(ConsentExtensionConstants.ACCESS_METHODS))));
        Assert.assertEquals(accountDetailList1
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.STATIC_BALANCE);

        JSONObject accountDetailList2 = (JSONObject) accountDetailsList.get(1);
        JSONArray accessMethods2 = new JSONArray().appendElement(AccessMethodEnum.ACCOUNTS.toString())
                .appendElement(AccessMethodEnum.TRANSACTIONS.toString());
        Assert.assertTrue(accessMethods2.containsAll(((JSONArray) accountDetailList2
                .get(ConsentExtensionConstants.ACCESS_METHODS))));
        Assert.assertEquals(accountDetailList2
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.STATIC_TRANSACTION);

        JSONObject accountDetailList3 = (JSONObject) accountDetailsList.get(2);
        JSONArray accessMethods3 = new JSONArray().appendElement(AccessMethodEnum.ACCOUNTS.toString());
        Assert.assertTrue(accessMethods3.containsAll(((JSONArray) accountDetailList3
                .get(ConsentExtensionConstants.ACCESS_METHODS))));
        Assert.assertEquals(accountDetailList3
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.STATIC_ACCOUNT);
    }

    @Test
    public void testValidAccountDataSetForBankOfferedAccounts() throws URISyntaxException, OpenBankingException,
            IOException, ParseException {

        JSONObject payloadJSON = (JSONObject) parser
                .parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_BANK_OFFERED_CONSENT);

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_BANK_OFFERED_CONSENT,
                consentId, clientId));

        jsonObject = TestUtil.getSampleAccountsConsentDataJSONObject((JSONObject) payloadJSON
                .get(ConsentExtensionConstants.ACCESS), PermissionEnum.BANK_OFFERED.toString());
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountDetailsList = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_DETAILS_LIST);
        Assert.assertNotNull(accountDetailsList);

        JSONObject accountDetailList1 = (JSONObject) accountDetailsList.get(0);
        Assert.assertEquals(accountDetailList1
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.SELECT_BALANCE);

        JSONObject accountDetailList2 = (JSONObject) accountDetailsList.get(1);
        Assert.assertEquals(accountDetailList2
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.SELECT_TRANSACTION);

        JSONObject accountDetailList3 = (JSONObject) accountDetailsList.get(2);
        Assert.assertEquals(accountDetailList3
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.SELECT_ACCOUNT);
    }

    @Test
    public void testValidAccountDataSetForAvailableAccounts() throws URISyntaxException, OpenBankingException,
            IOException, ParseException {

        JSONObject payloadJSON = (JSONObject) parser
                .parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS);

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS,
                consentId, clientId));

        jsonObject = TestUtil.getSampleAccountsConsentDataJSONObject((JSONObject) payloadJSON
                .get(ConsentExtensionConstants.ACCESS), PermissionEnum.AVAILABLE_ACCOUNTS.toString());
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountDetailsList = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_DETAILS_LIST);
        Assert.assertNotNull(accountDetailsList);
        Assert.assertEquals(accountDetailsList.size(), 1);

        JSONObject accountDetailList1 = (JSONObject) accountDetailsList.get(0);
        JSONArray accessMethods1 = new JSONArray().appendElement(AccessMethodEnum.ACCOUNTS.toString());
        Assert.assertTrue(accessMethods1.containsAll(((JSONArray) accountDetailList1
                .get(ConsentExtensionConstants.ACCESS_METHODS))));
        Assert.assertEquals(accountDetailList1
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.STATIC_BULK);
    }

    @Test
    public void testValidAccountDataSetForAvailableAccountsWithBalance() throws URISyntaxException,
            OpenBankingException, IOException, ParseException {

        JSONObject payloadJSON = (JSONObject) parser
                .parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS_WITH_BALANCE);

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.ACCOUNTS.toString(),
                TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS_WITH_BALANCE,
                consentId, clientId));

        jsonObject = TestUtil.getSampleAccountsConsentDataJSONObject((JSONObject) payloadJSON
                .get(ConsentExtensionConstants.ACCESS), PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString());
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountDetailsList = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_DETAILS_LIST);
        Assert.assertNotNull(accountDetailsList);
        Assert.assertEquals(accountDetailsList.size(), 1);

        JSONObject accountDetailList1 = (JSONObject) accountDetailsList.get(0);
        JSONArray accessMethods1 = new JSONArray().appendElement(AccessMethodEnum.ACCOUNTS.toString())
                .appendElement(AccessMethodEnum.BALANCES.toString());
        Assert.assertTrue(accessMethods1.containsAll(((JSONArray) accountDetailList1
                .get(ConsentExtensionConstants.ACCESS_METHODS))));
        Assert.assertEquals(accountDetailList1
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.STATIC_BULK);
    }

    @Test
    public void testValidAccountDataSetForAllPsd2Accounts() throws URISyntaxException, OpenBankingException,
            IOException, ParseException {

        JSONObject payloadJSON = (JSONObject) parser
                .parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2);

        mockBackend("src/test/resources/mock-backend-accounts.json");
        ConsentData consentData = TestUtil.getSampleRegulatoryConsentDataResource("ais:" + consentId);
        consentData.setConsentResource(TestUtil.getSampleConsentResource(null,
                ConsentTypeEnum.ACCOUNTS.toString(), TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                consentId, clientId));

        jsonObject = TestUtil.getSampleAccountsConsentDataJSONObject((JSONObject) payloadJSON
                .get(ConsentExtensionConstants.ACCESS), PermissionEnum.ALL_PSD2.toString());
        berlinAccountListRetrievalStep.execute(consentData, jsonObject);

        JSONObject accountData = (JSONObject) jsonObject.get(ConsentExtensionConstants.ACCOUNT_DATA);
        Assert.assertNotNull(accountData);

        JSONArray accountDetailsList = (JSONArray) accountData.get(ConsentExtensionConstants.ACCOUNT_DETAILS_LIST);
        Assert.assertNotNull(accountDetailsList);
        Assert.assertEquals(accountDetailsList.size(), 1);

        JSONObject accountDetailList1 = (JSONObject) accountDetailsList.get(0);
        JSONArray accessMethods1 = new JSONArray().appendElement(AccessMethodEnum.ACCOUNTS.toString())
                .appendElement(AccessMethodEnum.BALANCES.toString())
                .appendElement(AccessMethodEnum.TRANSACTIONS.toString());
        Assert.assertTrue(accessMethods1.containsAll(((JSONArray) accountDetailList1
                .get(ConsentExtensionConstants.ACCESS_METHODS))));
        Assert.assertEquals(accountDetailList1
                .getAsString(ConsentExtensionConstants.ACCOUNT_TYPE), ConsentExtensionConstants.STATIC_BULK);
    }

    @Test
    public void testValidateAccountRefObjectWithValidData() throws Exception {

        JSONObject accountRefObject = new JSONObject();
        accountRefObject.put("iban", "DE12345678901234567890");
        accountRefObject.put("currency", "EUR");

        JSONObject account1 = new JSONObject();
        account1.put("isDefault", true);
        account1.put("iban", "DE12345678901234567890");
        account1.put("currency", "USD");

        JSONObject account2 = new JSONObject();
        account2.put("isDefault", true);
        account2.put("iban", "DE12345678901234567890");
        account2.put("currency", "EUR");

        JSONArray accountArray = new JSONArray();
        accountArray.appendElement(account1);
        accountArray.appendElement(account2);

        doReturn(true).when(commonConfigParserMock).isPaymentDebtorAccountCurrencyValidationEnabled();
        Assert.assertTrue(WhiteboxImpl.invokeMethod(new PISAccountListRetrievalHandler(),
                "getValidatedAccountRefObject", accountRefObject, accountArray) instanceof JSONObject);
        Assert.assertEquals(((JSONObject) WhiteboxImpl.invokeMethod(new PISAccountListRetrievalHandler(),
                "getValidatedAccountRefObject", accountRefObject, accountArray))
                .get(ConsentExtensionConstants.CURRENCY), accountRefObject.get(ConsentExtensionConstants.CURRENCY));
    }

    @Test
    public void testValidateAccountRefObjectWithSingleAccountData() throws Exception {

        JSONObject accountRefObject = new JSONObject();
        accountRefObject.put("iban", "DE12345678901234567890");
        accountRefObject.put("currency", "EUR");

        JSONObject account2 = new JSONObject();
        account2.put("isDefault", true);
        account2.put("iban", "DE12345678901234567890");
        account2.put("currency", "EUR");

        JSONArray accountArray = new JSONArray();
        accountArray.appendElement(account2);

        doReturn(true).when(commonConfigParserMock).isPaymentDebtorAccountCurrencyValidationEnabled();
        Assert.assertNull(WhiteboxImpl.invokeMethod(new PISAccountListRetrievalHandler(),
                "getValidatedAccountRefObject", accountRefObject, accountArray));
    }

    @Test
    public void testValidateAccountRefObjectWithSingleAccountDataOfMismatchingCurrency() throws Exception {

        JSONObject accountRefObject = new JSONObject();
        accountRefObject.put("iban", "DE12345678901234567890");

        JSONObject account2 = new JSONObject();
        account2.put("isDefault", true);
        account2.put("iban", "DE12345678901234567890");
        account2.put("currency", "USD");

        JSONArray accountArray = new JSONArray();
        accountArray.appendElement(account2);

        doReturn(true).when(commonConfigParserMock).isPaymentDebtorAccountCurrencyValidationEnabled();
        Assert.assertTrue(WhiteboxImpl.invokeMethod(new PISAccountListRetrievalHandler(),
                "getValidatedAccountRefObject", accountRefObject, accountArray) instanceof JSONObject);
        Assert.assertTrue(((JSONObject) WhiteboxImpl.invokeMethod(new PISAccountListRetrievalHandler(),
                "getValidatedAccountRefObject", accountRefObject, accountArray))
                .containsValue("DE12345678901234567890"));
    }

    @Test
    public void testValidateAccountRefObjectWithoutCurrencyData() throws Exception {

        JSONObject accountRefObject = new JSONObject();
        accountRefObject.put("iban", "DE12345678901234567890");

        JSONObject account1 = new JSONObject();
        account1.put("isDefault", true);
        account1.put("iban", "DE12345678901234567890");
        account1.put("currency", "USD");

        JSONObject account2 = new JSONObject();
        account2.put("isDefault", true);
        account2.put("iban", "DE12345678901234567890");
        account2.put("currency", "EUR");

        JSONArray accountArray = new JSONArray();
        accountArray.appendElement(account1);
        accountArray.appendElement(account2);

        doReturn(true).when(commonConfigParserMock).isPaymentDebtorAccountCurrencyValidationEnabled();
        Assert.assertNull(WhiteboxImpl.invokeMethod(new PISAccountListRetrievalHandler(),
                "getValidatedAccountRefObject", accountRefObject, accountArray));
    }

    private void mockBackend(String accountsFilePath) throws IOException, OpenBankingException {

        // Mocking the mock backend
        StatusLine statusLineMock = Mockito.mock(StatusLine.class);
        Mockito.doReturn(HttpStatus.SC_OK).when(statusLineMock).getStatusCode();

        File file = new File(accountsFilePath);
        byte[] crlBytes = FileUtils.readFileToString(file, String.valueOf(StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
        InputStream inStream = new ByteArrayInputStream(crlBytes);

        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(statusLineMock).when(httpResponseMock).getStatusLine();
        Mockito.doReturn(httpEntityMock).when(httpResponseMock).getEntity();

        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doReturn(httpResponseMock).when(closeableHttpClientMock).execute(Mockito.any(HttpGet.class));

        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);
    }

}
