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
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE)
                .toString(), BerlinConstants.INVALID_STATUS_VALUE)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "The requested consent is already deleted")
    }
}
