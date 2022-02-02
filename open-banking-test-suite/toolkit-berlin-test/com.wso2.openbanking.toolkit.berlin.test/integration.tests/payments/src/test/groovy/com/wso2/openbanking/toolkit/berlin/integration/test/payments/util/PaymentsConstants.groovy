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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.util

import com.wso2.openbanking.test.framework.util.ConfigParser

class PaymentsConstants {

    static config = ConfigParser.getInstance()
    static API_VERSION = config.getApiVersion()

    static final String PISP_PATH = getConsentPath()
    static final String SINGLE_PAYMENTS_PATH = PISP_PATH + "payments"
    static final String BULK_PAYMENTS_PATH = PISP_PATH + "bulk-payments"
    static final String PERIODIC_PAYMENTS_PATH = PISP_PATH + "periodic-payments"

    static final String V110_PISP_PATH = "PaymentsAPI/v1.1.0/"
    static final String V110_SINGLE_PAYMENTS_PATH = V110_PISP_PATH + "payments"
    static final String V110_BULK_PAYMENTS_PATH = V110_PISP_PATH + "bulk-payments"
    static final String V110_PERIODIC_PAYMENTS_PATH = V110_PISP_PATH + "periodic-payments"

    static final String TRANSACTION_STATUS_RECEIVED = "RCVD"
    static final String TRANSACTION_STATUS_ACCP = "ACCP"
    static final String TRANSACTION_STATUS_REJECTED = "RJCT"
    static final String TRANSACTION_STATUS_CANC = "CANC"
    static final String TRANSACTION_STATUS_ACTC = "ACTC"

    static final String SCA_STATUS_RECEIVED = "received"
    static final String SCA_STATUS_PSU_AUTHENTICATED= "psuAuthenticated"

    static final String PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS = "sepa-credit-transfers"
    static final String PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS = "instant-sepa-credit-transfers"
    static final String PAYMENT_PRODUCT_TARGET_2_PAYMENTS = "target-2-payments"
    static final String PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS = "cross-border-credit-transfers"

    static final String instructedAmount = "123.50"
    static final String instructedAmountCurrency = "EUR"
    static final String instructedAmountCurrency2 = "USD"
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
    static final String executionRulePreceding = "preceding"
    static final String frequency = "Monthly"
    static final String dayOfExecution  = "01"

    static final String bbanAccount = "5390 0754 7034"
    static final String panAccount = "4685-2421-1836-5024"

    static String getConsentPath() {

        def pispPath

        if (API_VERSION.equalsIgnoreCase("1.1.0")) {
            pispPath = "PaymentsAPI/v1.1.0/"

        } else if (API_VERSION.equalsIgnoreCase("1.3.3")) {
            pispPath = "xs2a/1.3.3/"

        } else if (API_VERSION.equalsIgnoreCase("1.3.6")) {
            pispPath = "xs2a/v1/"
        }
        return pispPath
    }
}
