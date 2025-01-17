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

package org.wso2.openbanking.berlin.consent.extensions.util;

import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Test constants.
 */
public class TestConstants {

    public static final String VALID_PAYMENT_INITIATION_PATH = "payments/sepa-credit-transfers";
    public static final String WELL_KNOWN_ENDPOINT = "https://localhost:8243/.well-known/openid-configuration";
    public static final String PAYABLE_ACCOUNT_RETRIEVAL_ENDPOINT = "http://localhost:9443/api/openbanking/berlin/" +
            "backend/services/v130/accounts/payable";
    public static final String SHAREABLE_ACCOUNT_RETRIEVAL_ENDPOINT = "http://localhost:9443/api/openbanking/berlin/" +
            "backend/services/v130/accounts/shareable";
    public static final String USER_ID = "admin@wso2.com";
    public static final String DIFFERENT_USER_ID = "psu@wso2.com@carbon.super";
    public static final String REDIRECT_URI = "https://www.wso2.com";
    public static final String SAMPLE_QUERY_PARAMS = "sampleQueryParams";
    public static final String SAMPLE_APP_NAME = "sampleApp";
    public static final String INVALID_REQUEST_PATH = "invalid request path";
    public static final String SINGLE_CURRENCY_ACC_NUMBER = "DE73459340345034563141";
    public static final String MULTI_CURRENCY_ACC_NUMBER = "DE12345678901234567890";
    public static final List<String> SUPPORTED_ACC_REF_TYPES = Arrays.asList(ConsentExtensionConstants.IBAN,
            ConsentExtensionConstants.BBAN, ConsentExtensionConstants.MASKED_PAN);
    public static final String SCA_TYPE = "authenticationType";
    public static final String SCA_ID = "authenticationMethodId";
    public static final String SCA_VERSION = "authenticationVersion";
    public static final String SCA_NAME = "name";
    public static final String SCA_DESCRIPTION = "explanation";


}
