/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.util.CommonValidationUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Validate payments submission requests.
 */
public class PaymentConsentValidator implements SubmissionValidator {

    private static final Log log = LogFactory.getLog(PaymentConsentValidator.class);
    private static final String HTTP_METHOD = "httpMethod";
    private static final String RESOURCE_PATH = "ResourcePath";
    public static final String DELETE = "DELETE";
    public static final String AUTH_STATUS = "authStatus";

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();
        String currentStatus = detailedConsentResource.getCurrentStatus();

        // Validate path payment ID with consent ID
        String resourcePath = consentValidateData.getResourceParams().get(RESOURCE_PATH);
        List<String> pathElements = Arrays.asList(resourcePath.split("/"));

        String pathPaymentId;
        if (resourcePath.endsWith("status")) {
            pathPaymentId = pathElements.get(pathElements.size() - 2);
        } else {
            pathPaymentId = pathElements.get(pathElements.size() - 1);
        }

        if (!StringUtils.equals(consentValidateData.getConsentId(), pathPaymentId)) {
            log.error("Payment ID in the path mismatches with the payment consent ID");
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.FORBIDDEN.getStatusCode(), TPPMessage.CodeEnum.CONSENT_UNKNOWN.toString(),
                    "Payment ID in the path mismatches with the payment consent ID");
            return;
        }

        log.debug("Validating if the Consent Id belongs to the client");
        if (!StringUtils.equals(detailedConsentResource.getClientID(), consentValidateData.getClientId())
                || !StringUtils.equals(consentValidateData.getConsentId(), detailedConsentResource.getConsentID())) {
            log.error(ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.NOT_FOUND.getStatusCode(), TPPMessage.CodeEnum.RESOURCE_UNKNOWN.toString(),
                    ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
            return;
        }

        // Only DELETE and GET payment requests will go through consent validation
        if (StringUtils.equals(DELETE, consentValidateData.getResourceParams().get(HTTP_METHOD))) {

            /* Check whether payment consent is in CANC (payment is already cancelled), REVOKED or RJCT status.
               The consent can be in REVOKED status if the payment is already revoked by the consent portal.
               The consent can be in RJCT status if the payment is not consented. */
            if (StringUtils.equals(TransactionStatusEnum.CANC.name(), currentStatus)
                    || StringUtils.equals(TransactionStatusEnum.REVOKED.name(), currentStatus)
                    || StringUtils.equals(TransactionStatusEnum.RJCT.name(), currentStatus)) {
                if (log.isDebugEnabled()) {
                    log.debug("The payment consent " + consentValidateData.getConsentId() + " is is not eligible " +
                            "for deletion");
                }
                CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                        ResponseStatus.UNAUTHORIZED.getStatusCode(), TPPMessage.CodeEnum.CONSENT_INVALID.toString(),
                        ErrorConstants.CONSENT_ALREADY_DELETED);
            } else if (StringUtils.equals(TransactionStatusEnum.PATC.name(), currentStatus)) {
                // The payment is not yet submitted since it is not fully authorized yet
                setCustomFieldToConsentInfoHeader(consentValidationResult, AUTH_STATUS, "partial");
            } else {
                consentValidationResult.setValid(true);
            }
        } else {
            /* Check whether payment consent is in ACCP (consent is authorized), ACTC or PATC status.
               The consent can be in ACTC status if the payment is to be cancelled with an authorization.
               The consent can be in PATC status if the payment is partially authorized. */
            log.debug("Checking if the consent is in ACCP, ACTC or PATC status");
            if (!(StringUtils.equals(TransactionStatusEnum.ACCP.name(), currentStatus)
                    || StringUtils.equals(TransactionStatusEnum.ACTC.name(), currentStatus)
                    || StringUtils.equals(TransactionStatusEnum.PATC.name(), currentStatus))) {
                log.error(ErrorConstants.CONSENT_INVALID_STATE);
                CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                        ResponseStatus.BAD_REQUEST.getStatusCode(), TPPMessage.CodeEnum.CONSENT_UNKNOWN.toString(),
                        ErrorConstants.CONSENT_INVALID_STATE);
                return;
            }

            if (StringUtils.equals(TransactionStatusEnum.PATC.name(), currentStatus)) {
                // The payment is not yet submitted since it is not fully authorized yet
                setCustomFieldToConsentInfoHeader(consentValidationResult, AUTH_STATUS, "partial");
                return;
            }

            /* If the authorisation status is in the expected status, mark validation as valid.
               No need to check for expiration of the consent since payment consent doesn't define an expiry time. */
            consentValidationResult.setValid(true);
        }
    }

    /**
     * This method is used to add an extra field to the consent-info header to be sent to the executor.
     * The field we send can be used to do specific validations in the executor level.
     *
     * @param consentValidationResult consent validation result
     * @param fieldKey field key
     * @param fieldValue field value
     */
    private void setCustomFieldToConsentInfoHeader(ConsentValidationResult consentValidationResult, String fieldKey,
                                                   String fieldValue) {

        /* Appending an additional field to consent info header for the bank to identify this payment is
           not fully authorized. This case is specific to multi level scenarios where only one of many
           users can authorize a multi level payment and can try to get the payment status. In this case
           bank will identify it using this field and respond accordingly. */
        consentValidationResult.setValid(true);
        JSONObject consentInfoJson = consentValidationResult.getConsentInformation();
        consentInfoJson.appendField(fieldKey, fieldValue);
        consentValidationResult.setConsentInformation(consentInfoJson);
    }
}
