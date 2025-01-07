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

package com.wso2.openbanking.test.framework.model;


import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import com.wso2.openbanking.test.framework.util.AppConfigReader;
import com.wso2.openbanking.test.framework.util.TestConstants;
import com.wso2.openbanking.test.framework.util.TestUtil;

import java.util.ArrayList;
import java.util.List;

import static com.wso2.openbanking.test.framework.util.TestConstants.CLIENT_ASSERTION_TYPE;
import static com.wso2.openbanking.test.framework.util.TestConstants.CLIENT_ASSERTION_TYPE_KEY;

/**
 * Model class for User Access Token Request.
 */
public class UserAccessTokenDto {

  private String grantType;
  private String code;
  private List<String> scopes;
  private String clientAssertionType;
  private String clientAssertion;
  private String redirectUrl;
  private String codeVerifier;
  private AccessTokenJwtDto accessTokenJwtDto;
  private String clientId;

  public String getCodeVerifier() {

    return codeVerifier;
  }

  public void setCodeVerifier(String codeVerifier) {

    this.codeVerifier = codeVerifier;
  }

  public AccessTokenJwtDto getAccessTokenJwtDto() {

    return accessTokenJwtDto;
  }

  public void setAccessTokenJwtDto(AccessTokenJwtDto accessTokenJwtDto) {

    this.accessTokenJwtDto = accessTokenJwtDto;
  }

  public String getGrantType() {

    return grantType;
  }

  public void setGrantType(String grantType) {

    this.grantType = grantType;
  }

  public String getCode() {

    return code;
  }

  public void setCode(String code) {

    this.code = code;
  }

  public List<String> getScopes() {

    return scopes;
  }

  public void setScopes(List<String> scopes) {

    this.scopes = scopes;
  }

  public String getClientAssertionType() {

    return clientAssertionType;
  }

  public void setClientAssertionType(String clientAssertionType) {

    this.clientAssertionType = clientAssertionType;
  }

  public String getClientAssertion() {

    return clientAssertion;
  }

  public void setClientAssertion(String clientAssertion) {

    this.clientAssertion = clientAssertion;
  }

  public String getRedirectUrl() {

    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {

    this.redirectUrl = redirectUrl;
  }

  public String getClientId() {

    return clientId;
  }

  public void setClientId(String clientId) {

    this.clientId = clientId;
  }

  /**
   * Method to generate User Access token Payload.
   *
   * @return String Payload
   * @throws TestFrameworkException When Access Token payload generation failed
   *                                using the provided certificate
   */
  public String getPayload() throws TestFrameworkException {

    if (grantType == null) {
      setGrantType(TestConstants.AUTH_CODE);
    }

    if (redirectUrl == null) {
      setRedirectUrl(AppConfigReader.getRedirectURL());
    }

    if (scopes == null) {
      List<String> scopes = new ArrayList<>();
      scopes.add("com.wso2.openbanking.toolkit.berlin.integration.test.accounts");
      setScopes(scopes);
    }

    if (clientAssertionType == null) {
      setClientAssertionType(CLIENT_ASSERTION_TYPE);
    }

    if (code == null) {
      setCode("");
    }

    if (clientAssertion == null) {
      if (getAccessTokenJwtDto() == null) {
        setAccessTokenJwtDto(new AccessTokenJwtDto());
      }
      setClientAssertion(accessTokenJwtDto.getJwt());
    }

    String payload = "";
    String delimiter = "&";
    payload = payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
        .concat(TestConstants.CODE_KEY + "=" + getCode() + delimiter)
        .concat(TestConstants.SCOPE_KEY + "="
            + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
        .concat(CLIENT_ASSERTION_TYPE_KEY + "=" + getClientAssertionType() + delimiter)
        .concat(TestConstants.CLIENT_ASSERTION_KEY + "=" + getClientAssertion() + delimiter)
        .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUrl() + delimiter)
        .concat(TestConstants.CLIENT_ID + "=" + getClientId());

    if (codeVerifier != null) {
      payload = payload.concat(delimiter + TestConstants.CODE_VERIFIER_KEY + "=" + codeVerifier);
    }
    return payload;
  }

  /**
   * Method to generate User Access token Payload based on Auth Method.
   * @param authMethodType authMethodType
   * @return payload
   * @throws TestFrameworkException exception
   */
  public String getPayload(String authMethodType) throws TestFrameworkException {

    if (grantType == null) {
      setGrantType(TestConstants.AUTH_CODE);
    }

    if (redirectUrl == null) {
      setRedirectUrl(AppConfigReader.getRedirectURL());
    }

    if (scopes == null) {
      List<String> scopes = new ArrayList<>();
      scopes.add("com.wso2.openbanking.toolkit.berlin.integration.test.accounts");
      setScopes(scopes);
    }

    if (code == null) {
      setCode("");
    }

    if (clientId == null) {
      setClientId(AppConfigReader.getClientId());
    }

    String payload = "";
    String delimiter = "&";

    if (authMethodType == TestConstants.TLS_AUTH_METHOD) {
      payload = payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
              .concat(TestConstants.CODE_KEY + "=" + getCode() + delimiter)
              .concat(TestConstants.SCOPE_KEY + "="
                      + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
              .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUrl() + delimiter)
              .concat(TestConstants.CLIENT_ID + "=" + getClientId());
    } else {

      if (clientAssertionType == null) {
        setClientAssertionType(CLIENT_ASSERTION_TYPE);
      }

      if (clientAssertion == null) {
        if (getAccessTokenJwtDto() == null) {
          setAccessTokenJwtDto(new AccessTokenJwtDto());
        }
        setClientAssertion(accessTokenJwtDto.getJwt());
      }

      payload = payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
              .concat(TestConstants.CODE_KEY + "=" + getCode() + delimiter)
              .concat(TestConstants.SCOPE_KEY + "="
                      + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
              .concat(CLIENT_ASSERTION_TYPE_KEY + "=" + getClientAssertionType() + delimiter)
              .concat(TestConstants.CLIENT_ASSERTION_KEY + "=" + getClientAssertion() + delimiter)
              .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUrl() + delimiter)
              .concat(TestConstants.CLIENT_ID + "=" + getClientId());
    }

    if (codeVerifier != null) {
      payload = payload.concat(delimiter + TestConstants.CODE_VERIFIER_KEY + "=" + codeVerifier);
    }

    return payload;
  }
}
