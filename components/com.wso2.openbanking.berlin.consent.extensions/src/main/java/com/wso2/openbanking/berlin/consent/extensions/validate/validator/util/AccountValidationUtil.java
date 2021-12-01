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

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Account validation util class.
 */
public class AccountValidationUtil {

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
                    && StringUtils.equals(mappingResource.getAccountID(), accountId)) {
                isValidAccessMethodForAccount = true;
                break;
            }
        }

        for (ConsentMappingResource mappingResource : mappingResources) {
            if (StringUtils.equals(mappingResource.getPermission(), AccessMethodEnum.BALANCES.toString())
                    && StringUtils.equals(mappingResource.getAccountID(), accountId)) {
                isValidBalanceAccessMethodForAccount = true;
                break;
            }
        }

        if (isWithBalance) {
            return isValidAccessMethodForAccount && isValidBalanceAccessMethodForAccount;
        } else {
            return isValidAccessMethodForAccount;
        }
    }

    /**
     * Checks if a consent is mapped to an account containing the provided accessMethod.
     *
     * @param accessMethod     access method
     * @param mappingResources consent mapping resources
     * @return returns true if a consent is mapped to an account containing the provided accessMethod
     */
    public static boolean isWhichAccessMethod(String accessMethod, ArrayList<ConsentMappingResource> mappingResources) {

        for (ConsentMappingResource mappingResource : mappingResources) {
            if (StringUtils.equals(mappingResource.getPermission(), accessMethod)) {
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
