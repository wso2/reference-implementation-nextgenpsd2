/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util

import com.wso2.openbanking.test.framework.util.ConfigParser
import org.testng.annotations.DataProvider

class AccountsDataProviders {

    private static String apiVersion = ConfigParser.getInstance().getApiVersion()

    /**
     * Payloads of All Available Accounts
     * @return allAvailableAccounts
     *
     */
    @DataProvider(name = "AllAvailableAccounts")
    static Iterator<Object[]> getAllAvailableAccounts() {

        Collection<Object[]> allAvailableAccounts = new ArrayList<Object[]>()
        List<Map<String, String>> listOfParamMaps = new ArrayList<Map<String, String>>()
        Map<String, String> allAvailableAccountsMap = new HashMap<String, String>()
        Map<String, String> allAccountsWithBalancesMap = new HashMap<String, String>()

        allAvailableAccountsMap.put("consentPath", AccountsConstants.CONSENT_PATH)
        allAvailableAccountsMap.put("initiationPayload", AccountsInitiationPayloads.initiationPayloadForAllAccounts)

        if (apiVersion.equalsIgnoreCase("1.3.3")) {
            allAccountsWithBalancesMap.put("consentPath", AccountsConstants.CONSENT_PATH)
            allAccountsWithBalancesMap.put("initiationPayload", AccountsInitiationPayloads.initiationPayloadForAvailableAccountsWithBalances)
        } else {
            allAccountsWithBalancesMap.put("consentPath", AccountsConstants.CONSENT_PATH)
            allAccountsWithBalancesMap.put("initiationPayload", AccountsInitiationPayloads.initiationPayloadForAvailableAccountsWithBalance)
        }

        listOfParamMaps.add(allAvailableAccountsMap)
        listOfParamMaps.add(allAccountsWithBalancesMap)

        for (Map<String, String> map : listOfParamMaps) {
            allAvailableAccounts.add([map] as Object[])
        }
        return allAvailableAccounts.iterator()
    }

    /**
     * Data for Bank Offered Consent Flow Tests
     * @return bankOfferedConsentList
     *
     */
    @DataProvider(name = "BankOfferedConsentData")
    Object[][] getBankOfferedConsentData() {

        def bankOfferedConsentList = new ArrayList<Object[]>()
        bankOfferedConsentList.add(["com.wso2.openbanking.toolkit.berlin.integration.test.accounts, balances and transactions from bank offered consent",
                                    [AccountsConstants.DD_BALANCES_ACC_LIST,
                                     AccountsConstants.DD_TRANSACTION_ACC_LIST,
                                     AccountsConstants.DD_ACCOUNTS_ACC_LIST],
                                    AccountsInitiationPayloads.AllAccessBankOfferedConsentPayload] as Object[])

        return bankOfferedConsentList
    }

    /**
     * Data for Invalid Bank Offered Consent Flow Tests
     * @return bankOfferedConsentList
     *
     */
    @DataProvider(name = "InvalidBankOfferedConsentData")
    Object[][] getInvalidBankOfferedConsentData() {

        def invalidBankOfferedConsentList = new ArrayList<Object[]>()

        invalidBankOfferedConsentList.add(["balances and transactions from bank offered consent",
                                           [AccountsConstants.DD_BALANCES_ACC_LIST,
                                            AccountsConstants.DD_TRANSACTION_ACC_LIST],
                                           AccountsInitiationPayloads.TransactionAndBalancesBankOfferedConsentPayload] as Object[])

        invalidBankOfferedConsentList.add(["transactions from bank offered consent",
                                           [AccountsConstants.DD_TRANSACTION_ACC_LIST],
                                           AccountsInitiationPayloads.TransactionBankOfferedConsentPayload] as Object[])

        invalidBankOfferedConsentList.add(["balances from bank offered consent",
                                           [AccountsConstants.DD_BALANCES_ACC_LIST],
                                           AccountsInitiationPayloads.BalancesBankOfferedConsentPayload] as Object[])

        invalidBankOfferedConsentList.add(["com.wso2.openbanking.toolkit.berlin.integration.test.accounts from bank offered consent",
                                           [AccountsConstants.DD_ACCOUNTS_ACC_LIST],
                                           AccountsInitiationPayloads.AccountsBankOfferedConsentPayload] as Object[])

        return invalidBankOfferedConsentList
    }

    /**
     * Get Account Retrieval Resource Paths
     * @return versionSpecificData
     *
     */
    @DataProvider(name = "AccountRetrievalResourcePaths")
    static Iterator<Object[]> getAccountRetrievalResourcePaths() {

        Collection<Object[]> accountResourcePaths = new ArrayList<Object[]>()
        List<Map<String, String>> listOfParamMaps = new ArrayList<Map<String, String>>()
        Map<String, String> bulkAccounts = new HashMap<String, String>()
        Map<String, String> specificAccount = new HashMap<String, String>()
        Map<String, String> bulkBalances = new HashMap<String, String>()
        Map<String, String> cardAccounts = new HashMap<String, String>()
        Map<String, String> specificCardAccounts = new HashMap<String, String>()
        Map<String, String> cardAccountsBalances = new HashMap<String, String>()

        bulkAccounts.put("resourcePath", AccountsConstants.ACCOUNTS_PATH)
        specificAccount.put("resourcePath", AccountsConstants.SPECIFIC_ACCOUNT_PATH)
        bulkBalances.put("resourcePath", AccountsConstants.BALANCES_PATH)
        cardAccounts.put("resourcePath", AccountsConstants.CARD_ACCOUNTS_PATH)
        specificCardAccounts.put("resourcePath", AccountsConstants.SPECIFIC_CARD_ACCOUNTS_PATH)
        cardAccountsBalances.put("resourcePath", AccountsConstants.CARD_ACCOUNTS_BALANCES_PATH)

        listOfParamMaps.add(bulkAccounts)
        listOfParamMaps.add(specificAccount)
        listOfParamMaps.add(bulkBalances)
        listOfParamMaps.add(cardAccounts)
        listOfParamMaps.add(specificCardAccounts)
        listOfParamMaps.add(cardAccountsBalances)

        for (Map<String, String> map : listOfParamMaps) {
            accountResourcePaths.add([map] as Object[])
        }
        return accountResourcePaths.iterator()
    }

    /**
     * Transaction Retrieval Resource Paths
     * @return versionSpecificData
     *
     */
    @DataProvider(name = "TransactionRetrievalResourcePaths")
    static Iterator<Object[]> getTransactionRetrievalResourcePaths() {

        Collection<Object[]> transactionResourcePaths = new ArrayList<Object[]>()
        List<Map<String, String>> listOfParamMaps = new ArrayList<Map<String, String>>()
        Map<String, String> bulkTransaction = new HashMap<String, String>()
        Map<String, String> specificTransaction = new HashMap<String, String>()
        Map<String, String> cardAccountsTransactions = new HashMap<String, String>()

        bulkTransaction.put("resourcePath", AccountsConstants.TRANSACTIONS_PATH)
        specificTransaction.put("resourcePath", AccountsConstants.SPECIFIC_TRANSACTIONS_PATH)
        cardAccountsTransactions.put("resourcePath", AccountsConstants.CARD_ACCOUNTS_TRANSACTIONS_PATH)

        listOfParamMaps.add(bulkTransaction)
        listOfParamMaps.add(specificTransaction)
        listOfParamMaps.add(cardAccountsTransactions)

        for (Map<String, String> map : listOfParamMaps) {
            transactionResourcePaths.add([map] as Object[])
        }
        return transactionResourcePaths.iterator()
    }
}
