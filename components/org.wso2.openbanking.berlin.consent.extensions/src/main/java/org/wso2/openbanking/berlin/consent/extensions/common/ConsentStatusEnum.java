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

package org.wso2.openbanking.berlin.consent.extensions.common;

import org.apache.commons.lang3.StringUtils;

/**
 * Consent Status enum.
 */
public enum ConsentStatusEnum {

    RECEIVED("received"),
    REJECTED("rejected"),
    PARTIALLY_AUTHORISED("partiallyAuthorised"),
    VALID("valid"),
    REVOKED_BY_PSU("revokedByPsu"),
    EXPIRED("expired"),
    TERMINATED_BY_TPP("terminatedByTpp");

    private String value;

    ConsentStatusEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(value);
    }

    public static ConsentStatusEnum fromValue(String text) {

        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }

        for (ConsentStatusEnum b : ConsentStatusEnum.values()) {
            if (text.equals(String.valueOf(b.value))) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + text);
    }

}
