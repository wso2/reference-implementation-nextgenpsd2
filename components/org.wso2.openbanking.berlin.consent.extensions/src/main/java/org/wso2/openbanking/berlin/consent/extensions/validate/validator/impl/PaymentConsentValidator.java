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

package org.wso2.openbanking.berlin.consent.extensions.validate.validator.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.util.CommonValidationUtil;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.HttpMethod;

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

        // Get consent resource for the payment ID provided in the path
        ConsentCoreServiceImpl coreService = getConsentService();
        ConsentResource consentResourceByPathId;

        try {
            consentResourceByPathId = coreService.getConsent(pathPaymentId, false);
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_NOT_FOUND_ERROR, e);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.FORBIDDEN.getStatusCode(), TPPMessage.CodeEnum.CONSENT_UNKNOWN.toString(),
                    ErrorConstants.CONSENT_NOT_FOUND_ERROR);
            return;
        }

        // Validate consent type
        String consentType = consentResourceByPathId.getConsentType();
        // payment type is always in the 2nd position
        String consentTypeInPath = pathElements.get(1);

        // Restrict instant payment cancellation
        if (StringUtils.equals(ConsentExtensionConstants.PAYMENTS, consentTypeInPath)
                && StringUtils.equals(HttpMethod.DELETE, consentValidateData.getResourceParams().get(HTTP_METHOD))) {
            log.error(ErrorConstants.CANCELLATION_NOT_APPLICABLE);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.METHOD_NOT_ALLOWED.getStatusCode(),
                    TPPMessage.CodeEnum.CANCELLATION_INVALID.toString(), ErrorConstants.CANCELLATION_NOT_APPLICABLE);
            return;
        }

        // Validate consent type provided in path
        if (!StringUtils.equals(consentType, consentTypeInPath)) {
            log.error(ErrorConstants.CONSENT_ID_TYPE_MISMATCH);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.UNAUTHORIZED.getStatusCode(), TPPMessage.CodeEnum.CONSENT_INVALID.toString(),
                    ErrorConstants.CONSENT_ID_TYPE_MISMATCH);
            return;
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
            if (StringUtils.equals(TransactionStatusEnum.RCVD.name(), currentStatus)) {
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

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
