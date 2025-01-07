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

package com.wso2.berlin.test.framework.utility

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.berlin.test.framework.constant.BerlinConstants
import org.testng.Reporter

/**
 * Build OAuth Authorization for Berlin.
 */
class BerlinOAuthAuthorization {

    private static BGConfigurationService bgConfiguration = new BGConfigurationService()

    private CodeVerifier verifier

    private AuthorizationRequest request

    private params = [
            endpoint     : new URI("${bgConfiguration.getAuthorisationServerURL}/oauth2/authorize/"),
            response_type: new ResponseType("code"),
            client_id    : new ClientID(bgConfiguration.getAppInfoClientID()),
            redirect_uri : new URI(bgConfiguration.getAppDCRRedirectUri()),
            state        : new State(UUID.randomUUID().toString())
    ]

    BerlinOAuthAuthorization(BerlinConstants.SCOPES scopes, String consentId) {

        verifier = new CodeVerifier()

        request = new AuthorizationRequest.Builder(params.response_type, params.client_id)
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(params.redirect_uri)
                .scope(new Scope(scopes.getConsentScope(consentId)))
                .codeChallenge(verifier, CodeChallengeMethod.S256)
                .state(params.state)
                .build()
    }


    /**
     * Get Authorize URL.
     * @return
     */
    String getAuthoriseUrl() {

        Reporter.log("Build Redirection url: ${request.toURI()}")
        return request.toURI().toString()

    }

    /**
     * Get Code Verifier.
     * @return
     */
    CodeVerifier getVerifier() {
        return verifier
    }
}
