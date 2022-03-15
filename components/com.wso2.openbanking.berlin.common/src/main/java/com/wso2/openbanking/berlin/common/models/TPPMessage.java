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
        CERTIFICATE_INVALID("CERTIFICATE_INVALID", "401"),
        CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED", "401"),
        CERTIFICATE_BLOCKED("CERTIFICATE_BLOCKED", "401"),
        CERTIFICATE_REVOKED("CERTIFICATE_REVOKED", "401"),
        CERTIFICATE_MISSING("CERTIFICATE_MISSING", "401"),
        SIGNATURE_INVALID("SIGNATURE_INVALID", "401"),
        SIGNATURE_MISSING("SIGNATURE_MISSING", "401"),
        FORMAT_ERROR("FORMAT_ERROR", "400"),
        PSU_CREDENTIALS_INVALID("PSU_CREDENTIALS_INVALID", "401"),
        SERVICE_INVALID("SERVICE_INVALID", "400"),
        SERVICE_INVALID_405("SERVICE_INVALID", "405"),
        SERVICE_BLOCKED("SERVICE_BLOCKED", "403"),
        CORPORATE_ID_INVALID("CORPORATE_ID_INVALID", "401"),
        CONSENT_UNKNOWN_403("CONSENT_UNKNOWN", "403"),
        CONSENT_UNKNOWN("CONSENT_UNKNOWN", "400"),
        CONSENT_INVALID("CONSENT_INVALID", "401"),
        CONSENT_EXPIRED("CONSENT_EXPIRED", "401"),
        TOKEN_UNKNOWN("TOKEN_UNKNOWN", "401"),
        TOKEN_INVALID("TOKEN_INVALID", "401"),
        TOKEN_EXPIRED("TOKEN_EXPIRED", "401"),
        RESOURCE_UNKNOWN("RESOURCE_UNKNOWN", "400"),
        RESOURCE_UNKNOWN_403("RESOURCE_UNKNOWN", "403"),
        RESOURCE_UNKNOWN_404("RESOURCE_UNKNOWN", "404"),
        RESOURCE_EXPIRED("RESOURCE_EXPIRED", "400"),
        RESOURCE_EXPIRED_403("RESOURCE_EXPIRED", "403"),
        ROLE_INVALID("ROLE_INVALID", "401"),
        TIMESTAMP_INVALID("TIMESTAMP_INVALID", "400"),
        PERIOD_INVALID("PERIOD_INVALID", "400"),
        SCA_METHOD_UNKNOWN("SCA_METHOD_UNKNOWN", "400"),
        SESSIONS_NOT_SUPPORTED("SESSIONS_NOT_SUPPORTED", "400"),
        ACCESS_EXCEEDED("ACCESS_EXCEEDED", "429"),
        REQUESTED_FORMATS_INVALID("REQUESTED_FORMATS_INVALID", "406"),
        INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "500"),

        // PIS specific HTTP error codes
        PRODUCT_INVALID("PRODUCT_INVALID", "403"),
        PRODUCT_UNKNOWN("PRODUCT_UNKNOWN", "404"),
        PAYMENT_FAILED("PAYMENT_FAILED", "400"),
        REQUIRED_KID_MISSING("REQUIRED_KID_MISSING", "401"),
        EXECUTION_DATE_INVALID("EXECUTION_DATE_INVALID", "400"),
        CANCELLATION_INVALID("CANCELLATION_INVALID", "405");

        private String value;
        private String statusCode;

        CodeEnum(String value, String statusCode) {
            this.value = value;
            this.statusCode = statusCode;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        public String getStatusCode() {

            return statusCode;
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
