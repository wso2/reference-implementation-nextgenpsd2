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
