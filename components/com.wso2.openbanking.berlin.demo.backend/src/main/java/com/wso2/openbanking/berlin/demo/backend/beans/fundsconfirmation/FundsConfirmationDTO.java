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

package com.wso2.openbanking.berlin.demo.backend.beans.fundsconfirmation;

/**
 * FundsConfirmationDTO class
 */
public class FundsConfirmationDTO {

    private String cardNumber = null;

    private AccountReferenceDTO account = null;

    private String payee = null;

    private AmountDTO instructedAmount = null;

    public String getCardNumber() {

        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {

        this.cardNumber = cardNumber;
    }

    public FundsConfirmationDTO cardNumber(String cardNumber) {

        this.cardNumber = cardNumber;
        return this;
    }

    public AccountReferenceDTO getAccount() {

        return account;
    }

    public void setAccount(AccountReferenceDTO account) {

        this.account = account;
    }

    public FundsConfirmationDTO account(AccountReferenceDTO account) {

        this.account = account;
        return this;
    }

    public void setPayee(String payee) {

        this.payee = payee;
    }

    public FundsConfirmationDTO payee(String payee) {

        this.payee = payee;
        return this;
    }

    public AmountDTO getInstructedAmount() {

        return instructedAmount;
    }

    public void setInstructedAmount(AmountDTO instructedAmount) {

        this.instructedAmount = instructedAmount;
    }

    public FundsConfirmationDTO instructedAmount(AmountDTO instructedAmount) {

        this.instructedAmount = instructedAmount;
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class FundsConfirmationDTO {\n");

        sb.append("    cardNumber: ").append(toIndentedString(cardNumber)).append("\n");
        sb.append("    account: ").append(toIndentedString(account)).append("\n");
        sb.append("    payee: ").append(toIndentedString(payee)).append("\n");
        sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private static String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
