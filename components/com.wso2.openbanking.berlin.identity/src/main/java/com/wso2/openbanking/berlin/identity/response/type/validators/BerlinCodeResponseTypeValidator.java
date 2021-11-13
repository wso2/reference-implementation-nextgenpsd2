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

import com.wso2.openbanking.accelerator.identity.auth.extensions.response.validator.OBCodeResponseTypeValidator;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;

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

        // Perform super class validation
        super.validateRequiredParameters(request);
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
