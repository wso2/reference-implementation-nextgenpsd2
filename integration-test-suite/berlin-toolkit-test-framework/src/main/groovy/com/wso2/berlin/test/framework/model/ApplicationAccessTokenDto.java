/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.berlin.test.framework.model;

import com.wso2.berlin.test.framework.configuration.AppConfigReader;
import com.wso2.berlin.test.framework.constant.BerlinConstants;
import com.wso2.berlin.test.framework.utility.BerlinTestUtil;
import com.wso2.bfsi.test.framework.exception.TestFrameworkException;

import java.util.List;

class ApplicationAccessTokenDto {

    private String grantType;
    private List<String> scopes;
    private String clientAssertionType;
    private String clientAssertion;
    private String redirectUri;
    private String contentType;
    private AccessTokenJwtDto accessTokenJwtDto;
    private String appKeystoreLocation;
    private String appKeystorePassword;
    private String appKeystoreAlias;
    private long exp;
    private String jti;


    String getGrantType() {

        return grantType;
    }
    void setGrantType(String grantType) {

        this.grantType = grantType;
    }

    List<String> getScopes() {

        return scopes;
    }

    void setScopes(List<String> scopes) {

        this.scopes = scopes;
    }

    String getClientAssertionType() {

        return clientAssertionType;
    }

    void setClientAssertionType(String clientAssertionType) {

        this.clientAssertionType = clientAssertionType;
    }

    String getClientAssertion() {

        return clientAssertion;
    }

    void setClientAssertion(String clientAssertion) {

        this.clientAssertion = clientAssertion;
    }

    String getRedirectUri() {

        return redirectUri;
    }

    void setRedirectUri(String redirectUri) {

        this.redirectUri = redirectUri;
    }

    String getContentType() {

        return contentType;
    }

    void setContentType(String contentType) {

        this.contentType = contentType;
    }

    AccessTokenJwtDto getAccessTokenJwtDto() {

        return accessTokenJwtDto;
    }

    void setAccessTokenJwtDto(AccessTokenJwtDto accessTokenJwtDto) {

        this.accessTokenJwtDto = accessTokenJwtDto;
    }
    void setApplicationKeystoreLocation(String appKeystoreLocation) {

        this.appKeystoreLocation = appKeystoreLocation;
    }

    void setApplicationKeystorePassword(String appKeystorePassword) {

        this.appKeystorePassword = appKeystorePassword;
    }

    void setApplicationKeystoreAlias(String appKeystoreAlias) {

        this.appKeystoreAlias = appKeystoreAlias;
    }

    void setExp(long exp) {

        this.exp = exp;
    }


    /**
     * Method to generate the Payload for Application Access token.
     *
     * @return String of Payload
     * @throws TestFrameworkException When failed to generate the Access Token Payload
     */
    String getPayload() throws TestFrameworkException {
        return getPayload(null);
    }

    /**
     * Method to generate the Payload for Application Access token.
     * If the clientId is provided, it will be used to generate the client assertion.
     *
     * @param clientId - Client id
     * @return String of Payload
     * @throws TestFrameworkException When failed to generate the Access Token Payload
     */
    String getPayload(String clientId) throws TestFrameworkException {

        if (grantType == null) {
            setGrantType(BerlinConstants.CLIENT_CREDENTIALS);
        }

        if (redirectUri == null) {
            setRedirectUri(AppConfigReader.getRedirectURL());
        }

        if (scopes == null) {
            setScopes(BerlinConstants.ACCOUNTS_DEFAULT_SCOPES);
        }

        if (clientAssertionType == null) {
            setClientAssertionType(BerlinConstants.CLIENT_ASSERTION_TYPE);
        }

        if (clientAssertion == null) {
            if (accessTokenJwtDto == null) {
                setAccessTokenJwtDto(new AccessTokenJwtDto());
            }
            if (clientId == null) {
                setClientAssertion(accessTokenJwtDto.getJwt());
            } else {
                //use jwk thumbprint
                setClientAssertion(accessTokenJwtDto.getJwt(clientId));
            }
        }

        String payload = "";
        String delimiter = "&";
        return payload.concat(BerlinConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
                .concat(BerlinConstants.SCOPE_KEY + "="
                        + BerlinTestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
                .concat(BerlinConstants.CLIENT_ASSERTION_TYPE_KEY + "="
                        + getClientAssertionType() + delimiter)
                .concat(BerlinConstants.CLIENT_ASSERTION_KEY + "=" + getClientAssertion() + delimiter)
                .concat(BerlinConstants.REDIRECT_URI_KEY + "=" + getRedirectUri() + delimiter)
                .concat(BerlinConstants.CLIENT_ID + "=" + clientId);
    }
}
