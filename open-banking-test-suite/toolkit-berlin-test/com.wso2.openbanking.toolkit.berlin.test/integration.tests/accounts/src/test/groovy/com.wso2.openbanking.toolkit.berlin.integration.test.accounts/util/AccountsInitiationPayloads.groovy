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

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil

class AccountsInitiationPayloads {

    static final String defaultInitiationPayload = """{
            "access":{
                "accounts":[
                    {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                    }
                ],
                "balances":[
                    {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String initiationPayloadWithoutTransactionsPermission = """{
            "access":{  
                "accounts":[
                    {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                    }
                ],
                "balances":[
                    {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String initiationPayloadWithoutBalancesPermission = """
          {
               "access":{
                  "accounts":[
                     {
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}",
                        "currency":"${BerlinConstants.CURRENCY1}"
                     }
                  ],
                  "transactions":[
                     {
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                     }
                  ]
               },
               "recurringIndicator":true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":4,
               "combinedServiceIndicator":false
        }
    """.stripIndent()

    static final String initiationPayloadForAllAccounts = """
          {  
               "access":{
                    "availableAccounts": "allAccounts"
               },
               "recurringIndicator":true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(3)}",
               "frequencyPerDay":4,
               "combinedServiceIndicator":false
               
        }
    """.stripIndent()

    static final String initiationPayloadForAvailableAccountsWithBalance = """
          {  
               "access":{  
                    "availableAccountsWithBalance": "allAccounts"
               },
               "recurringIndicator":true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":4,
               "combinedServiceIndicator":false
        }
    """.stripIndent()

    static final String initiationPayloadForAvailableAccountsWithBalances = """
          {  
               "access":{  
                    "availableAccountsWithBalances": "allAccounts"
               },
               "recurringIndicator":true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":4,
               "combinedServiceIndicator":false
        }
    """.stripIndent()

    static final String initiationPayloadForAllPsd2 = """
          {  
               "access":{
                    "allPsd2": "allAccounts"
               },
               "recurringIndicator":true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":4,
               "combinedServiceIndicator":false
        }
    """.stripIndent()

    public static final String AllAccessBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[],
                  "balances":[],
                  "transactions":[]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(2)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String TransactionAndBalancesBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[  
                     {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}",
                        "currency":"${BerlinConstants.CURRENCY1}"
                     }
                  ],
                  "balances":[],
                  "transactions":[]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String TransactionBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[  
                     {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                     }
                  ],
                  "balances":[  
                     {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                     }
                  ],
                  "transactions":[]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String BalancesBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[  
                     {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                     }
                  ],
                  "balances":[],
                  "transactions":[  
                     {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                     }
                  ]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String AccountsBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[],
                  "balances":[  
                     {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                     }
                  ],
                  "transactions":[  
                     {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                     }
                  ]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String defaultCardAccountInitiationPayload = """{
            "access":{
                "accounts":[
                    {  
                        "maskedPan":"525412******3241"
                    }
                ],
                "balances":[
                    {  
                        "maskedPan":"525412******3241"
                    }
                ],
                "transactions":[  
                    {  
                        "maskedPan":"525412******3241"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String subAccLevelMultiCurrencyInitiationPayload = """{
            "access":{
                "accounts":[
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}",
                        "currency":"${BerlinConstants.CURRENCY1}"
                    }
                ],
                "balances":[
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}",
                        "currency":"${BerlinConstants.CURRENCY2}"
                    }
                ],
                "transactions":[  
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}",
                        "currency":"${BerlinConstants.CURRENCY3}"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String aggregationLevelMultiCurrencyInitiationPayload = """{
            "access":{
                "accounts":[
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}"
                    }
                ],
                "balances":[
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}"
                    }
                ],
                "transactions":[  
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String subAndAggregartionMultiCurrencyInitiationPayload = """{
            "access":{
                "accounts":[
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}",
                        "currency":"${BerlinConstants.CURRENCY1}"
                    }
                ],
                "balances":[
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}",
                        "currency":"${BerlinConstants.CURRENCY2}"
                    }
                ],
                "transactions":[  
                    {  
                        "iban":"${BerlinConstants.MULTICURRENCY_ACCOUNT}"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String initiationPayloadWithPan = """{
            "access":{
                "accounts":[
                    {  
                        "pan":"${BerlinConstants.PAN_ACCOUNT}"
                    }
                ],
                "balances":[
                    {  
                        "pan":"${BerlinConstants.PAN_ACCOUNT}"
                    }
                ],
                "transactions":[  
                    {  
                        "pan":"${BerlinConstants.PAN_ACCOUNT}"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String initiationPayloadWithBban = """{
            "access":{
                "accounts":[
                    {  
                        "bban":"${BerlinConstants.BBAN_ACCOUNT}"
                    }
                ],
                "balances":[
                    {  
                        "bban":"${BerlinConstants.BBAN_ACCOUNT}"
                    }
                ],
                "transactions":[  
                    {  
                        "bban":"${BerlinConstants.BBAN_ACCOUNT}"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String cardAccountPayloadWithoutBalancesPermission = """
          {
               "access":{
                  "accounts":[
                     {
                        "maskedPan":"525412******3241",
                        "currency":"${BerlinConstants.CURRENCY1}"
                     }
                  ],
                  "transactions":[
                     {
                        "maskedPan":"525412******3241"
                     }
                  ]
               },
               "recurringIndicator":true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":4,
               "combinedServiceIndicator":false
        }
    """.stripIndent()

    static final String cardAccountPayloadWithoutTransactionsPermission = """{
            "access":{  
                "accounts":[
                    {  
                        "maskedPan":"525412******3241"
                    }
                ],
                "balances":[
                    {  
                        "maskedPan":"525412******3241"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": false
        }"""
            .stripIndent()


    static final String initiationPayloadForAllAccountsWithoutRecurringIndicator = """
          {  
               "access":{
                    "availableAccounts": "allAccounts"
               },
               "recurringIndicator":false,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":4,
               "combinedServiceIndicator":false
               
        }
    """.stripIndent()
}
