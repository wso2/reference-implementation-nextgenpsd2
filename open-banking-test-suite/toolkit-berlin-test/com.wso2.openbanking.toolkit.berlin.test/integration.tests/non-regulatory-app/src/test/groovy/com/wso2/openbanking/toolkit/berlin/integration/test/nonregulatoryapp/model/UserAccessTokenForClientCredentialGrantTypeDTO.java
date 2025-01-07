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

package com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.model;

import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import com.wso2.openbanking.test.framework.util.ConfigParser;
import com.wso2.openbanking.test.framework.util.TestConstants;

import java.util.List;

/**
 * Model class for User Access token request with Password Grant Type.
 */
public class UserAccessTokenForClientCredentialGrantTypeDTO {

    private String clientId;
    private String grantType;
    private List<String> scopes;
    private String redirectUri;

    public String getClientId() {

        return clientId;
    }

    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    public String getGrantType() {

        return grantType;
    }

    public void setGrantType(String grantType) {

        this.grantType = grantType;
    }

    public List<String> getScope() {

        return scopes;
    }

    public void setScope(List<String> scope) {

        this.scopes = scope;
    }

    public String getRedirectUri() {

        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {

        this.redirectUri = redirectUri;
    }

    /**
     * Method to generate the Payload for Application Access token
     *
     * @return String of Payload
     * @throws TestFrameworkException When failed to generate the Access Token Payload
     */
    public String getPayload() throws TestFrameworkException {

        if (clientId == null) {
            setClientId(ConfigParser.getInstance().getNonRegulatoryClientId());
        }

        if (grantType == null) {
            setGrantType(TestConstants.CLIENT_CREDENTIALS);
        }

        if (redirectUri == null) {
            setRedirectUri(ConfigParser.getInstance().getNonRegulatoryRedirectURL());
        }

        if (scopes == null) {
            setScope(TestConstants.SCOPES_OPEN_ID);

        }

        String payload = "";
        String delimiter = "&";
        return payload.concat(TestConstants.CLIENT_ID_KEY+"=" + getClientId() + delimiter)
                .concat(TestConstants.GRANT_TYPE_KEY+"=" + getGrantType() + delimiter)
                .concat(TestConstants.SCOPE_KEY+"=" + getScope() + delimiter)
                .concat(TestConstants.REDIRECT_URI_KEY+"=" + getRedirectUri());

    }
}
