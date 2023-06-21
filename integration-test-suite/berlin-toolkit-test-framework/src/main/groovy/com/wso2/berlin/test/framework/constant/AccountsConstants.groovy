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

import com.wso2.berlin.test.framework.configuration.ConfigParser

class AccountsConstants {

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
}

