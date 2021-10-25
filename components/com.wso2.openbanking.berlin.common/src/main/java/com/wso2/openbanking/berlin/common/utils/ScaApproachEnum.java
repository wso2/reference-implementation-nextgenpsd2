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

package com.wso2.openbanking.berlin.common.utils;

/**
 * SCA Approaches enum.
 */
public enum ScaApproachEnum {

    REDIRECT("REDIRECT"),
    DECOUPLED("DECOUPLED"),
    EMBEDDED("EMBEDDED");

    private String value;

    ScaApproachEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(value);
    }

    public static ScaApproachEnum fromValue(String text) {
        for (ScaApproachEnum b : ScaApproachEnum.values()) {
            if (text.equals(String.valueOf(b.value))) {
                return b;
            }
        }
        return null;
    }

}
