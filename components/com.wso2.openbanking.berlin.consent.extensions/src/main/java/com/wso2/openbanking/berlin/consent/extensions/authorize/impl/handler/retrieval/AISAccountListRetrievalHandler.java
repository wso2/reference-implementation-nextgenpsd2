/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.DataRetrievalUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.PermissionEnum;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Account account list data retrieval for Authorize.
 */
public class AISAccountListRetrievalHandler implements AccountListRetrievalHandler {

    private static final Log log = LogFactory.getLog(AISAccountListRetrievalHandler.class);

    @Override
    public JSONObject getAccountData(ConsentData consentData, JSONObject consentDataJSON) throws ConsentException {

        String permission = consentDataJSON.getAsString(ConsentExtensionConstants.PERMISSION);
        JSONObject accessObject = (JSONObject) consentDataJSON.get(ConsentExtensionConstants.ACCESS_OBJECT);

        JSONArray accountDetailsArray = null;

        /*
        Access methods 'balances' and 'transactions' includes 'accounts' permission implicitly.

        For dedicated accounts and bank offered accounts consents
        ---------------------------------------------------------
        The access methods are added in scenarios where the accounts are finalised with
        the access methods, not adding them for bank offered consent scenarios since the
        accounts are yet to be given access methods.

        If access method is an empty array then add bank offered accounts.
        If not empty array then add the validated account details from initiation payload.
         */
        if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.DEDICATED_ACCOUNTS.toString())) {
            accountDetailsArray = getDedicatedAccountDetails(accessObject, consentData.getUserId());
        } else if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.BANK_OFFERED.toString())) {
            accountDetailsArray = getBankOfferedAccountDetails(accessObject, consentData.getUserId());
        } else if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.ALL_PSD2.toString())) {
            accountDetailsArray = getAllPsd2AccountDetails(consentData.getUserId());
        } else if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.AVAILABLE_ACCOUNTS.toString())
                || StringUtils.equalsIgnoreCase(permission,
                PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString())) {
            accountDetailsArray = getListOfAvailableAccountDetails(permission, consentData.getUserId());
        }

        if (accountDetailsArray == null || accountDetailsArray.isEmpty()) {
            log.error(ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_REQUEST,
                            ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER, consentData.getRedirectURI(),
                            consentData.getState()));
        }

        return new JSONObject().appendField(ConsentExtensionConstants.ACCOUNT_DETAILS_LIST, accountDetailsArray);
    }

    @Override
    public void appendAccountDetailsToMetadata(Map<String, Object> metaDataMap, JSONObject accountDataJSON) {

        JSONArray accountDetailsArray = (JSONArray) accountDataJSON.get(ConsentExtensionConstants.ACCOUNT_DETAILS_LIST);

        JSONArray accountsAccountRefObjects = new JSONArray();
        JSONArray balancesAccountRefObjects = new JSONArray();
        JSONArray transactionsAccountRefObjects = new JSONArray();

        for (Object accountDetails : accountDetailsArray) {
            JSONObject accountDetailsObj = (JSONObject) accountDetails;
            JSONArray accountRefObjects = (JSONArray) accountDetailsObj
                    .get(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
            JSONArray accessMethods = (JSONArray) accountDetailsObj
                    .get(ConsentExtensionConstants.ACCESS_METHODS);

            for (Object accessMethodJson : accessMethods) {
                String accessMethod = (String) accessMethodJson;

                if (StringUtils.equals(accessMethod, AccessMethodEnum.ACCOUNTS.toString())) {
                    accountsAccountRefObjects.addAll(accountRefObjects);
                }
                if (StringUtils.equals(accessMethod, AccessMethodEnum.BALANCES.toString())) {
                    balancesAccountRefObjects.addAll(accountRefObjects);
                }
                if (StringUtils.equals(accessMethod, AccessMethodEnum.TRANSACTIONS.toString())) {
                    transactionsAccountRefObjects.addAll(accountRefObjects);
                }
            }
        }
        metaDataMap.put(ConsentExtensionConstants.ACCOUNTS_ACCOUNT_REF_OBJECTS, accountsAccountRefObjects);
        metaDataMap.put(ConsentExtensionConstants.BALANCES_ACCOUNT_REF_OBJECTS, balancesAccountRefObjects);
        metaDataMap.put(ConsentExtensionConstants.TRANSACTIONS_ACCOUNT_REF_OBJECTS, transactionsAccountRefObjects);
    }

    /**
     * Gets the validated account details using the initiation payload.
     *
     * @param accessObject access object
     * @param userId       user id
     * @return accounts array
     */
    private JSONArray getDedicatedAccountDetails(JSONObject accessObject, String userId) {

        JSONArray accountDetailsArray = new JSONArray();

        String shareableAccountsEndpoint = CommonConfigParser.getInstance().getShareableAccountsRetrieveEndpoint();
        JSONArray bankOfferedAccounts = DataRetrievalUtil.getAccountsFromEndpoint(userId, shareableAccountsEndpoint,
                new HashMap<>(), new HashMap<>());

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            return null;
        }

        JSONArray accountsAccountRefObjects = (JSONArray) accessObject
                .get(AccessMethodEnum.ACCOUNTS.toString());
        JSONArray balancesAccountRefObjects = (JSONArray) accessObject
                .get(AccessMethodEnum.BALANCES.toString());
        JSONArray transactionsAccountRefObjects = (JSONArray) accessObject
                .get(AccessMethodEnum.TRANSACTIONS.toString());

        boolean areAccountsInvalid = false;
        if (accountsAccountRefObjects != null && !accountsAccountRefObjects.isEmpty()) {
            accountsAccountRefObjects = getValidatedAccountRefObjects(accountsAccountRefObjects, bankOfferedAccounts);
            if (accountsAccountRefObjects == null || accountsAccountRefObjects.isEmpty()) {
                areAccountsInvalid = true;
            }
        }

        if (balancesAccountRefObjects != null && !balancesAccountRefObjects.isEmpty()) {
            balancesAccountRefObjects = getValidatedAccountRefObjects(balancesAccountRefObjects, bankOfferedAccounts);
            if (balancesAccountRefObjects == null || balancesAccountRefObjects.isEmpty()) {
                areAccountsInvalid = true;
            }
        }

        if (transactionsAccountRefObjects != null && !transactionsAccountRefObjects.isEmpty()) {
            transactionsAccountRefObjects = getValidatedAccountRefObjects(transactionsAccountRefObjects,
                    bankOfferedAccounts);
            if (transactionsAccountRefObjects == null || transactionsAccountRefObjects.isEmpty()) {
                areAccountsInvalid = true;
            }
        }

        if (areAccountsInvalid) {
            log.error("Consent accounts mismatch");
            return null;
        }

        if (balancesAccountRefObjects != null) {
            JSONArray permissionArray = new JSONArray();
            JSONArray accessMethodArray = new JSONArray();

            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.BALANCES_PERMISSION);

            accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
            accessMethodArray.add(AccessMethodEnum.BALANCES.toString());

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS, balancesAccountRefObjects);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_BALANCE);

            accountDetailsArray.add(object);
        }

        if (transactionsAccountRefObjects != null) {
            JSONArray permissionArray = new JSONArray();
            JSONArray accessMethodArray = new JSONArray();

            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.TRANSACTIONS_PERMISSION);

            accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
            accessMethodArray.add(AccessMethodEnum.TRANSACTIONS.toString());

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS, transactionsAccountRefObjects);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_TRANSACTION);

            accountDetailsArray.add(object);
        }

        if (accountsAccountRefObjects != null) {
            JSONArray permissionArray = new JSONArray();
            JSONArray accessMethodArray = new JSONArray();

            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);

            accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS, accountsAccountRefObjects);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_ACCOUNT);

            accountDetailsArray.add(object);
        }

        return accountDetailsArray;
    }

    /**
     * Gets the bank offered account details using the initiation payload.
     *
     * @param accessObject access object
     * @param userId       user id
     * @return accounts array
     */
    private JSONArray getBankOfferedAccountDetails(JSONObject accessObject, String userId) {

        JSONArray accountDetailsArray = new JSONArray();

        String shareableAccountsEndpoint = CommonConfigParser.getInstance().getShareableAccountsRetrieveEndpoint();
        JSONArray bankOfferedAccounts = DataRetrievalUtil.getAccountsFromEndpoint(userId, shareableAccountsEndpoint,
                new HashMap<>(), new HashMap<>());

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            return null;
        }

        JSONArray accountsAccountRefObjects = (JSONArray) accessObject
                .get(AccessMethodEnum.ACCOUNTS.toString());
        JSONArray balancesAccountRefObjects = (JSONArray) accessObject
                .get(AccessMethodEnum.BALANCES.toString());
        JSONArray transactionsAccountRefObjects = (JSONArray) accessObject
                .get(AccessMethodEnum.TRANSACTIONS.toString());

        if (balancesAccountRefObjects != null && balancesAccountRefObjects.isEmpty()) {
            JSONArray permissionArray = new JSONArray();
            JSONArray accessMethodArray = new JSONArray();

            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.BALANCES_PERMISSION);

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS, bankOfferedAccounts);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.SELECT_BALANCE);

            accountDetailsArray.add(object);
        }

        if (transactionsAccountRefObjects != null && transactionsAccountRefObjects.isEmpty()) {
            JSONArray permissionArray = new JSONArray();
            JSONArray accessMethodArray = new JSONArray();

            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.TRANSACTIONS_PERMISSION);

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS, bankOfferedAccounts);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.SELECT_TRANSACTION);

            accountDetailsArray.add(object);
        }

        if (accountsAccountRefObjects != null && accountsAccountRefObjects.isEmpty()) {
            JSONArray permissionArray = new JSONArray();
            JSONArray accessMethodArray = new JSONArray();

            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS, bankOfferedAccounts);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.SELECT_ACCOUNT);

            accountDetailsArray.add(object);
        }

        return accountDetailsArray;
    }

    /**
     * Gets the account details using the bank backend.
     *
     * @param permission consent permission
     * @param userId     user id
     * @return accounts array
     */
    private JSONArray getListOfAvailableAccountDetails(String permission, String userId) {

        JSONArray accountDetailsArray = new JSONArray();

        String shareableAccountsEndpoint = CommonConfigParser.getInstance().getShareableAccountsRetrieveEndpoint();
        JSONArray bankOfferedAccounts = DataRetrievalUtil.getAccountsFromEndpoint(userId,
                shareableAccountsEndpoint, new HashMap<>(), new HashMap<>());

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            return null;
        }

        JSONArray permissionArray = new JSONArray();
        JSONArray accessMethodArray = new JSONArray();

        if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString())) {
            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.BALANCES_PERMISSION);

            accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
            accessMethodArray.add(AccessMethodEnum.BALANCES.toString());
        } else {
            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);

            accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
        }

        JSONObject object = new JSONObject();
        object.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS, bankOfferedAccounts);
        object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
        object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
        object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_BULK);

        accountDetailsArray.add(object);

        return accountDetailsArray;
    }

    /**
     * Gets the account details using the bank backend.
     *
     * @param userId user id
     * @return accounts array
     */
    private JSONArray getAllPsd2AccountDetails(String userId) {

        JSONArray permissionArray = new JSONArray();
        JSONArray accessMethodArray = new JSONArray();

        permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
        permissionArray.add(ConsentExtensionConstants.BALANCES_PERMISSION);
        permissionArray.add(ConsentExtensionConstants.TRANSACTIONS_PERMISSION);

        accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
        accessMethodArray.add(AccessMethodEnum.BALANCES.toString());
        accessMethodArray.add(AccessMethodEnum.TRANSACTIONS.toString());

        String shareableAccountsEndpoint = CommonConfigParser.getInstance().getShareableAccountsRetrieveEndpoint();
        JSONArray bankOfferedAccounts = DataRetrievalUtil.getAccountsFromEndpoint(userId,
                shareableAccountsEndpoint, new HashMap<>(), new HashMap<>());

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            return null;
        }

        JSONObject object = new JSONObject();
        object.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS, bankOfferedAccounts);
        object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
        object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
        object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_BULK);

        JSONArray accountDetailsArray = new JSONArray();
        accountDetailsArray.add(object);

        return accountDetailsArray;
    }

    /**
     * Returns the validated account reference objects after
     * considering accounts service related multi-currency validations.
     *
     * @param accountRefObjects account reference objects array from initiation payload
     * @param accountArray      accounts array retrieved from bank backend
     * @return validated account ref objects array
     */
    private JSONArray getValidatedAccountRefObjects(JSONArray accountRefObjects, JSONArray accountArray) {

        JSONArray validatedAccountRefObjects = new JSONArray();
        for (Object accountObject : accountRefObjects) {
            JSONObject accountRefObject = (JSONObject) accountObject;
            JSONArray filteredAccountRefObjects = ConsentAuthUtil.getFilteredAccountsForAccountNumber(accountRefObject,
                    accountArray);

            if (filteredAccountRefObjects.size() > 1) {
                // Multi currency account
                if (accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY)) {
                    for (Object object : filteredAccountRefObjects) {
                        JSONObject filteredAccountRefObject = (JSONObject) object;
                        if (filteredAccountRefObject.getAsString(ConsentExtensionConstants.CURRENCY)
                                .equalsIgnoreCase(accountRefObject.getAsString(ConsentExtensionConstants.CURRENCY))) {
                            validatedAccountRefObjects.appendElement(accountRefObject);
                            break;
                        }
                    }
                } else {
                    // Adding all the accounts when TPP initiated a multi-currency account
                    // without specifying the currency
                    validatedAccountRefObjects.addAll(filteredAccountRefObjects);
                }
            } else if (filteredAccountRefObjects.size() == 1) {
                if (!accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY)) {
                    validatedAccountRefObjects.appendElement(accountRefObject);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        return validatedAccountRefObjects;
    }
}