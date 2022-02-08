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

package com.wso2.openbanking.berlin.gateway.test;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Constants used by unit tests for Gateway module.
 */
public class GatewayTestConstants {

    public static final String INVALID_EXECUTOR_CLASS =
            "com.wso2.openbanking.berlin.gateway.test.executor.InvalidClass";
    public static final String VALID_EXECUTOR_CLASS =
            "com.wso2.openbanking.berlin.gateway.test.MockOBExecutor";

    public static final Map<Integer, String> VALID_EXECUTOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>(1, VALID_EXECUTOR_CLASS))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static final Map<String, Map<Integer, String>> FULL_VALIDATOR_MAP = Stream.of(
                    new AbstractMap.SimpleImmutableEntry<>("Default", VALID_EXECUTOR_MAP),
                    new AbstractMap.SimpleImmutableEntry<>("Payments", VALID_EXECUTOR_MAP))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}
