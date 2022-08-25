/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
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
    void "OB-1675_Send account retrieval request for the same consent for number of times specified in frequency"() {

        //Send Account Retrieval call 4 times.
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.SPECIFIC_TRANSACTIONS_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)

    }

    @Test (groups = "1.3.6", invocationCount = 10, enabled = true)
    void "OB-1676_Send account retrieval request for the same consent exceeding number of times specified in frequency"() {

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
