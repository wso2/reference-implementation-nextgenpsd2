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

package com.wso2.openbanking.berlin.consent.extensions.util;

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

}
