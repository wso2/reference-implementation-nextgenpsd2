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

package com.wso2.openbanking.berlin.demo.backend.utils;

/**
 * Constant values.
 */
public final class Constants {

    private Constants() {

    }

    /**
     * AccountService constants class
     */
    public static final class AccountService {

        public static final String VALID = "valid";
        public static final String MESSAGE = "message";
        public static final String CODE = "code";
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String BOOKED = "booked";
        public static final String PENDING = "pending";
        public static final String BOTH = "both";

        private AccountService() {

        }
    }
}
