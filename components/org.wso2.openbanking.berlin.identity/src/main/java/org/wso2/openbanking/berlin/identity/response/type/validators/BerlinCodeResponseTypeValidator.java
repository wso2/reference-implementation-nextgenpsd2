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

import com.wso2.openbanking.accelerator.identity.auth.extensions.response.validator.OBCodeResponseTypeValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

/**
 * Response type validator for implementation.
 */
public class BerlinCodeResponseTypeValidator extends OBCodeResponseTypeValidator {

    private static final Log log = LogFactory.getLog(BerlinCodeResponseTypeValidator.class);

    private static final String STATE_PARAMETER = "state";
    private static final List<String> BERLIN_QUALIFIER_SCOPES = Arrays.asList("ais", "pis", "piis");
    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String CONSENT_MGT_SCOPE = "consentmgt";

    @Override
    public void validateRequiredParameters(HttpServletRequest request) throws OAuthProblemException {

        log.debug("Berlin request validation triggered for authorisation request");

        if (!StringUtils.contains(request.getQueryString(), "scope")) {
            throw OAuthProblemException
                    .error("invalid_request")
                    .description("Scopes are not present or invalid");
        } else {
            String[] scopesString = request.getQueryString().split("&");
            for (int queryParamIndex = 0; queryParamIndex < scopesString.length; queryParamIndex++) {
                if (StringUtils.contains(scopesString[queryParamIndex], "scope")) {
                    String[] scopes = scopesString[queryParamIndex].split("=");
                    if (scopes.length < 2) {
                        throw OAuthProblemException
                                .error("invalid_request")
                                .description("Scopes are not present or invalid");
                    }
                }
            }
        }

        /*
            Check if request qualifies as an berlin authorisation request.
             NextGenPSD2 XS2A - Implementation Guide V1.3 - Section 13.1
         */
        Optional<String> openIdScopes = Optional.ofNullable(request.getParameter("scope"));

        /* Skip the code challenge method validation only for consentmgt portal request */
        if (openIdScopes.isPresent() && !openIdScopes.get().contains(CONSENT_MGT_SCOPE)) {
            validateCodeChallengeMethod(request);
        }

        // Skip validation if scopes not present
        if (!openIdScopes.isPresent()) {
            log.debug("No scopes present for applicable validation");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Extracted scope string %s", openIdScopes.get()));
        }

        // if berlin qualified scopes are non present, halt validation.
        if (BERLIN_QUALIFIER_SCOPES.stream().noneMatch(partial -> openIdScopes.get().toLowerCase().contains(partial))) {
            log.info("Request doesn't qualify as berlin authorisation flow");
            return;
        }

        /*
            Validate Mandatory State Parameter.
            NextGenPSD2 XS2A - Implementation Guide V1.3 - Section 13.1
         */
        String state = request.getParameter(STATE_PARAMETER);
        log.info("Request qualifies as berlin authorisation flow mandating scope");

        if (StringUtils.isBlank(state)) {
            throw OAuthProblemException
                    .error("invalid_request")
                    .description("'state' parameter is required");
        }
    }

    /**
     * Method to validate code challenge method set in the authorization request.
     *
     * @param request the servlet request
     * @throws OAuthProblemException thrown if validation fails
     */
    private void validateCodeChallengeMethod(HttpServletRequest request) throws OAuthProblemException {

        if (request.getParameterMap().get(CODE_CHALLENGE_METHOD) == null) {
            throw OAuthProblemException
                    .error("PKCE is mandatory for this application. PKCE Challenge is not provided or is not " +
                            "upto RFC 7636 specification.");
        }

        // Always only one code_challenge is sent in an authorization request
        String requestedCodeChallengeMethod = Arrays.asList(request.getParameterMap()
                .get("code_challenge_method")).get(0);

        boolean isMatchingChallengeMethods = false;
        List<String> supportedCodeChallengeMethods =
                CommonConfigParser.getInstance().getSupportedCodeChallengeMethods();;

        if (!supportedCodeChallengeMethods.isEmpty()) {
            for (String challengeMethod : supportedCodeChallengeMethods) {
                if (requestedCodeChallengeMethod.equals(challengeMethod)) {
                    isMatchingChallengeMethods = true;
                }
            }
        } else {
            // If not configured, don't engage this validation (original IS validations will be used)
            return;
        }

        if (!isMatchingChallengeMethods) {
            throw OAuthProblemException
                    .error("invalid_request")
                    .description("Invalid 'code_challenge_method' in request");
        }
    }
}
