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

package com.wso2.openbanking.berlin.gateway.executors.utils;

/**
 * Gateway constants
 */
public class GatewayConstants {

    public static final String X_IDEMPOTENCY_KEY = "X-Request-ID";
    public static final String CREATED_TIME = "Date";
    public static final String REQUEST_CACHE_KEY = "Request";
    public static final String RESPONSE_CACHE_KEY = "Response";
    public static final String CREATED_TIME_CACHE_KEY = "Created_Time";
    public static final String IDEMPOTENCY_KEY_CACHE_KEY = "Idempotency_Key";
    public static final String IS_RETURN_RESPONSE = "isReturnResponse";
    public static final String MODIFIED_STATUS = "ModifiedStatus";

    public static final String TRUE = "true";
    public static final String IS_IDEMPOTENT = "isIdempotent";
    public static final String PAYMENTS = "payment";
    public static final String BULK_PAYMENTS = "bulk-payments";
    public static final String PERIODIC_PAYMENTS = "periodic-payments";
}
