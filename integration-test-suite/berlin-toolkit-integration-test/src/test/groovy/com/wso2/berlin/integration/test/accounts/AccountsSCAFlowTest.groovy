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

package com.wso2.berlin.integration.test.accounts

import com.wso2.berlin.test.framework.BGTest
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.berlin.test.framework.request_builder.BerlinRequestBuilder
import com.wso2.berlin.test.framework.utility.AccountsInitiationPayloads
import com.wso2.berlin.test.framework.utility.BerlinTestUtil
import org.testng.Assert
import org.testng.ITestContext
import org.testng.annotations.Test

/**
 * Accounts OAuth Redirection Flow Test for API v1.3.3 and v1.1.0.
 * Method Covers both Consent Approve and Deny Flow.
 */
class AccountsSCAFlowTest extends BGTest{

    String consentPath = BerlinConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @Test(priority = 2,dependsOnMethods = "TC0201001_Accounts initiation for SCA implicit accept scenario")
    void "TC0101009_Verify Get Application Access Token"(ITestContext context){

        // retrieve from context using key
        accessToken = getApplicationAccessToken(context.getAttribute(BerlinConstants.CLIENT_ID).toString())
        Assert.assertNotNull(accessToken)
    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void "TC0201001_Accounts initiation for SCA implicit accept scenario"() {

        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        Assert.assertNotNull(BerlinTestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
        Assert.assertNotNull(BerlinTestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))

        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, BerlinConstants.CONSENT_STATUS_RECEIVED)
    }


    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = ["TC0201001_Accounts initiation for SCA implicit accept scenario"])
    void "TC0202013_Accounts Authorization for SCA implicit accept scenario"() {

        doAuthorizationFlow()

        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, BerlinConstants.CONSENT_STATUS_VALID)

        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = ["TC0202013_Accounts Authorization for SCA implicit accept scenario"])
    void "TC0210011_Retrieval Request to get the details of a specific Account"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(BerlinConstants.SPECIFIC_ACCOUNT_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("account"))

    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 1)
    void "TC0202014_Accounts Authorization for SCA implicit deny scenario"() {

        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, BerlinConstants.CONSENT_STATUS_RECEIVED)

        doConsentDenyFlow()

        Assert.assertEquals(code, "User denied the consent")

        doStatusRetrieval(consentPath)

        Assert.assertEquals(consentStatus, BerlinConstants.CONSENT_STATUS_REJECTED)
    }

}
