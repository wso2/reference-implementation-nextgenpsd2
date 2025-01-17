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

package org.wso2.openbanking.berlin.gateway.executors.utils;

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
