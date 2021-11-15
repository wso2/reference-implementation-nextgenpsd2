/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.common.utils

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import com.wso2.openbanking.berlin.common.utils.BerlinConstants.SCOPES
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
import org.testng.Reporter

/**
 * Build OAuth Authorization for Berlin.
 */
class BerlinOAuthAuthorization {

    private CodeVerifier verifier

    private AuthorizationRequest request

    private params = [
            endpoint     : new URI("${ConfigParser.instance.authorisationServerURL}/oauth2/authorize/"),
            response_type: new ResponseType("code"),
            client_id    : new ClientID(AppConfigReader.getClientId()),
            redirect_uri : new URI(AppConfigReader.getRedirectURL()),
            state        : new State(UUID.randomUUID().toString())
    ]

    BerlinOAuthAuthorization(SCOPES scopes, String consentId) {

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

    BerlinOAuthAuthorization(SCOPES scopes, String consentId, CodeChallengeMethod codeChallengeMethod) {

        verifier = new CodeVerifier()

        request = new AuthorizationRequest.Builder(params.response_type, params.client_id)
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(params.redirect_uri)
                .scope(new Scope(scopes.getConsentScope(consentId)))
                .codeChallenge(verifier, codeChallengeMethod)
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
