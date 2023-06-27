/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
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
