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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.payments.util

import com.wso2.openbanking.test.framework.util.ConfigParser

class PaymentsConstants {

    static config = ConfigParser.getInstance()
    static String CONSENT_MGT_API_CONTEXT = config.getInternalConsentMgtApiContext()

    static final String MANAGE_API = CONSENT_MGT_API_CONTEXT + "/manage"
    static final String SINGLE_PAYMENTS_CONSENT_PATH = MANAGE_API + "/payments" + "/sepa-credit-transfers"
    static final String BULK_PAYMENTS_CONSENT_PATH = MANAGE_API + "/bulk-payments" + "/sepa-credit-transfers"
    static final String PERIODIC_PAYMENTS_CONSENT_PATH = MANAGE_API + "/periodic-payments" + "/sepa-credit-transfers"

    static final String TRANSACTION_STATUS_RECEIVED = "RCVD"
    static final String TRANSACTION_STATUS_ACCP = "ACCP"
    static final String TRANSACTION_STATUS_REJECTED = "RJCT"
    static final String TRANSACTION_STATUS_CANC = "CANC"
    static final String TRANSACTION_STATUS_ACTC = "ACTC"

    static final String SCA_STATUS_RECEIVED = "received"
    static final String SCA_STATUS_PSU_AUTHENTICATED= "psuAuthenticated"

    static final String PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS = "sepa-credit-transfers"

    static final String instructedAmount = "123.50"
    static final String instructedAmountCurrency = "EUR"
    static final String debtorAccount1 = "DE12345678901234567890"
    static final String creditorAccount1 = "DE98765432109876543210"
    static final String creditorName1 = "Merchant123"
    static final String debtorAccount2 = "DE40100100103307118608"
    static final String creditorAccount2 = "DE23100120020123456789"
    static final String creditorName2 = "Merchant"
    static final String accountAttributeIban = "iban"
    static final String accountAttributeBban = "bban"
    static final String accountAttributePan = "pan"

    static final String executionRuleFollowing = "following"
    static final String executionRuleLatest = "latest"
    static final String frequency = "Monthly"
    static final String dayOfExecution  = "01"

    static final String bbanAccount = "5390 0754 7034"
    static final String panAccount = "4685-2421-1836-5024"
}
