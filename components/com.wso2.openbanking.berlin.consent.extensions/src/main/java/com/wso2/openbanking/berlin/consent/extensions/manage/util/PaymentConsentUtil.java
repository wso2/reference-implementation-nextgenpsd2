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

package com.wso2.openbanking.berlin.consent.extensions.manage.util;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.utils.CommonConstants;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.common.utils.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.utils.ErrorConstants;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.common.utils.ScaApproach;
import com.wso2.openbanking.berlin.common.utils.ScaMethod;
import com.wso2.openbanking.berlin.common.utils.TPPMessage;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;

/**
 * Contains functions used for payments consent flow.
 */
public class PaymentConsentUtil {

    private static final Log log = LogFactory.getLog(PaymentConsentUtil.class);

    /**
     * Method to validate payments service payload.
     *
     * @param payload
     * @param configuredAccReference
     */
    public static void validatePaymentsPayload(JSONObject payload, String configuredAccReference) {

        validateDebtorAccount(payload, configuredAccReference);
        validateCommonPaymentElements(payload);
    }

    /**
     * Method to validate periodic payments payload.
     *
     * @param payload
     * @param configuredAccReference
     */
    public static void validatePeriodicPaymentsPayload(JSONObject payload, String configuredAccReference) {

        // Validate common elements
        validatePaymentsPayload(payload, configuredAccReference);

        log.debug("Validating periodic payments payload for start date");
        if (payload.get(ConsentExtensionConstants.START_DATE) == null
                || StringUtils.isBlank(payload.getAsString(ConsentExtensionConstants.START_DATE))) {
            log.error(ErrorConstants.START_DATE_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.START_DATE_MISSING));
        } else {
            log.debug("Validating start date for correct date format");
            parseDateToISO((String) payload.get(ConsentExtensionConstants.START_DATE), TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.START_DATE_INVALID);
        }

        log.debug("Validating periodic payments payload for frequency");
        if (payload.get(ConsentExtensionConstants.FREQUENCY) == null
                || StringUtils.isBlank(payload.getAsString(ConsentExtensionConstants.FREQUENCY))) {
            log.error(ErrorConstants.FREQUENCY_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.FREQUENCY_MISSING));
        }

        if (payload.get(ConsentExtensionConstants.END_DATE) != null && StringUtils.isNotBlank(payload.getAsString(
                ConsentExtensionConstants.END_DATE))) {
            log.debug("Validating whether periodic payments end date if a future date");
            LocalDate endDate = parseDateToISO((String) payload.get(ConsentExtensionConstants.END_DATE),
                    TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.END_DATE_NOT_VALID);
            LocalDate startDate = LocalDate.parse(payload.get(ConsentExtensionConstants.START_DATE).toString(),
                    DateTimeFormatter.ISO_DATE);
            validateFutureDate(endDate);
            areDatesValid(startDate, endDate);
        }

        if (payload.get(ConsentExtensionConstants.EXECUTION_RULE) != null && StringUtils.isNotBlank(payload.getAsString(
                ConsentExtensionConstants.EXECUTION_RULE))) {
            log.debug("Validating execution rule");
            String executionRule = payload.getAsString(ConsentExtensionConstants.EXECUTION_RULE);
            if (!(StringUtils.equals(ConsentExtensionConstants.FOLLOWING_EXECUTION_RULE, executionRule)
                    || StringUtils.equals(ConsentExtensionConstants.PRECEDING_EXECUTION_RULE, executionRule))) {
                log.error(ErrorConstants.INVALID_EXECUTION_RULE);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.INVALID_EXECUTION_RULE));
            }
        }

    }

    /**
     * Method to validate bulk payments payload.
     *
     * @param payload
     * @param maxPaymentExecutionDays
     * @param configuredAccReference
     */
    public static void validateBulkPaymentsPayload(JSONObject payload, String maxPaymentExecutionDays,
                                                   String configuredAccReference) {

        validateDebtorAccount(payload, configuredAccReference);

        log.debug("Validating requested execution date");
        if (payload.get(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE) != null
                && StringUtils.isNotBlank(payload.getAsString(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE))) {

            LocalDate requestedExecutionDate =
                    parseDateToISO((String) payload.get(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE),
                            TPPMessage.CodeEnum.EXECUTION_DATE_INVALID,
                            ErrorConstants.REQUESTED_EXECUTION_DATE_INVALID);

            LocalDate today = LocalDate.now();

            if (!requestedExecutionDate.isAfter(LocalDate.now())) {
                log.error(ErrorConstants.EXECUTION_DATE_NOT_FUTURE);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.EXECUTION_DATE_NOT_FUTURE));
            } else if (StringUtils.isNotBlank(maxPaymentExecutionDays)) {
                long maxNumberPaymentExecutionDays = Long.parseLong(maxPaymentExecutionDays);
                if (ChronoUnit.DAYS.between(today.plusDays(maxNumberPaymentExecutionDays),
                        requestedExecutionDate) > 0) {
                    log.error(ErrorConstants.PAYMENT_EXECUTION_DATE_EXCEEDED);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                            TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.PAYMENT_EXECUTION_DATE_EXCEEDED));
                }
            }
        }

        JSONArray payments;

        log.debug("Validate presence of payment objects");
        if (payload.get(ConsentExtensionConstants.PAYMENTS) == null) {
            log.error(ErrorConstants.NO_PAYMENTS_IN_BODY);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.NO_PAYMENTS_IN_BODY));
        } else {
            payments = (JSONArray) payload.get(ConsentExtensionConstants.PAYMENTS);
            if (payments.size() == 0) {
                log.error(ErrorConstants.EMPTY_PAYMENTS_ELEMENT);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.EMPTY_PAYMENTS_ELEMENT));
            }
        }

        log.debug("Iterating and validating payment objects");
        for (Object payment : payments) {
            validateCommonPaymentElements((JSONObject) payment);
        }
    }

    /**
     * Method to validate debtor account element of the payload.
     *
     * @param payload
     * @param configuredAccReference
     */
    private static void validateDebtorAccount(JSONObject payload, String configuredAccReference) {

        log.debug("Validating payload for debtor account");
        if (payload.get(ConsentExtensionConstants.DEBTOR_ACCOUNT) == null) {
            log.error(ErrorConstants.DEBTOR_ACCOUNT_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.DEBTOR_ACCOUNT_MISSING));
        } else {
            log.debug("Validating debtor account references");
            String accountReference = null;

            JSONObject accountObject = (JSONObject) payload.get(ConsentExtensionConstants.DEBTOR_ACCOUNT);

            if (StringUtils.equals(ConsentExtensionConstants.IBAN, configuredAccReference)) {
                if (accountObject.get(ConsentExtensionConstants.IBAN) != null
                        && StringUtils.isNotBlank(accountObject.getAsString(ConsentExtensionConstants.IBAN))) {
                    accountReference = (String) accountObject.get(ConsentExtensionConstants.IBAN);
                }
            } else if (StringUtils.equals(ConsentExtensionConstants.BBAN, configuredAccReference)) {
                if (accountObject.get(ConsentExtensionConstants.BBAN) != null
                        && StringUtils.isNotBlank(accountObject.getAsString(ConsentExtensionConstants.BBAN))) {
                    accountReference = (String) accountObject.get(ConsentExtensionConstants.BBAN);
                }
            } else if (StringUtils.equals(ConsentExtensionConstants.PAN, configuredAccReference)) {
                if (accountObject.get(ConsentExtensionConstants.PAN) != null
                        && StringUtils.isNotBlank(accountObject.getAsString(ConsentExtensionConstants.PAN))) {
                    accountReference = (String) accountObject.get(ConsentExtensionConstants.PAN);
                }
            } else {
                log.error(ErrorConstants.INVALID_ACCOUNT_REFERENCE_TYPE);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.INVALID_ACCOUNT_REFERENCE_TYPE));
            }

            if (accountReference == null) {
                log.error(ErrorConstants.ACCOUNT_REFERENCE_TYPE_MISSING);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.ACCOUNT_REFERENCE_TYPE_MISSING));
            }
        }
    }

    /**
     * Method to validate a provided date is a future date.
     *
     * @param date
     */
    private static void validateFutureDate(LocalDate date) {

        if (!date.isAfter(LocalDate.now())) {
            log.error(ErrorConstants.END_DATE_NOT_FUTURE);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.END_DATE_NOT_FUTURE));
        }
    }

    /**
     * Method to validate whether the dates are consistent.
     *
     * @param startDate
     * @param endDate
     */
    private static void areDatesValid(LocalDate startDate, LocalDate endDate) {

        if (endDate.compareTo(startDate) <= 0) {
            log.error(ErrorConstants.DATES_INCONSISTENT);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.DATES_INCONSISTENT));
        }
    }

    /**
     * Method to parse a provided date to ISO date. Throws an error is the provided date is invalid.
     *
     * @param dateToParse
     * @param errorCode
     * @param errorMessage
     * @return
     * @throws ConsentException
     */
    private static LocalDate parseDateToISO(String dateToParse, TPPMessage.CodeEnum errorCode, String errorMessage)
            throws ConsentException {

        LocalDate parsedDate;

        try {
            parsedDate = LocalDate.parse(dateToParse, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            log.error(errorMessage);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, errorCode, errorMessage));
        }
        return parsedDate;
    }

    /**
     * Method to validate common payload elements.
     *
     * @param payload
     */
    private static void validateCommonPaymentElements(JSONObject payload) {

        log.debug("Validating payload for instructed amount");
        if (payload.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT) == null
                || StringUtils.isBlank(payload.getAsString(ConsentExtensionConstants.INSTRUCTED_AMOUNT))) {
            log.error(ErrorConstants.INSTRUCTED_AMOUNT_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.INSTRUCTED_AMOUNT_MISSING));
        } else {
            log.debug("Validate the amount and currency of instructed amount");
            JSONObject instructedAmountJson = (JSONObject) payload.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
            if (instructedAmountJson.get(ConsentExtensionConstants.CURRENCY) == null
                    || StringUtils.isBlank(instructedAmountJson.getAsString(ConsentExtensionConstants.CURRENCY))) {
                log.error(ErrorConstants.CURRENCY_CODE_MISSING);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.CURRENCY_CODE_MISSING));
            }

            if (instructedAmountJson.get(ConsentExtensionConstants.AMOUNT) == null
                    || StringUtils.isBlank(instructedAmountJson.getAsString(ConsentExtensionConstants.AMOUNT))) {
                log.error(ErrorConstants.AMOUNT_IS_MISSING);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.AMOUNT_IS_MISSING));
            }
        }

        log.debug("Validating payload for creditor account");
        if (payload.get(ConsentExtensionConstants.CREDITOR_ACCOUNT) == null) {
            log.error(ErrorConstants.CREDITOR_ACCOUNT_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.CREDITOR_ACCOUNT_MISSING));
        }

        log.debug("Validating payload for creditor name");
        if (payload.get(ConsentExtensionConstants.CREDITOR_NAME) == null
                || StringUtils.isBlank(payload.getAsString(ConsentExtensionConstants.CREDITOR_NAME))) {
            log.error(ErrorConstants.CREDITOR_NAME_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.CREDITOR_NAME_MISSING));
        }
    }

    /**
     * Method to construct payment initiation response.
     *
     * @param consentManageData consent manage data
     * @param createdConsent the created consent
     * @param apiVersion the configured API version to construct the self links
     * @param isSCARequired whether SCA is required or not as configured
     * @param isTransactionFeeEnabled whether a transaction fee is charged or not as configured
     * @param transactionFee configured transaction fee
     * @param transactionFeeCurrency configured transaction fee currency
     * @return the constructed initiation response
     */
    public static JSONObject constructPaymentInitiationResponse(ConsentManageData consentManageData,
                                                                DetailedConsentResource createdConsent,
                                                                boolean isExplicitAuth, boolean isRedirectPreferred,
                                                                String apiVersion, boolean isSCARequired,
                                                                boolean isTransactionFeeEnabled,
                                                                int transactionFee, String transactionFeeCurrency) {

        String requestPath = consentManageData.getRequestPath();
        String locationString = String.format(ConsentExtensionConstants.SELF_LINK_TEMPLATE,
                apiVersion, requestPath, createdConsent.getConsentID());
        consentManageData.setResponseHeader(ConsentExtensionConstants.LOCATION_PROPER_CASE_HEADER,
                locationString);

        Map<String, Object> scaElements = CommonUtil.getScaApproachAndMethods(isRedirectPreferred,
                isSCARequired);
        ScaApproach scaApproach = (ScaApproach) scaElements.get(CommonConstants.SCA_APPROACH_KEY);
        ArrayList<ScaMethod> scaMethods =
                (ArrayList<ScaMethod>) scaElements.get(CommonConstants.SCA_METHODS_KEY);
        consentManageData.setResponseHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH,
                scaApproach.getApproach().toString());

        JSONObject responseWithoutLinks = PaymentConsentUtil.getPaymentInitiationResponse(createdConsent, scaMethods,
                transactionFee, transactionFeeCurrency, isTransactionFeeEnabled);

        String authId = null;

        if (!isExplicitAuth) {
            // Always only one auth resource is created for implicit initiation
            ArrayList<AuthorizationResource> authResources = createdConsent.getAuthorizationResources();
            AuthorizationResource implicitAuthResource = authResources.get(0);
            authId = implicitAuthResource.getAuthorizationID();
        }

        JSONObject links = LinksConstructor.getInitiationLinks(isExplicitAuth, scaApproach,
                scaMethods, requestPath, createdConsent.getConsentID(), authId, ConsentTypeEnum.PAYMENTS.toString());

        return responseWithoutLinks.appendField(ConsentExtensionConstants.LINKS, links);
    }

    /**
     * Method to get the payment initiation response without links.
     *
     * @param createdConsent
     * @param scaMethods
     * @param transactionFee
     * @param transactionFeeCurrency
     * @param isTransactionFeeEnabled
     * @return
     */
    private static JSONObject getPaymentInitiationResponse(DetailedConsentResource createdConsent,
                                                           ArrayList<ScaMethod> scaMethods, int transactionFee,
                                                           String transactionFeeCurrency,
                                                           boolean isTransactionFeeEnabled) {

        JSONObject responseObject = new JSONObject();
        responseObject.appendField(ConsentExtensionConstants.TRANSACTION_STATUS, createdConsent.getCurrentStatus());
        responseObject.appendField(ConsentExtensionConstants.PAYMENT_ID, createdConsent.getConsentID());

        JSONArray chosenSCAMethods = new JSONArray();
        for (ScaMethod scaMethod : scaMethods) {
            chosenSCAMethods.add(CommonUtil.convertObjectToJson(scaMethod));
        }

        if (scaMethods.size() > 1) {
            responseObject.appendField(ConsentExtensionConstants.SCA_METHODS, chosenSCAMethods);
        } else {
            responseObject.appendField(ConsentExtensionConstants.CHOSEN_SCA_METHOD, chosenSCAMethods);
        }

        if (isTransactionFeeEnabled) {
            responseObject.appendField(ConsentExtensionConstants.TRANSACTION_FEE_INDICATOR,
                    true);

            JSONObject transactionFees = new JSONObject();
            transactionFees.appendField(ConsentExtensionConstants.AMOUNT, transactionFee);
            transactionFees.appendField(ConsentExtensionConstants.CURRENCY,
                    transactionFeeCurrency);
            responseObject.appendField(ConsentExtensionConstants.TRANSACTION_FEES, transactionFees);
        }

        return responseObject;
    }

    /**
     * Returns the payment product from the request path.
     *
     * @param requestPath
     * @return
     */
    public static String getPaymentProduct(String requestPath) {

        // Payment product always contains in this position
        // The payment product is validated from gateway under swagger validation, therefore index out of bound
        // exceptions will not occur
        return requestPath.split("/")[1];
    }
}
