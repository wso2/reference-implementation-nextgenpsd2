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

package com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.Nonce
import com.wso2.openbanking.test.framework.util.ConfigParser
import org.testng.Reporter

/**
 * Model Class for Authorization Request
 */
class OAuthAuthorization {
    private AuthorizationRequest request

    private params = [
            endpoint     : new URI("${ConfigParser.instance.authorisationServerURL}/oauth2/authorize/"),
            response_type: new ResponseType("code id_token"),
            client_id    : new ClientID(ConfigParser.getInstance().getNonRegulatoryClientId()),
            redirect_uri : new URI(ConfigParser.getInstance().getNonRegulatoryRedirectURL()),
            state        : new State(UUID.randomUUID().toString()),
            nonce        : new Nonce(UUID.randomUUID().toString())
    ]

    OAuthAuthorization(NonRegulatoryConstants.SCOPES scopes) {

        String scope = String.join(" ", scopes.scopes)

        request = new AuthorizationRequest.Builder(params.response_type, params.client_id)
                .responseType(ResponseType.parse("code id_token"))
                .endpointURI(params.endpoint)
                .redirectionURI(params.redirect_uri)
                .scope(new Scope(scope))
                .state(params.state)
                .customParameter("nonce",UUID.randomUUID().toString())
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
}
