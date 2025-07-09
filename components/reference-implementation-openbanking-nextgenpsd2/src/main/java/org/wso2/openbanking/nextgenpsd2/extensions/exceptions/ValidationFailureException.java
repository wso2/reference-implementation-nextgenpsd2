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

package org.wso2.openbanking.nextgenpsd2.extensions.exceptions;

import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;

/**
 * Exception class for success responses carrying FailedResponse objects.
 */
public class ValidationFailureException extends Exception {

    /**
     * Error response code enums.
     */
    public enum ErrorCode {
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        METHOD_NOT_ALLOWED(405),
        INTERNAL_SERVER_ERROR(500);

        private final int code;

        ErrorCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private final ErrorCode errorCode;
    private final JSONObject data;

    public ValidationFailureException(ErrorCode errorCode, JSONObject data) {
        super(ErrorUtil.getErrorMessage(data));
        this.errorCode = errorCode;
        this.data = data;
    }

    public ValidationFailureException(ErrorCode errorCode, JSONObject data, Throwable e) {
        super(ErrorUtil.getErrorMessage(data), e);
        this.errorCode = errorCode;
        this.data = data;
    }

    /**
     * Format error to the expected FailedResponse object format.
     * @return
     */
    public JSONObject getFormattedError() {
        return ErrorUtil.getFormattedFailedResponse(this.errorCode.getCode(), this.data);
    }

    /**
     * Return formatted error as string.
     * @return
     */
    public String getFormattedErrorAsString() {
        return getFormattedError().toString();
    }
}
