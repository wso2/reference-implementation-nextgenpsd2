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
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser

/**
 * Authorization Requests Builder class
 */
class OAuthAuthorizationRequestBuilder {

    static params = [
            endpoint     : new URI("${ConfigParser.instance.authorisationServerURL}/oauth2/authorize/"),
    ]

    /**
     * Build Authorization Request Without Response Type
     * @param scopes
     * @param accountId
     * @return authorization_response
     */
    static AuthorizationRequest OAuthRequestWithoutResponseType(BerlinConstants.SCOPES scopes, String accountId) {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(AppConfigReader.getClientId()))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .codeChallenge(new CodeVerifier(), CodeChallengeMethod.S256)
                .state(new State(UUID.randomUUID().toString()))
                .build()
    }

    /**
     * Build Authorization Request Without Redirection URI
     * @param scopes
     * @param accountId
     * @return authorization_response
     */
    static AuthorizationRequest OAuthRequestWithoutRedirectionURI(BerlinConstants.SCOPES scopes, String accountId) {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(AppConfigReader.getClientId()))
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .codeChallenge(new CodeVerifier(), CodeChallengeMethod.S256)
                .state(new State(UUID.randomUUID().toString()))
                .build()
    }

    /**
     * Build Authorization Request Without Code Challenge
     * @param scopes
     * @param accountId
     * @return authorization_response
     */
    static AuthorizationRequest OAuthRequestWithoutCodeChallenge(BerlinConstants.SCOPES scopes, String accountId) {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(AppConfigReader.getClientId()))
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .state(new State(UUID.randomUUID().toString()))
                .build()
    }

    /**
     * Build Authorization Request Without State
     * @param scopes
     * @param accountId
     * @return authorization_response
     */
    static AuthorizationRequest OAuthRequestWithoutState(BerlinConstants.SCOPES scopes, String accountId) {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(AppConfigReader.getClientId()))
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .codeChallenge(new CodeVerifier(), CodeChallengeMethod.S256)
                .build()
    }

    /**
     * Build Authorization Request With Invalid Client Id
     * @param scopes
     * @param accountId
     * @return authorization_response
     */
    static AuthorizationRequest OAuthRequestWithInvalidClientId(BerlinConstants.SCOPES scopes, String accountId) {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(UUID.randomUUID().toString()))
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .codeChallenge(new CodeVerifier(), CodeChallengeMethod.S256)
                .state(new State(UUID.randomUUID().toString()))
                .build()
    }

    /**
     * Build Authorization Request With Unsupported Code Challenge Method
     * @param scopes
     * @param accountId
     * @return authorization_response
     */
    static AuthorizationRequest OAuthRequestWithUnsupportedCodeChallengeMethod(BerlinConstants.SCOPES scopes, String accountId) {

        CodeChallengeMethod codeChallengeMethod = new CodeChallengeMethod("RS256")

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(AppConfigReader.getClientId()))
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .codeChallenge(new CodeVerifier(), codeChallengeMethod)
                .state(new State(UUID.randomUUID().toString()))
                .build()
    }

    /**
     * Build Authorization Request Without Scope attribute
     * @return authorization_response
     */
    static AuthorizationRequest OAuthRequestWithoutScope() {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(AppConfigReader.getClientId()))
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .codeChallenge(new CodeVerifier(), CodeChallengeMethod.S256)
                .state(new State(UUID.randomUUID().toString()))
                .build()
    }

    /**
     * Build Authorization Request Without Scope attribute
     * @param scopes
     * @param accountId
     * @return authorization_response
     */
    static AuthorizationRequest OAuthRequestWithUnsupportedScope(BerlinConstants.SCOPES scopes, String accountId) {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(AppConfigReader.getClientId()))
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .codeChallenge(new CodeVerifier(), CodeChallengeMethod.S256)
                .state(new State(UUID.randomUUID().toString()))
                .build()
    }
}
