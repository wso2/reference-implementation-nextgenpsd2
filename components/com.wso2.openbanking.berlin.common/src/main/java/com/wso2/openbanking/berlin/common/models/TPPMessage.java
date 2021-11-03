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

package com.wso2.openbanking.berlin.common.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotNull;

/**
 * TPPMessage class.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TPPMessage {


    /**
     * Error category enum.
     */
    public enum CategoryEnum {
        ERROR, WARNING
    };
    @NotNull
    private CategoryEnum category = null;

    /**
     * Error Code Enum.
     */
    public enum CodeEnum {

        // Service unspecific HTTP error codes
        CERTIFICATE_INVALID("CERTIFICATE_INVALID"),
        CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED"),
        CERTIFICATE_BLOCKED("CERTIFICATE_BLOCKED"),
        CERTIFICATE_REVOKED("CERTIFICATE_REVOKED"),
        CERTIFICATE_MISSING("CERTIFICATE_MISSING"),
        SIGNATURE_INVALID("SIGNATURE_INVALID"),
        SIGNATURE_MISSING("SIGNATURE_MISSING"),
        FORMAT_ERROR("FORMAT_ERROR"),
        PSU_CREDENTIALS_INVALID("PSU_CREDENTIALS_INVALID"),
        SERVICE_INVALID("SERVICE_INVALID"),
        SERVICE_BLOCKED("AcceptedCustomerProfile"),
        CORPORATE_ID_INVALID("CORPORATE_ID_INVALID"),
        CONSENT_UNKNOWN("CONSENT_UNKNOWN"),
        CONSENT_INVALID("CONSENT_INVALID"),
        CONSENT_EXPIRED("CONSENT_EXPIRED"),
        TOKEN_UNKNOWN("TOKEN_UNKNOWN"),
        TOKEN_INVALID("TOKEN_INVALID"),
        TOKEN_EXPIRED("TOKEN_EXPIRED"),
        RESOURCE_UNKNOWN("RESOURCE_UNKNOWN"),
        RESOURCE_EXPIRED("RESOURCE_EXPIRED"),
        TIMESTAMP_INVALID("TIMESTAMP_INVALID"),
        PERIOD_INVALID("PERIOD_INVALID"),
        SCA_METHOD_UNKNOWN("SCA_METHOD_UNKNOWN"),
        TRANSACTION_ID_INVALID("TRANSACTION_ID_INVALID"),
        SESSIONS_NOT_SUPPORTED("SESSIONS_NOT_SUPPORTED"),
        ACCESS_EXCEEDED("ACCESS_EXCEEDED"),
        REQUESTED_FORMATS_INVALID("REQUESTED_FORMATS_INVALID"),
        INVALID_STATUS_VALUE("INVALID_STATUS_VALUE"),

        // PIS specific HTTP error codes
        PRODUCT_INVALID("PRODUCT_INVALID"),
        PRODUCT_UNKNOWN("PRODUCT_UNKNOWN"),
        PAYMENT_FAILED("PAYMENT_FAILED"),
        REQUIRED_KID_MISSING("REQUIRED_KID_MISSING"),
        EXECUTION_DATE_INVALID("EXECUTION_DATE_INVALID"),
        CANCELLATION_INVALID("CANCELLATION_INVALID");

        private String value;

        CodeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static CodeEnum fromValue(String text) {
            for (CodeEnum b : CodeEnum.values()) {
                if (text.equals(String.valueOf(b.value))) {
                    return b;
                }
            }
            return null;
        }
    }

    private CodeEnum code = null;

    private String text = null;

    private String path = null;
    /**
     **/
    @JsonProperty("category")
    public CategoryEnum getCategory() {
        return category;
    }
    public void setCategory(CategoryEnum category) {
        this.category = category;
    }


    /**
     **/
    @JsonProperty("code")
    public CodeEnum getCode() {
        return code;
    }
    public void setCode(CodeEnum code) {
        this.code = code;
    }


    /**
     **/
    @JsonProperty("text")
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    /**
     **/
    @JsonProperty("path")
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class TppMessageDTO {\n");

        sb.append("  category: ").append(category).append("\n");
        sb.append("  code: ").append(code).append("\n");
        sb.append("  text: ").append(text).append("\n");
        sb.append("  path: ").append(path).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
