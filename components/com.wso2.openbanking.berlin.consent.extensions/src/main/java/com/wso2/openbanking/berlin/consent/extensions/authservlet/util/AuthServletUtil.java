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

package com.wso2.openbanking.berlin.consent.extensions.authservlet.util;

import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Util class for servlet extension that handles Berlin scenarios.
 */
public class AuthServletUtil {

    /**
     * Converts the plain string account reference details from the consent page to JSON array.
     *
     * @param accountRefs checked account reference details from consent page
     * @return JSON account refs array
     */
    public static JSONArray convertToAccountRefObjectsArray(String[] accountRefs) {

        JSONArray accountRefObjects = new JSONArray();
        String configuredAccountReference = CommonConfigParser.getInstance().getAccountReferenceType();

        for (String accountRef : accountRefs) {
            JSONObject accountRefObject = new JSONObject();
            if (accountRef.matches(".*\\(([A-Z]{3})\\)")) {
                String[] accountDetails = accountRef.split(" ");
                String accountNumber = accountDetails[0].trim();
                String currencyString = accountDetails[1].trim();
                accountRefObject.put(configuredAccountReference, accountNumber);
                accountRefObject.put(ConsentExtensionConstants.CURRENCY,
                        currencyString.substring(1, currencyString.length() - 1));
            } else {
                accountRefObject.put(configuredAccountReference, accountRef.trim());
            }
            accountRefObjects.put(accountRefObject);
        }

        return accountRefObjects;
    }

    /**
     * Method to populate payments data to be sent to consent page.
     *
     * @param httpServletRequest
     * @param dataSet
     * @return
     */
    public static Map<String, Object> populatePaymentsData(HttpServletRequest httpServletRequest, JSONObject dataSet) {

        Map<String, Object> returnMaps = new HashMap<>();

        JSONArray dataRequestedJsonArray = dataSet.getJSONObject(ConsentExtensionConstants.CONSENT_DATA)
                .getJSONArray(ConsentExtensionConstants.CONSENT_DETAILS);
        Map<String, List<String>> dataRequested = new LinkedHashMap<>();

        for (int requestedDataIndex = 0; requestedDataIndex < dataRequestedJsonArray.length(); requestedDataIndex++) {
            JSONObject dataObj = dataRequestedJsonArray.getJSONObject(requestedDataIndex);
            String title = dataObj.getString(ConsentExtensionConstants.TITLE);
            JSONArray dataArray = dataObj.getJSONArray(ConsentExtensionConstants.DATA_SIMPLE);
            ArrayList<String> listData = new ArrayList<>();

            for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                listData.add(dataArray.getString(dataIndex));
            }
            dataRequested.put(title, listData);
        }
        returnMaps.put(ConsentExtensionConstants.DATA_REQUESTED, dataRequested);
        httpServletRequest.setAttribute(ConsentExtensionConstants.CONSENT_TYPE,
                dataSet.getString(ConsentExtensionConstants.TYPE));
        httpServletRequest.setAttribute(ConsentExtensionConstants.AUTH_TYPE,
                dataSet.getString(ConsentExtensionConstants.AUTH_TYPE));

        return returnMaps;
    }

    /**
     * Method to populate accounts data to be sent to consent page.
     *
     * @param httpServletRequest
     * @param dataSet
     * @return
     */
    public static Map<String, Object> populateAccountsData(HttpServletRequest httpServletRequest, JSONObject dataSet) {

        Map<String, Object> returnMaps = new HashMap<>();

        JSONArray dataRequestedJsonArray = dataSet.getJSONObject(ConsentExtensionConstants.CONSENT_DATA)
                .getJSONArray(ConsentExtensionConstants.CONSENT_DETAILS);
        Map<String, List<String>> dataRequested = new LinkedHashMap<>();

        for (int consentDataIndex = 0; consentDataIndex < dataRequestedJsonArray.length(); consentDataIndex++) {
            JSONObject dataObj = dataRequestedJsonArray.getJSONObject(consentDataIndex);
            String title = dataObj.getString(ConsentExtensionConstants.TITLE);
            JSONArray dataArray = dataObj.getJSONArray(ConsentExtensionConstants.DATA_SIMPLE);
            ArrayList<String> listData = new ArrayList<>();

            for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                listData.add(dataArray.getString(dataIndex));
            }
            dataRequested.put(title, listData);
        }

        // Setting the account details and permission to be displayed in the consent page
        JSONArray accountDetailsJsonArray = dataSet.getJSONObject(ConsentExtensionConstants.ACCOUNT_DATA)
                .getJSONArray(ConsentExtensionConstants.ACCOUNT_DETAILS_LIST);
        Map<String, List<String>> staticBulkMap = new HashMap<>();
        boolean isStaticBulk = false;

        Map<String, List<String>> selectBalanceMap = new HashMap<>();
        boolean isSelectBalance = false;

        Map<String, List<String>> staticBalanceMap = new HashMap<>();
        boolean isStaticBalance = false;

        Map<String, List<String>> selectAccountMap = new HashMap<>();
        boolean isSelectAccount = false;

        Map<String, List<String>> staticAccountMap = new HashMap<>();
        boolean isStaticAccount = false;

        Map<String, List<String>> selectTransactionMap = new HashMap<>();
        boolean isSelectTransaction = false;

        Map<String, List<String>> staticTransactionMap = new HashMap<>();
        boolean isStaticTransaction = false;

        String configuredAccountReference = CommonConfigParser.getInstance().getAccountReferenceType();
        for (int accountDetailsIndex = 0; accountDetailsIndex < accountDetailsJsonArray.length();
             accountDetailsIndex++) {
            JSONObject dataObj = accountDetailsJsonArray.getJSONObject(accountDetailsIndex);
            String accountType = dataObj.getString(ConsentExtensionConstants.ACCOUNT_TYPE);
            JSONArray accountRefObjects = dataObj.getJSONArray(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
            JSONArray permissionsJsonArray = dataObj.getJSONArray(ConsentExtensionConstants.PERMISSIONS);

            List<String> accountRefs = new ArrayList<>();
            List<String> permissions = new ArrayList<>();

            for (int accNumberIndex = 0; accNumberIndex < accountRefObjects.length(); accNumberIndex++) {
                JSONObject obj = accountRefObjects.getJSONObject(accNumberIndex);
                if (obj.has(ConsentExtensionConstants.CURRENCY)) {
                    String accountNumber = obj.getString(configuredAccountReference);
                    String currency = obj.getString(ConsentExtensionConstants.CURRENCY);
                    String accountRef = String.format("%s (%s)", accountNumber, currency);
                    accountRefs.add(accountRef);
                } else {
                    accountRefs.add(obj.getString(configuredAccountReference));
                }
            }

            for (int permissionIndex = 0; permissionIndex < permissionsJsonArray.length(); permissionIndex++) {
                permissions.add(permissionsJsonArray.getString(permissionIndex));
            }

            if (StringUtils.equals(accountType, ConsentExtensionConstants.STATIC_BULK)) {
                isStaticBulk = true;
                staticBulkMap.put(ConsentExtensionConstants.ACCOUNT_REFS, accountRefs);
                staticBulkMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.SELECT_BALANCE)) {
                isSelectBalance = true;
                selectBalanceMap.put(ConsentExtensionConstants.ACCOUNT_REFS, accountRefs);
                selectBalanceMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.STATIC_BALANCE)) {
                isStaticBalance = true;
                staticBalanceMap.put(ConsentExtensionConstants.ACCOUNT_REFS, accountRefs);
                staticBalanceMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.SELECT_ACCOUNT)) {
                isSelectAccount = true;
                selectAccountMap.put(ConsentExtensionConstants.ACCOUNT_REFS, accountRefs);
                selectAccountMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.STATIC_ACCOUNT)) {
                isStaticAccount = true;
                staticAccountMap.put(ConsentExtensionConstants.ACCOUNT_REFS, accountRefs);
                staticAccountMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.SELECT_TRANSACTION)) {
                isSelectTransaction = true;
                selectTransactionMap.put(ConsentExtensionConstants.ACCOUNT_REFS, accountRefs);
                selectTransactionMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.STATIC_TRANSACTION)) {
                isStaticTransaction = true;
                staticTransactionMap.put(ConsentExtensionConstants.ACCOUNT_REFS, accountRefs);
                staticTransactionMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            }
        }

        returnMaps.put(ConsentExtensionConstants.DATA_REQUESTED, dataRequested);

        returnMaps.put(ConsentExtensionConstants.STATIC_BULK, staticBulkMap);
        returnMaps.put(ConsentExtensionConstants.SELECT_BALANCE, selectBalanceMap);
        returnMaps.put(ConsentExtensionConstants.STATIC_BALANCE, staticBalanceMap);
        returnMaps.put(ConsentExtensionConstants.SELECT_ACCOUNT, selectAccountMap);
        returnMaps.put(ConsentExtensionConstants.STATIC_ACCOUNT, staticAccountMap);
        returnMaps.put(ConsentExtensionConstants.SELECT_TRANSACTION, selectTransactionMap);
        returnMaps.put(ConsentExtensionConstants.STATIC_TRANSACTION, staticTransactionMap);

        returnMaps.put("isStaticBulk", isStaticBulk);
        returnMaps.put("isSelectBalance", isSelectBalance);
        returnMaps.put("isStaticBalance", isStaticBalance);
        returnMaps.put("isSelectAccount", isSelectAccount);
        returnMaps.put("isStaticAccount", isStaticAccount);
        returnMaps.put("isSelectTransaction", isSelectTransaction);
        returnMaps.put("isStaticTransaction", isStaticTransaction);

        httpServletRequest.setAttribute(ConsentExtensionConstants.CONSENT_TYPE,
                dataSet.getString(ConsentExtensionConstants.TYPE));
        httpServletRequest.setAttribute(ConsentExtensionConstants.AUTH_TYPE,
                dataSet.getString(ConsentExtensionConstants.AUTH_TYPE));

        return returnMaps;
    }

    /**
     * Method to populate funds confirmation data to be sent to consent page.
     *
     * @param httpServletRequest
     * @param dataSet
     * @return
     */
    public static Map<String, Object> populateFundsConfirmationData(HttpServletRequest httpServletRequest,
                                                                    JSONObject dataSet) {

        Map<String, Object> returnMaps = new HashMap<>();

        JSONArray dataRequestedJsonArray = dataSet.getJSONObject(ConsentExtensionConstants.CONSENT_DATA)
                .getJSONArray(ConsentExtensionConstants.CONSENT_DETAILS);
        Map<String, List<String>> dataRequested = new LinkedHashMap<>();

        for (int requestedDataIndex = 0; requestedDataIndex < dataRequestedJsonArray.length(); requestedDataIndex++) {
            JSONObject dataObj = dataRequestedJsonArray.getJSONObject(requestedDataIndex);
            String title = dataObj.getString(ConsentExtensionConstants.TITLE);
            JSONArray dataArray = dataObj.getJSONArray(ConsentExtensionConstants.DATA_SIMPLE);
            ArrayList<String> listData = new ArrayList<>();

            for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                listData.add(dataArray.getString(dataIndex));
            }
            dataRequested.put(title, listData);
        }
        returnMaps.put(ConsentExtensionConstants.DATA_REQUESTED, dataRequested);
        httpServletRequest.setAttribute(ConsentExtensionConstants.CONSENT_TYPE,
                dataSet.getString(ConsentExtensionConstants.TYPE));
        httpServletRequest.setAttribute(ConsentExtensionConstants.AUTH_TYPE,
                dataSet.getString(ConsentExtensionConstants.AUTH_TYPE));

        return returnMaps;
    }
}
