/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework.constant

import com.wso2.berlin.test.framework.configuration.ConfigParser
import com.wso2.openbanking.test.framework.constant.OBConstants

class BerlinConstants extends OBConstants {

    static config = ConfigParser.getInstance()
    static API_VERSION = config.getApiVersion()
    static final String regularAcc = "DE98765432109876543210"
    public static String AISP_PATH = getConsentPath()
    static final String ACCOUNTS_PATH = AISP_PATH + "accounts"
    static final String CONSENT_PATH = AISP_PATH + "consents"
    static final String SPECIFIC_ACCOUNT_PATH = ACCOUNTS_PATH + "/" + regularAcc
    static final String CONSENT_STATUS_RECEIVED = "received"
    static final String CONSENT_STATUS_VALID = "valid"
    static final String CONSENT_STATUS_REJECTED = "rejected"
    static final String CONSENT_ID = "consentId"
    static final String CONSENT_STATUS = "consentStatus"



    static String getConsentPath() {

        def aispPath

        if (API_VERSION.equalsIgnoreCase("1.1.0")) {
            aispPath = "AccountsInfoAPI/v1.1.0/"

        } else if (API_VERSION.equalsIgnoreCase("1.3.3")) {
            aispPath = "xs2a/1.3.3/"

        } else if (API_VERSION.equalsIgnoreCase("1.3.6")) {
            aispPath = "xs2a/v1/"
        }
        return aispPath
    }

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

    public static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
    public static final String DIGEST = "Digest";
    public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    public static final String DATE = "DATE";
    public static final String PSU_CORPORATE_ID_HEADER = "PSU-Corporate-ID";

    public static final String SIGNATURE = "Signature";

    public static final String PSU_ID_VALUE = "PSU-ID";

    static final String MULTICURRENCY_ACCOUNT = "DE12345678901234567890"
    static final String PAN_ACCOUNT = "5409050000000000"
    static final String BBAN_ACCOUNT = "BARC12345612345678"
    static final String CURRENCY1 = "USD"
    static final String CURRENCY2 = "EUR"
    static final String CURRENCY3 = "GBP"
    static final String ACCOUNTS_SUBMIT_XPATH = """//*[@id="approve"]"""
    static final String ACCOUNTS_DENY_XPATH = """//*[@id="oauth2_authz_confirm"]/div/div[3]/div/input[2]"""

    public static final String X_REQUEST_ID = "X-Request-ID"
    static final String PSU_IP_ADDRESS = "PSU-IP-Address"
    public static final String Date = "Date"
    static final String PSU_ID = "PSU-ID"
    static final String PSU_TYPE = "PSU-ID-Type"
    static final String CONSENT_ID_HEADER = "Consent-ID"

    public static final String FIREFOX_DRIVER_NAME = "webdriver.gecko.driver";
    public static final String CHROME_DRIVER_NAME = "webdriver.chrome.driver";
    public static final String LBL_SMSOTP_AUTHENTICATOR = "//h2[text()='Authenticating with SMSOTP']";
    public static final String TXT_OTP_CODE = "OTPcode";
    public static final String BTN_AUTHENTICATE = "//input[@id='authenticate']";
    public static final String AUTH_SIGNIN_XPATH = "//*[contains(@type,'submit')]";

    //Non-Regulatory
    public static final ArrayList<String> SCOPES_OPEN_ID = new ArrayList<>(Arrays.asList("openid"));
    public static final String USERNAME_FIELD_ID = "usernameUserInput";

    public static String CLIENT_ID = "CLIENT_ID"
    public static String APP_ACCESS_TKN = "APP_ACCESS_TKN"
    public static String USER_ACCESS_TKN = "USER_ACCESS_TKN"
}

