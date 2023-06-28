/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework.utility

import com.wso2.berlin.test.framework.constant.BerlinConstants


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
                ],
                "transactions":[  
                    {  
                        "iban":"${BerlinConstants.CURRENT_ACCOUNT}"
                    }
                ]
            },
            "recurringIndicator": true,
            "validUntil":"${BerlinTestUtil.getDateAndTime(4)}",
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
               "validUntil":"${BerlinTestUtil.getDateAndTime(0)}",
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
               "validUntil":"${BerlinTestUtil.getDateAndTime(0)}",
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
               "validUntil":"${BerlinTestUtil.getDateAndTime(0)}",
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
            "validUntil":"${BerlinTestUtil.getDateAndTime(0)}",
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
            "validUntil":"${BerlinTestUtil.getDateAndTime(0)}",
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
}

