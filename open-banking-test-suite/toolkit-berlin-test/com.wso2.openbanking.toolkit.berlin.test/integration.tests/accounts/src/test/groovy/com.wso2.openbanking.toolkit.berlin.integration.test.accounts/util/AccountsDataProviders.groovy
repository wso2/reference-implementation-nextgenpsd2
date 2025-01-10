/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
//        Map<String, String> allAccountsWithBalancesMap = new HashMap<String, String>()

        allAvailableAccountsMap.put("consentPath", AccountsConstants.CONSENT_PATH)
        allAvailableAccountsMap.put("initiationPayload", AccountsInitiationPayloads.initiationPayloadForAllAccounts)

//        if (apiVersion.equalsIgnoreCase("1.3.3")) {
//            allAccountsWithBalancesMap.put("consentPath", AccountsConstants.CONSENT_PATH)
//            allAccountsWithBalancesMap.put("initiationPayload", AccountsInitiationPayloads.initiationPayloadForAvailableAccountsWithBalances)
//        } else {
//            allAccountsWithBalancesMap.put("consentPath", AccountsConstants.CONSENT_PATH)
//            allAccountsWithBalancesMap.put("initiationPayload", AccountsInitiationPayloads.initiationPayloadForAvailableAccountsWithBalance)
//        }

        listOfParamMaps.add(allAvailableAccountsMap)
//        listOfParamMaps.add(allAccountsWithBalancesMap)

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
        bankOfferedConsentList.add(["accounts, balances and transactions from bank offered consent",
                                    [AccountsConstants.BALANCES_LIST_NORMAL_ACC,
                                     AccountsConstants.TRANSACTION_LIST_NORMAL_ACC,
                                     AccountsConstants.ACCOUNTS_LIST_NORMAL_ACC],
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
                                           [AccountsConstants.BALANCES_LIST_NORMAL_ACC,
                                            AccountsConstants.TRANSACTION_LIST_NORMAL_ACC],
                                           AccountsInitiationPayloads.TransactionAndBalancesBankOfferedConsentPayload] as Object[])

        invalidBankOfferedConsentList.add(["transactions from bank offered consent",
                                           [AccountsConstants.TRANSACTION_LIST_NORMAL_ACC],
                                           AccountsInitiationPayloads.TransactionBankOfferedConsentPayload] as Object[])

        invalidBankOfferedConsentList.add(["balances from bank offered consent",
                                           [AccountsConstants.BALANCES_LIST_NORMAL_ACC],
                                           AccountsInitiationPayloads.BalancesBankOfferedConsentPayload] as Object[])

        invalidBankOfferedConsentList.add(["com.wso2.openbanking.toolkit.berlin.integration.test.accounts from bank offered consent",
                                           [AccountsConstants.ACCOUNTS_LIST_NORMAL_ACC],
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
