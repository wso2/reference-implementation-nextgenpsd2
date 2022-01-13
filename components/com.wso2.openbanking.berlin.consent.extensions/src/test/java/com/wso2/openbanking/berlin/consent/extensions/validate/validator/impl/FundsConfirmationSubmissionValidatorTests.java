/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

        doReturn(ConsentExtensionConstants.IBAN).when(commonConfigParserMock).getAccountReferenceType();

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
