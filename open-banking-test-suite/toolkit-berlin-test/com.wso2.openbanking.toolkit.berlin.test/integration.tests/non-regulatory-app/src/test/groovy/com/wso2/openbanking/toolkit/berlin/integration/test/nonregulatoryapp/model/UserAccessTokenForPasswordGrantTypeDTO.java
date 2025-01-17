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
public class UserAccessTokenForPasswordGrantTypeDTO {

    private String grantType;
    private List<String> scope;
    private String username;
    private String password;
    private String redirectUri;

    public String getGrantType() {

        return grantType;
    }

    public void setGrantType(String grantType) {

        this.grantType = grantType;
    }

    public List<String> getScope() {

        return scope;
    }

    public void setScope(List<String> scope) {

        this.scope = scope;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
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

        if (grantType == null) {
            setGrantType(TestConstants.CLIENT_CREDENTIALS);
        }

        if (redirectUri == null) {
            setRedirectUri(ConfigParser.getInstance().getNonRegulatoryRedirectURL());
        }

        if (scope == null) {
            setScope(TestConstants.SCOPES_OPEN_ID);

        }

        if (username == null) {
            setUsername(ConfigParser.getInstance().getPSU().toString());
        }

        if (password == null) {
            setPassword(ConfigParser.getInstance().getPSUPassword().toString());
        }

        String payload = "";
        String delimiter = "&";
        return payload.concat(TestConstants.GRANT_TYPE_KEY+"=" + getGrantType() + delimiter)
                    .concat(TestConstants.SCOPE_KEY+"=" + getScope() + delimiter)
                    .concat(TestConstants.USER_NAME+"=" + getUsername() + delimiter)
                    .concat(TestConstants.PASSWORD+"=" + getPassword() + delimiter)
                    .concat(TestConstants.REDIRECT_URI_KEY+"=" + getRedirectUri());

    }

}
