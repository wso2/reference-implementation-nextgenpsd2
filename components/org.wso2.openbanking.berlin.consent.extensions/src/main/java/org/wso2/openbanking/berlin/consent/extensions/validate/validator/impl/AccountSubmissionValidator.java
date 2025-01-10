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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.AccountConsentUtil;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.util.AccountValidationUtil;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.util.CommonValidationUtil;

import java.util.ArrayList;

/**
 * Validate Accounts submission requests.
 */
public class AccountSubmissionValidator implements SubmissionValidator {

    private static final Log log = LogFactory.getLog(AccountSubmissionValidator.class);

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();

        String requestPath = consentValidateData.getRequestPath();
        ConsentCoreServiceImpl coreService = getConsentService();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Checking if consent id %s is expired", detailedConsentResource.getConsentID()));
        }
        boolean isConsentExpiredStatus = StringUtils.equals(detailedConsentResource.getCurrentStatus(),
                ConsentStatusEnum.EXPIRED.toString());
        if (isConsentExpiredStatus || (detailedConsentResource.isRecurringIndicator() &&
                AccountConsentUtil.isConsentExpired(
                        detailedConsentResource.getValidityPeriod(), detailedConsentResource.getUpdatedTime()))) {
            if (!isConsentExpiredStatus) {
                try {
                    coreService.updateConsentStatus(detailedConsentResource.getConsentID(),
                            ConsentStatusEnum.EXPIRED.toString());
                } catch (ConsentManagementException e) {
                    log.error(ErrorConstants.CONSENT_UPDATE_ERROR, e);
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            ErrorConstants.CONSENT_UPDATE_ERROR);
                }
            }
            log.error(ErrorConstants.CONSENT_EXPIRED);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.UNAUTHORIZED.getStatusCode(), TPPMessage.CodeEnum.CONSENT_EXPIRED.toString(),
                    ErrorConstants.CONSENT_EXPIRED);
            return;
        }

        log.debug("Checking if consent: " + consentValidateData.getConsentId() + " is not in a valid state");
        if (!StringUtils.equals(detailedConsentResource.getCurrentStatus(), ConsentStatusEnum.VALID.toString())) {
            log.error(ErrorConstants.CONSENT_INVALID_STATE);
            CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                    ResponseStatus.BAD_REQUEST.getStatusCode(), TPPMessage.CodeEnum.CONSENT_UNKNOWN.toString(),
                    ErrorConstants.CONSENT_INVALID_STATE);
            return;
        }

        if (AccountValidationUtil.isSingleAccountRetrieveRequest(requestPath)) {
            log.debug("Validating single accounts retrieval for user: " + consentValidateData.getUserId());
            AccountValidationUtil
                    .validateAccountPermissionsForSingleAccounts(consentValidateData, consentValidationResult);
        } else if (AccountValidationUtil.isBulkAccountRetrieveRequest(requestPath)) {
            log.debug("Validating bulk accounts retrieval for user: " + consentValidateData.getUserId());
            ArrayList<ConsentMappingResource> mappingResources = consentValidateData.getComprehensiveConsent()
                    .getConsentMappingResources();

            if (!CommonValidationUtil.hasAnyActiveMappingResource(mappingResources)) {
                log.error(ErrorConstants.NO_VALID_ACCOUNTS_FOR_CONSENT);
                CommonValidationUtil.handleConsentValidationError(consentValidationResult,
                        ResponseStatus.UNAUTHORIZED.getStatusCode(), TPPMessage.CodeEnum.CONSENT_INVALID.toString(),
                        ErrorConstants.NO_VALID_ACCOUNTS_FOR_CONSENT);
                return;
            }

            consentValidationResult.setValid(true);
        }

        if (!detailedConsentResource.isRecurringIndicator()) {
            try {
                log.debug("Expiring consent: " + detailedConsentResource.getConsentID() + " for one off consents " +
                        "after one time use");
                coreService.updateConsentStatus(detailedConsentResource.getConsentID(),
                        ConsentStatusEnum.EXPIRED.toString());
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.CONSENT_UPDATE_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.CONSENT_UPDATE_ERROR);
            }
        }
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }

}
