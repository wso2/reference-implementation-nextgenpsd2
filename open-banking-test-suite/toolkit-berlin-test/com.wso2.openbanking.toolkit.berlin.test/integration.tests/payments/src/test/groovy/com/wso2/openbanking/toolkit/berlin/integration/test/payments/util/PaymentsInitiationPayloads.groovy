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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.util

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil

import java.time.LocalDate
import java.time.OffsetDateTime


class PaymentsInitiationPayloads {

    final static String instructedAmount = PaymentsConstants.instructedAmount
    final static String instructedAmountCurrency = PaymentsConstants.instructedAmountCurrency
    final static String instructedAmountCurrency2 = PaymentsConstants.instructedAmountCurrency2
    final static String debtorAccount1 = PaymentsConstants.debtorAccount1
    final static String creditorAccount1 = PaymentsConstants.creditorAccount1
    final static String creditorName1 = PaymentsConstants.creditorName1
    final static String debtorAccount2 = PaymentsConstants.debtorAccount2
    final static String creditorAccount2 = PaymentsConstants.creditorAccount2
    final static String creditorName2 = PaymentsConstants.creditorName2

    final static String executionRuleFollowing = PaymentsConstants.executionRuleFollowing
    final static String executionRuleLatest = PaymentsConstants.executionRulePreceding
    final static String frequency = PaymentsConstants.frequency
    final static String dayOfExecution = PaymentsConstants.dayOfExecution
    final static LocalDate paymentDate = OffsetDateTime.now().toLocalDate().plusDays(5)
    final static LocalDate paymentEndDate = OffsetDateTime.now().toLocalDate().plusDays(10)

    static final String singlePaymentPayload = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}" 
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()

    static final String singlePaymentPayloadWithAdditionalParams = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}" 
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant",
            "extra_params_foo" : "123abc",
            "taxid" : "12abc12"
        }"""
            .stripIndent()

    static final String bulkPaymentPayload = """{
            "batchBookingPreferred": true,
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
                },
            "requestedExecutionDate": "${paymentDate}",
            "payments":[
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName1}",
                    "creditorAccount": {
                        "iban": "${creditorAccount1}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                },
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName2}",
                    "creditorAccount": {
                        "iban": "${creditorAccount2}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                }
            ]
        }"""
            .stripIndent()

    static final String periodicPaymentPayload = """
        {
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },

            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "startDate": "${paymentDate}",
            "executionRule": "${executionRuleFollowing}",
            "frequency": "${frequency}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()

    static final String v110BulkPaymentPayload = """[
            {
                "instructedAmount": {
                    "currency": "${instructedAmountCurrency}",
                    "amount": "${instructedAmount}"
                },
                "debtorAccount": {
                    "iban": "${debtorAccount1}",
                    "currency": "${instructedAmountCurrency}"
                },
                "creditorName": "${creditorName1}",
                "creditorAccount": {
                    "iban": "${creditorAccount1}"
                },
                "remittanceInformationUnstructured": "Ref Number Merchant"
            },
            {
                "instructedAmount": {
                    "currency": "${instructedAmountCurrency}",
                    "amount": "${instructedAmount}"
                },
                "debtorAccount": {
                    "iban": "${debtorAccount1}",
                    "currency": "${instructedAmountCurrency}"
                },
                "creditorName": "${creditorName1}",
                "creditorAccount": {
                    "iban": "${creditorAccount1}"
                },
                "remittanceInformationUnstructured": "Ref Number Merchant"
            }
        ]"""
            .stripIndent()

    static final String v110PeriodicPaymentPayload = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}", 
                "amount": "123"
            }, 
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            }, 
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            }, 
            "remittanceInformationUnstructured": "Ref Number Abonnement", 
            "startDate": "${paymentDate}",
            "executionRule": "${executionRuleLatest}",
            "frequency": "MONTHLY",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()


    static final String singlePaymentPayloadWithoutDebtorAccount = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()


    static final String singlePaymentPayloadWithoutInstructedAmount = """{
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()

    static final String singlePaymentPayloadWithoutInstructedAmount_Currency = """{
            "instructedAmount": {
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()

    static final String singlePaymentPayloadWithoutInstructedAmount_Amount = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()


    static String singlePaymentPayloadBuilder(instructedAmountCurrency, instructedAmount,
                                              debtorAccount, creditorName, creditorAccount) {

        String singlePaymentPayload = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName}",
            "creditorAccount": {
                "iban": "${creditorAccount}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
                .stripIndent()

        return singlePaymentPayload

    }

    static String bulkPaymentPayloadBuilder(instructedAmountCurrency, instructedAmount, accountAttribute,
                                            debtorAccount, creditorName, creditorAccount) {

        String bulkPaymentPayload = """{
            "batchBookingPreferred": true,
            "debtorAccount": {
                "${accountAttribute}": "${debtorAccount}",
                "currency": "${instructedAmountCurrency}"
                },
            "requestedExecutionDate": "${paymentDate}",
            "payments":[
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName}",
                    "creditorAccount": {
                        "${accountAttribute}": "${creditorAccount}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                },
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName}",
                    "creditorAccount": {
                        "${accountAttribute}": "${creditorAccount}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                }
            ]
        }"""
                .stripIndent()

        return bulkPaymentPayload
    }

    static final String singlePaymentPayloadWithoutCreditorAccount = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()

    static final String singlePaymentPayloadWithoutCreditorName = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()

    static final String singlePaymentPayloadWithOptionalData = """{
            "endToEndIdentification":"123",
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "creditorAgent": "AAAADEBBXXX",
            "creditorAddress":{
                "street": "rue blue",
                "buildingNumber": "89",
                "city": "Paris",
                "postalCode": "75000",
                "country": "FR"
       },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()

    static final String singlePaymentPayloadWithUnspecifiedData  = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant",
            "extraParams":"extraValue"
        }"""
            .stripIndent()

    static final String bulkPaymentPayloadWithOnlyMandatoryElements = """{
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
                },
            "payments":[
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName1}",
                    "creditorAccount": {
                        "iban": "${creditorAccount1}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                },
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName2}",
                    "creditorAccount": {
                        "iban": "${creditorAccount2}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                }
            ]
        }"""
            .stripIndent()

    static final String bulkPaymentPayloadWithoutDebtorAccount = """{
            "batchBookingPreferred": true,
            "requestedExecutionDate": "${paymentDate}",
            "payments":[
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName1}",
                    "creditorAccount": {
                        "iban": "${creditorAccount1}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                },
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "debtorAccount": {
                        "iban": "${debtorAccount2}",
                        "currency": "${instructedAmountCurrency}"
                    },
                    "creditorName": "${creditorName2}",
                    "creditorAccount": {
                        "iban": "${creditorAccount2}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                }
            ]
        }"""
            .stripIndent()

    static final String bulkPaymentPayloadWithoutPayments = """{
            "batchBookingPreferred": true,
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
                },
            "requestedExecutionDate": "${paymentDate}"
        }"""
            .stripIndent()

    static String bulkPaymentPayloadWithConfigurablePayments(String paymentArr) {

        String bulkPaymentPayload = """{
            "batchBookingPreferred": true,
            "debtorAccount": {
                "iban": "${PaymentsConstants.debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
                },
            "requestedExecutionDate": "${paymentDate}",
            "payments":$paymentArr
        }"""
                .stripIndent()

        return bulkPaymentPayload
    }

    static final String periodicPaymentPayloadWithOnlyMandatoryElements = """
        {
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },

            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "startDate": "${paymentDate}",
            "frequency": "${frequency}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()

    static final String periodicPaymentPayloadWithoutStartDate = """
        {
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },

            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "executionRule": "${executionRuleFollowing}",
            "frequency": "${frequency}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()

    static final String periodicPaymentPayloadWithoutFrequency = """
        {
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },

            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "startDate": "${paymentDate}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()

    static String periodicPaymentPayloadWithoutInstructedAmount = """
        {
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "startDate": "${paymentDate}",
            "executionRule": "${executionRuleFollowing}",
            "frequency": "${frequency}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()

    static String periodicPaymentPayloadWithoutDebtorAccount = """
        {
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "startDate": "${paymentDate}",
            "executionRule": "${executionRuleFollowing}",
            "frequency": "${frequency}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()

    static String periodicPaymentPayloadWithoutCreditorName = """
        {
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "startDate": "${paymentDate}",
            "executionRule": "${executionRuleFollowing}",
            "frequency": "${frequency}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()

    static String periodicPaymentPayloadWithoutCreditorAccount = """
        {
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },

            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "startDate": "${paymentDate}",
            "executionRule": "${executionRuleFollowing}",
            "frequency": "${frequency}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
            .stripIndent()


    static String periodicPaymentPayloadBuilder(LocalDate startDate, String executionRule,String frequency,
                                                LocalDate endDate, String dayOfExecution) {

        String periodicPaymentPayload = """
        {
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },

            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Abonnement",
            "startDate": "${startDate}",
            "executionRule": "${executionRule}",
            "frequency": "${frequency}",
            "endDate": "${endDate}",
            "dayOfExecution": "${dayOfExecution}"
        }"""
                .stripIndent()

        return periodicPaymentPayload
    }

    static final String futureDatedPaymentPayload = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "requestedExecutionDate": "${paymentDate}",
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()

    static String futureDatedPaymentPayloadBuilder(LocalDate paymentDate) {

        String futureDatedPaymentPayload = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "requestedExecutionDate": "${paymentDate}",
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
                .stripIndent()

        return futureDatedPaymentPayload
    }

    static final String singlePaymentPayloadWithMismatchingCurrency = """{
            "instructedAmount": {
                "currency": "${instructedAmountCurrency2}",
                "amount": "${instructedAmount}"
            },
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}" 
            },
            "creditorName": "${creditorName1}",
            "creditorAccount": {
                "iban": "${creditorAccount1}"
            },
            "remittanceInformationUnstructured": "Ref Number Merchant"
        }"""
            .stripIndent()

    static final String bulkPaymentPayloadWithSingleAndFutureDatedEntries = """{

            "batchBookingPreferred": true,
            "debtorAccount": {
                "iban": "${debtorAccount1}",
                "currency": "${instructedAmountCurrency}"
                },
            "requestedExecutionDate": "${paymentDate}",
            "payments":[
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName1}",
                    "creditorAccount": {
                        "iban": "${creditorAccount1}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"
                },
                {
                    "instructedAmount": {
                        "currency": "${instructedAmountCurrency}",
                        "amount": "${instructedAmount}"
                    },
                    "creditorName": "${creditorName1}",
                        "creditorAccount": {
                        "iban": "${creditorAccount1}"
                    },
                    "remittanceInformationUnstructured": "Ref Number Merchant"                
                }
            ]
        }"""
            .stripIndent()

    static final String accountsInitiationPayload = """{
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

}
