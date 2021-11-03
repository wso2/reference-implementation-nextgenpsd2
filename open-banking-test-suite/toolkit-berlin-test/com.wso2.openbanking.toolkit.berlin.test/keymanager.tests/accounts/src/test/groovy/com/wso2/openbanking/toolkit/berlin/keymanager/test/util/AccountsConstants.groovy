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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.util

import com.wso2.openbanking.test.framework.util.ConfigParser

/**
 * Account Constant.
 */
class AccountsConstants {

    static config = ConfigParser.getInstance()
    static String CONSENT_MGT_API_CONTEXT = config.getInternalConsentMgtApiContext()

    static final String regularAcc = UUID.randomUUID().toString()
    static final String transactionAcc = UUID.randomUUID().toString()
    static final String transactionId = UUID.randomUUID().toString()

    static final String MANAGE_API = CONSENT_MGT_API_CONTEXT + "/manage"
    static final String VALIDATE_API = CONSENT_MGT_API_CONTEXT + "/validate"
    static final String ACCOUNTS_CONSENT_PATH = MANAGE_API + "/consents"

    static final String BULK_ACCOUNT_PATH = VALIDATE_API + "/" + "accounts"
    static final String SPECIFIC_ACCOUNT_PATH = BULK_ACCOUNT_PATH + "/" + regularAcc
    static final String TRANSACTIONS_PATH = BULK_ACCOUNT_PATH + "/" + transactionAcc + "/transactions"
    static final String SPECIFIC_TRANSACTIONS_PATH = BULK_ACCOUNT_PATH + "/" + transactionAcc +
            "/transactions/" + transactionId
    static final String BALANCES_PATH = BULK_ACCOUNT_PATH + "/" + regularAcc + "/balances"

    static final String CARD_ACCOUNTS_PATH = VALIDATE_API + "/" + "card-accounts"
    static final String SPECIFIC_CARD_ACCOUNTS_PATH = CARD_ACCOUNTS_PATH + "/" + regularAcc
    static final String CARD_ACCOUNTS_BALANCES_PATH = CARD_ACCOUNTS_PATH + "/" + regularAcc + "/balances"
    static final String CARD_ACCOUNTS_TRANSACTIONS_PATH = CARD_ACCOUNTS_PATH + "/" + transactionAcc + "/transactions"

    static final String CONSENT_STATUS_RECEIVED = "received"
    static final String CONSENT_STATUS_VALID = "valid"
    static final String CONSENT_STATUS_REJECTED = "rejected"
    static final String CONSENT_STATUS_TERMINATEDBYTPP = "terminatedByTpp"
    static final String CONSENT_STATUS_PSUAUTHENTICATED = "psuAuthenticated"
    static final String CONSENT_STATUS_REVOKEDBYPSU = "revokedByPsu"

    //Bank Offered Consent - Account Selection
    static final String DD_ACCOUNTS_ACC_LIST = "//select[@id='accSelectAccounts']"
    static final String DD_TRANSACTION_ACC_LIST = "//select[@id='accSelectTransactions']"
    static final String DD_BALANCES_ACC_LIST = "//select[@id='accSelectBalances']"

    static final String ACCOUNT_ID = "DE12345678901234567890"
}
