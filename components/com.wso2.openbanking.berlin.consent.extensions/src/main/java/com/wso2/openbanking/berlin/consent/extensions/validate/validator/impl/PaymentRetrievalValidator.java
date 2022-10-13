/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.util.CommonValidationUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Validate payments submission requests.
 */
public class PaymentRetrievalValidator implements SubmissionValidator {

    private static final Log log = LogFactory.getLog(PaymentRetrievalValidator.class);

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();
        ConsentCoreServiceImpl coreService = getConsentService();
        ArrayList<AuthorizationResource> authorizationResources;

        /* Check whether payment consent is in ACCP (consent is authorized) or ACTC status.
           The consent can be in ACTC status if the payment is to be cancelled with an authorization */
        log.debug("Checking if the consent is in ACCP, ACTC or PATC status");
        String currentStatus = detailedConsentResource.getCurrentStatus();
        if (!(StringUtils.equals(TransactionStatusEnum.ACCP.name(), currentStatus)
                || StringUtils.equals(TransactionStatusEnum.ACTC.name(), currentStatus)
                || StringUtils.equals(TransactionStatusEnum.PATC.name(), currentStatus))) {
            log.error(ErrorConstants.CONSENT_INVALID_STATE);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.BAD_REQUEST.getStatusCode(), TPPMessage.CodeEnum.CONSENT_UNKNOWN.toString(),
                    ErrorConstants.CONSENT_INVALID_STATE);
            return;
        }

        // Get the relevant authorisation resource for the consent
        try {
            authorizationResources = coreService.searchAuthorizations(detailedConsentResource.getConsentID());
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.AUTHORISATIONS_NOT_FOUND);
        }

        /* If any of the authorisation resources are not in psuAuthenticated status, throw an error.
           No need to check for the emptiness of authorizationResources since it is handled from accelerator level.
           This check needs to be changed after fixing
           issue:https://github.com/wso2-enterprise/financial-open-banking/issues/7796 */
        if (authorizationResources.size() > 1) {
            for (AuthorizationResource authResource : authorizationResources) {
                if (!StringUtils.equals(authResource.getAuthorizationStatus(),
                        ScaStatusEnum.PSU_AUTHENTICATED.toString())) {
                    /* Appending an additional field to consent info header for the bank to identify this payment is
                       not fully authorized. This case is specific to multi level scenarios where only one of many
                       users can authorize a multi level payment and can try to get the payment status. In this case
                       bank will identify it using this field and respond accordingly.*/
                    consentValidationResult.setValid(true);
                    JSONObject consentInfoJson = consentValidationResult.getConsentInformation();
                    consentInfoJson.appendField("authStatus", "partial");
                    consentValidationResult.setConsentInformation(consentInfoJson);
                    return;
                }
            }
        }

        /* If the authorisation status is in the expected status, mark validation as valid.
           No need to check for expiration of the consent since payment consent doesn't define an expiry time. */
        consentValidationResult.setValid(true);
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
