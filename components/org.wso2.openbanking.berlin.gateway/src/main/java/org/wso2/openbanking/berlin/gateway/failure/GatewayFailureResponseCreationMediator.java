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

package org.wso2.openbanking.berlin.gateway.failure;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.report.ImmutableValidationReport;
import com.atlassian.oai.validator.report.ValidationReport;
import net.minidev.json.JSONObject;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.json.JSONException;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.gateway.utils.GatewayConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class mediator to compute the error response for gateway failures.
 */
public class GatewayFailureResponseCreationMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(GatewayFailureResponseCreationMediator.class);

    public static final String EMPTY_SPACE = " ";
    public static final String COLON = ":";

    // Payment consent initiation paths
    public static final String PAYMENTS_SERVICE_PATH = "/payments";
    public static final String PERIODIC_PAYMENTS_SERVICE_PATH = "/periodic-payments";
    public static final String BULK_PAYMENTS_SERVICE_PATH = "/bulk-payments";

    // swagger schema definitions order for Payment products
    public static final String PAYMENTS_SCHEMA_VALIDATION_REF = "/anyOf/0";
    public static final String PERIODIC_PAYMENTS_SCHEMA_VALIDATION_REF = "/anyOf/1";
    public static final String BULK_PAYMENTS_SCHEMA_VALIDATION_REF = "/anyOf/2";

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
            } else if (Integer.toString(errorCode).startsWith("404")) {
                errorData = getResourceFailureResponse(errorMessage);
            } else if (errorDetail.startsWith(GatewayConstants.SCHEMA_VALIDATION_FAILURE_IDENTIFIER)) {
                // Retrieving the schema validation report in order to construct Berlin specific errors
                ImmutableValidationReport schemaValidationReport = null;
                if (messageContext.getProperty(GatewayConstants.SCHEMA_VALIDATION_REPORT_IDENTIFIER)
                        instanceof ImmutableValidationReport) {
                    schemaValidationReport = (ImmutableValidationReport) messageContext
                            .getProperty(GatewayConstants.SCHEMA_VALIDATION_REPORT_IDENTIFIER);
                }
                errorData = getSchemaValidationFailureResponse(schemaValidationReport, errorCode);
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
     * Method to get the error response for schema validation failures. This method validates the schema validation
     * report and constructs the Berlin specific errors.
     *
     * @param schemaValidationReport schema validation report
     * @param errorCode              error code
     * @return a json object of the constructed error/s
     */
    private static JSONObject getSchemaValidationFailureResponse(ImmutableValidationReport schemaValidationReport,
                                                                 int errorCode) {

        List<TPPMessage> errorList = new ArrayList<>();
        JSONObject errorData = new JSONObject();
        String berlinErrorCode;
        String errorMessage;

        if (schemaValidationReport != null) {

            String path = StringUtils.EMPTY;
            Pattern payloadPath = Pattern.compile("'.*']");
            Pattern headerPath = Pattern.compile("'[a-zA-Z-]+'");
            Pattern date = Pattern.compile("\\sdate");
            Pattern header = Pattern.compile("header");
            Pattern parameter = Pattern.compile("parameter");
            String regexErrorMessage = ".*'].*";
            Pattern invalidResourceEnum = Pattern.compile("validation.request.parameter.schema.enum");

            for (ValidationReport.Message message : schemaValidationReport.getMessages()) {

                String key = message.getKey();
                errorMessage = message.getMessage();
                Matcher matchBodyPath = payloadPath.matcher(errorMessage);
                Matcher matchHeaderPath = headerPath.matcher(errorMessage);
                Matcher matchDate = date.matcher(errorMessage);
                Matcher matchHeader = header.matcher(key);
                Matcher matchParameter = parameter.matcher(key);
                Matcher matchInvalidResourceEnum = invalidResourceEnum.matcher(key);

                // Get details for header validation violation
                if (matchHeader.find() || matchParameter.find()) {

                    if (matchHeaderPath.find()) {
                        path = GatewayConstants.PATH_HEADER + "." + matchHeaderPath.group(0)
                                .replaceFirst("'", "");
                    } else if (message.getContext().isPresent() && message.getContext().get().getParameter()
                            .isPresent()) {
                        if (GatewayConstants.PATH_QUERY.equalsIgnoreCase(message.getContext().get().getParameter()
                                .get().getIn())) {
                            path = GatewayConstants.PATH_QUERY + "." + message.getContext().get().getParameter()
                                    .get().getName();
                        } else {
                            path = GatewayConstants.PATH_HEADER + "." + message.getContext().get().getParameter()
                                    .get().getName();
                        }
                    }
                } else {
                    // Get details for request body validation violation
                    if (matchBodyPath.find()) {
                        path = matchBodyPath.group(0).replace("/", ".")
                                .replaceFirst("\\.", "")
                                .replace("'", "").replace("]", "");
                    }
                    if (message.getMessage().matches(regexErrorMessage)) {
                        errorMessage = message.getMessage().split("']")[1];

                    } else if (isPaymentInitiationRequest(message) && isSchemaFailedToMatchError(message)) {

                        // Improve Error msg for payment initiation requests without mandatory payload elements
                        // https://github.com/wso2-enterprise/financial-open-banking/issues/4437
                        errorMessage = getImprovedErrorMessageForPaymentInitiationRequest(message);
                    } else {
                        errorMessage = message.getMessage();
                    }

                }

                TPPMessage error = new TPPMessage();

                if (matchDate.find()) {
                    berlinErrorCode = TPPMessage.CodeEnum.TIMESTAMP_INVALID.toString();
                } else if (matchInvalidResourceEnum.find()
                        && GatewayConstants.PATH_PAYMENT_PRODUCT.equalsIgnoreCase(path)) {
                    berlinErrorCode = TPPMessage.CodeEnum.PRODUCT_UNKNOWN.toString();
                    errorCode = HttpStatus.SC_NOT_FOUND;
                } else if (path.contains(GatewayConstants.PATH_QUERY)) {
                    berlinErrorCode = TPPMessage.CodeEnum.PARAMETER_NOT_CONSISTENT.toString();
                } else {
                    berlinErrorCode = TPPMessage.CodeEnum.FORMAT_ERROR.toString();
                    // According to the specification, path should only be added to FORMAT_ERROR scenarios
                    if (StringUtils.isNotBlank(path)) {
                        error.setPath(path);
                    }
                }

                error.setCategory(TPPMessage.CategoryEnum.ERROR);
                error.setCode(TPPMessage.CodeEnum.valueOf(berlinErrorCode));
                error.setText(errorMessage);
                errorList.add(error);
            }

            errorData.put(GatewayConstants.STATUS_CODE, errorCode);
            // errorList won't be empty since this method is only executed if a schema error is present
            errorData.put(GatewayConstants.ERROR_RESPONSE, ErrorUtil.constructBerlinError(errorList));
        }
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
        String errorText = (errorDetail == null) ? errorMessage : errorDetail;
        JSONObject errorResponse = ErrorUtil.constructBerlinError("", TPPMessage.CategoryEnum.ERROR,
                TPPMessage.CodeEnum.ACCESS_EXCEEDED, errorText);

        errorData.put(GatewayConstants.STATUS_CODE, 429);
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
     * Constructs the error for resource failure scenarios.
     *
     * @param errorMessage error message
     * @return a json object of the constructed error
     */
    private static JSONObject getResourceFailureResponse(String errorMessage) {

        JSONObject errorData = new JSONObject();
        JSONObject errorResponse;

        errorResponse = ErrorUtil.constructBerlinError("", TPPMessage.CategoryEnum.ERROR,
                TPPMessage.CodeEnum.RESOURCE_UNKNOWN_403, errorMessage);
        errorData.put(GatewayConstants.STATUS_CODE, 403);
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

    /**
     * Method to identify schema validation issue is from payment initiation request.
     *
     * @param message schema validation error msg
     * @return
     */
    private static boolean isPaymentInitiationRequest(ValidationReport.Message message) {

        Request.Method method = null;
        String path = "";
        Optional<ValidationReport.MessageContext> context = message.getContext();
        if (context.isPresent()) {
            ValidationReport.MessageContext messageContext = context.get();
            if (messageContext.getRequestMethod().isPresent()) {
                method = messageContext.getRequestMethod().get();
            }
            if (messageContext.getRequestPath().isPresent()) {
                path = messageContext.getRequestPath().get();
            }
            // Payment initiation request.
            return method == Request.Method.POST && (path.startsWith(PAYMENTS_SERVICE_PATH) ||
                    path.startsWith(BULK_PAYMENTS_SERVICE_PATH) || path.startsWith(PERIODIC_PAYMENTS_SERVICE_PATH));
        }
        return false;
    }

    /**
     * Method to verify schema failed to match error
     *
     * @param message
     * @return
     */
    private static boolean isSchemaFailedToMatchError(ValidationReport.Message message) {

        return message.getMessage().contains("Instance failed to match");
    }

    /**
     * Method to get improved error message for payment initiation request
     *
     * @param message
     * @return
     */
    private static String getImprovedErrorMessageForPaymentInitiationRequest(ValidationReport.Message message) {

        String path = "";
        String schemaValidationRef = "";
        StringBuilder improvedError = new StringBuilder(message.getMessage());

        Optional<ValidationReport.MessageContext> context = message.getContext();
        if (context.isPresent()) {
            ValidationReport.MessageContext messageContext = context.get();
            if (messageContext.getRequestPath().isPresent()) {
                path = messageContext.getRequestPath().get();
            }
        }
        if (path.startsWith(PAYMENTS_SERVICE_PATH)) {
            schemaValidationRef = PAYMENTS_SCHEMA_VALIDATION_REF;
        } else if (path.startsWith(PERIODIC_PAYMENTS_SERVICE_PATH)) {
            schemaValidationRef = PERIODIC_PAYMENTS_SCHEMA_VALIDATION_REF;
        } else if (path.startsWith(BULK_PAYMENTS_SERVICE_PATH)) {
            schemaValidationRef = BULK_PAYMENTS_SCHEMA_VALIDATION_REF;
        }

        if (StringUtils.isBlank(path) || StringUtils.isBlank(schemaValidationRef)) {
            return message.getMessage();
        }

        for (String msg : message.getAdditionalInfo()) {
            if (msg.startsWith(schemaValidationRef)) {
                improvedError.append(EMPTY_SPACE)
                        .append(msg.replace(schemaValidationRef, ""))
                        .append(EMPTY_SPACE);
            }
        }
        return improvedError.toString();
    }

}
