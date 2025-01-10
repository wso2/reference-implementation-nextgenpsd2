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
package com.wso2.openbanking.test.framework.model;

import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import com.wso2.openbanking.test.framework.util.AppConfigReader;
import com.wso2.openbanking.test.framework.util.TestConstants;
import com.wso2.openbanking.test.framework.util.TestUtil;

import java.util.List;

/**
 * Model class for Application Access token request.
 */
public class ApplicationAccessTokenDto {

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

  public String getGrantType() {

    return grantType;
  }

  public void setGrantType(String grantType) {

    this.grantType = grantType;
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

  public String getRedirectUri() {

    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {

    this.redirectUri = redirectUri;
  }

  public String getContentType() {

    return contentType;
  }

  public void setContentType(String contentType) {

    this.contentType = contentType;
  }

  public AccessTokenJwtDto getAccessTokenJwtDto() {

    return accessTokenJwtDto;
  }

  public void setAccessTokenJwtDto(AccessTokenJwtDto accessTokenJwtDto) {

    this.accessTokenJwtDto = accessTokenJwtDto;
  }
  public void setApplicationKeystoreLocation(String appKeystoreLocation) {

    this.appKeystoreLocation = appKeystoreLocation;
  }

  public void setApplicationKeystorePassword(String appKeystorePassword) {

    this.appKeystorePassword = appKeystorePassword;
  }

  public void setApplicationKeystoreAlias(String appKeystoreAlias) {

    this.appKeystoreAlias = appKeystoreAlias;
  }

  public void setExp(long exp) {

    this.exp = exp;
  }


  /**
   * Method to generate the Payload for Application Access token.
   *
   * @return String of Payload
   * @throws TestFrameworkException When failed to generate the Access Token Payload
   */
  public String getPayload() throws TestFrameworkException {
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
  public String getPayload(String clientId) throws TestFrameworkException {

    if (grantType == null) {
      setGrantType(TestConstants.CLIENT_CREDENTIALS);
    }

    if (redirectUri == null) {
      setRedirectUri(AppConfigReader.getRedirectURL());
    }

    if (scopes == null) {
      setScopes(TestConstants.ACCOUNTS_DEFAULT_SCOPES);
    }

    String payload = "";
    String delimiter = "&";
    return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
            .concat(TestConstants.SCOPE_KEY + "="
                    + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
            .concat(TestConstants.CLIENT_ID + "=" + clientId);
  }

  /**
   * Method to generate the Payload for Application Access token with relevant auth method.
   *
   * @param clientId client id
   * @param authMethodType token_endpoint_auth_method
   * @return payload
   * @throws TestFrameworkException
   */
  public String getPayload(String clientId, String authMethodType) throws TestFrameworkException {

      if (grantType == null) {
        setGrantType(TestConstants.CLIENT_CREDENTIALS);
      }

      if (redirectUri == null) {
        setRedirectUri(AppConfigReader.getRedirectURL());
      }

      if (scopes == null) {
        setScopes(TestConstants.ACCOUNTS_DEFAULT_SCOPES);
      }

      String payload = "";
      String delimiter = "&";

    if (authMethodType == TestConstants.TLS_AUTH_METHOD) {
      return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
              .concat(TestConstants.SCOPE_KEY + "="
                      + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
              .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUri()  + delimiter)
              .concat(TestConstants.CLIENT_ID + "=" + clientId);

    } else {
      if (clientAssertionType == null) {
        setClientAssertionType(TestConstants.CLIENT_ASSERTION_TYPE);
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

      return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
              .concat(TestConstants.SCOPE_KEY + "="
                      + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
              .concat(TestConstants.CLIENT_ASSERTION_TYPE_KEY + "="
                      + getClientAssertionType() + delimiter)
              .concat(TestConstants.CLIENT_ASSERTION_KEY + "=" + getClientAssertion() + delimiter)
              .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUri() + delimiter)
              .concat(TestConstants.CLIENT_ID + "=" + clientId);
    }
  }

  /**
   * Method to generate the Payload for Application Access token with relevant signing algorithm and certs.
   *
   * @param clientId         client id
   * @param signingAlg       signing algorithm
   * @return payload
   * @throws TestFrameworkException exception
   */
  public String getPayloadForSpecificSignature(String clientId, String signingAlg) throws TestFrameworkException {

    if (grantType == null) {
      setGrantType(TestConstants.CLIENT_CREDENTIALS);
    }

    if (redirectUri == null) {
      setRedirectUri(AppConfigReader.getRedirectURL());
    }

    if (scopes == null) {
      setScopes(TestConstants.ACCOUNTS_DEFAULT_SCOPES);
    }

    if (clientAssertionType == null) {
      setClientAssertionType(TestConstants.CLIENT_ASSERTION_TYPE);
    }

    if (appKeystoreLocation == null) {
      setApplicationKeystoreLocation(AppConfigReader.getApplicationKeystoreLocation());
    }

    if (appKeystorePassword == null) {
      setApplicationKeystorePassword(AppConfigReader.getApplicationKeystorePassword());
    }

    if (appKeystoreAlias == null) {
      setApplicationKeystoreAlias(AppConfigReader.getApplicationKeystoreAlias());
    }
    setExp(exp);

    if (clientAssertion == null) {
      if (accessTokenJwtDto == null) {
        setAccessTokenJwtDto(new AccessTokenJwtDto());
      }
      if (clientId == null) {
        setClientAssertion(accessTokenJwtDto.getJwt());
      } else {
        //use jwk thumbprint
        setClientAssertion(TestUtil.getSignedRequestObjectWithDefinedCert(accessTokenJwtDto.getJwt(clientId, exp),
                signingAlg, appKeystoreLocation, appKeystorePassword, appKeystoreAlias));
      }
    }
    String payload = "";
    String delimiter = "&";
    return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
            .concat(TestConstants.SCOPE_KEY + "="
                    + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
            .concat(TestConstants.CLIENT_ASSERTION_TYPE_KEY + "="
                    + getClientAssertionType() + delimiter)
            .concat(TestConstants.CLIENT_ASSERTION_KEY + "=" + getClientAssertion() + delimiter)
            .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUri() + delimiter)
            .concat(TestConstants.CLIENT_ID + "=" + clientId);
  }

  /**
   * Method to generate the Payload for Application Access token without client Id.
   *
   * @param clientId       client id
   * @param authMethodType authMethodType
   * @return payload
   * @throws TestFrameworkException exception
   */
  public String getPayloadWithoutClientId(String clientId, String authMethodType) throws TestFrameworkException {

    if (grantType == null) {
      setGrantType(TestConstants.CLIENT_CREDENTIALS);
    }

    if (redirectUri == null) {
      setRedirectUri(AppConfigReader.getRedirectURL());
    }

    if (scopes == null) {
      setScopes(TestConstants.ACCOUNTS_DEFAULT_SCOPES);
    }

    String payload = "";
    String delimiter = "&";

    if (authMethodType == TestConstants.TLS_AUTH_METHOD) {
      return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
              .concat(TestConstants.SCOPE_KEY + "="
                      + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
              .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUri());

    } else {
      if (clientAssertionType == null) {
        setClientAssertionType(TestConstants.CLIENT_ASSERTION_TYPE);
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
      return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
              .concat(TestConstants.SCOPE_KEY + "="
                      + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
              .concat(TestConstants.CLIENT_ASSERTION_TYPE_KEY + "="
                      + getClientAssertionType() + delimiter)
              .concat(TestConstants.CLIENT_ASSERTION_KEY + "=" + getClientAssertion() + delimiter)
              .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUri());
    }
  }

  /**
   * Method to generate the Payload for Application Access token with different client Id.
   * @param clientId client id
   * @param secondaryClientId secondary client id
   * @return payload
   * @throws TestFrameworkException exception
   */
  public String getPayloadWithDifferentClientId(String clientId, String secondaryClientId)
          throws TestFrameworkException {

    if (grantType == null) {
      setGrantType(TestConstants.CLIENT_CREDENTIALS);
    }

    if (redirectUri == null) {
      setRedirectUri(AppConfigReader.getRedirectURL());
    }

    if (scopes == null) {
      setScopes(TestConstants.ACCOUNTS_DEFAULT_SCOPES);
    }

    if (clientAssertionType == null) {
      setClientAssertionType(TestConstants.CLIENT_ASSERTION_TYPE);
    }

    if (clientAssertion == null) {
      if (accessTokenJwtDto == null) {
        setAccessTokenJwtDto(new AccessTokenJwtDto());
      }
      if (clientId == null) {
        setClientAssertion(accessTokenJwtDto.getJwt());
      } else {
        //use jwk thumbprint
        setClientAssertion(accessTokenJwtDto.getJwt(secondaryClientId));
      }
    }

    String payload = "";
    String delimiter = "&";
    return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
            .concat(TestConstants.SCOPE_KEY + "="
                    + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
            .concat(TestConstants.CLIENT_ASSERTION_TYPE_KEY + "="
                    + getClientAssertionType() + delimiter)
            .concat(TestConstants.CLIENT_ASSERTION_KEY + "=" + getClientAssertion() + delimiter)
            .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUri() + delimiter)
            .concat(TestConstants.CLIENT_ID + "=" + clientId);
  }

  public String getPayloadWithoutClientAssertion(String clientId) throws TestFrameworkException {

    if (grantType == null) {
      setGrantType(TestConstants.CLIENT_CREDENTIALS);
    }

    if (redirectUri == null) {
      setRedirectUri(AppConfigReader.getRedirectURL());
    }

    if (scopes == null) {
      setScopes(TestConstants.ACCOUNTS_DEFAULT_SCOPES);
    }

    if (clientAssertionType == null) {
      setClientAssertionType(TestConstants.CLIENT_ASSERTION_TYPE);
    }

    String payload = "";
    String delimiter = "&";
    return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
            .concat(TestConstants.SCOPE_KEY + "="
                    + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
            .concat(TestConstants.CLIENT_ASSERTION_TYPE_KEY + "="
                    + getClientAssertionType() + delimiter)
            .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUri() + delimiter)
            .concat(TestConstants.CLIENT_ID + "=" + clientId);
  }

  public String getPayloadWithoutClientAssertionType(String clientId) throws TestFrameworkException {

    if (grantType == null) {
      setGrantType(TestConstants.CLIENT_CREDENTIALS);
    }

    if (redirectUri == null) {
      setRedirectUri(AppConfigReader.getRedirectURL());
    }

    if (scopes == null) {
      setScopes(TestConstants.ACCOUNTS_DEFAULT_SCOPES);
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
    return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
            .concat(TestConstants.SCOPE_KEY + "="
                    + TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
            .concat(TestConstants.CLIENT_ASSERTION_KEY + "=" + getClientAssertion() + delimiter)
            .concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUri() + delimiter)
            .concat(TestConstants.CLIENT_ID + "=" + clientId);
  }
}
