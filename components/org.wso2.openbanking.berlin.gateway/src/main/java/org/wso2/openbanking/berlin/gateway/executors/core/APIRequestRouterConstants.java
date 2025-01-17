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

package org.wso2.openbanking.berlin.gateway.executors.core;

/**
 * Constants used by the request router.
 */
public class APIRequestRouterConstants {

    public static final String API_TYPE_CUSTOM_PROP = "x-wso2-api-type";
    public static final String API_TYPE_NON_REGULATORY = "non-regulatory";
    public static final String DEFAULT = "Default";
    public static final String PAYMENTS = "Payments";
    public static final String ACCOUNTS = "Accounts";
    public static final String FUNDS_CONFIRMATIONS = "FundsConfirmations";
    public static final String PAYMENTS_TYPE = "payments";
    public static final String ACCOUNTS_TYPE = "accounts";
    public static final String ACCOUNTS_INITIATION_TYPE = "consents";
    public static final String FUNDS_CONFIRMATIONS_TYPE = "funds-confirmations";
    public static final String CONFIRMATION_OF_FUNDS_TYPE = "confirmation-of-funds";
}
