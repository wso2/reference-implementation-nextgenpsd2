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

package com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util

import com.wso2.openbanking.test.framework.util.ConfigParser

/**
 * Constant class for non regulatory flow
 */
class NonRegulatoryConstants {

    static config = ConfigParser.getInstance()
    static API_VERSION = config.getApiVersion()

    //Non-Regulatory Constants
    public static final String CHARSET = "charset"
    static final String CHARSET_TYPE="UTF-8"
    static final String CONTENT_TYPE="application/json"
    public static final String API_PATH = "/pizzashack/1.0.0/order"
    public static final String NON_REG_XPATH_APPROVE_BTN = "//div/input[@id='approve']"
    public static final String PASSWORD_GRANT = "password"
    static final String UK_AISP_PATH = "/open-banking/v3.1/aisp/"
    static final String UK_ACCOUNTS_PATH = UK_AISP_PATH + "accounts"
    static final String BG_AISP_PATH = "/xs2a/1.3.3/"
    static final String BG_ACCOUNTS_PATH = BG_AISP_PATH + "accounts"
    public static final String CLIENT_CREDENTIALS = "client_credentials"
    public static final ArrayList<String> SCOPES_OPEN_ID = new ArrayList<>(Arrays.asList("openid"))
    public static final ArrayList<String> SCOPES_ACCOUNTS = new ArrayList<>(Arrays.asList("openid", "accounts"))
    public static final BASIC_HEADER_WITHOUT_VALUE = "Basic "
    public static final REGULATORY_API_PATH = "/open-banking/v3.1/aisp/accounts"

    static final String BERLIN_AISP_PATH = getConsentPath()
    static final String BERLIN_ACCOUNTS_PATH = BERLIN_AISP_PATH + "accounts"

    static String getConsentPath() {

        def aispPath

        if (API_VERSION.equalsIgnoreCase("1.1.0")) {
            aispPath = "/AccountsInfoAPI/v1.1.0/"

        } else if (API_VERSION.equalsIgnoreCase("1.3.3")) {
            aispPath = "/xs2a/1.3.3/"

        } else if (API_VERSION.equalsIgnoreCase("1.3.6")) {
            aispPath = "/xs2a/v1/"
        }
        return aispPath
    }

    static final enum SCOPES {
        OPEN_ID(["openid"])

        private final List<String> scopes

        SCOPES(List<String> scopes) {
            this.scopes = scopes
        }

        List<String> getScopes() {
            return scopes
        }

    }


    static final String ERROR_DESCRIPTION = "error_description"
    static final String ERROR = "error"
    static final String ERRORS_ERRORCODE = "Errors[0].ErrorCode"
    static final String ERRORS_PATH = "Errors[0].Path"
    static final String ERRORS_MESSAGE = "Errors[0].Message"

    static final int STATUS_CODE_204 = 204
    static final int STATUS_CODE_201 = 201
    static final int STATUS_CODE_200 = 200
    static final int STATUS_CODE_400 = 400
    static final int STATUS_CODE_401 = 401
    static final int STATUS_CODE_403 = 403
    static final int STATUS_CODE_404 = 404
    static final int STATUS_CODE_500 = 500
}
