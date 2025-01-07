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
