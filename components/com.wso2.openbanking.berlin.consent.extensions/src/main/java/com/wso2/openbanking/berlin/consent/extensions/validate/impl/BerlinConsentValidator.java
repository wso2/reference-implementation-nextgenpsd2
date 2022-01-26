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

        HeaderValidator.validateXRequestId(consentValidateData.getHeaders());
        consentValidationResult.getConsentInformation()
                .appendField(ConsentExtensionConstants.X_REQUEST_ID_HEADER,
                        consentValidateData.getHeaders().getAsString(ConsentExtensionConstants
                                .X_REQUEST_ID_HEADER));

        if (consentValidateData.getHeaders().containsKey(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER)) {
            HeaderValidator.validatePsuIpAddress(consentValidateData.getHeaders());
        }

        HeaderValidator.validateConsentId(consentValidateData.getHeaders());

        String clientIdFromToken = consentValidateData.getClientId();
        String psuIdFromToken = consentValidateData.getUserId();

        // Cleaning up any extra appended tenant domains to psuIdFromToken
        for (int occurrence = 1; occurrence < StringUtils.countMatches(psuIdFromToken,
                ConsentExtensionConstants.SUPER_TENANT_DOMAIN); occurrence++) {
            psuIdFromToken = StringUtils.removeEndIgnoreCase(psuIdFromToken,
                    ConsentExtensionConstants.SUPER_TENANT_DOMAIN);
        }

        log.debug("Validating if the Consent Id belongs to the client");
        if (!StringUtils.equals(consentValidateData.getComprehensiveConsent().getClientID(), clientIdFromToken)) {
            log.error(ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
            consentValidationResult.setHttpCode(ResponseStatus.FORBIDDEN.getStatusCode());
            consentValidationResult.setErrorCode(TPPMessage.CodeEnum.RESOURCE_UNKNOWN.toString());
            consentValidationResult.setErrorMessage(ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
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
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setErrorCode(TPPMessage.CodeEnum.PSU_CREDENTIALS_INVALID.toString());
            consentValidationResult.setErrorMessage(ErrorConstants.NO_MATCHING_USER_FOR_CONSENT);
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
