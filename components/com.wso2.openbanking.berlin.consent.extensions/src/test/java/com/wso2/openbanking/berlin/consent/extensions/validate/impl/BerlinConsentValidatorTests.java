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

package com.wso2.openbanking.berlin.consent.extensions.validate.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.factory.SubmissionValidatorFactory;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * Test class for Berlin Consent Validator class.
 */
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
@PrepareForTest({SubmissionValidatorFactory.class})
public class BerlinConsentValidatorTests extends PowerMockTestCase {

    @Mock
    SubmissionValidator submissionValidator;

    BerlinConsentValidator berlinConsentValidator = new BerlinConsentValidator();
    private final JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    String clientId;
    String consentId;

    @BeforeClass
    public void init() {

        clientId = UUID.randomUUID().toString();
        consentId = UUID.randomUUID().toString();

        submissionValidator = mock(SubmissionValidator.class);
        doNothing().when(submissionValidator).validate(Mockito.anyObject(), Mockito.anyObject());
    }

    @Test
    public void testBerlinConsentValidateValidScenario() throws ParseException {

        PowerMockito.mockStatic(SubmissionValidatorFactory.class);
        PowerMockito.when(SubmissionValidatorFactory.getSubmissionValidator(Mockito.anyString()))
                .thenReturn(submissionValidator);

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);
        TreeMap<String, String> headersTreeMap = TestPayloads.getMandatoryValidateHeadersTreeMap(consentId, true);
        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2), "/accounts",
                consentId, TestConstants.USER_ID, clientId, new HashMap<>(), headersTreeMap);
        consentValidateData.setComprehensiveConsent(TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID));

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        berlinConsentValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertNotNull(consentValidationResult.getConsentInformation()
                .get(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER));
    }

    @Test
    public void testBerlinConsentValidateWithInvalidClientId() throws ParseException {

        PowerMockito.mockStatic(SubmissionValidatorFactory.class);
        PowerMockito.when(SubmissionValidatorFactory.getSubmissionValidator(Mockito.anyString()))
                .thenReturn(submissionValidator);

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);
        TreeMap<String, String> headersTreeMap = TestPayloads.getMandatoryValidateHeadersTreeMap(consentId, true);
        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2), "/accounts",
                consentId, TestConstants.USER_ID, clientId, new HashMap<>(), headersTreeMap);
        consentValidateData.setComprehensiveConsent(TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                UUID.randomUUID().toString(), ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID));

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        berlinConsentValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertEquals(consentValidationResult.getHttpCode(), ResponseStatus.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testBerlinConsentValidateWithInvalidUserId() throws ParseException {

        PowerMockito.mockStatic(SubmissionValidatorFactory.class);
        PowerMockito.when(SubmissionValidatorFactory.getSubmissionValidator(Mockito.anyString()))
                .thenReturn(submissionValidator);

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);
        TreeMap<String, String> headersTreeMap = TestPayloads.getMandatoryValidateHeadersTreeMap(consentId, true);
        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2), "/accounts",
                consentId, TestConstants.USER_ID, clientId, new HashMap<>(), headersTreeMap);
        consentValidateData.setComprehensiveConsent(TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.DIFFERENT_USER_ID));

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        berlinConsentValidator.validate(consentValidateData, consentValidationResult);
        Assert.assertEquals(consentValidationResult.getHttpCode(), ResponseStatus.UNAUTHORIZED.getStatusCode());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testBerlinConsentValidateWithInvalidRequestPath() throws ParseException {

        JSONObject headers = TestPayloads.getMandatoryValidateHeadersMap(consentId, true);
        TreeMap<String, String> headersTreeMap = TestPayloads.getMandatoryValidateHeadersTreeMap(consentId, true);
        ConsentValidateData consentValidateData = new ConsentValidateData(headers,
                (JSONObject) parser.parse(TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2), "abc",
                consentId, TestConstants.USER_ID, clientId, new HashMap<>(), headersTreeMap);
        consentValidateData.setComprehensiveConsent(TestUtil.getSampleDetailedStoredTestConsentResource(consentId,
                clientId, ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.VALID.toString(),
                UUID.randomUUID().toString(), AuthTypeEnum.AUTHORISATION.toString(), TestConstants.USER_ID));

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();

        berlinConsentValidator.validate(consentValidateData, consentValidationResult);
    }

}
