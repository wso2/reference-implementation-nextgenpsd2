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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.util

import com.wso2.openbanking.test.framework.util.ConfigParser
import org.apache.commons.lang3.StringUtils

/**
 * Constants Variables of COF Tests
 */
class CofConstants {

    static config = ConfigParser.getInstance()
    static API_VERSION = config.getApiVersion()

    static final String CONSENT_PATH = getConsentPath()
    static final String COF_CONSENT_PATH = CONSENT_PATH + "consents/confirmation-of-funds"

    static final String CONSENT_STATUS_RECEIVED = "received"
    static final String CONSENT_STATUS_REJECTED = "rejected"
    static final String CONSENT_STATUS_TERMINATED_BY_TPP = "terminatedByTpp"
    static final String CONSENT_STATUS_VALID = "valid"
    static final String CONSENT_STATUS_PSUAUTHENTICATED = "psuAuthenticated"
    static final String SCA_STATUS_FINALISED = "finalised"

    static final String COF_RETRIEVAL_PATH = StringUtils.replace(CONSENT_PATH, "v2", "v1") +
            "funds-confirmations"

    static String getConsentPath() {

        def cofConsentPath

        if (API_VERSION.equalsIgnoreCase("1.3.3")) {
            cofConsentPath = "xs2a/1.3.3/"

        } else if (API_VERSION.equalsIgnoreCase("1.3.6")) {
            cofConsentPath = "xs2a/v2/"
        }
        return cofConsentPath
    }

    static final String currency = "EUR"
}
