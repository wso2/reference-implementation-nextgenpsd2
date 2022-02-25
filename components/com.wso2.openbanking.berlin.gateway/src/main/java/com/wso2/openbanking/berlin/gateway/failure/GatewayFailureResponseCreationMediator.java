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

package com.wso2.openbanking.berlin.gateway.failure;

import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.gateway.utils.GatewayConstants;
import net.minidev.json.JSONObject;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.json.JSONException;

/**
 * Class mediator to compute the error response for gateway failures.
 */
public class GatewayFailureResponseCreationMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(GatewayFailureResponseCreationMediator.class);

    public GatewayFailureResponseCreationMediator() {

    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if ((messageContext.getProperty(GatewayConstants.ERROR_CODE)) != null) {
            int errorCode = (int) messageContext.getProperty(GatewayConstants.ERROR_CODE);
            String errorMessage = (String) messageContext.getProperty(GatewayConstants.ERROR_MSG);
            String errorDetail = (String) messageContext.getProperty(GatewayConstants.ERROR_DETAIL);

            JSONObject errorData;

            if (Integer.toString(errorCode).equals(GatewayConstants.THROTTLE_FAILURE_IDENTIFIER)) {
                errorData = getThrottleFailureResponse(errorMessage, errorDetail);
            } else if (Integer.toString(errorCode).startsWith(GatewayConstants.AUTH_FAILURE_IDENTIFIER)) {
                errorData = getAuthFailureResponse(errorCode, errorMessage, errorDetail);
            } else if (errorCode == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                errorData = getMethodNotAllowedFailureResponse(errorMessage);
            } else if (errorDetail.startsWith(GatewayConstants.SCHEMA_VALIDATION_FAILURE_IDENTIFIER)) {
                errorData = getSchemaValidationFailureResponse(errorCode, errorDetail);
            } else {
                return true;
            }

            JSONObject errorResponse = (JSONObject) errorData.get(GatewayConstants.ERROR_RESPONSE);
            int status = (int) errorData.get(GatewayConstants.STATUS_CODE);
            setFaultPayload(messageContext, errorResponse, status);
        }

        return true;
    }

    /**
     * Method to get the error response for schema validation failures.
     *
     * @param errorDetail
     * @return
     */
    private static JSONObject getSchemaValidationFailureResponse(int errorCode, String errorDetail) {

        JSONObject errorData = new JSONObject();
        String berlinErrorCode;
        int status;

        errorDetail = errorDetail.replace(GatewayConstants.SCHEMA_VALIDATION_FAILURE_IDENTIFIER, "").trim();
        if (errorDetail.endsWith(",")) {
            errorDetail = errorDetail.substring(0, errorDetail.length() - 1);
        }

        // TODO: Handle error response creation for Schema Validation failures
        // https://github.com/wso2-enterprise/financial-open-banking/issues/7218
        if (errorDetail.matches(GatewayConstants.PAYMENT_PRODUCT_ERROR_PATTERN)) {
            berlinErrorCode = TPPMessage.CodeEnum.PRODUCT_UNKNOWN.toString();
            status = HttpStatus.SC_NOT_FOUND;
        } else {
            berlinErrorCode = TPPMessage.CodeEnum.FORMAT_ERROR.toString();
            status = errorCode;
        }

        JSONObject errorResponse = ErrorUtil.constructBerlinError("", TPPMessage.CategoryEnum.ERROR,
                TPPMessage.CodeEnum.valueOf(berlinErrorCode),
                errorDetail);

        errorData.put(GatewayConstants.STATUS_CODE, status);
        errorData.put(GatewayConstants.ERROR_RESPONSE, errorResponse);

        return errorData;
    }

    /**
     * Method to get the error response for auth failures.
     *
     * @param errorCode
     * @param errorMessage
     * @param errorDetail
     * @return
     */
    private static JSONObject getAuthFailureResponse(int errorCode, String errorMessage, String errorDetail) {

        JSONObject errorData = new JSONObject();
        String berlinErrorCode;
        int status;
        JSONObject errorResponse;

        if (errorCode == GatewayConstants.INVALID_SCOPE) {
            berlinErrorCode = TPPMessage.CodeEnum.TOKEN_INVALID.toString();
            status = HttpStatus.SC_FORBIDDEN;
            errorResponse = ErrorUtil.constructBerlinError("", TPPMessage.CategoryEnum.ERROR,
                    TPPMessage.CodeEnum.valueOf(berlinErrorCode), "Token does not consist of the required" +
                            " permissions for this resource");
        } else if (errorCode == GatewayConstants.API_AUTH_INVALID_CREDENTIALS) {
            berlinErrorCode = TPPMessage.CodeEnum.TOKEN_INVALID.toString();
            status = HttpStatus.SC_UNAUTHORIZED;
            errorResponse = ErrorUtil.constructBerlinError("", TPPMessage.CategoryEnum.ERROR,
                    TPPMessage.CodeEnum.valueOf(berlinErrorCode), "Token is not valid");
        } else {
            status = HttpStatus.SC_UNAUTHORIZED;
            String errorText = (errorDetail == null) ? errorMessage : errorDetail;
            errorResponse = ErrorUtil.constructBerlinError("", TPPMessage.CategoryEnum.ERROR,
                    null, errorText);
        }

        errorData.put(GatewayConstants.STATUS_CODE, status);
        errorData.put(GatewayConstants.ERROR_RESPONSE, errorResponse);

        return errorData;
    }

    private static JSONObject getThrottleFailureResponse(String errorMessage, String errorDetail) {

        JSONObject errorData = new JSONObject();
        int status = 429;
        String errorText = (errorDetail == null) ? errorMessage : errorDetail;
        JSONObject errorResponse = ErrorUtil.constructBerlinError("", TPPMessage.CategoryEnum.ERROR,
                TPPMessage.CodeEnum.ACCESS_EXCEEDED, errorText);

        errorData.put(GatewayConstants.STATUS_CODE, status);
        errorData.put(GatewayConstants.ERROR_RESPONSE, errorResponse);
        return errorData;
    }

    private static JSONObject getMethodNotAllowedFailureResponse(String errorMessage) {

        JSONObject errorData = new JSONObject();
        JSONObject errorResponse;

        int status = HttpStatus.SC_METHOD_NOT_ALLOWED;
        errorResponse = ErrorUtil.constructBerlinError("", TPPMessage.CategoryEnum.ERROR,
                TPPMessage.CodeEnum.SERVICE_INVALID_405, errorMessage);
        errorData.put(GatewayConstants.STATUS_CODE, status);
        errorData.put(GatewayConstants.ERROR_RESPONSE, errorResponse);

        return errorData;
    }

    /**
     * set the error message to the jsonPayload to be sent back.
     *
     * @param messageContext the messageContext sent back to the user
     * @param errorData      the details of the error for validation failure
     */
    private static void setFaultPayload(MessageContext messageContext, JSONObject errorData, int status) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        axis2MessageContext.setProperty(GatewayConstants.MESSAGE_TYPE, GatewayConstants.JSON_CONTENT_TYPE);
        axis2MessageContext.setProperty(NhttpConstants.HTTP_SC, status);
        try {
            //setting the payload as the message payload
            JsonUtil.getNewJsonPayload(axis2MessageContext, errorData.toString(), true,
                    true);
            messageContext.setResponse(true);
            messageContext.setProperty(GatewayConstants.RESPONSE_CAPS, GatewayConstants.TRUE);
            messageContext.setTo(null);
            axis2MessageContext.removeProperty(GatewayConstants.NO_ENTITY_BODY);
        } catch (JSONException jsonException) {
            log.error(GatewayConstants.PAYLOAD_FORMING_ERROR, jsonException);
        } catch (AxisFault axisFault) {
            log.error(GatewayConstants.PAYLOAD_SETTING_ERROR, axisFault);
        }
    }
}
