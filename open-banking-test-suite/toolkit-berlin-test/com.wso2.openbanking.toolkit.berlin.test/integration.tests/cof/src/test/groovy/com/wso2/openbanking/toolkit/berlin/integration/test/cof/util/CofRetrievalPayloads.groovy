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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.util

class CofRetrievalPayloads {

    static String defaultRetrievalPayload = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE98765432109876543210"
        },
        "instructedAmount": {
            "currency": "EUR",
            "amount": "123"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithEmptyAccountsAttribute = """
    {
        "cardNumber": "12345678901234",
        "instructedAmount": {
            "currency": "EUR",
            "amount": "123"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithInvalidIban = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "INVALID_IBAN",
        "currency": "${CofConstants.currency}" 
        },
        "instructedAmount": {
            "currency": "EUR",
            "amount": "123"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithEmptyIban = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "",
        "currency": "${CofConstants.currency}" 
    },
        "instructedAmount": {
            "currency": "EUR",
            "amount": "123"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithoutIbanAttribute = """
    {
        "cardNumber": "12345678901234",
        "account": {
       
        },
        "instructedAmount": {
            "currency": "EUR",
            "amount": "123"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithoutInstructedAmountAttribute = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE12345678901234567890",
        "currency": "${CofConstants.currency}" 
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithoutAmountAttribute = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE98765432109876543210"
        },
        "instructedAmount": {
            "currency": "EUR"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithEmptyAmount = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE98765432109876543210"
        },
        "instructedAmount": {
            "currency": "EUR",
            "amount": ""
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithoutCurrencyAttribute = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE98765432109876543210"
        },
        "instructedAmount": {
            "amount": "123"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithEmptyCurrencyValue = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE98765432109876543210"
        },
        "instructedAmount": {
            "currency": "",
            "amount": "123"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithInvalidCurrencyValue = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE98765432109876543210"
        },
        "instructedAmount": {
            "currency": "123",
            "amount": "123"
        }
    }""".stripIndent()
}
