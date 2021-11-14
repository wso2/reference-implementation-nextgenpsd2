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

package com.wso2.openbanking.berlin.demo.backend.beans.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account class
 */
public class Account {

    /**
     * IBAN of account
     **/
    private String iban;

    /**
     * BBAN of account
     **/
    private String bban;

    /**
     * PAN of account
     **/
    private String pan;

    private String maskedPan;

    private String msisdn;

    private String currency;

    /**
     * IBAN of account
     *
     * @return iban
     **/
    @JsonProperty("iban")
    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Account iban(String iban) {
        this.iban = iban;
        return this;
    }

    /**
     * BBAN of account
     *
     * @return bban
     **/
    @JsonProperty("bban")
    public String getBban() {
        return bban;
    }

    public void setBban(String bban) {
        this.bban = bban;
    }

    public Account bban(String bban) {
        this.bban = bban;
        return this;
    }

    /**
     * PAN of account
     *
     * @return pan
     **/
    @JsonProperty("pan")
    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public Account pan(String pan) {
        this.pan = pan;
        return this;
    }

    @JsonProperty("maskedPan")
    public String getMaskedPan() {

        return maskedPan;
    }

    public void setMaskedPan(String maskedPan) {

        this.maskedPan = maskedPan;
    }

    @JsonProperty("msisdn")
    public String getMsisdn() {

        return msisdn;
    }

    public void setMsisdn(String msisdn) {

        this.msisdn = msisdn;
    }

    @JsonProperty("currency")
    public String getCurrency() {

        return currency;
    }

    public void setCurrency(String currency) {

        this.currency = currency;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Account {\n");

        sb.append("    iban: ").append(toIndentedString(iban)).append("\n");
        sb.append("    bban: ").append(toIndentedString(bban)).append("\n");
        sb.append("    pan: ").append(toIndentedString(pan)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

