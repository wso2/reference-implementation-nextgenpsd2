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
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Account Consent data persistence for Authorize.
 */
public class AccountsConsentPersistHandler implements ConsentPersistHandler {

    private static final Log log = LogFactory.getLog(AccountsConsentPersistHandler.class);

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
        JSONArray checkedAccountsAccountRefObjects = (JSONArray) payload
                .get(ConsentExtensionConstants.CHECKED_ACCOUNTS_ACCOUNT_REFS);
        JSONArray checkedBalancesAccountRefObjects = (JSONArray) payload
                .get(ConsentExtensionConstants.CHECKED_BALANCES_ACCOUNT_REFS);
        JSONArray checkedTransactionsAccountRefObjects = (JSONArray) payload
                .get(ConsentExtensionConstants.CHECKED_TRANSACTIONS_ACCOUNT_REFS);

        // Data if not bank offered consent
        Map<String, Object> metaDataMap = consentPersistData.getConsentData().getMetaDataMap();
        JSONArray staticAccountsAccountRefObjects = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.ACCOUNTS_ACCOUNT_REF_OBJECTS);
        JSONArray staticBalancesAccountRefObjects = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.BALANCES_ACCOUNT_REF_OBJECTS);
        JSONArray staticTransactionsAccountRefObjects = (JSONArray) metaDataMap
                .get(ConsentExtensionConstants.TRANSACTIONS_ACCOUNT_REF_OBJECTS);

        // Mapping account Ids with permissions
        mapAccountIdAndCurrencyWithPermissions(staticAccountsAccountRefObjects,
                AccessMethodEnum.ACCOUNTS.toString());
        mapAccountIdAndCurrencyWithPermissions(checkedAccountsAccountRefObjects,
                AccessMethodEnum.ACCOUNTS.toString());
        mapAccountIdAndCurrencyWithPermissions(staticBalancesAccountRefObjects,
                AccessMethodEnum.BALANCES.toString());
        mapAccountIdAndCurrencyWithPermissions(checkedBalancesAccountRefObjects,
                AccessMethodEnum.BALANCES.toString());
        mapAccountIdAndCurrencyWithPermissions(staticTransactionsAccountRefObjects,
                AccessMethodEnum.TRANSACTIONS.toString());
        mapAccountIdAndCurrencyWithPermissions(checkedTransactionsAccountRefObjects,
                AccessMethodEnum.TRANSACTIONS.toString());

        if (accountIdMapWithPermissions.isEmpty()) {
            // Updating the consent and auth statuses during bank offered consent deny scenario when
            // accounts are not selected
            if (!isApproved) {
                consentCoreService.updateConsentStatus(consentResource.getConsentID(),
                        ConsentStatusEnum.REJECTED.toString());
                consentCoreService.updateAuthorizationStatus(authorisationId, authStatus);
                consentCoreService.updateAuthorizationUser(authorisationId, StringUtils.removeEndIgnoreCase(userId,
                        ConsentExtensionConstants.SUPER_TENANT_DOMAIN));
                return;
            } else {
                log.error(ErrorConstants.APPROVE_WITH_NO_ACCOUNTS_ERROR);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.APPROVE_WITH_NO_ACCOUNTS_ERROR);
            }
        }

        ConsentPersistHandlerService consentPersistHandlerService =
                new ConsentPersistHandlerService(consentCoreService);
        consentPersistHandlerService.persistAuthorisation(consentResource, accountIdMapWithPermissions,
                authorisationId, userId, authStatus, isApproved);

        // Updating the consent receipt after authorizing bank offered consent
        if ((checkedAccountsAccountRefObjects != null && !checkedAccountsAccountRefObjects.isEmpty())
                || (checkedBalancesAccountRefObjects != null && !checkedBalancesAccountRefObjects.isEmpty())
                || (checkedTransactionsAccountRefObjects != null && !checkedTransactionsAccountRefObjects.isEmpty())) {
            String consentId = consentResource.getConsentID();
            DetailedConsentResource detailedConsentResource = consentCoreService.getDetailedConsent(consentId);

            String updatedReceipt = null;
            try {
                updatedReceipt = getBankOfferedConsentReceipt(detailedConsentResource.getReceipt(),
                        detailedConsentResource.getConsentMappingResources());
            } catch (ParseException e) {
                log.error(ErrorConstants.BANK_OFFERED_CONSENT_UPDATE_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.JSON_PARSE_ERROR);
            }
            consentCoreService.amendConsentData(consentId, updatedReceipt, null, null);
        }
    }

    /**
     * Mapping account reference info with permissions.
     *
     * @param accountRefObjects account reference objects
     * @param accessMethod      access method
     */
    private void mapAccountIdAndCurrencyWithPermissions(JSONArray accountRefObjects, String accessMethod) {

        if (accountRefObjects == null || accountRefObjects.isEmpty()) {
            return;
        }

        for (Object object : accountRefObjects) {
            JSONObject accountRefObject = (JSONObject) object;
            String accountReference = ConsentExtensionUtil.getAccountReferenceToPersist(accountRefObject);
            if (accountIdMapWithPermissions.containsKey(accountReference)) {
                ArrayList<String> currentPermissions = accountIdMapWithPermissions.get(accountReference);
                if (!currentPermissions.contains(accessMethod)) {
                    currentPermissions.add(accessMethod);
                    accountIdMapWithPermissions.put(accountReference, currentPermissions);
                }
            } else {
                ArrayList<String> permissions = new ArrayList<>();
                permissions.add(accessMethod);

                // Account permission with "balances" or "transactions" implicitly has "accounts" permission as well
                if (StringUtils.equals(accessMethod, AccessMethodEnum.BALANCES.toString())
                        || StringUtils.equals(accessMethod, AccessMethodEnum.TRANSACTIONS.toString())) {
                    permissions.add(AccessMethodEnum.ACCOUNTS.toString());
                }

                accountIdMapWithPermissions.put(accountReference, permissions);
            }
        }
    }

    /**
     * Returns the updated consent receipt for bank offered consents after authorizing accounts.
     *
     * @param receipt          consent receipt
     * @param mappingResources mapping resources
     * @return updated receipt with authorized account details
     * @throws ParseException
     */
    private String getBankOfferedConsentReceipt(String receipt, ArrayList<ConsentMappingResource> mappingResources)
            throws ParseException {

        JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receipt);
        JSONObject accessObject = (JSONObject) receiptJSON.get(ConsentExtensionConstants.ACCESS);

        ArrayList<ConsentMappingResource> accountsMappingResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> balancesMappingResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> transactionsMappingResources = new ArrayList<>();

        for (ConsentMappingResource mappingResource : mappingResources) {
            if (StringUtils.equals(mappingResource.getPermission(), AccessMethodEnum.ACCOUNTS.toString())) {
                accountsMappingResources.add(mappingResource);
            } else if (StringUtils.equals(mappingResource.getPermission(), AccessMethodEnum.BALANCES.toString())) {
                balancesMappingResources.add(mappingResource);
            } else if (StringUtils.equals(mappingResource.getPermission(), AccessMethodEnum.TRANSACTIONS.toString())) {
                transactionsMappingResources.add(mappingResource);
            }
        }

        JSONObject updatedAccessObject = new JSONObject();
        if (accessObject.containsKey(AccessMethodEnum.ACCOUNTS.toString())
                && !accountsMappingResources.isEmpty()) {
            updatedAccessObject.appendField(AccessMethodEnum.ACCOUNTS.toString(),
                    getAccountRefObjectsForMappingResources(accountsMappingResources));
        }

        if (accessObject.containsKey(AccessMethodEnum.BALANCES.toString())
                && !balancesMappingResources.isEmpty()) {
            updatedAccessObject.appendField(AccessMethodEnum.BALANCES.toString(),
                    getAccountRefObjectsForMappingResources(balancesMappingResources));
        }

        if (accessObject.containsKey(AccessMethodEnum.TRANSACTIONS.toString())
                && !transactionsMappingResources.isEmpty()) {
            updatedAccessObject.appendField(AccessMethodEnum.TRANSACTIONS.toString(),
                    getAccountRefObjectsForMappingResources(transactionsMappingResources));
        }

        receiptJSON.appendField(ConsentExtensionConstants.ACCESS, updatedAccessObject);
        return receiptJSON.toString();
    }

    /**
     * Converts the mapping resources to account ref objects.
     *
     * @param mappingResources mapping resources
     * @return account reference objects
     */
    private JSONArray getAccountRefObjectsForMappingResources(ArrayList<ConsentMappingResource> mappingResources) {

        JSONArray accountRefObjects = new JSONArray();

        for (ConsentMappingResource mappingResource : mappingResources) {
            JSONObject accountRefObject = new JSONObject();
            String accountId = mappingResource.getAccountID();
            String[] accountDetails = accountId.split(CommonConstants.DELIMITER);
            String accountRefType = accountDetails[0].trim();
            String accountNumber = accountDetails[1].trim();
            if (accountDetails.length > 2) {
                String currency = accountDetails[2].trim();
                accountRefObject.put(accountRefType, accountNumber);
                accountRefObject.put(ConsentExtensionConstants.CURRENCY, currency);
            } else {
                accountRefObject.put(accountRefType, accountNumber);
            }
            accountRefObjects.appendElement(accountRefObject);
        }
        return accountRefObjects;
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentPersistData consentPersistData) {
        return null;
    }
}
