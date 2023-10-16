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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.util.ConfigParser

class AccountsConstants {

    static config = ConfigParser.getInstance()
    static API_VERSION = config.getApiVersion()

    static final String regularAcc = "DE98765432109876543210"
    static final String transactionAcc = "DE98765432109876543210"
    static final String transactionId = "DE98765432109876543210"

    public static String AISP_PATH = getConsentPath()
    static final String ACCOUNTS_PATH = AISP_PATH + "accounts"
    static final String CONSENT_PATH = AISP_PATH + "consents"
    static final String SPECIFIC_ACCOUNT_PATH = ACCOUNTS_PATH + "/" + regularAcc
    static final String TRANSACTIONS_PATH = ACCOUNTS_PATH + "/" + transactionAcc + "/transactions"
    static final String SPECIFIC_TRANSACTIONS_PATH = ACCOUNTS_PATH + "/" + transactionAcc + "/transactions/" + transactionId
    static final String BALANCES_PATH = ACCOUNTS_PATH + "/" + regularAcc + "/balances"
    static final String TRANSACTIONS_PATHT_MULTICURRENCY = ACCOUNTS_PATH + "/" + BerlinConstants
            .MULTICURRENCY_ACCOUNT + "/transactions"
    static final String BALANCES_PATH_MULTICURRENCY = ACCOUNTS_PATH + "/" + BerlinConstants
            .MULTICURRENCY_ACCOUNT + "/balances"

    static final String CARD_ACCOUNTS_PATH = AISP_PATH + "card-accounts"
    static final String SPECIFIC_CARD_ACCOUNTS_PATH = CARD_ACCOUNTS_PATH + "/" + regularAcc
    static final String CARD_ACCOUNTS_BALANCES_PATH = CARD_ACCOUNTS_PATH + "/" + regularAcc + "/balances"
    static final String CARD_ACCOUNTS_TRANSACTIONS_PATH = CARD_ACCOUNTS_PATH + "/" + transactionAcc + "/transactions"

    static final String V110_AISP_PATH = "AccountsInfoAPI/v1.1.0/"
    static final String V110_CONSENT_PATH = V110_AISP_PATH + "consents"
    static final String V110_SPECIFIC_ACCOUNT_PATH = V110_AISP_PATH + "accounts" + "/" + regularAcc

    static final String CONSENT_STATUS_RECEIVED = "received"
    static final String CONSENT_STATUS_VALID = "valid"
    static final String CONSENT_STATUS_REJECTED = "rejected"
    static final String CONSENT_STATUS_TERMINATEDBYTPP = "terminatedByTpp"
    static final String CONSENT_STATUS_PSUAUTHENTICATED = "psuAuthenticated"
    static final String CONSENT_STATUS_FINALISED = "finalised"

    //Bank Offered Consent - Account Selection
    static final String ACCOUNTS_LIST_NORMAL_ACC = "//label[4]/input[@name='checkedAccountsAccountRefs']"
    static final String TRANSACTION_LIST_NORMAL_ACC = "//label[4]/input[@name='checkedTransactionsAccountRefs']"
    static final String BALANCES_LIST_NORMAL_ACC = "//label[4]/input[@name='checkedBalancesAccountRefs']"
    static final String ACCOUNTS_LIST_MULTICURRENCY_ACC = "//label[1]/input[@name='checkedAccountsAccountRefs']"
    static final String TRANSACTION_LIST_MULTICURRENCY_ACC = "//label[1]/input[@name='checkedTransactionsAccountRefs']"
    static final String BALANCES_LIST_MULTICURRENCY_ACC = "//label[1]/input[@name='checkedBalancesAccountRefs']"

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
