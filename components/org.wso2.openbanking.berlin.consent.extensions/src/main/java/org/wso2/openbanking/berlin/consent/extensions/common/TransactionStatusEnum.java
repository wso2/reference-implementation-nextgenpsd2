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

package org.wso2.openbanking.berlin.consent.extensions.common;

import org.apache.commons.lang3.StringUtils;

/**
 * Specifies the status of a particular transaction.
 */
public enum TransactionStatusEnum {

    // Preceding check of technical validation was successful. Customer profile check was also successful.
    ACCP("AcceptedCustomerProfile"),
    // Settlement on the debtor’s account has been completed.
    ACSC("AcceptedSettlementCompleted"),
    // All preceding checks such as technical validation and customer profile were successful and therefore the
    // payment initiation has been accepted for execution.
    ACSP("AcceptedSettlementInProcess"),
    // Authentication and syntactical and semantic validation are successful
    ACTC("AcceptedTechnicalValidation"),
    // Instruction is accepted but a change will be made, such as date or remittance not sent.
    ACWC("AcceptedWithChange"),
    // Payment instruction included in the credit transfer is accepted without being posted to the creditor
    // customer’s account.
    ACWP("AcceptedWithoutPosting"),
    // Payment initiation has been received by the receiving agent.
    RCVD("Received"),
    // Payment initiation or individual transaction included in the payment initiation is pending. Further
    // checks and status update will be performed.
    PDNG("Pending"),
    // Payment initiation or individual transaction included in the payment initiation has been rejected.
    RJCT("Rejected"),
    // PSU has revoked a previously given consent for a particular payment.
    REVOKED("Revoked"),
    // Cancellation of a payment
    CANC("Cancelled"),
    // Partially Accepted Technical Correct in cases of multi level SCA.
    PATC("PartiallyAcceptedTechnicalCorrect");

    private String value;

    TransactionStatusEnum(String value) {

        this.value = value;
    }

    public static TransactionStatusEnum fromValue(String text) {

        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }

        for (TransactionStatusEnum statusEnum : TransactionStatusEnum.values()) {
            if (String.valueOf(statusEnum.value).equals(text)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + text);
    }

    public String toString() {

        return String.valueOf(value);
    }
}
