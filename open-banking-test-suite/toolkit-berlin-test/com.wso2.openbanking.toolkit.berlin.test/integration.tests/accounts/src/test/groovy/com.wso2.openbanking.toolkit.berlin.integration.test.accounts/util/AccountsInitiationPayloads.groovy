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

import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil

class AccountsInitiationPayloads {

    static final String defaultInitiationPayload = """{
            "access":{
                "com.wso2.openbanking.toolkit.berlin.integration.test.accounts":[
                    {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                    }
                ],
                "balances":[
                    {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                    }
                ],
                "transactions":[  
                    {  
                        "iban":"DE12345678901234567890"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": true
        }"""
            .stripIndent()

    static final String initiationPayloadWithoutTransactionsPermission = """{
            "access":{  
                "com.wso2.openbanking.toolkit.berlin.integration.test.accounts":[
                    {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                    }
                ],
                "balances":[
                    {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": true
        }"""
            .stripIndent()

    static final String initiationPayloadWithoutBalancesPermission = """
          {
               "access":{
                  "com.wso2.openbanking.toolkit.berlin.integration.test.accounts":[
                     {
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ],
                  "transactions":[
                     {
                        "iban":"DE98765432109876543210"
                     }
                  ]
               },
               "recurringIndicator":true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":4,
               "combinedServiceIndicator":true
        }
    """.stripIndent()

    static final String initiationPayloadForAllAccounts = """
          {  
               "access":{
                    "availableAccounts": "allAccounts"
               },
               "recurringIndicator":false,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":1,
               "combinedServiceIndicator":true
               
        }
    """.stripIndent()

    static final String initiationPayloadForAvailableAccountsWithBalance = """
          {  
               "access":{  
                    "availableAccountsWithBalance": "allAccounts"
               },
               "recurringIndicator":false,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":1,
               "combinedServiceIndicator":true
        }
    """.stripIndent()

    static final String initiationPayloadForAvailableAccountsWithBalances = """
          {  
               "access":{  
                    "availableAccountsWithBalances": "allAccounts"
               },
               "recurringIndicator":false,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":1,
               "combinedServiceIndicator":true
        }
    """.stripIndent()

    static final String initiationPayloadForAllPsd2 = """
          {  
               "access":{
                    "allPsd2": "allAccounts"
               },
               "recurringIndicator":false,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay":1,
               "combinedServiceIndicator":true
        }
    """.stripIndent()

    static final String AllAccessBankOfferedConsentPayload = """
          {  
               "access":{  
                  "com.wso2.openbanking.toolkit.berlin.integration.test.accounts":[],
                  "balances":[],
                  "transactions":[]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": true
        }
    """.stripIndent()

    static final String TransactionAndBalancesBankOfferedConsentPayload = """
          {  
               "access":{  
                  "com.wso2.openbanking.toolkit.berlin.integration.test.accounts":[  
                     {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ],
                  "balances":[],
                  "transactions":[]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": true
        }
    """.stripIndent()

    static final String TransactionBankOfferedConsentPayload = """
          {  
               "access":{  
                  "com.wso2.openbanking.toolkit.berlin.integration.test.accounts":[  
                     {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ],
                  "balances":[  
                     {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ],
                  "transactions":[]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": true
        }
    """.stripIndent()

    static final String BalancesBankOfferedConsentPayload = """
          {  
               "access":{  
                  "com.wso2.openbanking.toolkit.berlin.integration.test.accounts":[  
                     {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ],
                  "balances":[],
                  "transactions":[  
                     {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": true
        }
    """.stripIndent()

    static final String AccountsBankOfferedConsentPayload = """
          {  
               "access":{  
                  "com.wso2.openbanking.toolkit.berlin.integration.test.accounts":[],
                  "balances":[  
                     {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ],
                  "transactions":[  
                     {  
                        "iban":"DE12345678901234567890",
                        "currency":"USD"
                     }
                  ]
               },
               "recurringIndicator": true,
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": true
        }
    """.stripIndent()
}
