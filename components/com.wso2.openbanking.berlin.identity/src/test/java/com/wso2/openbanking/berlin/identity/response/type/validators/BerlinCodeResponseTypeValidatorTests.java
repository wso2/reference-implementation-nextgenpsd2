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

package com.wso2.openbanking.berlin.identity.response.type.validators;

import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
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
