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
 * AmountDTO class
 */
public class AmountDTO {

    private String currency = null;

    private String amount = null;

    public String getCurrency() {

        return currency;
    }

    public void setCurrency(String currency) {

        this.currency = currency;
    }

    public AmountDTO currency(String currency) {

        this.currency = currency;
        return this;
    }

    void setAmount(String amount) {

        this.amount = amount;
    }

    public AmountDTO amount(String amount) {

        this.amount = amount;
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AmountDTO {\n");

        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
        sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
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

