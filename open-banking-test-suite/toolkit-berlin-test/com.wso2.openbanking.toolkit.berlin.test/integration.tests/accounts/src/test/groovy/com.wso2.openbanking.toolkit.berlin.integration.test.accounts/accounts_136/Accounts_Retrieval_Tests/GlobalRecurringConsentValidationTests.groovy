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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.accounts_136.Accounts_Retrieval_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Account Retrieval Test for Multiple Recurring Consents Service
 */
class GlobalRecurringConsentValidationTests extends AbstractAccountsFlow {

    String oldConsentId
    String newConsentId
    String userAccessTokenForOldConsent
    boolean isMultipleConsentServiceSupported

    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.initiationPayloadForAllPsd2

    @Test(groups = ["1.3.6"])
    void "OB-1672_Retrieval Request to get the Account List with multiple recurring global consents"() {

        //Consent Initiation - 1st time
        doDefaultInitiation(consentPath, initiationPayload)
        oldConsentId = accountId

        //Consent Authorization - 1st time
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        getUserAccessToken(code.toString())
        userAccessTokenForOldConsent = userAccessToken

        //to check if multiple recurring consent support is enabled or not
        isMultipleConsentServiceSupported = consentResponse.
                getHeader("ASPSP-Multiple-Consent-Support").contains("true")

        //Retrieve Account Details - 1st time
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(response.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))

        //Consent Initiation - 2nd Time
        doDefaultInitiation(consentPath, initiationPayload)
        newConsentId = accountId

        //Consent Authorization - 2nd Time
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Retrieve Account Details After New Consent Authorisation
        def response2 = BerlinRequestBuilder
                .buildBasicRequest(userAccessTokenForOldConsent)
                .header(BerlinConstants.CONSENT_ID_HEADER, oldConsentId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        if (isMultipleConsentServiceSupported) {
            Assert.assertEquals(response2.statusCode(), BerlinConstants.STATUS_CODE_200)
            Assert.assertNotNull(response2.jsonPath().getJsonObject("accounts"))
        } else {
            Assert.assertEquals(response2.getStatusCode(), BerlinConstants.STATUS_CODE_401)
            Assert.assertEquals(TestUtil.parseResponseBody(response2, BerlinConstants.TPPMESSAGE_CODE).toString(),
                    BerlinConstants.CONSENT_EXPIRED)
            Assert.assertTrue(TestUtil.parseResponseBody(response2, BerlinConstants.TPPMESSAGE_TEXT).toString().
                    contains("The consent is expired"))
        }
    }
}
