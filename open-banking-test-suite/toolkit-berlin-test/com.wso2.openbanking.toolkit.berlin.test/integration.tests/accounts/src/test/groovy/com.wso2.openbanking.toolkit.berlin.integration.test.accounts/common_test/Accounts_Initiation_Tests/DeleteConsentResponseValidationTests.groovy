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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Response Validation Tests on Delete Consent Request.
 */
class DeleteConsentResponseValidationTests extends AbstractAccountsFlow {

    def consentPath = AccountsConstants.CONSENT_PATH
    def initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void "TC0207001_Delete the Consent with Valid AccountId"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete Consent
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))

        //Check Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207002_Delete Consent With Empty Consent Id"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete Consent
        def consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}/")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207003_Delete Consent Without Consent Id Parameter"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete Consent
        def consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207004_Delete Already Terminated Consent"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete Consent
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))

        //Check Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)

        //Delete Consent which is already terminated
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE)
                .toString(), BerlinConstants.CONSENT_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "The requested consent is already deleted")
    }
}
