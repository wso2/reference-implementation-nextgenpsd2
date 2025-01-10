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

package org.wso2.openbanking.berlin.gateway.utils;

/**
 * Contains gateway related constants.
 */
public class GatewayConstants {

    public static final String ERROR_CODE = "ERROR_CODE";
    public static final String ERROR_MSG = "ERROR_MESSAGE";
    public static final String ERROR_DETAIL = "ERROR_DETAIL";
    public static final String STATUS_CODE = "statusCode";
    public static final String ERROR_RESPONSE = "errorResponse";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String RESPONSE_CAPS = "RESPONSE";
    public static final String TRUE = "true";
    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    public static final String PAYLOAD_FORMING_ERROR = "Error while forming fault payload";
    public static final String PAYLOAD_SETTING_ERROR = "Error while setting the json payload";
    public static final String THROTTLE_FAILURE_IDENTIFIER = "900806";
    public static final String AUTH_FAILURE_IDENTIFIER = "9";

    // Schema validation constants
    public static final String SCHEMA_VALIDATION_FAILURE_IDENTIFIER = "Schema validation failed in the Request:";
    public static final String SCHEMA_VALIDATION_REPORT_IDENTIFIER = "schema-validation-report";
    public static final String PATH_HEADER = "Header";
    public static final String PATH_QUERY = "Query";
    public static final String PATH_PAYMENT_PRODUCT = "Header.payment-product";

    public static final int API_AUTH_GENERAL_ERROR = 900900;
    public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
    public static final int API_AUTH_MISSING_CREDENTIALS = 900902;
    public static final int API_AUTH_ACCESS_TOKEN_EXPIRED = 900903;
    public static final int API_AUTH_ACCESS_TOKEN_INACTIVE = 900904;
    public static final int API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE = 900905;
    public static final int API_AUTH_INCORRECT_API_RESOURCE = 900906;
    public static final int API_BLOCKED = 900907;
    public static final int API_AUTH_FORBIDDEN = 900908;
    public static final int SUBSCRIPTION_INACTIVE = 900909;
    public static final int INVALID_SCOPE = 900910;
    public static final int API_AUTH_MISSING_OPEN_API_DEF = 900911;
    public static final int GRAPHQL_INVALID_QUERY = 900422;
}
