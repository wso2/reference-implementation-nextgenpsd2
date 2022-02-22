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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.models.ScaApproach;
import com.wso2.openbanking.berlin.common.models.ScaMethod;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;

/**
 * Contains functions used for payments consent flow.
 */
public class PaymentConsentUtil {

    private static final Log log = LogFactory.getLog(PaymentConsentUtil.class);

    /**
     * Method to validate debtor account element of the payload.
     *
     * @param payload                the request payload
     */
    public static void validateDebtorAccount(JSONObject payload) {

        JSONObject debtorAccountObject = (JSONObject) payload.get(ConsentExtensionConstants.DEBTOR_ACCOUNT);

        log.debug("Validating payload for debtor account");
        CommonConsentUtil.validateAccountRefObject(debtorAccountObject);
    }

    /**
     * Method to validate a provided date is a future date. Throws an exception if the date is a past date.
     *
     * @param date
     */
    public static void validateFutureDate(LocalDate date, String errorMessage) {

        if (!date.isAfter(LocalDate.now())) {
            log.error(errorMessage);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    errorMessage));
        }
    }

    /**
     * Method to validate whether the dates are consistent.
     *
     * @param startDate
     * @param endDate
     */
    public static void areDatesValid(LocalDate startDate, LocalDate endDate) {

        if (endDate.compareTo(startDate) <= 0) {
            log.error(ErrorConstants.DATES_INCONSISTENT);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.DATES_INCONSISTENT));
        }
    }

    /**
     * Method to validate common payload elements.
     *
     * @param payload request payload
     */
    public static void validateCommonPaymentElements(JSONObject payload) {

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
        } else {
            JSONObject creditorAccountObject = (JSONObject) payload.get(ConsentExtensionConstants.CREDITOR_ACCOUNT);

            if (!creditorAccountObject.containsKey(ConsentExtensionConstants.IBAN)
                    && !creditorAccountObject.containsKey(ConsentExtensionConstants.BBAN)
                    && !creditorAccountObject.containsKey(ConsentExtensionConstants.PAN)
                    && !creditorAccountObject.containsKey(ConsentExtensionConstants.MASKED_PAN)
                    && !creditorAccountObject.containsKey(ConsentExtensionConstants.MSISDN)) {

                log.error(ErrorConstants.CREDITOR_ACCOUNT_REFERENCE_MISSING);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.CREDITOR_ACCOUNT_REFERENCE_MISSING));
            }
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
     * Method to validate requested execution date.
     *
     * @param payload request payload
     * @param maxPaymentExecutionDays maximum payment execution days allowed
     */
    public static void validateRequestedExecutionDate(JSONObject payload, String maxPaymentExecutionDays) {

        log.debug("Validating requested execution date");
        if (payload.get(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE) != null
                && StringUtils.isNotBlank(payload.getAsString(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE))) {

            LocalDate requestedExecutionDate =
                    ConsentExtensionUtil.parseDateToISO((String) payload.get(ConsentExtensionConstants
                                    .REQUESTED_EXECUTION_DATE), TPPMessage.CodeEnum.EXECUTION_DATE_INVALID,
                            ErrorConstants.REQUESTED_EXECUTION_DATE_INVALID);

            LocalDate today = LocalDate.now();

            if (!requestedExecutionDate.isAfter(today)) {
                log.error(ErrorConstants.EXECUTION_DATE_NOT_FUTURE);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.EXECUTION_DATE_INVALID,
                        ErrorConstants.EXECUTION_DATE_NOT_FUTURE));
            } else if (StringUtils.isNotBlank(maxPaymentExecutionDays)) {
                long maxNumberPaymentExecutionDays = Long.parseLong(maxPaymentExecutionDays);
                if (ChronoUnit.DAYS.between(today.plusDays(maxNumberPaymentExecutionDays),
                        requestedExecutionDate) > 0) {
                    log.error(ErrorConstants.PAYMENT_EXECUTION_DATE_EXCEEDED);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                            TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.EXECUTION_DATE_INVALID,
                            ErrorConstants.PAYMENT_EXECUTION_DATE_EXCEEDED));
                }
            }
        }
    }

    /**
     * Method to construct payment initiation response.
     *
     * @param consentManageData consent manage data
     * @param createdConsent the created consent
     * @param apiVersion the configured API version to construct the self links
     * @param isSCARequired whether SCA is required or not as configured
     * @return the constructed initiation response
     */
    public static JSONObject constructPaymentInitiationResponse(ConsentManageData consentManageData,
                                                                DetailedConsentResource createdConsent,
                                                                boolean isExplicitAuth, boolean isRedirectPreferred,
                                                                String apiVersion, boolean isSCARequired) {

        String requestPath = consentManageData.getRequestPath();
        String locationString = String.format(ConsentExtensionConstants.SELF_LINK_TEMPLATE,
                apiVersion, requestPath, createdConsent.getConsentID());
        consentManageData.setResponseHeader(ConsentExtensionConstants.LOCATION_HEADER,
                locationString);

        Map<String, Object> scaElements = CommonUtil.getScaApproachAndMethods(isRedirectPreferred,
                isSCARequired);
        ScaApproach scaApproach = (ScaApproach) scaElements.get(CommonConstants.SCA_APPROACH_KEY);
        ArrayList<ScaMethod> scaMethods =
                (ArrayList<ScaMethod>) scaElements.get(CommonConstants.SCA_METHODS_KEY);
        consentManageData.setResponseHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH,
                scaApproach.getApproach().toString());

        JSONObject responseWithoutLinks = PaymentConsentUtil.getPaymentInitiationResponse(createdConsent, scaMethods);

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
     * @return
     */
    private static JSONObject getPaymentInitiationResponse(DetailedConsentResource createdConsent,
                                                           ArrayList<ScaMethod> scaMethods) {

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
            responseObject.appendField(ConsentExtensionConstants.CHOSEN_SCA_METHOD, chosenSCAMethods.get(0));
        }
        return responseObject;
    }

    /**
     * Construct the payments GET response.
     *
     * @param retrievedConsent the retrieved consent resource
     * @return the response for the payments GET request
     * @throws ParseException thrown if an error occurs when parsing the consent receipt
     */
    public static JSONObject getConstructedPaymentsGetResponse(ConsentResource retrievedConsent) throws ParseException {

        JSONObject consentReceipt =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(retrievedConsent.getReceipt());

        JSONObject paymentsGetResponse = new JSONObject();
        setDebtorAccountToResponse(paymentsGetResponse, consentReceipt);
        setCommonPaymentElementsToResponse(paymentsGetResponse, consentReceipt);
        paymentsGetResponse.appendField(ConsentExtensionConstants.TRANSACTION_STATUS,
                retrievedConsent.getCurrentStatus());

        return paymentsGetResponse;
    }

    /**
     * Construct the periodic payments GET response.
     *
     * @param retrievedConsent the retrieved consent resource
     * @return the response for the periodic payments GET request
     * @throws ParseException thrown if an error occurs when parsing the consent receipt
     */
    public static JSONObject getConstructedPeriodicPaymentGetResponse(ConsentResource retrievedConsent)
            throws ParseException {

        JSONObject consentReceipt =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(retrievedConsent.getReceipt());

        JSONObject periodicPaymentsResponse = new JSONObject();

        periodicPaymentsResponse.appendField(ConsentExtensionConstants.TRANSACTION_STATUS,
                retrievedConsent.getCurrentStatus());
        setCommonPaymentElementsToResponse(periodicPaymentsResponse, consentReceipt);
        periodicPaymentsResponse.appendField(ConsentExtensionConstants.START_DATE,
                consentReceipt.get(ConsentExtensionConstants.START_DATE));
        if (consentReceipt.containsKey(ConsentExtensionConstants.END_DATE)) {
            periodicPaymentsResponse.appendField(ConsentExtensionConstants.END_DATE,
                    consentReceipt.get(ConsentExtensionConstants.END_DATE));
        }
        if (consentReceipt.containsKey(ConsentExtensionConstants.EXECUTION_RULE)) {
            periodicPaymentsResponse.appendField(ConsentExtensionConstants.EXECUTION_RULE,
                    consentReceipt.get(ConsentExtensionConstants.EXECUTION_RULE));
        }
        periodicPaymentsResponse.appendField(ConsentExtensionConstants.FREQUENCY,
                consentReceipt.get(ConsentExtensionConstants.FREQUENCY));
        if (consentReceipt.containsKey(ConsentExtensionConstants.DAY_OF_EXECUTION)) {
            periodicPaymentsResponse.appendField(ConsentExtensionConstants.DAY_OF_EXECUTION,
                    consentReceipt.get(ConsentExtensionConstants.DAY_OF_EXECUTION));
        }
        return periodicPaymentsResponse;
    }

    /**
     * Constructs the bulk payment GET response.
     *
     * @param retrievedConsent the retrieved consent resource
     * @return the response for the periodic payments GET request
     * @throws ParseException thrown if an error occurs when parsing the consent receipt
     */
    public static JSONObject getConstructedBulkPaymentGetResponse(ConsentResource retrievedConsent)
            throws ParseException {

        JSONObject consentReceipt =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(retrievedConsent.getReceipt());

        JSONObject bulkPaymentResponse = new JSONObject();

        bulkPaymentResponse.appendField(ConsentExtensionConstants.TRANSACTION_STATUS,
                retrievedConsent.getCurrentStatus());
        if (consentReceipt.containsKey(ConsentExtensionConstants.BATCH_BOOKING_PREFERRED)) {
            bulkPaymentResponse.appendField(ConsentExtensionConstants.BATCH_BOOKING_PREFERRED,
                    consentReceipt.get(ConsentExtensionConstants.BATCH_BOOKING_PREFERRED));
        }
        if (consentReceipt.containsKey(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE)) {
            bulkPaymentResponse.appendField(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE,
                    consentReceipt.get(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE));
        }
        setDebtorAccountToResponse(bulkPaymentResponse, consentReceipt);
        bulkPaymentResponse.appendField(ConsentExtensionConstants.PAYMENTS,
                consentReceipt.get(ConsentExtensionConstants.PAYMENTS));

        return bulkPaymentResponse;
    }

    /**
     * Sets debtor account element to response.
     *
     * @param response response object of the request
     * @param receipt  the receipt of the consent
     */
    private static void setDebtorAccountToResponse(JSONObject response, JSONObject receipt) {

        response.appendField(ConsentExtensionConstants.DEBTOR_ACCOUNT,
                receipt.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));
    }

    /**
     * Sets common payment elements to response.
     *
     * @param response response object of the request
     * @param receipt  the receipt of the consent
     */
    private static void setCommonPaymentElementsToResponse(JSONObject response, JSONObject receipt) {

        if (receipt.containsKey(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE)) {
            response.appendField(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE,
                    receipt.get(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE));
        }

        if (receipt.containsKey(ConsentExtensionConstants.END_TO_END_IDENTIFICATION)) {
            response.appendField(ConsentExtensionConstants.END_TO_END_IDENTIFICATION,
                    receipt.get(ConsentExtensionConstants.END_TO_END_IDENTIFICATION));
        }
        response.appendField(ConsentExtensionConstants.DEBTOR_ACCOUNT,
                receipt.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));
        response.appendField(ConsentExtensionConstants.INSTRUCTED_AMOUNT,
                receipt.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT));
        response.appendField(ConsentExtensionConstants.CREDITOR_ACCOUNT,
                receipt.get(ConsentExtensionConstants.CREDITOR_ACCOUNT));
        if (receipt.containsKey(ConsentExtensionConstants.CREDITOR_AGENT)) {
            response.appendField(ConsentExtensionConstants.CREDITOR_AGENT,
                    receipt.get(ConsentExtensionConstants.CREDITOR_AGENT));
        }
        response.appendField(ConsentExtensionConstants.CREDITOR_NAME,
                receipt.get(ConsentExtensionConstants.CREDITOR_NAME));
        if (receipt.containsKey(ConsentExtensionConstants.CREDITOR_ADDRESS)) {
            response.appendField(ConsentExtensionConstants.CREDITOR_ADDRESS,
                    receipt.get(ConsentExtensionConstants.CREDITOR_ADDRESS));
        }
        if (receipt.containsKey(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED)) {
            response.appendField(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED, receipt.get(
                    ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED));
        }
        if (receipt.containsKey(ConsentExtensionConstants.TRANSACTION_STATUS)) {
            response.appendField(ConsentExtensionConstants.TRANSACTION_STATUS,
                    receipt.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        }
    }

    /**
     * Constructs the payment cancellation response.
     *
     * @param updatedConsent the updated consent resource
     * @param requestPath    the request path string
     * @param isSCARequired  param to determine whether the SCA is required as configured
     * @return the payment cancellation response
     */
    public static JSONObject getPaymentCancellationResponse(ConsentResource updatedConsent, String requestPath,
                                                            boolean isSCARequired) {

        JSONObject paymentCancellationResponse = new JSONObject();

        Map<String, Object> scaElements = CommonUtil.getScaApproachAndMethods(true,
                isSCARequired);
        ArrayList<ScaMethod> scaMethods =
                (ArrayList<ScaMethod>) scaElements.get(CommonConstants.SCA_METHODS_KEY);

        paymentCancellationResponse.appendField(ConsentExtensionConstants.TRANSACTION_STATUS,
                updatedConsent.getCurrentStatus());

        JSONArray chosenSCAMethods = new JSONArray();
        for (ScaMethod scaMethod : scaMethods) {
            JSONObject scaMethodJson = new JSONObject();
            scaMethodJson.appendField(CommonConstants.SCA_TYPE, scaMethod.getAuthenticationType());
            scaMethodJson.appendField(CommonConstants.SCA_VERSION, scaMethod.getVersion());
            scaMethodJson.appendField(CommonConstants.SCA_ID, scaMethod.getAuthenticationMethodId());
            scaMethodJson.appendField(CommonConstants.SCA_NAME, scaMethod.getName());
            scaMethodJson.appendField(CommonConstants.SCA_DESCRIPTION, scaMethod.getDescription());

            chosenSCAMethods.add(scaMethodJson);
        }

        if (scaMethods.size() > 1) {
            paymentCancellationResponse.appendField(ConsentExtensionConstants.SCA_METHODS, chosenSCAMethods);
        } else {
            paymentCancellationResponse.appendField(ConsentExtensionConstants.CHOSEN_SCA_METHOD,
                    chosenSCAMethods.get(0));
        }

        JSONObject links = LinksConstructor.getCancellationLinks(requestPath, updatedConsent.getConsentID(),
                ConsentTypeEnum.PAYMENTS.toString());
        paymentCancellationResponse.appendField(ConsentExtensionConstants.LINKS, links);

        return paymentCancellationResponse;
    }

    /**
     * Filters the authorisation resources by the provided authorisation status.
     *
     * @param retrievedAuthResources the retrieved authorization resources
     * @param authType               the authorization status that should filter
     * @return the list of filtered authorization resources
     */
    public static ArrayList<AuthorizationResource> filterAuthorizations(ArrayList<AuthorizationResource>
                                                                                retrievedAuthResources,
                                                                        AuthTypeEnum authType) {
        ArrayList<AuthorizationResource> cancellationAuthResources = new ArrayList<>();
        for (AuthorizationResource authResource : retrievedAuthResources) {
            if (StringUtils.equals(authType.toString(), authResource.getAuthorizationType())) {
                cancellationAuthResources.add(authResource);
            }
        }
        if (CollectionUtils.isNotEmpty(cancellationAuthResources)) {
            return cancellationAuthResources;
        } else {
            return null;
        }
    }

    /**
     * Returns the payment product from the request path.
     *
     * @param requestPath the request path string
     * @return the payment product of the request
     */
    public static String getPaymentProduct(String requestPath) {

        // Payment product always contains in this position
        // The payment product is validated from gateway under swagger validation, therefore index out of bound
        // exceptions will not occur
        return requestPath.split("/")[1];
    }
}
