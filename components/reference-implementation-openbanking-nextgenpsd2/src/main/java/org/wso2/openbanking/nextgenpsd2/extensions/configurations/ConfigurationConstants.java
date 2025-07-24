/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.nextgenpsd2.extensions.configurations;

import java.util.List;
import java.util.Map;

/**
 * Placeholder class for configuration setup.
 */
public class ConfigurationConstants {
    public static final String IS_SCA_REQUIRED = "true";
    public static final String FREQ_PER_DAY = "4";
    public static final String VALID_UNTIL_DATE_CAP_ENABLED = "false";
    public static final String VALID_UNTIL_DAYS = "0";
    public static final List<Map<String, String>> SUPPORTED_SCA_APPROACHES = List.of(
            Map.of(
                    "Name", "REDIRECT",
                    "Default", "true"
            ),
            // DECOUPLED is not supported by the solution, included for testing purposes
            Map.of(
                    "Name", "DECOUPLED",
                    "Default", "false"
            )
    );
    public static final List<Map<String, String>> SUPPORTED_SCA_METHODS = List.of(
            Map.of(
                    "Type", "SMS_OTP",
                    "Version", "1.0",
                    "Id", "sms-otp",
                    "Name", "SMS OTP on Mobile",
                    "MappedApproach", "REDIRECT",
                    "Description", "SMS based one time password",
                    "Default", "true"
            ),
            // PUSH_OTP is not supported by the solution, included for testing purposes
            Map.of(
                    "Type", "PUSH_OTP",
                    "Version", "1.0",
                    "Id", "push-otp",
                    "Name", "PUSH OTP on Mobile app",
                    "MappedApproach", "DECOUPLED",
                    "Description", "Mobile push notification",
                    "Default", "false"
            )
    );
    public static final List<String> SUPPORTED_ACC_REFERNCE_TYPES = List.of("iban", "bban", "maskedPan");
    public static final String AIS_API_VERSION = "v1";
    public static final String PIS_API_VERSION = "v1";
    public static final String PIIS_API_VERSION = "v2";
    public static final String MAX_FUTURE_PAYMENT_DAYS = "30";
    public static final String OAUTH_METADATA_ENDPOINT = "https://localhost:8243/.well-known/openid-configuration";
    public static final String SHARABLE_ACCOUNTS_RETRIEVAL_ENDPOINT = "http://localhost:9766/api/openbanking/" +
            "nextgenpsd2/backend/services/v130/accounts/shareable";
    public static final String PAYABLE_ACCOUNTS_RETRIEVAL_ENDPOINT = "http://localhost:9766/api/openbanking/" +
            "nextgenpsd2/backend/services/v130/accounts/payable";
    public static final String PAYMENT_BACKEND_URL = "http://localhost:9766/api/openbanking/nextgenpsd2/backend/" +
            "services/payments";
}
