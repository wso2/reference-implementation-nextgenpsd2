/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.gateway.executors.idempotency;

import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.executor.idempotency.OpenBankingIdempotencyHandlingExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.IdempotencyConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.gateway.executors.utils.GatewayConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OpenBankingIdempotencyHandlingExecutorImpl.
 */
public class OpenBankingIdempotencyHandlingExecutorBGImpl extends OpenBankingIdempotencyHandlingExecutor {

    private static final Log log = LogFactory.getLog(OpenBankingIdempotencyHandlingExecutorBGImpl.class);

    @Override
    public String getCreatedTimeFromResponse(OBAPIResponseContext obapiResponseContext) {

        String createdTime = null;

        // Retrieve created time from headers
        String createdTimeFromHeader = obapiResponseContext.getMsgInfo().getHeaders()
                .get(GatewayConstants.CREATED_TIME);
        OffsetDateTime time = OffsetDateTime.parse(createdTimeFromHeader, DateTimeFormatter.RFC_1123_DATE_TIME);
        createdTimeFromHeader = time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (createdTimeFromHeader != null) {
            createdTime = createdTimeFromHeader;
        }
        return createdTime;
    }

    @Override
    public Map<String, Object> getPayloadFromRequest(OBAPIRequestContext obapiRequestContext) {

        Map<String, Object> map = new HashMap<>();
        if (obapiRequestContext.getRequestPayload() != null) {
            map.put(IdempotencyConstants.PAYLOAD, obapiRequestContext.getRequestPayload());
        } else {
            map.put(IdempotencyConstants.PAYLOAD, "");
        }
        map.put(IdempotencyConstants.HTTP_STATUS, HttpStatus.SC_CREATED);
        return map;
    }

    @Override
    public boolean isValidIdempotencyRequest(OBAPIRequestContext obapiRequestContext) {
        //Return if the request contains a error
        if (obapiRequestContext.isError()) {
            return false;
        }

        //Retrieve headers and payload
        Map<String, String> requestHeaders = obapiRequestContext.getMsgInfo().getHeaders();

        //Retrieve idempotency key from headers
        String idempotencyKey = requestHeaders.get(getIdempotencyKeyConstantFromConfig());

        if (StringUtils.isEmpty(idempotencyKey)) {
            log.error(ErrorConstants.EXECUTOR_IDEMPOTENCY_KEY_NOT_FOUND);
            obapiRequestContext.setError(true);
            obapiRequestContext.setErrors(handleIdempotencyErrors(obapiRequestContext,
                    ErrorConstants.EXECUTOR_IDEMPOTENCY_KEY_NOT_FOUND,
                    ErrorConstants.HEADER_MISSING));
            return false;
        }
        return true;
    }

    @Override
    public boolean isValidIdempotencyResponse(OBAPIResponseContext obapiResponseContext) {
        //Return if the request contains a error
        if (obapiResponseContext.isError()) {
            return false;
        }

        // Checking whether the request is an idempotent request and return
        // Do not cache the response for idempotent requests
        if (Boolean.parseBoolean(obapiResponseContext.getMsgInfo().getHeaders().get(GatewayConstants.IS_IDEMPOTENT))) {
            return false;
        }

        // Checking whether status code is 4xx or 5xx
        if (obapiResponseContext.getStatusCode() >= 400) {
            return false;
        }

        return true;
    }

    @Generated(message = "Ignoring since error cases are covered in accelerator unit tests")
    @Override
    protected ArrayList<OpenBankingExecutorError> handleIdempotencyErrors(OBAPIRequestContext obapiRequestContext,
                                                                          String message, String errorCode) {

        if (Objects.equals(errorCode, IdempotencyConstants.Error.HEADER_INVALID)) {
            errorCode = TPPMessage.CodeEnum.FORMAT_ERROR.toString();
        }
        OpenBankingExecutorError error = new OpenBankingExecutorError(errorCode,
                IdempotencyConstants.Error.IDEMPOTENCY_HANDLE_ERROR, message,
                OpenBankingErrorCodes.BAD_REQUEST_CODE);
        ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
        executorErrors.add(error);
        return executorErrors;
    }
}
