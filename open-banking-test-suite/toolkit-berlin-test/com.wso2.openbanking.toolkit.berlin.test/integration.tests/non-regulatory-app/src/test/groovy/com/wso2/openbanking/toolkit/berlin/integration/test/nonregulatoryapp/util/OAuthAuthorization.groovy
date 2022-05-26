/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
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
