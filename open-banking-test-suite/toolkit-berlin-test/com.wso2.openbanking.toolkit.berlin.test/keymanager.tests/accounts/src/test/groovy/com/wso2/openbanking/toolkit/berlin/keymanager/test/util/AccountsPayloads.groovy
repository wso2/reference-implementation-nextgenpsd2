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

package com.wso2.openbanking.toolkit.berlin.keymanager.test.util

import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader

/**
 * Accounts Payloads.
 */
class AccountsPayloads {

    static final String defaultInitiationPayload = """{
            "access":{
                "accounts":[
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
                "accounts":[
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
            "combinedServiceIndicator": false
        }"""
            .stripIndent()

    static final String initiationPayloadWithoutBalancesPermission = """
          {
               "access":{
                  "accounts":[
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
               "combinedServiceIndicator": false
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
               "combinedServiceIndicator": false
               
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
               "combinedServiceIndicator": false
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
               "combinedServiceIndicator": false
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
               "combinedServiceIndicator": false
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
               "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
               "frequencyPerDay": 4,
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String TransactionAndBalancesBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[  
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
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String TransactionBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[  
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
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String BalancesBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[  
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
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    static final String AccountsBankOfferedConsentPayload = """
          {  
               "access":{  
                  "accounts":[],
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
               "combinedServiceIndicator": false
        }
    """.stripIndent()

    //TODO: Update payload once the implementation complete.
    /**
     * Validate Request Payload.
     * @param accountId
     * @param consentId
     * @param accessMethod
     * @param tppId
     * @param psuId
     * @param xRequestId
     * @return payload
     */
    static final String buildValidationPayload(String accountId, String consentId, String accessMethod, String tppId,
                                  String psuId = "${PsuConfigReader.getPSU()}@${ConfigParser.getInstance().getTenantDomain()}",
                                  String xRequestId = UUID.randomUUID().toString()) {

        def payload = """
        {
            "accountId": "$accountId",
            "consentId": "$consentId",
            "access": "$accessMethod",
            "isIBANValidationEnabled":false,
            "withBalance": true,
            "tppUniqueId":"$tppId",
            "psuId":"$psuId",
            "xRequestId": "$xRequestId"
            }""".stripIndent()

        return payload
    }
}
