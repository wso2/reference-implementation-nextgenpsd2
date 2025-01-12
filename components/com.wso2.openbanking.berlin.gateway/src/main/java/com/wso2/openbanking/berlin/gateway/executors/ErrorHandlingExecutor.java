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

package com.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Executor to handle errors in Berlin format.
 */
public class ErrorHandlingExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(ErrorHandlingExecutor.class);

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        handleRequestError(obapiRequestContext);
    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        handleRequestError(obapiRequestContext);
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to construct the error response for errors in the Request.
     * @param obapiRequestContext
     */
    protected void handleRequestError(OBAPIRequestContext obapiRequestContext) {

        if (obapiRequestContext.isError()) {

            // If an error occurs in a single executor, all the other executors will not execute, therefore,
            // there will always be only one error.
            OpenBankingExecutorError error = obapiRequestContext.getErrors().get(0);

            String statusCode;

            if (StringUtils.isNotBlank(error.getHttpStatusCode())) {
                statusCode = error.getHttpStatusCode();
            } else {
                statusCode = TPPMessage.CodeEnum.valueOf(error.getCode()).getStatusCode();
            }

            Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
            addedHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            obapiRequestContext.setAddedHeaders(addedHeaders);

            obapiRequestContext.setModifiedPayload(getErrorJSON(error).toString());
            obapiRequestContext.addContextProperty(GatewayConstants.ERROR_STATUS_PROP, String.valueOf(statusCode));
        }
    }

    /**
     * Method to construct the error responses for requests.
     * @param error
     * @return
     */
    public static JSONObject getErrorJSON(OpenBankingExecutorError error) {

        // This means the error is from accelerator side
        if (StringUtils.startsWith(error.getCode(), "2")) {

            String message;

            if (StringUtils.isNotBlank(error.getMessage())) {
                message = error.getMessage();
            } else {
                message = error.getTitle();
            }

            // Get relative Berlin error code
            TPPMessage.CodeEnum berlinErrorCode = getBerlinErrorCodeFromOBErrorCode(error.getCode());
            return ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                    berlinErrorCode, message);
        } else {
            // This means the error is from Berlin toolkit side
            return ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                    TPPMessage.CodeEnum.valueOf(error.getCode()), error.getMessage());
        }
    }

    private static TPPMessage.CodeEnum getBerlinErrorCodeFromOBErrorCode(String obErrorCode) {
        
        if (StringUtils.equals("200003", obErrorCode)) {
            return TPPMessage.CodeEnum.CERTIFICATE_INVALID;
        } else if (StringUtils.equals("200004", obErrorCode)) {
            return TPPMessage.CodeEnum.ROLE_INVALID;
        } else if (StringUtils.equals("200001", obErrorCode)) {
            return TPPMessage.CodeEnum.TOKEN_INVALID;
        } else if (StringUtils.equals("200007", obErrorCode)) {
            return TPPMessage.CodeEnum.CERTIFICATE_MISSING;
        } else if (StringUtils.equals("200008", obErrorCode)) {
            return TPPMessage.CodeEnum.CERTIFICATE_EXPIRED;
        } else if (StringUtils.equals("200009", obErrorCode)) {
            return TPPMessage.CodeEnum.CERTIFICATE_REVOKED;
        } else {
            return TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR;
        }
    }
}
