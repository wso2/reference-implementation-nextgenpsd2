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

        String resourcePath = StringUtils.stripStart(consentValidateData.getResourceParams().get("ResourcePath"),
                "/");
        List<String> pathList = Arrays.asList(resourcePath.split("/"));
        String accountId = AccountValidationUtil.getAccountIdFromURL(pathList);
        String accessMethod = AccountValidationUtil.getAccessMethod(pathList);

        boolean isAccountIdValidationEnabled = CommonConfigParser.getInstance().isAccountIdValidationEnabled();
        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();
        ArrayList<ConsentMappingResource> mappingResources = detailedConsentResource.getConsentMappingResources();

        if (StringUtils.isBlank(accountId)) {
            log.debug("The Account ID can not be null or empty");
            log.error(ErrorConstants.ACCOUNT_ID_CANNOT_BE_EMPTY);
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setErrorCode(TPPMessage.CodeEnum.CONSENT_INVALID.toString());
            consentValidationResult.setErrorMessage(ErrorConstants.ACCOUNT_ID_CANNOT_BE_EMPTY);
            return;
        }

        /*
         * If the request is a card account request, permission validation will always be done against the consent.
         *  Card account id validation should be done from the bank. If the request is not a card account request,
         *  permission validation will be based on the account id validation configuration, if enabled, permissions
         *  will be validated against the account id (if block). If disabled, permissions will be validated against the
         *  consent (else block).
         */
        if (isAccountIdValidationEnabled
                && !pathList.contains(ConsentExtensionConstants.CARD_ACCOUNTS_SUBMISSION_PATH_IDENTIFIER)) {
            if (AccountValidationUtil
                    .hasValidPermissionsForAccountId(accountId, accessMethod, mappingResources)) {
                consentValidationResult.setValid(true);
            } else {
                log.error("The provided account Id: " + accountId + " does not contains necessary " +
                        "permission: " + accessMethod);
                consentValidationResult.setHttpCode(ResponseStatus.NOT_FOUND.getStatusCode());
                consentValidationResult.setErrorCode(TPPMessage.CodeEnum.RESOURCE_UNKNOWN_404.toString());
                consentValidationResult.setErrorMessage(ErrorConstants.NO_MATCHING_PERMISSIONS_FOR_ACCOUNT_ID);
            }
        } else {
            if (AccountValidationUtil
                    .hasValidPermissionsForConsentId(accessMethod, mappingResources)) {
                consentValidationResult.setValid(true);
            } else {
                log.error("The provided account Id: " + accountId + " does not contains necessary " +
                        "permission: " + accessMethod);
                consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
                consentValidationResult.setErrorCode(TPPMessage.CodeEnum.CONSENT_INVALID.toString());
                consentValidationResult.setErrorMessage(ErrorConstants.NO_MATCHING_PERMISSIONS_FOR_ACCOUNT_ID);
            }
        }
    }

    /**
     * Checks if there is a consent mapping resource that matches the provided
     * permission. This method should be used when account ID validation is disabled.
     * This method does not check whether the provided account ID is present in the mapping resource.
     * It only checks whether the permissions are present for the given consent.
     *
     * @param accessMethod     access method
     * @param mappingResources mapped consent resources
     * @return returns true if there is a consent mapping resource that matches the
     * account id and access method combination provided
     */
    public static boolean hasValidPermissionsForConsentId(String accessMethod,
                                                          ArrayList<ConsentMappingResource> mappingResources) {

        boolean isValidAccessMethodForAccount = false;

        for (ConsentMappingResource mappingResource : mappingResources) {

            if (StringUtils.equals(mappingResource.getPermission(), accessMethod)
                    && StringUtils.equals(mappingResource.getMappingStatus(), ConsentExtensionConstants.ACTIVE)) {
                isValidAccessMethodForAccount = true;
                break;
            }
        }
        return isValidAccessMethodForAccount;
    }

    /**
     * Checks if there is a consent mapping resource that matches the provided
     * account id and permission. This method should be used when account ID validation is enabled.
     *
     * @param accessMethod     access method
     * @param mappingResources mapped consent resources
     * @return returns true if there is a consent mapping resource that matches the
     * account id and access method combination provided
     */
    public static boolean hasValidPermissionsForAccountId(String accountId, String accessMethod,
                                                          ArrayList<ConsentMappingResource> mappingResources) {

        boolean isValidAccessMethodForAccount = false;

        for (ConsentMappingResource mappingResource : mappingResources) {

            String accountReference = mappingResource.getAccountID();
            boolean isCardAccount = accountReference.contains(ConsentExtensionConstants.MASKED_PAN)
                    || accountReference.contains(ConsentExtensionConstants.PAN);
            if (!isCardAccount
                    && StringUtils.equals(mappingResource.getPermission(), accessMethod)
                    && accountReference.contains(accountId)
                    && StringUtils.equals(mappingResource.getMappingStatus(), ConsentExtensionConstants.ACTIVE)) {
                isValidAccessMethodForAccount = true;
                break;
            }
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
