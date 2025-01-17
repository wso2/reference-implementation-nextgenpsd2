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

import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil

/**
 * Initiation Payloads of COF Tests
 */
class CofInitiationPayloads {

    static String defaultInitiationPayload = """
    {
        "account":
            {
                "iban": "DE98765432109876543210"
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2025-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithoutAccountAttribute = """
    {
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2020-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithoutAccountReference = """
    {
        "account": {},
        "cardNumber": "1234567891234",
        "cardExpiryDate": "${BerlinTestUtil.getDateAndTime(5)}",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithoutCardNumberAttribute = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}" 
            },
        "cardExpiryDate": "${BerlinTestUtil.getDateAndTime(5)}",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithCardNumberOfMoreThan35Chars = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}" 
            },
        "cardNumber": "1234567890123456789012345678901234567890",
        "cardExpiryDate": "2023-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithoutCardExpiryDateAttribute = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}"  
            },
        "cardNumber": "1234567891234",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithInvalidCardExpiryDate = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}"  
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2025-12-312",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithPastCardExpiryDate = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}"  
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2000-12-12",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithoutCardInformationAttribute = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}"  
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "${BerlinTestUtil.getDateAndTime(5)}",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithCardInfoOfMoreThan140Chars = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}"  
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2020-12-31",
        "cardInformation": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus egestas condimentum tellus ut posuere. Nunc neque metus, aliquam eget urna ut.",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithoutRegInfoAttribute = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}"  
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2020-12-31",
        "cardInformation": "My Merchant Loyalty Card",
    }
    """.stripIndent()

    static String initiationPayloadWithRegInfoOfMoreThan140Chars = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}"  
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2020-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus egestas condimentum tellus ut posuere. Nunc neque metus, aliquam eget urna ut."
    }
    """.stripIndent()

    static String initiationPayloadWithPanAttribute = """
    {
        "account":
            {
                "pan": "DE12345678901234567890",
                "currency": "${CofConstants.currency}" 
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2025-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithDifferentAccountId = """
    {
        "account":
            {
                "iban": "DE123456789012345678901234",
                "currency": "${CofConstants.currency}"  
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2025-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadForSubAccLevelMultiCurrency = """
    {
        "account":
            {
                "iban": "DE12345678901234567890",
                "currency": "${CofConstants.currency}" 
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2025-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()
    static String initiationPayloadForAggregateLevelMultiCurrency = """
    {
        "account":
            {
                "iban": "DE12345678901234567890"
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2025-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()
}
