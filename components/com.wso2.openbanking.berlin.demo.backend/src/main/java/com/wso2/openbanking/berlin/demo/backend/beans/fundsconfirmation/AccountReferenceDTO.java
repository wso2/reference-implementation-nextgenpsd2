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
 * AccountReferenceDTO class
 */
public class AccountReferenceDTO {

    private String iban = null;

    private String bban = null;

    private String pan = null;

    private String maskedPan = null;

    private String msisdn = null;

    private String currency = null;

    public void setIban(String iban) {

        this.iban = iban;
    }

    public AccountReferenceDTO iban(String iban) {

        this.iban = iban;
        return this;
    }

    public void setBban(String bban) {

        this.bban = bban;
    }

    public AccountReferenceDTO bban(String bban) {

        this.bban = bban;
        return this;
    }

    public void setPan(String pan) {

        this.pan = pan;
    }

    public AccountReferenceDTO pan(String pan) {

        this.pan = pan;
        return this;
    }

    public void setMaskedPan(String maskedPan) {

        this.maskedPan = maskedPan;
    }

    public AccountReferenceDTO maskedPan(String maskedPan) {

        this.maskedPan = maskedPan;
        return this;
    }

    public void setMsisdn(String msisdn) {

        this.msisdn = msisdn;
    }

    public AccountReferenceDTO msisdn(String msisdn) {

        this.msisdn = msisdn;
        return this;
    }

    public void setCurrency(String currency) {

        this.currency = currency;
    }

    public AccountReferenceDTO currency(String currency) {

        this.currency = currency;
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReferenceDTO {\n");

        sb.append("    iban: ").append(toIndentedString(iban)).append("\n");
        sb.append("    bban: ").append(toIndentedString(bban)).append("\n");
        sb.append("    pan: ").append(toIndentedString(pan)).append("\n");
        sb.append("    maskedPan: ").append(toIndentedString(maskedPan)).append("\n");
        sb.append("    msisdn: ").append(toIndentedString(msisdn)).append("\n");
        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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
