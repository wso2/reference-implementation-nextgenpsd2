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

import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil

/**
 * Initiation Payloads of COF Tests
 */
class CofInitiationPayloads {

    static String defaultInitiationPayload = """
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
                "iban": "DE12345678901234567890" 
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
                "iban": "DE12345678901234567890" 
            },
        "cardNumber": "1234567890123456789012345678901234567890",
        "cardExpiryDate": "2020-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithoutCardExpiryDateAttribute = """
    {
        "account":
            {
                "iban": "DE12345678901234567890" 
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
                "iban": "DE12345678901234567890" 
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2020-12-312",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()

    static String initiationPayloadWithPastCardExpiryDate = """
    {
        "account":
            {
                "iban": "DE12345678901234567890" 
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
                "iban": "DE12345678901234567890" 
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
                "iban": "DE12345678901234567890" 
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
                "iban": "DE12345678901234567890" 
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
                "iban": "DE12345678901234567890" 
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
                "pan": "DE12345678901234567890" 
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
                "iban": "DE123456789012345678901234" 
            },
        "cardNumber": "1234567891234",
        "cardExpiryDate": "2025-12-31",
        "cardInformation": "My Merchant Loyalty Card",
        "registrationInformation": "Your contract Number 1234 with MyMerchant is completed with the registration with your bank."
    }
    """.stripIndent()
}
