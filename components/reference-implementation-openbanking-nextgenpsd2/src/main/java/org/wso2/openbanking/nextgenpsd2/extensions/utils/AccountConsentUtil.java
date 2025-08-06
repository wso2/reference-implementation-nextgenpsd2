/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.nextgenpsd2.extensions.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.configurations.ConfigurationConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.CommonConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ConsentTypeEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.PermissionEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.BadRequestException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ServerErrorException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ValidationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.AccountAccess;
import org.wso2.openbanking.nextgenpsd2.extensions.model.AccountInitiationPayload;
import org.wso2.openbanking.nextgenpsd2.extensions.model.AccountReference;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ScaMethod;
import org.wso2.openbanking.nextgenpsd2.extensions.model.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.Account;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.AuthorizedResourcesAuthorizedDataInner;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.Resource;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.StoredBasicConsentResourceData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.StoredDetailedConsentResourceData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for Account consent management.
 */
public class AccountConsentUtil {
    private static Log log = LogFactory.getLog(AccountConsentUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Method to get the account initiation response without links.
     *
     * @param createdConsent the created consent
     * @param scaMethods     decided SCA methods
     */
    public static void appendAccountInitiationResponseToPayload(StoredDetailedConsentResourceData createdConsent,
                                                                ArrayList<ScaMethod> scaMethods, JSONObject payload)
            throws BadRequestException {

        payload.put(ConsentExtensionConstants.CONSENT_STATUS, createdConsent.getStatus());
        payload.put(ConsentExtensionConstants.CONSENT_ID, createdConsent.getId());

        JSONArray chosenSCAMethods = new JSONArray();
        for (ScaMethod scaMethod : scaMethods) {
            chosenSCAMethods.put(CommonConsentValidationUtil.convertObjectToJson(scaMethod));
        }

        if (scaMethods.size() > 1) {
            payload.put(ConsentExtensionConstants.SCA_METHODS, chosenSCAMethods);
        } else if (scaMethods.size() == 1) {
            payload.put(ConsentExtensionConstants.CHOSEN_SCA_METHOD, chosenSCAMethods.get(0));
        }
    }

    /**
     * Converts a given date to a UTC timestamp.
     *
     * @param date date in string format
     * @return date/time after converting to UTC timestamp
     */
    public static long convertToUtcTimestamp(String date) throws ValidationFailureException, BadRequestException {

        LocalDate localDate = CommonConsentValidationUtil.parseDateToISO(date,
                TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.VALID_UNTIL_DATE_INVALID);
        LocalDateTime localDateTime = localDate.atStartOfDay();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(ConsentExtensionConstants.UTC));

        // Retrieve the UTC timestamp in long.
        return Instant.from(zonedDateTime).getEpochSecond();
    }

    /**
     * Checks if consent is expired based on validUntilDate.
     *
     * @param validUntilDate valid until time in epoch seconds
     * @return whether consent is expired or not
     */
    public static boolean isConsentExpired(long validUntilDate) {
        LocalDateTime expDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(validUntilDate), ZoneOffset.UTC);
        LocalDate expDate = expDateTime.toLocalDate();

        LocalDate currDate = LocalDate.now(ZoneOffset.UTC);

        return currDate.isAfter(expDate);
    }

    /**
     * Method to construct accounts consent get response.
     *
     * @param retrievedConsent consent object
     */
    public static void extendAccountConsentGetResponse(StoredBasicConsentResourceData retrievedConsent,
                                                       JSONObject payloadToSend) {

        AccountConsentUtil.addAdditionalAccountConsentAttributes(retrievedConsent, payloadToSend);
        payloadToSend.put(ConsentExtensionConstants.LINKS, getAccountConsentGetLinks());
    }

    /**
     * Constructs the links object for account consent get responses.
     *
     * @return constructed links for account consent get responses
     */
    public static JSONObject getAccountConsentGetLinks() {

        JSONObject links = new JSONObject();

        String apiVersion = CommonConsentValidationUtil.getApiVersion(ConsentTypeEnum.ACCOUNTS.toString());

        JSONObject account = new JSONObject();
        account.put(ConsentExtensionConstants.HREF,
                String.format(ConsentExtensionConstants.ACCOUNTS_LINK_TEMPLATE, apiVersion));
        links.put(ConsentExtensionConstants.ACCOUNT, account);

        return links;
    }

    /**
     * Method to get the account consent get response without links.
     *
     * @param retrievedConsent consent object
     */
    public static void addAdditionalAccountConsentAttributes(StoredBasicConsentResourceData retrievedConsent,
                                                             JSONObject payloadToSend) {

        payloadToSend.put(ConsentExtensionConstants.CONSENT_STATUS, retrievedConsent.getStatus());

        Date currentDate = new Date(retrievedConsent.getUpdatedTime() * 1000L);
        DateFormat dateFormat = new SimpleDateFormat(ConsentExtensionConstants.DATE_FORMAT);
        String lastActionDate = dateFormat.format(currentDate);

        payloadToSend.put(ConsentExtensionConstants.LAST_ACTION_DATE, lastActionDate);
    }

    /**
     * Method used to populate basic consent data for account consents.
     *
     * @param responseData
     * @param requestData
     * @throws BadRequestException
     */
    public static void populateAccountsBasicConsentData(SuccessResponsePopulateConsentAuthorizeScreenData responseData,
                                                        PopulateConsentAuthorizeScreenData requestData)
            throws BadRequestException {
        Map<String, List<String>> basicConsentData = new HashMap<>();
        JSONObject receipt = CommonConsentValidationUtil
                .convertObjectToJson(requestData.getConsentResource().getReceipt());

        List<String> consentDetails = new ArrayList<>();
        consentDetails.add(ConsentExtensionConstants.RECURRING_INDICATOR_TITLE + ": " +
                receipt.getBoolean(ConsentExtensionConstants.RECURRING_INDICATOR));
        consentDetails.add(ConsentExtensionConstants.VALID_UNTIL_TITLE + ": " +
                receipt.getString(ConsentExtensionConstants.VALID_UNTIL));
        consentDetails.add(ConsentExtensionConstants.FREQUENCY_PER_DAY_TITLE + ": " +
                receipt.getInt(ConsentExtensionConstants.FREQUENCY_PER_DAY));
        consentDetails.add(ConsentExtensionConstants.COMBINED_SERVICE_INDICATOR_TITLE + ": " +
                receipt.getBoolean(ConsentExtensionConstants.COMBINED_SERVICE_INDICATOR));

        // Add basic consent details
        basicConsentData.put(ConsentExtensionConstants.CONSENT_DETAILS_TITLE, consentDetails);

        responseData.getConsentData().setBasicConsentData(basicConsentData);
    }

    /**
     * Method used to identify requested permission type.
     *
     * @param receipt
     * @return
     */
    public static String identifyPermissionFromReceipt(AccountInitiationPayload receipt) throws BadRequestException {
        // Access object not validated here given that it's validated at consent initiation and cannot be updated since
        AccountAccess accessObject = receipt.getAccess();

        if (accessObject.getAccounts() == null && accessObject.getBalances() == null
                && accessObject.getTransactions() == null) {

            if (accessObject.getAvailableAccounts() != null) {
                return PermissionEnum.AVAILABLE_ACCOUNTS.toString();
            }

            if (accessObject.getAvailableAccountsWithBalances() != null) {
                return PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString();
            }

            if (accessObject.getAllPsd2() != null) {
                return PermissionEnum.ALL_PSD2.toString();
            }

        } else {
            /*
             * According to Berlin/Israel specifications, either all access arrays should be empty, or non-empty.
             * The following logic partially checks for this requirement for identifying permission.
             */

            int numberOfProvidedAccessTypes = 0;
            int numberOfEmptyAccessMethodArrays = 0;

            if (accessObject.getAccounts() != null) {
                numberOfProvidedAccessTypes++;
                if (accessObject.getAccounts().isEmpty()) {
                    numberOfEmptyAccessMethodArrays++;
                }
            }

            if (accessObject.getBalances() != null) {
                numberOfProvidedAccessTypes++;
                if (accessObject.getBalances().isEmpty()) {
                    numberOfEmptyAccessMethodArrays++;
                }
            }

            if (accessObject.getTransactions() != null) {
                numberOfProvidedAccessTypes++;
                if (accessObject.getTransactions().isEmpty()) {
                    numberOfEmptyAccessMethodArrays++;
                }
            }

            if (numberOfProvidedAccessTypes == numberOfEmptyAccessMethodArrays) {
                return PermissionEnum.BANK_OFFERED.toString();
            } else {
                return PermissionEnum.DEDICATED_ACCOUNTS.toString();
            }
        }

        // Should be unreachable since this is validated
        throw new BadRequestException("Poorly validated consent initiation payload received");
    }

    /**
     * Populates the populate consent authorize page response with consumer accounts and permissions with no initiated
     * accounts.
     *
     * @param responseData
     * @param receipt
     */
    public static void populatePermissionsAndConsumerAccounts(
            SuccessResponsePopulateConsentAuthorizeScreenData responseData, AccountInitiationPayload receipt,
            String userId) throws AuthorizationFailureException {

        List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> permissionList =
                new ArrayList<>();
        AccountAccess access = receipt.getAccess();

        if (access.getAccounts() != null) {
            permissionList.add(new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner()
                    .uid(UUID.randomUUID().toString())
                    .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION));
        }

        if (access.getBalances() != null) {
            permissionList.add(new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner()
                    .uid(UUID.randomUUID().toString())
                    .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION)
                    .addDisplayValuesItem(ConsentExtensionConstants.BALANCES_PERMISSION));
        }

        if (access.getTransactions() != null) {
            permissionList.add(new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner()
                    .uid(UUID.randomUUID().toString())
                    .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION)
                    .addDisplayValuesItem(ConsentExtensionConstants.TRANSACTIONS_PERMISSION));
        }

        // Set permissions in consent data
        responseData.getConsentData().setPermissions(permissionList);

        // Set multiple account selection
        responseData.getConsentData().setAllowMultipleAccounts(true);

        // Fetch bank offered consumer accounts
        String shareableAccountsEndpoint = ConfigurationConstants.SHARABLE_ACCOUNTS_RETRIEVAL_ENDPOINT;
        JSONArray bankOfferedAccounts = DataRetrievalUtil.getAccountsFromEndpoint(userId, shareableAccountsEndpoint,
                new HashMap<>(), new HashMap<>());

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            throw new AuthorizationFailureException("No sharable accounts found for the user.");
        }

        SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData = responseData.getConsumerData();

        if (consumerData == null) {
            consumerData = new SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData();
            responseData.setConsumerData(consumerData);
        }

        for (Object accountRefObj: bankOfferedAccounts) {
            JSONObject accountRefJSON = (JSONObject) accountRefObj;
            Account accountObject = ConsentAuthorizationUtil.getAccountFromAccountRef(accountRefJSON);

            consumerData.addAccountsItem(objectMapper.convertValue(accountObject,
                    SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner.class));
        }
    }

    /**
     * Returns build permission objects for the populate consent authorize page response when dedicated account
     * permissions are requested.
     *
     * @param receipt
     * @param userId
     * @return
     */
    public static List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner>
    buildPermissionsForDedicatedAccounts(AccountInitiationPayload receipt, String userId) throws ServerErrorException {
        List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> permissionsList =
                new ArrayList<>();
        AccountAccess accessObject = receipt.getAccess();

        String shareableAccountsEndpoint = ConfigurationConstants.SHARABLE_ACCOUNTS_RETRIEVAL_ENDPOINT;
        JSONArray bankOfferedAccounts = DataRetrievalUtil.getAccountsFromEndpoint(userId, shareableAccountsEndpoint,
                new HashMap<>(), new HashMap<>());

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            return null;
        }

        // From receipt
        List<AccountReference> accountsAccountRefObjects = accessObject.getAccounts();
        List<AccountReference> balancesAccountRefObjects = accessObject.getBalances();
        List<AccountReference> transactionsAccountRefObjects = accessObject.getTransactions();

        // For the populate consent page payload
        List<Account> validatedAccountsAccountObjects = null;
        List<Account> validatedBalancesAccountObjects = null;
        List<Account> validatedTransactionsAccountObjects = null;

        boolean areAccountsInvalid = false;
        if (accountsAccountRefObjects != null && !accountsAccountRefObjects.isEmpty()) {
            validatedAccountsAccountObjects = ConsentAuthorizationUtil
                    .getValidatedAccountObjects(accountsAccountRefObjects, bankOfferedAccounts);
            if (validatedAccountsAccountObjects == null || validatedAccountsAccountObjects.isEmpty()) {
                areAccountsInvalid = true;
            }
        }

        if (balancesAccountRefObjects != null && !balancesAccountRefObjects.isEmpty()) {
            validatedBalancesAccountObjects = ConsentAuthorizationUtil
                    .getValidatedAccountObjects(balancesAccountRefObjects, bankOfferedAccounts);
            if (validatedBalancesAccountObjects == null || validatedBalancesAccountObjects.isEmpty()) {
                areAccountsInvalid = true;
            }
        }

        if (transactionsAccountRefObjects != null && !transactionsAccountRefObjects.isEmpty()) {
            validatedTransactionsAccountObjects = ConsentAuthorizationUtil
                    .getValidatedAccountObjects(transactionsAccountRefObjects, bankOfferedAccounts);
            if (validatedTransactionsAccountObjects == null || validatedTransactionsAccountObjects.isEmpty()) {
                areAccountsInvalid = true;
            }
        }

        if (areAccountsInvalid) {
            log.error("Consent accounts mismatch");
            return null;
        }

        if (validatedBalancesAccountObjects != null) {
            SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner balancePermissions =
                    new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner();

            // Set permission uid
            balancePermissions.setUid(UUID.randomUUID().toString());

            // Set permission display values
            balancePermissions
                    .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION)
                    .addDisplayValuesItem(ConsentExtensionConstants.BALANCES_PERMISSION);

            // Set initiated accounts
            balancePermissions.setInitiatedAccounts(validatedBalancesAccountObjects);

            permissionsList.add(balancePermissions);
        }

        if (validatedTransactionsAccountObjects != null) {
            SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner transactionPermissions =
                    new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner();

            // Set permission uid
            transactionPermissions.setUid(UUID.randomUUID().toString());

            // Set permission display values
            transactionPermissions
                    .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION)
                    .addDisplayValuesItem(ConsentExtensionConstants.TRANSACTIONS_PERMISSION);

            // Set initiated accounts
            transactionPermissions.setInitiatedAccounts(validatedTransactionsAccountObjects);

            permissionsList.add(transactionPermissions);
        }

        if (validatedAccountsAccountObjects != null) {
            SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner accountsPermissions =
                    new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner();

            // Set permission uid
            accountsPermissions.setUid(UUID.randomUUID().toString());

            // Set permission display values
            accountsPermissions
                    .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION);

            // Set initiated accounts
            accountsPermissions.setInitiatedAccounts(validatedAccountsAccountObjects);

            permissionsList.add(accountsPermissions);
        }

        return permissionsList;
    }

    /**
     * Returns build permission objects for the populate consent authorize page response when all psd2 permissions for
     * all valid accounts are requested.
     *
     * @param userId
     * @return
     */
    public static List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner>
    buildPermissionsForAllPsd2Accounts(String userId) {
        List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> permissionsList =
                new ArrayList<>();

        String shareableAccountsEndpoint = ConfigurationConstants.SHARABLE_ACCOUNTS_RETRIEVAL_ENDPOINT;
        JSONArray bankOfferedAccounts = DataRetrievalUtil.getAccountsFromEndpoint(userId,
                shareableAccountsEndpoint, new HashMap<>(), new HashMap<>());

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            return null;
        }

        SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner permissionObj =
                new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner();

        // Set permission uid
        permissionObj.setUid(UUID.randomUUID().toString());

        // Set permission display values
        permissionObj
                .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION)
                .addDisplayValuesItem(ConsentExtensionConstants.BALANCES_PERMISSION)
                .addDisplayValuesItem(ConsentExtensionConstants.TRANSACTIONS_PERMISSION);


        // Set initiated accounts for populate consent page
        List<Account> initiatedAccounts = new ArrayList<>();
        for (Object accountObj: bankOfferedAccounts) {
            initiatedAccounts.add(ConsentAuthorizationUtil.getAccountFromAccountRef((JSONObject) accountObj));
        }
        permissionObj.setInitiatedAccounts(initiatedAccounts);

        permissionsList.add(permissionObj);

        return permissionsList;
    }

    /**
     * Returns build permission objects for the populate consent authorize page response when account permissions for
     * all valid accounts are requested.
     *
     * @param userId
     * @param permission
     * @return
     */
    public static List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner>
    buildPermissionsForAvailableAccounts(String userId, String permission) {
        List<SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner> permissionsList =
                new ArrayList<>();

        String shareableAccountsEndpoint = ConfigurationConstants.SHARABLE_ACCOUNTS_RETRIEVAL_ENDPOINT;
        JSONArray bankOfferedAccounts = DataRetrievalUtil.getAccountsFromEndpoint(userId,
                shareableAccountsEndpoint, new HashMap<>(), new HashMap<>());

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            return null;
        }

        SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner permissionObj =
                new SuccessResponsePopulateConsentAuthorizeScreenDataConsentDataPermissionsInner();

        // Set permission uid
        permissionObj.setUid(UUID.randomUUID().toString());

        // Set permission display values
        if (StringUtils.equalsIgnoreCase(permission, PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString())) {
            permissionObj
                    .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION)
                    .addDisplayValuesItem(ConsentExtensionConstants.BALANCES_PERMISSION);
        } else {
            permissionObj
                    .addDisplayValuesItem(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
        }

        // Set initiated accounts for populate consent page
        List<Account> initiatedAccounts = new ArrayList<>();
        for (Object accountObj: bankOfferedAccounts) {
            initiatedAccounts.add(ConsentAuthorizationUtil.getAccountFromAccountRef((JSONObject) accountObj));
        }
        permissionObj.setInitiatedAccounts(initiatedAccounts);

        permissionsList.add(permissionObj);

        return permissionsList;
    }

    /**
     * Returns a set of permission account mappings as expected by the /persist-authorized-consent endpoint.
     *
     * @param authorizedObject
     * @param permission
     * @return
     */
    public static List<Resource> createAccountPermissionMappings(
            AuthorizedResourcesAuthorizedDataInner authorizedObject,
            String permission) {
        List<Account> accountList = authorizedObject.getAccounts();
        List<Resource> accountPermissionMapping = new ArrayList<>();

        for (Account account : accountList) {
            String referenceType = CommonConsentValidationUtil
                    .getAccountReferenceType(account.getAdditionalProperties());

            // Append account reference type, reference and currency to account id for mapping
            String referenceToPersist = String.format("%s%s%s", referenceType, CommonConstants.DELIMITER,
                    account.getAdditionalProperties().get(referenceType));
            if (account.getAdditionalProperties().containsKey(ConsentExtensionConstants.CURRENCY)) {
                referenceToPersist += String.format("%s%s", CommonConstants.DELIMITER,
                        account.getAdditionalProperties().get(ConsentExtensionConstants.CURRENCY));
            }

            // Create a new Resource object with the account ID, permission, and status
            Resource resource = new Resource()
                    .accountId(referenceToPersist)
                    .permission(permission)
                    .status("active");

            // Add the resource to the mapping list
            accountPermissionMapping.add(resource);
        }

        return accountPermissionMapping;
    }

    /**
     * Updates older receipt with new authorized accounts.
     *
     * @param consentResource
     * @param newAccess
     */
    public static void updateReceiptAccess(StoredDetailedConsentResourceData consentResource, AccountAccess newAccess) {
        AccountInitiationPayload receiptObj = objectMapper.convertValue(consentResource.getReceipt(),
                AccountInitiationPayload.class);

        // Update receipt only if there are account selections made, to keep the receipt specification compliant
        if (newAccess.getAccounts() != null || newAccess.getBalances() != null || newAccess.getTransactions() != null) {
            AccountAccess oldAccess = receiptObj.getAccess();

            // Copy additional information, if exists
            newAccess.setAdditionalInformation(oldAccess.getAdditionalInformation());
            if (oldAccess.getAccounts() != null || oldAccess.getBalances() != null
                    || oldAccess.getTransactions() != null) {
                receiptObj.setAccess(newAccess);
                consentResource.setReceipt(receiptObj);
            }
        }
    }
}
