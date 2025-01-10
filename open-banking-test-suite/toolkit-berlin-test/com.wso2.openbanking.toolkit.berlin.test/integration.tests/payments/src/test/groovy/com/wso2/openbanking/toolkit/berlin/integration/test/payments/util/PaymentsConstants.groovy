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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.util

import com.wso2.openbanking.test.framework.util.ConfigParser

class PaymentsConstants {

    static config = ConfigParser.getInstance()
    static API_VERSION = config.getApiVersion()

    static final String PISP_PATH = getConsentPath()
    static final String SINGLE_PAYMENTS_PATH = PISP_PATH + "payments"
    static final String BULK_PAYMENTS_PATH = PISP_PATH + "bulk-payments"
    static final String PERIODIC_PAYMENTS_PATH = PISP_PATH + "periodic-payments"
    static final String ACCOUNTS_PATH = PISP_PATH + "consents"

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
    static final String SCA_STATUS_FINALISED = "finalised"

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

    static final String bbanAccount = "BARC12345612345678"
    static final String panAccount = "5409050000000000"

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
