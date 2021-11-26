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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Account Consent data persistence for Authorize.
 */
public class AccountsConsentPersistHandler implements ConsentPersistHandler {

    private Map<String, ArrayList<String>> accountIdMapWithPermissions = new HashMap<>();

    @Override
    public void consentPersist(ConsentPersistData consentPersistData, ConsentResource consentResource,
                               ConsentCoreServiceImpl coreService)
            throws ConsentManagementException {

        String authorisationId = consentPersistData.getConsentData().getAuthResource().getAuthorizationID();
        boolean isApproved = consentPersistData.getApproval();
        String userId = consentPersistData.getConsentData().getUserId();

        String authStatus;
        if (isApproved) {
            authStatus = ScaStatusEnum.PSU_AUTHENTICATED.toString();
        } else {
            authStatus = ScaStatusEnum.FAILED.toString();
        }

        // Data if bank offered consent
        JSONObject payload = consentPersistData.getPayload();
        JSONArray checkedAccounts = (JSONArray) payload.get(ConsentExtensionConstants.CHECKED_ACCOUNTS);
        JSONArray checkedBalances = (JSONArray) payload.get(ConsentExtensionConstants.CHECKED_BALANCES);
        JSONArray checkedTransactions = (JSONArray) payload.get(ConsentExtensionConstants.CHECKED_TRANSACTIONS);

        // Data if not bank offered consent
        Map<String, Object> metaDataMap = consentPersistData.getConsentData().getMetaDataMap();
        JSONArray staticAccountsAccNumbers = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.ACCOUNTS_ACC_NUMBER_SET);
        JSONArray staticBalancesAccNumbers = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.BALANCES_ACC_NUMBER_SET);
        JSONArray staticTransactionsAccNumbers = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.TRANSACTIONS_ACC_NUMBER_SET);

        // Mapping account Ids with permissions
        if (checkedAccounts == null || checkedAccounts.isEmpty()) {
            mapAccountIdWithPermissions(staticAccountsAccNumbers, AccessMethodEnum.ACCOUNTS.toString());
        } else {
            mapAccountIdWithPermissions(checkedAccounts, AccessMethodEnum.ACCOUNTS.toString());
        }

        if (checkedBalances == null || checkedBalances.isEmpty()) {
            mapAccountIdWithPermissions(staticBalancesAccNumbers, AccessMethodEnum.BALANCES.toString());
        } else {
            mapAccountIdWithPermissions(checkedBalances, AccessMethodEnum.BALANCES.toString());
        }

        if (checkedTransactions == null || checkedTransactions.isEmpty()) {
            mapAccountIdWithPermissions(staticTransactionsAccNumbers, AccessMethodEnum.TRANSACTIONS.toString());
        } else {
            mapAccountIdWithPermissions(checkedTransactions, AccessMethodEnum.TRANSACTIONS.toString());
        }

        ConsentPersistHandlerService consentPersistHandlerService = new ConsentPersistHandlerService();
        consentPersistHandlerService.persistAuthorisation(consentResource, accountIdMapWithPermissions,
                authorisationId, userId, authStatus, coreService);
    }

    private void mapAccountIdWithPermissions(JSONArray accountNumbers, String accessMethod) {

        ArrayList<String> permission = new ArrayList<>();
        permission.add(accessMethod);

        for (Object accountNumberObject : accountNumbers) {
            accountIdMapWithPermissions.put(accountNumberObject.toString(), permission);
        }
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentPersistData consentPersistData) {
        return null;
    }
}
