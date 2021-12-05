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

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.AccountConsentUtil;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.util.AccountValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        if (isConsentExpiredStatus || AccountConsentUtil.isConsentExpired(detailedConsentResource.getValidityPeriod(),
                detailedConsentResource.getUpdatedTime())) {
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
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_EXPIRED,
                    ErrorConstants.CONSENT_EXPIRED));
            return;
        }

        log.debug("Checking if consent is not in a valid state");
        if (!StringUtils.equals(detailedConsentResource.getCurrentStatus(), ConsentStatusEnum.VALID.toString())) {
            log.error(ErrorConstants.CONSENT_INVALID_STATE);
            consentValidationResult.setHttpCode(ResponseStatus.BAD_REQUEST.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_UNKNOWN,
                    ErrorConstants.CONSENT_INVALID_STATE));
            return;
        }

        log.debug("Consent is Authorized by User");
        if (AccountValidationUtil.isSingleAccountRetrieveRequest(requestPath)) {
            log.debug("Validating single accounts retrieval");
            validateAccountPermissionsForSingleAccounts(consentValidateData, consentValidationResult);
        } else if (AccountValidationUtil.isBulkAccountRetrieveRequest(requestPath)) {
            log.debug("Validating bulk accounts retrieval");
            ArrayList<ConsentMappingResource> mappingResources = consentValidateData.getComprehensiveConsent()
                    .getConsentMappingResources();

            if (mappingResources == null || mappingResources.size() < 1) {
                log.error(ErrorConstants.NO_VALID_ACCOUNTS_FOR_CONSENT);
                consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
                consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                        ErrorConstants.NO_VALID_ACCOUNTS_FOR_CONSENT));
                return;
            }

            consentValidationResult.setValid(true);
        }

        log.debug("Expiring consent for one off consents after one time use");
        if (!detailedConsentResource.isRecurringIndicator()) {
            try {
                coreService.updateConsentStatus(detailedConsentResource.getConsentID(),
                        ConsentStatusEnum.EXPIRED.toString());
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.CONSENT_UPDATE_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.CONSENT_UPDATE_ERROR);
            }
        }
    }

    private void validateAccountPermissionsForSingleAccounts(ConsentValidateData consentValidateData,
                                                             ConsentValidationResult consentValidationResult) {

        List<String> pathList = Arrays.asList(consentValidateData.getRequestPath().split("/"));
        String accountId = AccountValidationUtil.getAccountIdFromURL(pathList);
        String accessMethod = AccountValidationUtil.getAccessMethod(pathList);
        boolean isWithBalance = AccountValidationUtil.isWithBalance(consentValidateData.getRequestPath());

        boolean isAccountIdValidationEnabled = CommonConfigParser.getInstance().isAccountIdValidationEnabled();
        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();
        ArrayList<ConsentMappingResource> mappingResources = detailedConsentResource.getConsentMappingResources();

        boolean isAccountAccess = AccountValidationUtil
                .isWhichAccessMethod(AccessMethodEnum.ACCOUNTS.toString(), mappingResources);
        boolean isBalanceAccess = AccountValidationUtil
                .isWhichAccessMethod(AccessMethodEnum.BALANCES.toString(), mappingResources);
        boolean isTransactionAccess = AccountValidationUtil
                .isWhichAccessMethod(AccessMethodEnum.TRANSACTIONS.toString(), mappingResources);

        if (StringUtils.isEmpty(accountId)) {
            log.debug("The Account ID can not be null or empty");
            log.error(ErrorConstants.ACCOUNT_ID_CANNOT_BE_EMPTY);
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.ACCOUNT_ID_CANNOT_BE_EMPTY));
            return;
        }

        if (isAccountIdValidationEnabled) {
            if (!AccountValidationUtil
                    .hasValidAccountMappingResource(accountId, accessMethod, mappingResources, isWithBalance)) {
                log.debug("The Account ID in the request path is not contained in any of the mapped resources");
                log.error(ErrorConstants.NO_MATCHING_ACCOUNT_FOR_ACCOUNT_ID);
                consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
                consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                        ErrorConstants.NO_MATCHING_ACCOUNT_FOR_ACCOUNT_ID));
                return;
            }
        }

        if (!isAccountAccess || !isBalanceAccess || !isTransactionAccess) {
            log.error(ErrorConstants.NO_MATCHING_ACCOUNTS_FOR_PERMISSIONS);
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.NO_MATCHING_ACCOUNTS_FOR_PERMISSIONS));
            return;
        }

        consentValidationResult.setValid(true);
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }

}
