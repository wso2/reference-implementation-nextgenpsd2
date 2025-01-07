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

import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
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
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.AccountConsentUtil;
import org.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestDataProvider;
import org.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import org.wso2.openbanking.berlin.consent.extensions.util.TestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for Berlin Consent Validator class.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class AccountSubmissionValidatorTests extends PowerMockTestCase {

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    AccountSubmissionValidator accountSubmissionValidator = new AccountSubmissionValidator();
    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    String clientId;
    String consentId;
    String futureDate;

    @BeforeMethod
    public void init() {

        futureDate = TestUtil.getCurrentDate(2);
        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);

        accountSubmissionValidator = Mockito.spy(AccountSubmissionValidator.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        doReturn(consentCoreServiceMock).when(accountSubmissionValidator).getConsentService();
    }

    @Test(dataProvider = "BulkAccountsSubmissionTestDataProvider", dataProviderClass = TestDataProvider.class)
    public void testBulkAccountsSubmissionValidatorValidScenarios(String payload, String requestPath)
            throws ParseException {

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);

        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(payload), requestPath,
                consentId, TestConstants.USER_ID, clientId, new HashMap<>());

        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
        detailedConsentResource.setUpdatedTime(AccountConsentUtil
                .convertToUtcTimestamp(TestUtil.getCurrentDate(2)));

        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        accountSubmissionValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertTrue(consentValidationResult.isValid());
    }

    @Test(dataProvider = "SingleAccountSubmissionTestDataProvider", dataProviderClass = TestDataProvider.class)
    public void testSingleAccountSubmissionValidatorWithAccountIdValidation(String payload, String requestPath)
            throws ParseException {

        doReturn(true).when(commonConfigParserMock).isAccountIdValidationEnabled();
        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);

        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(payload), requestPath,
                consentId, TestConstants.USER_ID, clientId, new HashMap<>());

        Map<String, String> resourceParams = new HashMap<>();
        resourceParams.put("ResourcePath", requestPath);
        consentValidateData.setResourceParams(resourceParams);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                authId, AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
        detailedConsentResource.setUpdatedTime(AccountConsentUtil
                .convertToUtcTimestamp(TestUtil.getCurrentDate(2)));
        detailedConsentResource.setConsentMappingResources(TestUtil
                .getSampleTestConsentMappingResourcesWithPermissions(authId));

        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        accountSubmissionValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertTrue(consentValidationResult.isValid());
    }

    @Test(dataProvider = "SingleAccountSubmissionTestDataProvider", dataProviderClass = TestDataProvider.class)
    public void testSingleAccountSubmissionValidatorWithoutAccountIdValidation(String payload, String requestPath)
            throws ParseException {

        doReturn(false).when(commonConfigParserMock).isAccountIdValidationEnabled();
        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);

        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(payload), requestPath,
                consentId, TestConstants.USER_ID, clientId, new HashMap<>());

        Map<String, String> resourceParams = new HashMap<>();
        resourceParams.put("ResourcePath", requestPath);
        consentValidateData.setResourceParams(resourceParams);

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                authId, AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
        detailedConsentResource.setUpdatedTime(AccountConsentUtil
                .convertToUtcTimestamp(TestUtil.getCurrentDate(2)));
        detailedConsentResource.setConsentMappingResources(TestUtil
                .getSampleTestConsentMappingResourcesWithPermissions(authId));

        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        accountSubmissionValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertTrue(consentValidationResult.isValid());
    }

    @Test
    public void testBulkAccountsSubmissionValidatorExpiredScenario() throws ParseException {

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);

        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2), "accounts",
                consentId, TestConstants.USER_ID, clientId, new HashMap<>());

        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
        detailedConsentResource.setValidityPeriod(AccountConsentUtil
                .convertToUtcTimestamp(TestUtil.getCurrentDate(-2)));
        detailedConsentResource.setUpdatedTime(AccountConsentUtil.convertToUtcTimestamp("2021-12-20"));

        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        accountSubmissionValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertFalse(consentValidationResult.isValid());
    }

    @Test
    public void testBulkAccountsSubmissionValidatorInvalidStatusScenario() throws ParseException {

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);

        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2), "accounts",
                consentId, TestConstants.USER_ID, clientId, new HashMap<>());

        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
        detailedConsentResource.setUpdatedTime(AccountConsentUtil.convertToUtcTimestamp("2021-12-20"));

        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        accountSubmissionValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertFalse(consentValidationResult.isValid());
    }

}
