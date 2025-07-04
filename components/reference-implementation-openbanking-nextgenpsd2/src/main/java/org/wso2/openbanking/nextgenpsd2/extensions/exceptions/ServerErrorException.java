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
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;

import javax.ws.rs.core.Response;

/**
 * Exception class for internal server and bad request errors,
 * formatted to return a custom ErrorResponse object.
 */
public class ServerErrorException extends Exception {

    private final Response.Status errorStatus = Response.Status.INTERNAL_SERVER_ERROR;
    private final JSONObject data;

    public ServerErrorException(JSONObject data) {
        super(data.toString());
        this.data = data;
    }

    public ServerErrorException(JSONObject data, Throwable cause) {
        super(data.toString(), cause);
        this.data = data;
    }

    public ServerErrorException(TPPMessage.CodeEnum code, String message) {
        super(message);
        this.data = ErrorUtil.constructBerlinError(
                null, TPPMessage.CategoryEnum.ERROR, code,
                message);
    }

    public ServerErrorException(TPPMessage.CodeEnum code, String message, Throwable cause) {
        super(message, cause);
        this.data = ErrorUtil.constructBerlinError(
                null, TPPMessage.CategoryEnum.ERROR, code,
                message);
    }

    /**
     * Getter for error status to set to response.
     * @return
     */
    public Response.Status getStatus() {
        return this.errorStatus;
    }

    /**
     * Format the error to a simplified ErrorResponse object.
     * @return JSONObject representing the error
     */
    public JSONObject getFormattedError() {
        return ErrorUtil.getFormattedErrorResponse(data);
    }

    /**
     * Return the formatted error as a string.
     * @return String representation of the error
     */
    public String getFormattedErrorAsString() {
        return getFormattedError().toString();
    }
}
