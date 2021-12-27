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

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.util;

import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Account validation util class.
 */
public class AccountValidationUtil {

    private static final Log log = LogFactory.getLog(AccountValidationUtil.class);

    /**
     * Validates the permissions for single account retrieval requests.
     *
     * @param consentValidateData     data required for validating the consent
     * @param consentValidationResult validation response parameters
     */
    public static void validateAccountPermissionsForSingleAccounts(ConsentValidateData consentValidateData,
                                                                   ConsentValidationResult consentValidationResult) {

        List<String> pathList = Arrays.asList(consentValidateData.getRequestPath().split("/"));
        String accountId = AccountValidationUtil.getAccountIdFromURL(pathList);
        String accessMethod = AccountValidationUtil.getAccessMethod(pathList);
        boolean isWithBalance = AccountValidationUtil.isWithBalance(consentValidateData.getRequestPath());

        boolean isAccountIdValidationEnabled = CommonConfigParser.getInstance().isAccountIdValidationEnabled();
        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();
        ArrayList<ConsentMappingResource> mappingResources = detailedConsentResource.getConsentMappingResources();

        if (StringUtils.isBlank(accountId)) {
            log.debug("The Account ID can not be null or empty");
            log.error(ErrorConstants.ACCOUNT_ID_CANNOT_BE_EMPTY);
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.ACCOUNT_ID_CANNOT_BE_EMPTY));
            return;
        }

        // Validating the access method(permission) for single account retrieval requests
        // only if the account Id validation is enabled
        if (isAccountIdValidationEnabled) {
            if (AccountValidationUtil
                    .hasValidAccountMappingResource(accountId, accessMethod, mappingResources, isWithBalance)) {
                consentValidationResult.setValid(true);
                return;
            } else {
                log.debug("The Account ID in the request path is not contained in any of the mapped resources");
                log.error(ErrorConstants.NO_MATCHING_ACCOUNT_FOR_ACCOUNT_ID);
                consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
                consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                        ErrorConstants.NO_MATCHING_ACCOUNT_FOR_ACCOUNT_ID));
                return;
            }
        }

        // If account Id validation is not enabled, checking only if there are any active mapping resource with
        // any access method(permission; accounts, balances, transactions)
        boolean hasActiveAccountAccess = AccountValidationUtil
                .hasActiveAccess(AccessMethodEnum.ACCOUNTS.toString(), mappingResources);
        boolean hasActiveBalanceAccess = AccountValidationUtil
                .hasActiveAccess(AccessMethodEnum.BALANCES.toString(), mappingResources);
        boolean hasActiveTransactionAccess = AccountValidationUtil
                .hasActiveAccess(AccessMethodEnum.TRANSACTIONS.toString(), mappingResources);

        if (!hasActiveAccountAccess || !hasActiveBalanceAccess || !hasActiveTransactionAccess) {
            log.error(ErrorConstants.NO_MATCHING_ACCOUNTS_FOR_PERMISSIONS);
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.NO_MATCHING_ACCOUNTS_FOR_PERMISSIONS));
            return;
        }

        consentValidationResult.setValid(true);
    }

    /**
     * Checks if there is a consent mapping resource that matches the provided
     * account id and access method combination.
     *
     * @param accountId        account id
     * @param accessMethod     access method
     * @param mappingResources mapped consent resources
     * @param isWithBalance    is requesting with balance
     * @return returns true if there is a consent mapping resource that matches the
     * account id and access method combination provided
     */
    public static boolean hasValidAccountMappingResource(String accountId, String accessMethod,
                                                         ArrayList<ConsentMappingResource> mappingResources,
                                                         boolean isWithBalance) {

        boolean isValidAccessMethodForAccount = false;
        boolean isValidBalanceAccessMethodForAccount = false;

        for (ConsentMappingResource mappingResource : mappingResources) {
            if (StringUtils.equals(mappingResource.getPermission(), accessMethod)
                    && StringUtils.equals(mappingResource.getAccountID(), accountId)
                    && StringUtils.equals(mappingResource.getMappingStatus(), ConsentExtensionConstants.ACTIVE)) {
                isValidAccessMethodForAccount = true;
                break;
            }
        }

        if (isWithBalance) {
            for (ConsentMappingResource mappingResource : mappingResources) {
                if (StringUtils.equals(mappingResource.getPermission(), AccessMethodEnum.BALANCES.toString())
                        && StringUtils.equals(mappingResource.getAccountID(), accountId)
                        && StringUtils.equals(mappingResource.getMappingStatus(), ConsentExtensionConstants.ACTIVE)) {
                    isValidBalanceAccessMethodForAccount = true;
                    break;
                }
            }
            return isValidAccessMethodForAccount && isValidBalanceAccessMethodForAccount;
        }

        return isValidAccessMethodForAccount;
    }

    /**
     * Checks if a consent is mapped to an account containing the provided accessMethod.
     *
     * @param accessMethod     access method
     * @param mappingResources consent mapping resources
     * @return returns true if a consent is mapped to an account containing the provided accessMethod
     */
    public static boolean hasActiveAccess(String accessMethod, ArrayList<ConsentMappingResource> mappingResources) {

        for (ConsentMappingResource mappingResource : mappingResources) {
            if (StringUtils.equals(mappingResource.getPermission(), accessMethod)
                    && StringUtils.equals(mappingResource.getMappingStatus(), ConsentExtensionConstants.ACTIVE)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the request is for single account.
     *
     * @param url request path
     * @return true or false
     */
    public static boolean isSingleAccountRetrieveRequest(String url) {

        for (String path : ConsentExtensionConstants.SINGLE_ACCOUNT_ACCESS_METHODS_REGEX_LIST) {
            if (url.matches(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the request is for bulk accounts.
     *
     * @param url request path
     * @return true or false
     */
    public static boolean isBulkAccountRetrieveRequest(String url) {

        for (String path : ConsentExtensionConstants.BULK_ACCOUNT_ACCESS_METHODS_REGEX_LIST) {
            if (url.matches(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the request has withBalance flag.
     *
     * @param url request path
     * @return true or false
     */
    public static boolean isWithBalance(String url) {

        String[] paramList = url.split("\\?");
        if (paramList[paramList.length - 1].equals(ConsentExtensionConstants.WITH_BALANCE)) {
            return true;
        }
        return false;
    }

    /**
     * Get the retrieval access method.
     *
     * @param pathList list of request path strings after splitting
     * @return access method
     */
    public static String getAccessMethod(List<String> pathList) {

        for (String path : pathList) {
            if (path.contains(AccessMethodEnum.BALANCES.toString())) {
                return AccessMethodEnum.BALANCES.toString();
            } else if (path.contains(AccessMethodEnum.TRANSACTIONS.toString())) {
                return AccessMethodEnum.TRANSACTIONS.toString();
            }
        }
        return AccessMethodEnum.ACCOUNTS.toString();
    }

    /**
     * Get the transaction id from the incoming URL.
     *
     * @param pathList list of request path strings after splitting
     * @return transaction id
     */
    String getTransactionIdFromURL(List<String> pathList) {

        int transactionsIndex = pathList.indexOf(AccessMethodEnum.TRANSACTIONS.toString());
        int size = pathList.size();

        if (transactionsIndex == -1) {
            return "";
        } else if (transactionsIndex + 1 == size) {
            return "";
        } else if (pathList.get(transactionsIndex + 1).equals("")) {
            return "";
        } else {
            return pathList.get(transactionsIndex + 1);
        }
    }

    /**
     * Get the Account ID from the incoming URL.
     *
     * @param pathList list of request path strings after splitting
     * @return account id
     */
    public static String getAccountIdFromURL(List<String> pathList) {

        int accountsIndex = -1;

        if (pathList.contains(AccessMethodEnum.ACCOUNTS.toString())) {
            accountsIndex = pathList.indexOf(AccessMethodEnum.ACCOUNTS.toString());
        } else if (pathList.contains(ConsentExtensionConstants.CARD_ACCOUNTS)) {
            accountsIndex = pathList.indexOf(ConsentExtensionConstants.CARD_ACCOUNTS);
        }
        int size = pathList.size();

        if (accountsIndex + 1 == size) {
            return "";
        } else if (pathList.get(accountsIndex + 1).equals("")) {
            return "";
        } else {
            return pathList.get(accountsIndex + 1).split("\\?")[0];
        }
    }

}
