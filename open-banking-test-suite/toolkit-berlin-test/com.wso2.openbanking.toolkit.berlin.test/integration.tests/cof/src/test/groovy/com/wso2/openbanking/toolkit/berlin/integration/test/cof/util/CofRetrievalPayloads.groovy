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
        "iban": "DE12345678901234567890",
        "currency": "${CofConstants.currency}" 
        },
        "instructedAmount": {
            "currency": "EUR"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithEmptyAmount = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE12345678901234567890",
        "currency": "${CofConstants.currency}" 
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
        "iban": "DE12345678901234567890",
        "currency": "${CofConstants.currency}" 
        },
        "instructedAmount": {
            "amount": "123"
        }
    }""".stripIndent()

    static String cofRetrievalPayloadWithEmptyCurrencyValue = """
    {
        "cardNumber": "12345678901234",
        "account": {
        "iban": "DE12345678901234567890",
        "currency": "${CofConstants.currency}" 
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
        "iban": "DE12345678901234567890",
        "currency": "${CofConstants.currency}" 
        },
        "instructedAmount": {
            "currency": "123",
            "amount": "123"
        }
    }""".stripIndent()
}
