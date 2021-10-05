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

package com.wso2.openbanking.test.framework.util;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class to contain common constants used for Test Framework.
 */
public class TestConstants {
	public static final ConfigParser config = ConfigParser.getInstance();

	public static final String SOLUTION_VERSION_130 = "1.3.0";
	public static final String SOLUTION_VERSION_140 = "1.4.0";
	public static final String SOLUTION_VERSION_150 = "1.5.0";

	public static final String UK_SPEC_VERSION_316 = "3.1.6";
	public static final String UK_SPEC_VERSION_315 = "3.1.5";
	public static final String UK_SPEC_VERSION_300 = "3.0.0";

	public static final String CLIENT_CREDENTIALS = "client_credentials";
	public static final String AUTH_CODE = "authorization_code";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String PASSWORD_GRANT = "password";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String TOKEN = "token";
	public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
	public static final String AUTH_RESPONSE_TYPE = "code id_token";
	public static final String CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
	public static final String X_FAPI_FINANCIAL_ID_KEY = "x-fapi-financial-id";
	public static final String X_WSO2_CLIENT_ID_KEY = "x-wso2-client-id";
	public static final String ACCESS_TOKEN_CONTENT_TYPE = "application/x-www-form-urlencoded";
	public static final String SCA_CLAIM = "urn:openbanking:psd2:sca";
	public static final String CA_CLAIM = "urn:openbanking:psd2:ca";
	public static final String TIME_ZONE = "Asia/Colombo";
	public static final ArrayList<String> ACCOUNTS_DEFAULT_SCOPES = new ArrayList<>(Arrays.asList("com.wso2.openbanking.toolkit.berlin.integration.test.accounts", "openid"));
	public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
	public static final String AUTHORIZATION_BEARER_TAG = "Bearer ";
	public static final String X_FAPI_CUSTOMER_LAST_LOGGED_TIME = "x-fapi-customer-last-logged-time";
	public static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
	public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
	public static final String X_JWS_SIGNATURE = "";
	public static final String MTLS_CERTIFICATE_HEADER = "x-wso2-mutual-auth-cert";
	public static final String CONTENT_TYPE_APPLICATION_JWT = "application/jwt";

	//Invalid keystore details
	public static final String PATH_TO_INVALID_KEYSTORE = "./../../../../../test-artifacts/" +
					"tpp3-invalid-info/certs/signing/tpp3-invalid-signing.jks";
	public static final String INVALID_KEYSTORE_PASSWORD = "wso2carbon";
	public static final String INVALID_KEYSTORE_ALIAS = "tpp3-invalid";

	//Endpoints
	public static final String AISP_BASE_URL = "open-banking/v3.1/aisp";
	public static final String GET_ACCOUNT = AISP_BASE_URL + "/com.wso2.openbanking.toolkit.berlin.integration.test.accounts/";
	public static final String GET_BALANCE = "/balances/";
	public static final String ACCOUNT_CONSENT_INITIATION = AISP_BASE_URL + "/account-access-consents";
	public static final String TOKEN_ENDPOINT = "/token";
	public static final String OAUTH2_TOKEN_ENDPOINT = "/oauth2/token";
	public static final String AUTHORIZE_ENDPOINT = "/authorize/?";
	public static final String INTROSPECTION_ENDPOINT = "/oauth2/introspect";
	public static final String REGISTER_SCOPE_ENDPOINT = "/api/identity/oauth2/v1.0/scopes";
	public static final String OAUTH2_REVOKE_ENDPOINT = "/oauth2/revoke";

	//HTTP Status Codes
	public static final int OK = 200;
	public static final int BAD_REQUEST = 400;
	public static final int FORBIDDEN = 403;
	public static final int CREATED = 201;
	public static final int UNAUTHORIZED = 401;
	public static final int CONFLICT = 409;

	//JWT Claim keys
	public static final String ISSUER_KEY = "iss";
	public static final String SUBJECT_KEY = "sub";
	public static final String AUDIENCE_KEY = "aud";
	public static final String EXPIRE_DATE_KEY = "exp";
	public static final String ISSUED_AT_KEY = "iat";
	public static final String JTI_KEY = "jti";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String INTEGRATION_DATE = "integrationDate";

	//payload constants
	public static final String CODE_VERIFIER_KEY = "code_verifier";
	public static final String GRANT_TYPE_KEY = "grant_type";
	public static final String SCOPE_KEY = "scope";
	public static final String CLIENT_ASSERTION_TYPE_KEY = "client_assertion_type";
	public static final String CLIENT_ASSERTION_KEY = "client_assertion";
	public static final String REDIRECT_URI_KEY = "redirect_uri";
	public static final String PERMISSIONS_KEY = "Permissions";
	public static final String EXPIRATION_DATE_KEY = "ExpirationDateTime";
	public static final String TRANSACTION_FROM_DATE_KEY = "TransactionFromDateTime";
	public static final String TRANSACTION_TO_DATE_KEY = "TransactionToDateTime";
	public static final String DATA_KEY = "Data";
	public static final String RISK_KEY = "Risk";
	public static final String REQUEST_KEY = "request";
	public static final String RESPONSE_TYPE_KEY = "response_type";
	public static final String CLIENT_ID_KEY = "client_id";
	public static final String STATE_KEY = "state";
	public static final String PROMPT_KEY = "prompt";
	public static final String NONCE_KEY = "nonce";
	public static final String VALUES_KEY = "values";
	public static final String VALUE_KEY = "value";
	public static final String OB_INTENT_ID_KEY = "openbanking_intent_id";
	public static final String ESSENTIAL_KEY = "essential";
	public static final String ACR_KEY = "acr";
	public static final String ID_TOKEN_KEY = "id_token";
	public static final String USER_INFO_KEY = "userinfo";
	public static final String CLAIMS_KEY = "claims";
	public static final String MAX_AGE_KEY = "maxAge";
	public static final String CODE_KEY = "code";
	public static final String INSTRUCTED_AMOUNT = "InstructedAmount";
	public static final String CLIENT_ID = "client_id";
	public static final String USER_NAME = "username";
	public static final String PASSWORD = "password";

	//Selenium constants
	public static final String USERNAME_FIELD_ID = "usernameUserInput";
	public static final String PASSWORD_FIELD_ID = "password";
	public static final String HEADLESS_TAG = "--headless";
	public static final String ACCOUNT_SELECT_DROPDOWN_XPATH = "//*[@id=\"accselect\"]";
	public static final String REDIRECT_URL_ERROR="/html/body/div/div/div/div/div/div[2]/div/form/div/p[2]";
	public static final String INVALID_REQUEST_ERROR="/html/body/div/div/div/div/div/div[2]/div/form/div/h3";
	public static final String REDIRECT_URL_ERROR_MESSAGE="Redirect URI is not present in the authorization request";
	public static final String CONSENT_ERROR_MESSAGE=" The Consent ID does not match with the Client ID of the initiated request";
	public static final String CONSENT_ERROR_PATH="/html/body/div/div/div/div/div/div[2]/div/form/div/p[2]";
	public static final String MISSING_REDIRECT_URL_ERROR="/html/body/div/div/div/div/div/div[2]/div/form/div/p[2]";
	public static final String REQUEST_OBJECT_ERROR="invalid_request%2C+%27response_type%27+contains+%27id_token%27%3B+but+%27nonce%27+parameter+not+found";
	public static final String AUTH_SIGNIN_XPATH = "//*[contains(@type,'submit')]";
	public static final String AUTH_SIGNIN_XPATH_140 = "//button[contains(text(),'Sign in')]";
	public static final String CONSENT_DENY_OLD ="/html/body/div[1]/div/div/div/div/div[2]/form/div/h3[2]/div[1]/div/input[2]";
	public static final String CONSENT_DENY_XPATH = "//input[@value='Deny']";
	public static final String BTN_DEVPORTAL_SIGNIN = "//span[contains(text(),'Sign-in')]";
	public static final String APIM_USERNAME = "usernameUserInput";
	public static final String APIM_PASSWORD = "password";
	public static final String BTN_APIM_CONTINUE = "//button[contains(text(),'Continue')]";
	public static final String TAB_APPLICATIONS = "//span[contains(text(),'Applications')]";
	public static final String TBL_ROWS = "//tbody/tr";
	public static final String TAB_SUBSCRIPTIONS = "//p[text()='Subscriptions']";
	public static final String IS_USERNAME_ID = "txtUserName";
	public static final String IS_PASSWORD_ID = "txtPassword";
	public static final String BTN_IS_SIGNING = "//input[@value='Sign-in']";
	public static final String CONSENT_APPROVE_SUBMIT_ID = "approve";

	// Berlin Constants
	public static final String X_REQUEST_ID = "X-Request-ID";
	public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
	public static final String DATE = "DATE";
	public static final String PSU_ID = "PSU-ID";
	public static final String PSU_TYPE = "PSU-ID-Type";
	public static final String EXPLICIT_AUTH_PREFFERED = "TPP-Explicit-Authorisation-Preferred";
	public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
	public static final String DIGEST = "Digest";
	public static final String SIGNATURE = "Signature";
	public static final String PSU_CORPORATE_ID_HEADER = "PSU-Corporate-ID";
	public static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
	public static final String API_CONTEXT = "xs2a";
	public static final String LOGIN_LINK="//*[@id=\"btn-login\"]";
	public static final String SP_MENU_NAME=
					"/html/body/table/tbody/tr[2]/td[2]/table/tbody/tr[1]/td/div/ul/li[3]/ul/li[8]/ul/li[2]/a";
	public static final String SP_EDIT_XPATH=
					"/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/table[2]/tbody/tr/td/table/tbody/tr[1]/td[3]/a[1]";
	public static final String INBOUND_MENU="//*[@id=\"app_authentication_head\"]";
	public static final String DROPDOWN_KEY="//*[@id=\"oauth.config.head\"]";
	public static final String EDIT_LINK=
					"/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div[1]/form/div[5]/div[2]/table/tbody/tr/td/table/tbody/tr/td[3]/a[1]";
	public static final String SELECT_IMPLICIT_GRANT_XPATH="//*[@id=\"grant_implicit\"]";
	public static final String SELECT_CLIENT_CREDENTIALS_GRANT="//*[@id=\"grant_client_credentials\"]";
	public static final String SELECT_AUTH_CODE_GRANT="//*[@id=\"grant_authorization_code\"]";
	public static final String SELECT_REFRESH_TOKEN_GRANT="//*[@id=\"grant_refresh_token\"]";
	public static final String SELECT_SAML2_GRANT="//*[@id=\"grant_urn:ietf:params:oauth:grant-type:saml2-bearer\"]";
	public static final String UPDATE_BUTTON_XPATH=
					"/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[2]/td/input[1]";
	public static final String LOGIN_BUTTON=
					"/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr/td[2]/div/form/table/tbody/tr[4]/td[2]/input";
	public static final String USERNAME_XPATH="//*[@id=\"txtUserName\"]";
	public static final String PASSWORD_XPATH="//*[@id=\"txtPassword\"]";
	public static final String SIGNOUT_XPATH="/html/body/table/tbody/tr[1]/td/div/div[4]/div/ul/li[3]/a";
	public static final int TIMEOUT=10000;
	public static final String WINDOWS_SCROLL_100="window.scrollBy(0, 1000)";
	public static final String WINDOWS_SCROLL_20="window.scrollBy(0, 200)";

	//Non-Regulatory
	public static final ArrayList<String> SCOPES_OPEN_ID = new ArrayList<>(Arrays.asList("openid"));

	public static final String CLIENTID_NON_REGULATORY_APP = config.getNonRegulatoryClientId();
	public static final String CCPORTAL_SIGNIN_XPATH = "//button[contains(text(),'Sign in')]";

	public static final String OTP_CODE = "123456";
	public static final String LBL_SMSOTP_AUTHENTICATOR = "//h2[text()='Authenticating with SMSOTP']";
	public static final String TXT_OTP_CODE = "OTPcode";
	public static final String BTN_AUTHENTICATE = "//input[@id='authenticate']";
	public static final String LBL_OTP_TIMEOUT = "//div[@id='otpTimeout']";
	public static final String ELE_CONSENT_PAGE = "//form[@id='oauth2_authz_consent']";
	public static final String LBL_AUTHENTICATION_FAILURE = "//div[contains(text(),'Authentication Error')]/../p";
	public static final String LBL_FOOTER_DESCRIPTION = "//div[@class='ui segment']/div/form/div/div";

	public static final String TLS_AUTH_METHOD = "tls_client_auth";
	public static final String CHROME = "chrome";
	public static final String FIREFOX = "firefox";
	public static final String FIREFOX_DRIVER_NAME = "webdriver.gecko.driver";
	public static final String CHROME_DRIVER_NAME = "webdriver.chrome.driver";
	public static final String REST_API_CLIENT_REGISTRATION_ENDPOINT = "/client-registration/v0.16/register";
	public static final String REST_API_PUBLISHER_ENDPOINT = "/api/am/publisher/v1.1/apis/";
	public static final String REST_API_STORE_ENDPOINT = "/api/am/store/v1/";
	public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
	public static final String REST_API_SCIM2_ENDPOINT = "/scim2";
	public static final String CONTENT_TYPE_APPLICATION_SCIM_JSON = "application/scim+json";
}
