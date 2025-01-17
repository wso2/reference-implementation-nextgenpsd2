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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
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
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import org.wso2.openbanking.berlin.consent.extensions.util.TestUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;

/**
 * Test class for Berlin Funds Confirmation Validator class.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CommonConfigParser.class, ConsentCoreService.class})
public class FundsConfirmationSubmissionValidatorTests extends PowerMockTestCase {

    private static final String FUNDS_CONFIRMATION_PATH = "funds-confirmations";

    @Mock
    CommonConfigParser commonConfigParserMock;

    FundsConfirmationSubmissionValidator fundsConfirmationSubmissionValidator =
            new FundsConfirmationSubmissionValidator();
    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    String clientId;
    String consentId;

    @BeforeMethod
    public void init() {

        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);

        doReturn(TestConstants.SUPPORTED_ACC_REF_TYPES).when(commonConfigParserMock)
                .getSupportedAccountReferenceTypes();

        fundsConfirmationSubmissionValidator = Mockito.spy(FundsConfirmationSubmissionValidator.class);
    }

    @Test
    public void testValidFundsConfirmationsSubmissionValidatorScenario() throws ParseException {

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);

        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(TestPayloads.VALID_FUNDS_CONFIRMATION_SUBMISSION_PAYLOAD),
                FUNDS_CONFIRMATION_PATH, consentId, TestConstants.USER_ID, clientId, new HashMap<>());

        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), ConsentStatusEnum.VALID.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(TestUtil.getSampleTestConsentMappingResource(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), "DE73459340345034563141:USD",
                ConsentExtensionConstants.DEFAULT_PERMISSION, "active"));
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        fundsConfirmationSubmissionValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertTrue(consentValidationResult.isValid());
    }

    @Test
    public void testInvalidFundsConfirmationsSubmissionValidatorScenario() throws ParseException {

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);

        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(TestPayloads.VALID_FUNDS_CONFIRMATION_SUBMISSION_PAYLOAD),
                FUNDS_CONFIRMATION_PATH, consentId, TestConstants.USER_ID, clientId, new HashMap<>());

        DetailedConsentResource detailedConsentResource = TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID);

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(TestUtil.getSampleTestConsentMappingResource(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), "DE73459340345034563141:USD",
                ConsentExtensionConstants.DEFAULT_PERMISSION, "active"));
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        consentValidateData.setComprehensiveConsent(detailedConsentResource);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        fundsConfirmationSubmissionValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertFalse(consentValidationResult.isValid());
    }

}
