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

package org.wso2.openbanking.berlin.identity.response.type.validators;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({CommonConfigParser.class})
public class BerlinCodeResponseTypeValidatorTests extends PowerMockTestCase {

    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String STATE_PARAMETER = "state";
    MockHttpServletRequest mockHttpServletRequest;
    private static BerlinCodeResponseTypeValidator berlinCodeResponseTypeValidator;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @BeforeClass
    public void initClass() {

        berlinCodeResponseTypeValidator = new BerlinCodeResponseTypeValidator();
    }

    @BeforeMethod
    public void initMethod() {

        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        mockHttpServletRequest = new MockHttpServletRequest();
    }

    @Test
    public void testValidateRequiredParameters() throws OAuthProblemException {

        String consentId = UUID.randomUUID().toString();
        String state = UUID.randomUUID().toString();
        mockHttpServletRequest.setQueryString("scope=pis:" + consentId);

        Map<String, String[]> attributesMap = new HashMap<>();
        String[] codeChallengesArray = {"S256"};
        attributesMap.put(CODE_CHALLENGE_METHOD, codeChallengesArray);
        mockHttpServletRequest.setParameters(attributesMap);

        List<String> supportedCodeChallengesList = new ArrayList<>();
        supportedCodeChallengesList.add("S256");
        doReturn(supportedCodeChallengesList).when(commonConfigParserMock).getSupportedCodeChallengeMethods();

        mockHttpServletRequest.setParameter(STATE_PARAMETER, state);
        mockHttpServletRequest.setParameter("scope", "pis:" + consentId);

        berlinCodeResponseTypeValidator.validateRequiredParameters(mockHttpServletRequest);
    }

    @Test (expectedExceptions = OAuthProblemException.class)
    public void testValidateRequiredParametersWithInvalidQueryParams() throws OAuthProblemException {

        mockHttpServletRequest.setQueryString("invalid_query_string");
        berlinCodeResponseTypeValidator.validateRequiredParameters(mockHttpServletRequest);
    }

    @Test (expectedExceptions = OAuthProblemException.class)
    public void testValidateRequiredParametersWithEmptyScope() throws OAuthProblemException {

        mockHttpServletRequest.setQueryString("scope=");
        berlinCodeResponseTypeValidator.validateRequiredParameters(mockHttpServletRequest);
    }

    @Test (expectedExceptions = OAuthProblemException.class)
    public void testValidateRequiredParametersWithoutState() throws OAuthProblemException {

        String consentId = UUID.randomUUID().toString();
        mockHttpServletRequest.setQueryString("scope=pis:" + consentId);

        Map<String, String[]> attributesMap = new HashMap<>();
        String[] codeChallengesArray = {"S256"};
        attributesMap.put(CODE_CHALLENGE_METHOD, codeChallengesArray);
        mockHttpServletRequest.setParameters(attributesMap);

        List<String> supportedCodeChallengesList = new ArrayList<>();
        supportedCodeChallengesList.add("S256");
        doReturn(supportedCodeChallengesList).when(commonConfigParserMock).getSupportedCodeChallengeMethods();
        mockHttpServletRequest.setParameter("scope", "pis:" + consentId);

        berlinCodeResponseTypeValidator.validateRequiredParameters(mockHttpServletRequest);
    }

    @Test (expectedExceptions = OAuthProblemException.class)
    public void testValidateRequiredParametersWithMismatchingCodeChallenge() throws OAuthProblemException {

        String consentId = UUID.randomUUID().toString();
        String state = UUID.randomUUID().toString();
        mockHttpServletRequest.setQueryString("scope=pis:" + consentId);

        Map<String, String[]> attributesMap = new HashMap<>();
        String[] codeChallengesArray = {"S256"};
        attributesMap.put(CODE_CHALLENGE_METHOD, codeChallengesArray);
        mockHttpServletRequest.setParameters(attributesMap);

        List<String> supportedCodeChallengesList = new ArrayList<>();
        supportedCodeChallengesList.add("PLAIN");
        doReturn(supportedCodeChallengesList).when(commonConfigParserMock).getSupportedCodeChallengeMethods();
        mockHttpServletRequest.setParameter("scope", "pis:" + consentId);

        berlinCodeResponseTypeValidator.validateRequiredParameters(mockHttpServletRequest);
    }

    @Test (expectedExceptions = OAuthProblemException.class)
    public void testValidateRequiredParametersWithoutCodeChallengeMethod() throws OAuthProblemException {

        String consentId = UUID.randomUUID().toString();
        mockHttpServletRequest.setQueryString("scope=pis:" + consentId);

        Map<String, String[]> attributesMap = new HashMap<>();
        mockHttpServletRequest.setParameters(attributesMap);

        List<String> supportedCodeChallengesList = new ArrayList<>();
        supportedCodeChallengesList.add("S256");
        doReturn(supportedCodeChallengesList).when(commonConfigParserMock).getSupportedCodeChallengeMethods();
        mockHttpServletRequest.setParameter("scope", "pis:" + consentId);

        berlinCodeResponseTypeValidator.validateRequiredParameters(mockHttpServletRequest);
    }
}
