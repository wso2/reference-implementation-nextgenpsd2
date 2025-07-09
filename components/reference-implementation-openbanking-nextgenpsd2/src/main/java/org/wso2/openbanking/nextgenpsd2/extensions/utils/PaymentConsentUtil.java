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

package org.wso2.openbanking.nextgenpsd2.extensions.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.CommonConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.BadRequestException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ValidationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.BulkPaymentInitiationPayload;
import org.wso2.openbanking.nextgenpsd2.extensions.model.PeriodicPaymentInitiationPayload;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ScaMethod;
import org.wso2.openbanking.nextgenpsd2.extensions.model.SinglePaymentInitiationPayload;
import org.wso2.openbanking.nextgenpsd2.extensions.model.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.StoredDetailedConsentResourceData;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Utility class for payment consent management.
 */
public class PaymentConsentUtil {

    private static final Log log = LogFactory.getLog(PaymentConsentUtil.class);

    /**
     * Method to validate common payload elements.
     *
     * @param payload request payload
     * @throws ValidationFailureException
     */
    public static void validateCommonPaymentElements(String requestId, JSONObject payload) throws
            ValidationFailureException {
        log.debug("[" + requestId + "] " + "Validating payload for instructed amount");
        if (payload.opt(ConsentExtensionConstants.INSTRUCTED_AMOUNT) == null
                || !payload.has(ConsentExtensionConstants.INSTRUCTED_AMOUNT) ||
                !(payload.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT) instanceof JSONObject)) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null,
                            TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.INSTRUCTED_AMOUNT_MISSING));
        } else {
            log.debug("[" + requestId + "] " + "Validate the amount and currency of instructed amount");
            JSONObject instructedAmountJson = payload.getJSONObject(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
            if (instructedAmountJson.opt(ConsentExtensionConstants.CURRENCY) == null
                    || StringUtils.isBlank(instructedAmountJson.getString(ConsentExtensionConstants.CURRENCY))) {
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null,
                                TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                                ErrorConstants.CURRENCY_CODE_MISSING));
            }

            if (instructedAmountJson.opt(ConsentExtensionConstants.AMOUNT) == null
                    || StringUtils.isBlank(instructedAmountJson.getString(ConsentExtensionConstants.AMOUNT))) {
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null,
                                TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                                ErrorConstants.AMOUNT_IS_MISSING));
            }
        }

        log.debug("[" + requestId + "] " + "Validating payload for creditor account");
        if (payload.opt(ConsentExtensionConstants.CREDITOR_ACCOUNT) == null) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null,
                            TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.CREDITOR_ACCOUNT_MISSING));
        } else {
            JSONObject creditorAccountObject = payload.getJSONObject(ConsentExtensionConstants.CREDITOR_ACCOUNT);

            if (!creditorAccountObject.has(ConsentExtensionConstants.IBAN)
                    && !creditorAccountObject.has(ConsentExtensionConstants.BBAN)
                    && !creditorAccountObject.has(ConsentExtensionConstants.PAN)
                    && !creditorAccountObject.has(ConsentExtensionConstants.MASKED_PAN)
                    && !creditorAccountObject.has(ConsentExtensionConstants.MSISDN)) {

                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null,
                                TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                                ErrorConstants.CREDITOR_ACCOUNT_REFERENCE_MISSING));
            }
        }

        log.debug("[" + requestId + "] " + "Validating payload for creditor name");
        if (payload.opt(ConsentExtensionConstants.CREDITOR_NAME) == null
                || StringUtils.isBlank(payload.getString(ConsentExtensionConstants.CREDITOR_NAME))) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null,
                            TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.CREDITOR_NAME_MISSING));
        }
    }

    /**
     * Method to validate debtor account element of the payload.
     *
     * @param payload the request payload
     * @throws ValidationFailureException
     */
    public static void validateDebtorAccount(String requestId, JSONObject payload) throws ValidationFailureException {

        JSONObject debtorAccountObject = payload.getJSONObject(ConsentExtensionConstants.DEBTOR_ACCOUNT);

        log.debug("[" + requestId + "] " + "Validating payload for debtor account");
        CommonConsentValidationUtil.validateAccountRefObject(debtorAccountObject);
    }

    /**
     * Validates payment consent initiation payload based on payment type.
     *
     * @param requestPayload
     * @param resourcePath
     * @throws ValidationFailureException
     */
    public static void validatePaymentInitiationPayload(JSONObject requestPayload, String resourcePath)
            throws ValidationFailureException {
        switch (CommonConsentValidationUtil.getServiceDifferentiatingRequestPath(resourcePath)) {
            case ConsentExtensionConstants.PAYMENTS_SERVICE_PATH:
                CommonConsentValidationUtil
                        .validateJSONFromModel(requestPayload.toString(), SinglePaymentInitiationPayload.class);
                break;

            case ConsentExtensionConstants.BULK_PAYMENTS_SERVICE_PATH:
                CommonConsentValidationUtil
                        .validateJSONFromModel(requestPayload.toString(), BulkPaymentInitiationPayload.class);
                break;

            case ConsentExtensionConstants.PERIODIC_PAYMENTS_SERVICE_PATH:
                CommonConsentValidationUtil
                        .validateJSONFromModel(requestPayload.toString(), PeriodicPaymentInitiationPayload.class);
                break;

            default:
                // Execution shouldn't reach here given that path is validated prior to reaching this point
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.INTERNAL_SERVER_ERROR,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                TPPMessage.CodeEnum.RESOURCE_UNKNOWN_404, ErrorConstants.PATH_INVALID));
        }
    }

    /**
     * Validates debtor account, requested execution date, requested executed time presence in bulk payment elements.
     *
     * @param requestId id of the request
     * @param payments payment elements array
     * @throws ValidationFailureException
     */
    public static void validatePaymentElements(String requestId, JSONArray payments) throws ValidationFailureException {

        log.debug("[" + requestId + "] " + "Iterating and validating payment objects");
        JSONObject paymentJSON;
        for (int i = 0; i < payments.length(); i++) {
            paymentJSON = payments.getJSONObject(i);
            PaymentConsentUtil.validateCommonPaymentElements(requestId, paymentJSON);

            if (paymentJSON.has(ConsentExtensionConstants.DEBTOR_ACCOUNT)) {
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                TPPMessage.CodeEnum.FORMAT_ERROR,
                                String.format(ErrorConstants.INVALID_DATA_IN_PAYMENTS,
                                        ConsentExtensionConstants.DEBTOR_ACCOUNT)));
            }
            if (paymentJSON.has(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE)) {
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                TPPMessage.CodeEnum.FORMAT_ERROR,
                                String.format(ErrorConstants.INVALID_DATA_IN_PAYMENTS,
                                        ConsentExtensionConstants.REQUESTED_EXECUTION_DATE)));
            }
            if (paymentJSON.has(ConsentExtensionConstants.REQUESTED_EXECUTION_TIME)) {
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                TPPMessage.CodeEnum.FORMAT_ERROR,
                                String.format(ErrorConstants.INVALID_DATA_IN_PAYMENTS,
                                        ConsentExtensionConstants.REQUESTED_EXECUTION_TIME)));
            }
        }
    }

    /**
     * Method to validate requested execution date.
     *
     * @param payload                 request payload
     * @param maxPaymentExecutionDays maximum payment execution days allowed
     * @throws ValidationFailureException
     */
    public static void validateRequestedExecutionDate(String requestId, JSONObject payload,
                                                      String maxPaymentExecutionDays)
            throws ValidationFailureException {

        log.debug("[" + requestId + "] " + "Validating requested execution date");
        if (payload.opt(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE) != null
                && StringUtils.isNotBlank(payload.getString(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE))) {

            LocalDate requestedExecutionDate =
                    CommonConsentValidationUtil.parseDateToISO(payload.getString(ConsentExtensionConstants
                                    .REQUESTED_EXECUTION_DATE), TPPMessage.CodeEnum.EXECUTION_DATE_INVALID,
                            ErrorConstants.REQUESTED_EXECUTION_DATE_INVALID);

            LocalDate today = LocalDate.now(ZoneOffset.UTC);

            if (!requestedExecutionDate.isAfter(today)) { //Checks whether the requested execution date is a future date
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null,
                                TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.EXECUTION_DATE_INVALID,
                                ErrorConstants.EXECUTION_DATE_NOT_FUTURE));
            } else if (StringUtils.isNotBlank(maxPaymentExecutionDays)) {
                /* this block checks whether the requested execution date is within the maximum number of payment
                 execution days allowed by the ASPSP */
                long maxNumberPaymentExecutionDays = Long.parseLong(maxPaymentExecutionDays);
                if (ChronoUnit.DAYS.between(today.plusDays(maxNumberPaymentExecutionDays),
                        requestedExecutionDate) > 0) {
                    throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                            ErrorUtil.constructBerlinError(null,
                                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.EXECUTION_DATE_INVALID,
                                    ErrorConstants.PAYMENT_EXECUTION_DATE_EXCEEDED));
                }
            }
        }
    }

    /**
     * Method to get the payment initiation response without links.
     *
     * @param createdConsent
     * @param scaMethods
     * @return
     */
    public static void appendPaymentInitiationResponseToPayload(StoredDetailedConsentResourceData createdConsent,
                                                                ArrayList<ScaMethod> scaMethods, JSONObject payload) {

        payload.put(ConsentExtensionConstants.TRANSACTION_STATUS, createdConsent.getStatus());
        payload.put(CommonConstants.PAYMENT_ID, createdConsent.getId());

        JSONArray chosenSCAMethods = new JSONArray();
        for (ScaMethod scaMethod : scaMethods) {
            chosenSCAMethods.put(CommonConsentValidationUtil.convertObjectToJson(scaMethod));
        }

        if (scaMethods.size() > 1) {
            payload.put(ConsentExtensionConstants.SCA_METHODS, chosenSCAMethods);
        } else {
            payload.put(ConsentExtensionConstants.CHOSEN_SCA_METHOD, chosenSCAMethods.get(0));
        }
    }

    /**
     * Method to return a JSON Object with payment product.
     *
     * @param consentResourcePath
     * @return
     */
    public static Object getPaymentProductAttribute(String consentResourcePath) {
        JSONObject attributesJSON = new JSONObject();
        attributesJSON.put(ConsentExtensionConstants.PAYMENT_PRODUCT_CC, getPaymentProduct(consentResourcePath));
        return attributesJSON;
    }

    /**
     * Helper method to extract payment product from resource path.
     *
     * @param consentResourcePath
     * @return
     */
    public static String getPaymentProduct(String consentResourcePath) {
        return consentResourcePath.split("/")[1];
    }

    /**
     * Method to validate payment product for payment consents.
     *
     * @param attributes
     * @param consentResourcePath
     * @throws ValidationFailureException
     * @throws BadRequestException
     */
    public static void validatePaymentProductFromAttributes(Object attributes, String consentResourcePath)
            throws ValidationFailureException, BadRequestException {
        String paymentProductFromPath = getPaymentProduct(consentResourcePath);

        // Extract payment product from attributes
        String paymentProductFromAttributes;
        try {
            JSONObject attributesJSON = CommonConsentValidationUtil.convertObjectToJson(attributes);
            paymentProductFromAttributes = attributesJSON.getString(ConsentExtensionConstants.PAYMENT_PRODUCT_CC);
        } catch (JSONException e) {
            // Should be unreachable as payment product gets added as an attribute at initiation
            throw new BadRequestException("Payment product not stored at consent initiation. Product validation" +
                    " failed.");
        }

        if (!paymentProductFromAttributes.equals(paymentProductFromPath)) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.PRODUCT_INVALID,
                    "The provided consent ID valid but belongs to a different payment product"));
        }
    }
}
