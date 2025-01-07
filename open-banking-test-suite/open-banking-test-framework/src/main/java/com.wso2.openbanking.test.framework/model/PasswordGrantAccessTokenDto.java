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

/**
 * Model class for Password Grant Access token request.
 */
public class PasswordGrantAccessTokenDto {

	private String grantType;
	private List<String> scopes;
	private String clientAssertionType;
	private String clientAssertion;
	private String redirectUrl;
	private AccessTokenJwtDto accessTokenJwtDto;
	private String clientId;
	private String username;
	private String password;

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

	public String getUserName() {

		return username;
	}

	public void setUserName(String username) {

		this.username = username;
	}

	public String getPassword() {

		return password;
	}

	public void setPassword(String password) {

		this.password = password;
	}

	/**
	 * Method to generate Password Grant User Access token Payload.
	 *
	 * @return String Payload
	 * @throws TestFrameworkException When Access Token payload generation failed
	 *                                using the provided certificate
	 */
	public String getPayload(String clientId, String authMethodType) throws TestFrameworkException {

		if (grantType == null) {
			setGrantType(TestConstants.PASSWORD_GRANT);
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
			setClientAssertionType(TestConstants.CLIENT_ASSERTION_TYPE);
		}

		if (username == null) {
			setUserName("");
		}

		if (password == null) {
			setPassword("");
		}

		String payload = "";
		String delimiter = "&";

		if (authMethodType == TestConstants.TLS_AUTH_METHOD) {

			return payload.concat(TestConstants.GRANT_TYPE_KEY + "=" + getGrantType() + delimiter)
							.concat(TestConstants.SCOPE_KEY + "="
											+ TestUtil.getParamListAsString(getScopes(), ' ') + delimiter)
							.concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUrl()  + delimiter)
							.concat(TestConstants.USER_NAME + "=" + username  + delimiter)
							.concat(TestConstants.PASSWORD + "=" + password  + delimiter)
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
							.concat(TestConstants.REDIRECT_URI_KEY + "=" + getRedirectUrl() + delimiter)
							.concat(TestConstants.USER_NAME + "=" + username  + delimiter)
							.concat(TestConstants.PASSWORD + "=" + password  + delimiter)
							.concat(TestConstants.CLIENT_ID + "=" + clientId);
		}
	}
}
