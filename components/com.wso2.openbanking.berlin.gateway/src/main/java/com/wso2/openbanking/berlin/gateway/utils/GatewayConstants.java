/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.gateway.utils;

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
