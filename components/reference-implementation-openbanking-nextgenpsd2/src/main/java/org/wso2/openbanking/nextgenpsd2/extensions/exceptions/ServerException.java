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
import org.wso2.openbanking.nextgenpsd2.extensions.model.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.ErrorResponse;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;

import javax.ws.rs.core.Response;

/**
 * Exception class for internal server and bad request errors,
 * formatted to return a custom ErrorResponse object.
 */
public class ServerException extends RuntimeException {

    /**
     * Error code enum for server exceptions.
     */
    public enum ErrorCode {
        BAD_REQUEST(Response.Status.BAD_REQUEST),
        INTERNAL_SERVER_ERROR(Response.Status.INTERNAL_SERVER_ERROR);

        private final Response.Status status;

        ErrorCode(Response.Status status) {
            this.status = status;
        }

        public Response.Status getStatus() {
            return status;
        }
    }

    private final ErrorCode errorCode;
    private final JSONObject data;

    public ServerException(ErrorCode errorCode, JSONObject data) {
        super(data.toString());
        this.errorCode = errorCode;
        this.data = data;
    }

    public ServerException(ErrorCode errorCode, JSONObject data, Throwable cause) {
        super(data.toString(), cause);
        this.errorCode = errorCode;
        this.data = data;
    }

    public ServerException(ErrorCode errorCode, TPPMessage.CodeEnum code, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = ErrorUtil.constructBerlinError(
                null, TPPMessage.CategoryEnum.ERROR, code,
                message);
    }

    public ServerException(ErrorCode errorCode, TPPMessage.CodeEnum code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.data = ErrorUtil.constructBerlinError(
                null, TPPMessage.CategoryEnum.ERROR, code,
                message);
    }

    /**
     * Getter for error status to set to response.
     * @return
     */
    public Response.Status getStatus() {
        return this.errorCode.getStatus();
    }

    /**
     * Format the error to a simplified ErrorResponse object.
     * @return JSONObject representing the error
     */
    public JSONObject getFormattedError() {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(ErrorResponse.StatusEnum.ERROR);
        errorResponse.setData(data);
        return new JSONObject(errorResponse);
    }

    /**
     * Return the formatted error as a string.
     * @return String representation of the error
     */
    public String getFormattedErrorAsString() {
        return getFormattedError().toString();
    }
}
