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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import org.testng.Assert
import org.testng.annotations.Test

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Payload Validation Tests on Account Initiation Request.
 */
class AccountsInitiationRequestPayloadValidationTests extends AbstractAccountsFlow {

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201011_Initiation Request without access attribute in the payload"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{  
                   "recurringIndicator": true,
                   "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
                   "frequencyPerDay": 4,
                   "combinedServiceIndicator": true
            }"""
                .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Object has missing required properties ([\"access\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201012_Initiation Request with empty access attribute in the payload"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{  
                   "access":{  
                      
                   },
                   "recurringIndicator": true,
                   "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
                   "frequencyPerDay": 4,
                   "combinedServiceIndicator": true
            }"""
                .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Object has missing required properties ([\"access\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201013_Initiation Request without recurringIndicator attribute in the payload"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
            "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
            "frequencyPerDay": 4,
            "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Object has missing required properties ([\"recurringIndicator\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201014_Initiation Request without validUntil attribute in the payload"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "frequencyPerDay": 4,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Object has missing required properties ([\"validUntil\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201015_Initiation Request without frequencyPerDay attribute in the payload"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Object has missing required properties ([\"frequencyPerDay\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201016_Initiation Request without combinedServiceIndicator attribute in the payload"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
            "frequencyPerDay": 4
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Object has missing required properties ([\"combinedServiceIndicator\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201018_Initiation Request with recurringIndicator true and frequencyPerDay greater than 4"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "frequencyPerDay": 5,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.CONSENT_STATUS),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201019_Initiation Request with recurringIndicator true and frequencyPerDay less than 4"() {

        Thread.sleep(12000)
        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "frequencyPerDay": 1,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Frequency per day for recurring consent is lesser than the supported minimum value 4")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201020_Initiation Request with recurringIndicator false and frequencyPerDay 1"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "recurringIndicator": false,
           "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
           "frequencyPerDay": 1,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.CONSENT_STATUS),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201021_Initiation Request with recurringIndicator false and frequencyPerDay greater than 1"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "recurringIndicator": false,
           "validUntil":"${BerlinTestUtil.getDateAndTime(5)}",
           "frequencyPerDay": 4,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Set frequency per day attribute as 1,for one time account access.")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201022_Initiation Request with frequencyPerDay 0"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "frequencyPerDay": 0,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                        "Numeric instance is lower than the required minimum (minimum: 1, found: 0)")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201023_Initiation Request with frequencyPerDay in string format"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "frequencyPerDay": "4",
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                "Instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201024_Initiation Request with frequencyPerDay in alphabetic format"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "frequencyPerDay": "four",
           "combinedServiceIndicator": true
        }"""
                .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                "Instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201025_Initiation Request with validUntil in incorrect format"() {

        def validTime = LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "validUntil":"${validTime}",
           "frequencyPerDay": 4,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TIMESTAMP_INVALID)

        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim().
                contains("String \"${validTime}\" is invalid against requested date format(s) yyyy-MM-dd"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201026_Initiation Request with past date for validUntil"() {

        def validTime = LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE)

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "validUntil":"${validTime}",
           "frequencyPerDay": 4,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TIMESTAMP_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                "ValidUntil have to be today,"+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE) +" " +
                        "or a future date")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201027_Initiation Request with today date for validUntil"() {

        def validTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{  
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
           "validUntil":"${validTime}",
           "frequencyPerDay": 4,
           "combinedServiceIndicator": true
        }"""
            .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201028_Initiation Request with permission not supported by the spec"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{  
            "access":{
                "account":[
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

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                "Unrecognized property 'account'")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201029_Initiation Request with empty json payload"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{}""".stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                "Object has missing required properties ([\"access\",\"combinedServiceIndicator\",\"frequencyPerDay\"," +
                        "\"recurringIndicator\",\"validUntil\"])")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201030_Initiation Request with empty string payload"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """ """.stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim(),
                "Invalid request payload.")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201031_Initiation Request with frequencyPerDay in non integer or string format"() {

        def consentPath = AccountsConstants.CONSENT_PATH
        def initiationPayload = """{
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
           "frequencyPerDay": four,
           "combinedServiceIndicator": true
        }"""
                .stripIndent()

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)

        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).trim()
                .contains("Unable to parse JSON - Unrecognized token 'four': was expecting 'null', 'true', 'false' or NaN"))
    }
}
