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

package com.wso2.berlin.test.framework.constant

class BerlinConstants {
    static final String CURRENT_ACCOUNT = "DE98765432109876543210"
    static final enum AUTH_METHOD {
        PRIVATE_KEY_JWT
    }

    static final enum SCOPES {
        PAYMENTS(["openid", "payments"], "pis"), ACCOUNTS(["openid", "accounts"], "ais") ,
        COF(["openid", "fundsconfirmations"], "piis")

        private final List<String> scopes
        private final String consentScope

        SCOPES(List<String> scopes, String consentScope){
            this.scopes = scopes
            this.consentScope = consentScope
        }

        List<String> getScopes(){
            return scopes
        }

        String getConsentScope(String consentId){
            return "${consentScope}:${consentId}"
        }
    }
    public static final String ACCESS_TOKEN_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String TOKEN_ENDPOINT = "/oauth2/token";
    public static final String CLIENT_ID = "client_id";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String SCOPE_KEY = "scope";
    public static final String CLIENT_ASSERTION_TYPE_KEY = "client_assertion_type";
    public static final String CLIENT_ASSERTION_KEY = "client_assertion";
    public static final String REDIRECT_URI_KEY = "redirect_uri";
    public static final ArrayList<String> ACCOUNTS_DEFAULT_SCOPES = new ArrayList<>(Arrays.asList("accounts", "openid"));
    public static final String CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    public static final String ISSUER_KEY = "iss";
    public static final String SUBJECT_KEY = "sub";
    public static final String AUDIENCE_KEY = "aud";
    public static final String EXPIRE_DATE_KEY = "exp";
    public static final String ISSUED_AT_KEY = "iat";
    public static final String JTI_KEY = "jti";
    public static final String CONTENT_TYPE_APPLICATION_JWT = "application/jwt";


    static final int STATUS_CODE_201 = 201
    static final int STATUS_CODE_200 = 200


    public static final String X_REQUEST_ID = "X-Request-ID"
    static final String PSU_IP_ADDRESS = "PSU-IP-Address"
    public static final String Date = "Date"
    static final String PSU_ID = "PSU-ID"
    static final String PSU_TYPE = "PSU-ID-Type"
    static final String CONSENT_ID_HEADER = "Consent-ID"


    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";

    public static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
    public static final String DIGEST = "Digest";
    public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    public static final String DATE = "DATE";
    public static final String PSU_CORPORATE_ID_HEADER = "PSU-Corporate-ID";

    public static final String SIGNATURE = "Signature";

    public static final String INVALID_KEYSTORE_PASSWORD = "wso2carbon";
    public static final String PATH_TO_INVALID_KEYSTORE = "./../../../../../test-artifacts/" +
            "tpp3-invalid-info/certs/signing/tpp3-invalid-signing.jks";

    public static final String PSU_ID_VALUE = "PSU-ID";

    static final String MULTICURRENCY_ACCOUNT = "DE12345678901234567890"
    static final String PAN_ACCOUNT = "5409050000000000"
    static final String BBAN_ACCOUNT = "BARC12345612345678"
    static final String CURRENCY1 = "USD"
    static final String CURRENCY2 = "EUR"
    static final String CURRENCY3 = "GBP"
    static final String ACCOUNTS_SUBMIT_XPATH = """//*[@id="approve"]"""
    static final String ACCOUNTS_DENY_XPATH = """//*[@id="oauth2_authz_confirm"]/div/div[3]/div/input[2]"""


}
