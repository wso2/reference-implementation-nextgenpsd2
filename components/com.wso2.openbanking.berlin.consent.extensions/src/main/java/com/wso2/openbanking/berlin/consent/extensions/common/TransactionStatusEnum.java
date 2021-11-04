/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.common;

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

        for (TransactionStatusEnum statusEnum : TransactionStatusEnum.values()) {
            if (String.valueOf(statusEnum.value).equals(text)) {
                return statusEnum;
            }
        }
        return null;
    }

    public String toString() {

        return String.valueOf(value);
    }
}
