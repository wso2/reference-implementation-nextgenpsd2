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

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.util.FundsConfirmationValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Validate Funds Confirmation submission requests.
 */
public class FundsConfirmationSubmissionValidator implements SubmissionValidator {

    private static final Log log = LogFactory.getLog(FundsConfirmationSubmissionValidator.class);

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();

        log.debug("Checking if consent: " + consentValidateData.getConsentId() + " is not in a valid state");
        if (!StringUtils.equals(detailedConsentResource.getCurrentStatus(), ConsentStatusEnum.VALID.toString())) {
            log.error(ErrorConstants.CONSENT_INVALID_STATE);
            consentValidationResult.setHttpCode(ResponseStatus.BAD_REQUEST.getStatusCode());
            consentValidationResult.setErrorCode(TPPMessage.CodeEnum.CONSENT_UNKNOWN.toString());
            consentValidationResult.setErrorMessage(ErrorConstants.CONSENT_INVALID_STATE);
            return;
        }

        if (!FundsConfirmationValidationUtil.isPayloadValid(detailedConsentResource, consentValidationResult,
                consentValidateData.getPayload())) {
            return;
        }

        consentValidationResult.getConsentInformation()
                .appendField(ConsentExtensionConstants.PAYLOAD, consentValidateData.getPayload());
        consentValidationResult.setValid(true);
    }

}
