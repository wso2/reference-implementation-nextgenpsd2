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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to handle account list retrieval for authorize.
 */
public class BerlinAccountListRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(BerlinAccountListRetrievalStep.class);

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory() ||
                !StringUtils.equals(consentData.getType(), ConsentTypeEnum.ACCOUNTS.toString())) {
            return;
        }

        JSONArray consentDataArray = (JSONArray) jsonObject.get(ConsentExtensionConstants.CONSENT_DATA);
        if (consentDataArray == null || consentDataArray.size() == 0) {
            log.error(ErrorConstants.INCORRECT_CONSENT_DATA);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.SERVER_ERROR,
                            ErrorConstants.INCORRECT_CONSENT_DATA, consentData.getRedirectURI(),
                            consentData.getState()));
        }

        JSONObject consentDataObject = (JSONObject) consentDataArray.get(0);
        String permission = consentDataObject.getAsString(ConsentExtensionConstants.PERMISSION);
        JSONObject accessObject = (JSONObject) consentDataObject.get(ConsentExtensionConstants.ACCESS_OBJECT);

        if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.DEFAULT.toString())) {
            JSONArray accountDetailsArray = DataRetrievalUtil
                    .getAccountsFromPayload(accessObject, consentData.getUserId());

            if (accountDetailsArray == null) {
                log.error(ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER);
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_REQUEST,
                                ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER, consentData.getRedirectURI(),
                                consentData.getState()));
            }

            jsonObject.put(ConsentExtensionConstants.ACCOUNT_DETAILS, accountDetailsArray);
            addAccountDetailsToMetaData(consentData.getMetaDataMap(), accountDetailsArray);
        } else {

            JSONArray permissionArray = new JSONArray();
            JSONArray accessMethodArray = new JSONArray();

            if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.ALL_PSD2.toString())) {
                permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
                permissionArray.add(ConsentExtensionConstants.BALANCES_PERMISSION);
                permissionArray.add(ConsentExtensionConstants.TRANSACTIONS_PERMISSION);

                accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
                accessMethodArray.add(AccessMethodEnum.BALANCES.toString());
                accessMethodArray.add(AccessMethodEnum.TRANSACTIONS.toString());
            } else if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.AVAILABLE_ACCOUNTS.toString())) {
                permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);

                accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
            } else if (StringUtils.equalsIgnoreCase(permission,
                    PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString())) {
                permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
                permissionArray.add(ConsentExtensionConstants.BALANCES_PERMISSION);

                accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
                accessMethodArray.add(AccessMethodEnum.BALANCES.toString());
            }

            JSONArray accountArray = DataRetrievalUtil.getAccountsFromEndpoint(consentData.getUserId());

            if (accountArray == null) {
                log.error(ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER);
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_REQUEST,
                                ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER, consentData.getRedirectURI(),
                                consentData.getState()));
            }

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accountArray);
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_DEFAULT);

            JSONArray objArray = new JSONArray();
            objArray.add(object);

            jsonObject.put(ConsentExtensionConstants.ACCOUNT_DETAILS, objArray);
            addAccountDetailsToMetaData(consentData.getMetaDataMap(), objArray);
        }
    }

    private void addAccountDetailsToMetaData(Map<String, Object> metaDataMap, JSONArray accountDetailsArray) {

        for (Object accountDetails : accountDetailsArray) {
            JSONObject dataObj = (JSONObject) accountDetails;
            JSONArray accountNumbersJsonArray = (JSONArray) dataObj.get(ConsentExtensionConstants.ACCOUNT_NUMBERS);
            JSONArray accessMethodsJsonArray = (JSONArray) dataObj.get(ConsentExtensionConstants.ACCESS_METHODS);

            List<String> accountNumbers = new ArrayList<>();

            for (Object accountNumberJson : accountNumbersJsonArray) {
                JSONObject obj = (JSONObject) accountNumberJson;
                accountNumbers.add(obj.getAsString(ConsentExtensionConstants.IBAN));
            }

            Set<String> accountsAccNumberSet = new HashSet<>();
            Set<String> balancesAccNumberSet = new HashSet<>();
            Set<String> transactionsAccNumberSet = new HashSet<>();

            for (Object accessMethodJson : accessMethodsJsonArray) {
                String accessMethod = (String) accessMethodJson;

                if (StringUtils.equals(accessMethod, AccessMethodEnum.ACCOUNTS.toString())) {
                    accountsAccNumberSet.addAll(accountNumbers);
                }
                if (StringUtils.equals(accessMethod, AccessMethodEnum.BALANCES.toString())) {
                    balancesAccNumberSet.addAll(accountNumbers);
                }
                if (StringUtils.equals(accessMethod, AccessMethodEnum.TRANSACTIONS.toString())) {
                    transactionsAccNumberSet.addAll(accountNumbers);
                }
            }
            metaDataMap.put(ConsentExtensionConstants.ACCOUNTS_ACC_NUMBER_SET, toJsonArray(accountsAccNumberSet));
            metaDataMap.put(ConsentExtensionConstants.BALANCES_ACC_NUMBER_SET, toJsonArray(balancesAccNumberSet));
            metaDataMap.put(ConsentExtensionConstants.TRANSACTIONS_ACC_NUMBER_SET,
                    toJsonArray(transactionsAccNumberSet));
        }
    }

    private JSONArray toJsonArray(Set<String> set) {

        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(set);
        return jsonArray;
    }
}
