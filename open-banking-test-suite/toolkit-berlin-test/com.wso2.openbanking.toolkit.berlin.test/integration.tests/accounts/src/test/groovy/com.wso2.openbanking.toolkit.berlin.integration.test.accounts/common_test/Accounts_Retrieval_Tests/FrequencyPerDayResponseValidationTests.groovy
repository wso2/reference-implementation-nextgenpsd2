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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Retrieval_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import java.util.concurrent.atomic.AtomicInteger

/**
 * Accounts Retrieval Tests.
 */
class FrequencyPerDayResponseValidationTests extends AbstractAccountsFlow {

    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload
    AtomicInteger sequence = new AtomicInteger(0)

    @BeforeClass
    void "Get User Access Token"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorise Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Generate UserAccess Token
        getUserAccessToken(code)
        Assert.assertNotNull(userAccessToken)

    }

    @Test (groups = "1.3.6", invocationCount = 4, enabled = true)
    void "BG-383_Send account retrieval request for the same consent for number of times specified in frequency"() {

        //Send Account Retrieval call 4 times.
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.SPECIFIC_TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)

    }

    @Test (groups = "1.3.6", invocationCount = 10, enabled = true)
    void "BG-384_Send account retrieval request for the same consent exceeding number of times specified in frequency"() {

        //Send Account Retrieval call 10 times.
        def response = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
          .header(BerlinConstants.Date, getCurrentDate())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

        int currentCount =  sequence.addAndGet(1)

        if (currentCount < 5) {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        } else {
            Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_429)
            Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
                    BerlinConstants.ACCESS_EXCEEDED)
            Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains ("You have exceeded your quota .You can access API after"))

        }
    }
}
