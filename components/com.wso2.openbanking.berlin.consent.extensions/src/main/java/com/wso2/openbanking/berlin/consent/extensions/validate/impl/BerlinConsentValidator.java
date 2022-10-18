/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.validate.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidator;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.HeaderValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.factory.SubmissionValidatorFactory;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.util.CommonValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Consent validator implementation for Berlin.
 */
public class BerlinConsentValidator implements ConsentValidator {

    private static final Log log = LogFactory.getLog(BerlinConsentValidator.class);

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        // Validate X-Request-ID
        if (!HeaderValidator.isHeaderStringPresent(consentValidateData.getHeadersMap(),
                ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER)) {
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.BAD_REQUEST.getStatusCode(), TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                    ErrorConstants.X_REQUEST_ID_MISSING);
            return;
        }

        if (!HeaderValidator.isHeaderValidUUID(consentValidateData.getHeadersMap(),
                ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER)) {
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.BAD_REQUEST.getStatusCode(), TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                    ErrorConstants.X_REQUEST_ID_INVALID);
            return;
        }

        consentValidationResult.getConsentInformation()
                .appendField(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                        consentValidateData.getHeadersMap().get(ConsentExtensionConstants
                                .X_REQUEST_ID_PROPER_CASE_HEADER));

        if (consentValidateData.getHeadersMap().containsKey(
                ConsentExtensionConstants.PSU_IP_ADDRESS_PROPER_CASE_HEADER)) {
            HeaderValidator.validatePsuIpAddress(consentValidateData.getHeadersMap());
        }

        String clientIdFromToken = consentValidateData.getClientId();
        String psuIdFromToken = consentValidateData.getUserId();
        int tenantIdOccurrences = StringUtils.countMatches(psuIdFromToken,
                ConsentExtensionConstants.SUPER_TENANT_DOMAIN);

        // Cleaning up any appended tenant domains to psuIdFromToken
        for (int occurrence = 0; occurrence < tenantIdOccurrences; occurrence++) {
            psuIdFromToken = StringUtils.removeEndIgnoreCase(psuIdFromToken,
                    ConsentExtensionConstants.SUPER_TENANT_DOMAIN);
        }

        log.debug("Validating if the Consent Id belongs to the client");
        if (!StringUtils.equals(consentValidateData.getComprehensiveConsent().getClientID(), clientIdFromToken)
                || !StringUtils.equals(consentValidateData.getConsentId(),
                consentValidateData.getComprehensiveConsent().getConsentID())) {
            log.error(ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.NOT_FOUND.getStatusCode(), TPPMessage.CodeEnum.RESOURCE_UNKNOWN.toString(),
                    ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
            return;
        }

        log.debug("Validating if Consent Id belongs to the user");
        boolean isPsuIdMatching = false;
        ArrayList<AuthorizationResource> authResources = consentValidateData.getComprehensiveConsent()
                .getAuthorizationResources();
        for (AuthorizationResource resource : authResources) {
            if (psuIdFromToken.equals(resource.getUserID())) {
                isPsuIdMatching = true;
                break;
            }
        }

        if (!isPsuIdMatching) {
            log.error(ErrorConstants.NO_MATCHING_USER_FOR_CONSENT);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.UNAUTHORIZED.getStatusCode(), TPPMessage.CodeEnum.PSU_CREDENTIALS_INVALID.toString(),
                    ErrorConstants.NO_MATCHING_USER_FOR_CONSENT);
            return;
        }

        SubmissionValidator submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator(consentValidateData.getResourceParams().get("ResourcePath"));

        if (submissionValidator != null) {
            submissionValidator.validate(consentValidateData, consentValidationResult);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

}
