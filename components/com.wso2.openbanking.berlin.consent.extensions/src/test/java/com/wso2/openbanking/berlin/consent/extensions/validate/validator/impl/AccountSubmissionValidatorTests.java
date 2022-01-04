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

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl;

import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.AccountConsentUtil;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestDataProvider;
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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
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
        detailedConsentResource.setUpdatedTime(AccountConsentUtil.convertToUtcTimestamp("2021-12-20"));

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

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                authId, AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
        detailedConsentResource.setUpdatedTime(AccountConsentUtil.convertToUtcTimestamp("2021-12-20"));
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

        String authId = UUID.randomUUID().toString();
        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                authId, AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);
        detailedConsentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(futureDate));
        detailedConsentResource.setUpdatedTime(AccountConsentUtil.convertToUtcTimestamp("2021-12-20"));
        detailedConsentResource.setConsentMappingResources(TestUtil
                .getSampleTestConsentMappingResourcesWithPermissions(authId));

        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        accountSubmissionValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertTrue(consentValidationResult.isValid());
    }

}
