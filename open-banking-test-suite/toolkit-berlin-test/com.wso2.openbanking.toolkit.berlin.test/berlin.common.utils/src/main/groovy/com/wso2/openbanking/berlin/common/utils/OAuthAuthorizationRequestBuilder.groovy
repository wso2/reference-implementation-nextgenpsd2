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

    /**
     * Build Authorization Request With PLAIN in code_challenge method.
     * @param scopes
     * @param accountId
     * @return
     */
    static AuthorizationRequest OAuthRequestWithPlain(BerlinConstants.SCOPES scopes, String accountId) {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(AppConfigReader.getClientId()))
                .responseType(ResponseType.parse("code"))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .codeChallenge(new CodeVerifier(), CodeChallengeMethod.PLAIN)
                .state(new State(UUID.randomUUID().toString()))
                .build()
    }

    /**
     * Build Authorization Request with configurable attribute
     * @param scopes
     * @param accountId
     * @param clientId
     * @param responseType
     * @param codeChallengeMethod
     * @param state
     * @return authorization_request
     */
    static AuthorizationRequest OAuthRequestWithConfigurableParams(BerlinConstants.SCOPES scopes, String accountId,
                                                                   String clientId = AppConfigReader.getClientId(),
                                                                   String responseType = "code",
                                                                   CodeChallengeMethod codeChallengeMethod =
                                                                           CodeChallengeMethod.S256,
                                                                   String state = UUID.randomUUID().toString()) {

        return new AuthorizationRequest.Builder(new ResponseType(), new ClientID(clientId))
                .responseType(ResponseType.parse(responseType))
                .endpointURI(params.endpoint)
                .redirectionURI(new URI(AppConfigReader.getRedirectURL()))
                .scope(new Scope(scopes.getConsentScope(accountId)))
                .codeChallenge(new CodeVerifier(), codeChallengeMethod)
                .state(new State(state))
                .build()
    }
}
