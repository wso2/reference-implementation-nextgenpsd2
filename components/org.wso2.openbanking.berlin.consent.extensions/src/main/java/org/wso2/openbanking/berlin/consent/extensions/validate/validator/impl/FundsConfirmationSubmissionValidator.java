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

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.util.CommonValidationUtil;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.util.FundsConfirmationValidationUtil;

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
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.BAD_REQUEST.getStatusCode(), TPPMessage.CodeEnum.CONSENT_UNKNOWN.toString(),
                    ErrorConstants.CONSENT_INVALID_STATE);
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
