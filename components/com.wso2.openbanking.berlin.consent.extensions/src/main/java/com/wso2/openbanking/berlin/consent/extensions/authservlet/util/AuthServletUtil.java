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
     * Method to populate single payments data to be sent to consent page.
     *
     * @param httpServletRequest
     * @param dataSet
     * @return
     */
    public static Map<String, Object> populatePaymentsData(HttpServletRequest httpServletRequest, JSONObject dataSet) {

        Map<String, Object> returnMaps = new HashMap<>();

        JSONArray dataRequestedJsonArray = dataSet.getJSONArray(ConsentExtensionConstants.CONSENT_DATA);
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

    public static Map<String, Object> populateAccountsData(HttpServletRequest httpServletRequest, JSONObject dataSet) {

        Map<String, Object> returnMaps = new HashMap<>();

        // Setting the consent related details to display in the consent page
        JSONArray consentDataJsonArray = dataSet.getJSONArray(ConsentExtensionConstants.CONSENT_DATA);
        Map<String, List<String>> consentData = new LinkedHashMap<>();

        for (int consentDataIndex = 0; consentDataIndex < consentDataJsonArray.length(); consentDataIndex++) {
            JSONObject dataObj = consentDataJsonArray.getJSONObject(consentDataIndex);
            String title = dataObj.getString(ConsentExtensionConstants.TITLE);
            JSONArray dataArray = dataObj.getJSONArray(ConsentExtensionConstants.DATA_SIMPLE);
            ArrayList<String> listData = new ArrayList<>();

            for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                listData.add(dataArray.getString(dataIndex));
            }
            consentData.put(title, listData);
        }

        // Setting the account details and permission to be displayed in the consent page
        JSONArray accountDetailsJsonArray = dataSet.getJSONArray(ConsentExtensionConstants.ACCOUNT_DETAILS);
        Map<String, Map<String, String>> staticDefaultMap = new HashMap<>();
        boolean isStaticDefault = false;

        Map<String, Map<String, String>> selectBalanceMap = new HashMap<>();
        boolean isSelectBalance = false;

        Map<String, Map<String, String>> staticBalanceMap = new HashMap<>();
        boolean isStaticBalance = false;

        Map<String, Map<String, String>> selectAccountMap = new HashMap<>();
        boolean isSelectAccount = false;

        Map<String, Map<String, String>> staticAccountMap = new HashMap<>();
        boolean isStaticAccount = false;

        Map<String, Map<String, String>> selectTransactionMap = new HashMap<>();
        boolean isSelectTransaction = false;

        Map<String, Map<String, String>> staticTransactionMap = new HashMap<>();
        boolean isStaticTransaction = false;

        for (int accountDetailsIndex = 0; accountDetailsIndex < accountDetailsJsonArray.length();
             accountDetailsIndex++) {
            JSONObject dataObj = accountDetailsJsonArray.getJSONObject(accountDetailsIndex);
            String accountType = dataObj.getString(ConsentExtensionConstants.ACCOUNT_TYPE);
            JSONArray accountNumbersJsonArray = dataObj.getJSONArray(ConsentExtensionConstants.ACCOUNT_NUMBERS);
            JSONArray permissionsJsonArray = dataObj.getJSONArray(ConsentExtensionConstants.PERMISSIONS);

            Map<String, String> accountNumbers = new HashMap<>();
            Map<String, String> permissions = new HashMap<>();

            for (int accNumberIndex = 0; accNumberIndex < accountNumbersJsonArray.length(); accNumberIndex++) {
                JSONObject obj = accountNumbersJsonArray.getJSONObject(accNumberIndex);
                String currencyType = (StringUtils.isNotBlank(obj.getString(ConsentExtensionConstants.CURRENCY)) ?
                        obj.getString(ConsentExtensionConstants.CURRENCY) : StringUtils.EMPTY);
                accountNumbers.put(obj.getString(ConsentExtensionConstants.IBAN), currencyType);
            }

            for (int permissionIndex = 0; permissionIndex < permissionsJsonArray.length(); permissionIndex++) {
                permissions.put(ConsentExtensionConstants.PERMISSION,
                        permissionsJsonArray.getString(permissionIndex));
            }

            if (StringUtils.equals(accountType, ConsentExtensionConstants.STATIC_DEFAULT)) {
                isStaticDefault = true;
                staticDefaultMap.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accountNumbers);
                staticDefaultMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.SELECT_BALANCE)) {
                isSelectBalance = true;
                selectBalanceMap.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accountNumbers);
                selectBalanceMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.STATIC_BALANCE)) {
                isStaticBalance = true;
                staticBalanceMap.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accountNumbers);
                staticBalanceMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.SELECT_ACCOUNT)) {
                isSelectAccount = true;
                selectAccountMap.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accountNumbers);
                selectAccountMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.STATIC_ACCOUNT)) {
                isStaticAccount = true;
                staticAccountMap.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accountNumbers);
                staticAccountMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.SELECT_TRANSACTION)) {
                isSelectTransaction = true;
                selectTransactionMap.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accountNumbers);
                selectTransactionMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            } else if (StringUtils.equals(accountType, ConsentExtensionConstants.STATIC_TRANSACTION)) {
                isStaticTransaction = true;
                staticTransactionMap.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accountNumbers);
                staticTransactionMap.put(ConsentExtensionConstants.PERMISSIONS, permissions);
            }
        }

        returnMaps.put(ConsentExtensionConstants.DATA_REQUESTED, consentData);

        returnMaps.put(ConsentExtensionConstants.STATIC_DEFAULT, staticDefaultMap);
        returnMaps.put(ConsentExtensionConstants.SELECT_BALANCE, selectBalanceMap);
        returnMaps.put(ConsentExtensionConstants.STATIC_BALANCE, staticBalanceMap);
        returnMaps.put(ConsentExtensionConstants.SELECT_ACCOUNT, selectAccountMap);
        returnMaps.put(ConsentExtensionConstants.STATIC_ACCOUNT, staticAccountMap);
        returnMaps.put(ConsentExtensionConstants.SELECT_TRANSACTION, selectTransactionMap);
        returnMaps.put(ConsentExtensionConstants.STATIC_TRANSACTION, staticTransactionMap);

        returnMaps.put("isStaticDefault", isStaticDefault);
        returnMaps.put("isSelectBalance", isSelectBalance);
        returnMaps.put("isStaticBalance", isStaticBalance);
        returnMaps.put("isSelectAccount", isSelectAccount);
        returnMaps.put("isStaticAccount", isStaticAccount);
        returnMaps.put("isSelectTransaction", isSelectTransaction);
        returnMaps.put("isStaticTransaction", isStaticTransaction);

        httpServletRequest.setAttribute(ConsentExtensionConstants.CONSENT_TYPE,
                dataSet.getString(ConsentExtensionConstants.TYPE));

        return returnMaps;
    }

    public static Map<String, Object> populateFundsConfirmationData(HttpServletRequest httpServletRequest,
                                                                    JSONObject dataSet) {

        //todo: Implement for cof flow
        return new HashMap<>();
    }
}
