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
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.PermissionEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.AccountConsentUtil;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.util.AccountValidationUtil;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
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

        /*
        The 'permission'(can contain availableAccounts, availableAccountsWithBalances, allPsd2 or default)
        and 'accessMethod'(can contain allAccounts or allAccountsWithOwnerName) stored in the consent validation
        result is only relevant for 'Account List of Available Accounts' and 'Global' consents.

        The 'accessMethod'(can contain accounts, balances or transactions) inside the account of the account list
        and this is only relevant for 'Consent Request on Dedicated Accounts' and 'Bank Offered Consent' consents.
         */
        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();

        JSONObject consentReceipt;
        try {
            consentReceipt =
                    (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(detailedConsentResource.getReceipt());
        } catch (ParseException e) {
            log.error(ErrorConstants.JSON_PARSE_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.JSON_PARSE_ERROR);
        }

        String requestPath = consentValidateData.getRequestPath();
        ConsentCoreServiceImpl coreService = new ConsentCoreServiceImpl();

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
        JSONObject consentInfo = new JSONObject();

        String permission = detailedConsentResource.getConsentAttributes().get(ConsentExtensionConstants.PERMISSION);
        consentReceipt.appendField(ConsentExtensionConstants.CONSENT_ID, detailedConsentResource.getConsentID());

        if (!StringUtils.equalsIgnoreCase(permission, PermissionEnum.DEFAULT.toString())) {
            consentReceipt.appendField(ConsentExtensionConstants.VALIDATION_RESPONSE_PERMISSION, permission);
            consentReceipt.appendField(ConsentExtensionConstants.ACCESS_METHOD,
                    getAccessMethodForPermission(permission, consentReceipt));
        }

        consentInfo.appendField(ConsentExtensionConstants.ACCOUNT_CONSENT_INFO, consentReceipt);
        consentValidationResult.setConsentInformation(consentInfo);

        if (AccountValidationUtil.isSingleAccountRetrieveRequest(requestPath)) {
            log.debug("Validating single accounts retrieval");
            validateAccountPermissionsForSingleAccounts(consentValidateData, consentValidationResult);
        } else if (AccountValidationUtil.isBulkAccountRetrieveRequest(requestPath)) {
            log.debug("Validating bulk accounts retrieval");
            validateAccountPermissionsForBulkAccounts(consentValidateData, consentValidationResult, permission);

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

    private void validateAccountPermissionsForBulkAccounts(ConsentValidateData consentValidateData,
                                                           ConsentValidationResult consentValidationResult,
                                                           String permission) {

        List<String> pathList = Arrays.asList(consentValidateData.getRequestPath().split("/"));
        String accessMethod = AccountValidationUtil.getAccessMethod(pathList);
        boolean isWithBalance = AccountValidationUtil.isWithBalance(consentValidateData.getRequestPath());
        ArrayList<ConsentMappingResource> mappingResources = consentValidateData.getComprehensiveConsent()
                .getConsentMappingResources();

        if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.ALL_PSD2.toString())
                || StringUtils.equalsIgnoreCase(permission,
                PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString())) {
            AccountValidationUtil.setAccountInfoForBulkAccountRequests(consentValidationResult, mappingResources,
                    true, isWithBalance, accessMethod, true);
        } else {
            AccountValidationUtil.setAccountInfoForBulkAccountRequests(consentValidationResult, mappingResources,
                    true, isWithBalance, accessMethod, false);
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
            AccountValidationUtil.setAccountInfoForSingleAccountRequests(consentValidationResult, false,
                    false, null, null);
            return;
        }

        if (isAccountIdValidationEnabled) {
            if (!AccountValidationUtil
                    .isAccountIdInMappingResourceForAccessMethod(accountId, accessMethod, mappingResources)) {
                log.debug("The Account ID in the request path is not contained in any of the mapped resources");
                AccountValidationUtil.setAccountInfoForSingleAccountRequests(consentValidationResult, false,
                        false, null, null);
                return;
            }
        }

        if (StringUtils.equals(accessMethod, AccessMethodEnum.BALANCES.toString())) {
            AccountValidationUtil.setAccountInfoForSingleAccountRequests(consentValidationResult, isBalanceAccess,
                    isWithBalance, accountId, AccessMethodEnum.BALANCES.toString());
            return;
        }

        if (StringUtils.equals(accessMethod, AccessMethodEnum.ACCOUNTS.toString())) {
            AccountValidationUtil.setAccountInfoForSingleAccountRequests(consentValidationResult, isAccountAccess,
                    isWithBalance, accountId, AccessMethodEnum.ACCOUNTS.toString());
            return;
        }

        if (StringUtils.equals(accessMethod, AccessMethodEnum.TRANSACTIONS.toString())) {
            AccountValidationUtil.setAccountInfoForSingleAccountRequests(consentValidationResult, isTransactionAccess,
                    isWithBalance, accountId, AccessMethodEnum.TRANSACTIONS.toString());
            return;
        }

        AccountValidationUtil.setAccountInfoForSingleAccountRequests(consentValidationResult, false,
                false, null, null);
    }

    private String getAccessMethodForPermission(String permission, JSONObject consentReceipt) {

        JSONObject accessObject = (JSONObject) consentReceipt.get(ConsentExtensionConstants.ACCESS);
        return accessObject.getAsString(permission);
    }

}
