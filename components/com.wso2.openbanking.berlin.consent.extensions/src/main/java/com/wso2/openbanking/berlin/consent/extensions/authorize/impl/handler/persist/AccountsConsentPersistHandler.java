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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Account Consent data persistence for Authorize.
 */
public class AccountsConsentPersistHandler implements ConsentPersistHandler {

    private ConsentCoreServiceImpl consentCoreService;
    private Map<String, ArrayList<String>> accountIdMapWithPermissions = new HashMap<>();
    private HashMap<String, String> currencyConsentAttributes = new HashMap<>();

    public AccountsConsentPersistHandler(ConsentCoreServiceImpl consentCoreService) {

        this.consentCoreService = consentCoreService;
    }

    @Override
    public void consentPersist(ConsentPersistData consentPersistData, ConsentResource consentResource)
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
        JSONArray checkedAccountsAccountRefObjects = (JSONArray) payload.get(ConsentExtensionConstants.CHECKED_ACCOUNTS_ACCOUNT_REFS);
        JSONArray checkedBalancesAccountRefObjects = (JSONArray) payload.get(ConsentExtensionConstants.CHECKED_BALANCES_ACCOUNT_REFS);
        JSONArray checkedTransactionsAccountRefObjects = (JSONArray) payload.get(ConsentExtensionConstants.CHECKED_TRANSACTIONS_ACCOUNT_REFS);

        // Data if not bank offered consent
        Map<String, Object> metaDataMap = consentPersistData.getConsentData().getMetaDataMap();
        JSONArray staticAccountsAccountRefObjects = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.ACCOUNTS_ACCOUNT_REF_OBJECTS);
        JSONArray staticBalancesAccountRefObjects = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.BALANCES_ACCOUNT_REF_OBJECTS);
        JSONArray staticTransactionsAccountRefObjects = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.TRANSACTIONS_ACCOUNT_REF_OBJECTS);

        // Mapping account Ids with permissions
        mapAccountIdAndCurrencyWithPermissions(staticAccountsAccountRefObjects, AccessMethodEnum.ACCOUNTS.toString());
        mapAccountIdAndCurrencyWithPermissions(checkedAccountsAccountRefObjects, AccessMethodEnum.ACCOUNTS.toString());
        mapAccountIdAndCurrencyWithPermissions(staticBalancesAccountRefObjects, AccessMethodEnum.BALANCES.toString());
        mapAccountIdAndCurrencyWithPermissions(checkedBalancesAccountRefObjects, AccessMethodEnum.BALANCES.toString());
        mapAccountIdAndCurrencyWithPermissions(staticTransactionsAccountRefObjects, AccessMethodEnum.TRANSACTIONS.toString());
        mapAccountIdAndCurrencyWithPermissions(checkedTransactionsAccountRefObjects, AccessMethodEnum.TRANSACTIONS.toString());

        if (accountIdMapWithPermissions.isEmpty()) {
            return;
        }

        ConsentPersistHandlerService consentPersistHandlerService =
                new ConsentPersistHandlerService(consentCoreService);
        consentPersistHandlerService.persistAuthorisation(consentResource, accountIdMapWithPermissions,
                authorisationId, userId, authStatus);
    }

    /**
     * Mapping account Ids and currency info with permissions.
     *
     * @param accountRefObjects account reference objects
     * @param accessMethod access method
     */
    private void mapAccountIdAndCurrencyWithPermissions(JSONArray accountRefObjects, String accessMethod) {

        if (accountRefObjects == null || accountRefObjects.isEmpty()) {
            return;
        }

        for (Object object : accountRefObjects) {
            JSONObject accountRefObject = (JSONObject) object;
            String accountIdWithCurrency = ConsentExtensionUtil.getAccountIdWithCurrency(accountRefObject);
            if (accountIdMapWithPermissions.containsKey(accountIdWithCurrency)) {
                ArrayList<String> currentPermissions = accountIdMapWithPermissions.get(accountIdWithCurrency);
                if (!currentPermissions.contains(accessMethod)) {
                    currentPermissions.add(accessMethod);
                    accountIdMapWithPermissions.put(accountIdWithCurrency, currentPermissions);
                }
            } else {
                ArrayList<String> permissions = new ArrayList<>();
                permissions.add(accessMethod);

                // Account permission with "balances" or "transactions" implicitly has "accounts" permission as well
                if (StringUtils.equals(accessMethod, AccessMethodEnum.BALANCES.toString())
                        || StringUtils.equals(accessMethod, AccessMethodEnum.TRANSACTIONS.toString())) {
                    permissions.add(AccessMethodEnum.ACCOUNTS.toString());
                }

                accountIdMapWithPermissions.put(accountIdWithCurrency, permissions);
            }
        }
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentPersistData consentPersistData) {
        return null;
    }
}
