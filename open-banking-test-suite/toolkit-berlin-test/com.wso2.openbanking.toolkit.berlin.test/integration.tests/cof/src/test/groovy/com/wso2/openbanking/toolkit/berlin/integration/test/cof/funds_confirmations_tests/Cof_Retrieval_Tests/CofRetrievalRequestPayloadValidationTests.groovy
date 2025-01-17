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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Retrieval_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofRetrievalPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Payload Validation Tests on Funds Confirmations Retrieval Request.
 */
class CofRetrievalRequestPayloadValidationTests extends AbstractCofFlow {

    String resourcePath = CofConstants.COF_RETRIEVAL_PATH
    String consentPath = CofConstants.COF_CONSENT_PATH
    String initiationPayload = CofInitiationPayloads.defaultInitiationPayload

    void preRetrievalFlow() {

        //Do Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent and Extract the Code
        doCofAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
    }

    @Test (groups = ["1.3.6"])
    void "TC0605012_Confirm funds with an empty account attribute"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithEmptyAccountsAttribute)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT),
                "Invalid request payload, mandatory elements are missing")
    }

    @Test (groups = ["1.3.6"])
    void "TC0605013_Confirm funds with an invalid iban"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithInvalidIban)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString()
                .contains("ECMA 262 regex \"[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}\" does not match input string \"INVALID_IBAN\""))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605014_Confirm funds with an empty iban in payload"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithEmptyIban)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
          BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_PATH),
                "account.iban")
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString()
                .contains("ECMA 262 regex \"[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}\" does not match input string \"\""))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605015_Confirm funds without instructedAmount element in payload"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithoutInstructedAmountAttribute)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT),
                "Invalid request payload, mandatory elements are missing")
    }

    @Test (groups = ["1.3.6"])
    void "TC0605016_Confirm funds without amount element in payload"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithoutAmountAttribute)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_PATH),
          "instructedAmount")
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "Object has missing required properties ([\"amount\"])"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605017_Confirm funds with an empty amount in payload"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithEmptyAmount)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_PATH),
          "instructedAmount.amount")
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
          BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString()
                .contains("ECMA 262 regex \"^-?([0-9]{1,14})(\\.[0-9]{1,3})?\$\" does not match input string \"\""))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605018_Confirm funds without currency element in payload"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithoutCurrencyAttribute)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_PATH),
          "instructedAmount")
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
          BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "Object has missing required properties ([\"currency\"])"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605019_Confirm funds with an empty currency value in payload"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithEmptyCurrencyValue)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_PATH),
          "instructedAmount.currency")
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
          BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString()
                .contains("\"[A-Z]{3}\" does not match input string \"\""))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605020_Confirm funds with an invalid currency value in payload"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(CofRetrievalPayloads.cofRetrievalPayloadWithInvalidCurrencyValue)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_PATH),
          "instructedAmount.currency")
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
          BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString()
                .contains("\"[A-Z]{3}\" does not match input string \"123\""))
    }
}
