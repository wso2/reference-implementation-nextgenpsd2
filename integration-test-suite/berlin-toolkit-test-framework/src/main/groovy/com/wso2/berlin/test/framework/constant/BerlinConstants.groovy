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

package com.wso2.berlin.test.framework.constant


import com.wso2.openbanking.test.framework.configuration.OBConfigParser
import com.wso2.openbanking.test.framework.constant.OBConstants

class BerlinConstants extends OBConstants {

    public static final String CONFIG_FILE_LOCATION = "../berlin-toolkit-test-framework/src/main/resources/TestConfiguration.xml";
    static config = OBConfigParser.getInstance()
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

