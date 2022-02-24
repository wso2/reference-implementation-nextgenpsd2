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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Common_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * V1.3.0 Accounts With All Available Accounts Tests.
 */
class GlobalConsentEndToEndFlowTest extends AbstractAccountsFlow {

    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.initiationPayloadForAllPsd2

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void "TC0205003_Create a Global Consent"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))

        //Retrieve Consent Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = "TC0205003_Create a Global Consent")
    void "TC0206003_Authorize the Global Consent"() {

        //Authorize the Consent
        doAuthorizationFlow()

        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        //Retrieve Consent Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
    }

    @Test(groups = ["1.3.3", "1.3.6"], dependsOnMethods = "TC0206003_Authorize the Global Consent")
    void "Generate User Access Token for Global Consent"() {

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
    }

    @Test (groups = ["1.3.3", "1.3.6"], dependsOnMethods = "Generate User Access Token for Global Consent")
    void "TC0209015_Retrieve Bulk Accounts Request from Global Consent"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "TC0210015_Retrieve Specific Account from Global Consent"() {

        //Do Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

        Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "TC0210018_Retrieve Specific Account with Balances from Global Consent"() {

        //Do Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("withBalance")
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

        Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("account._links.balances"))
    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 1)
    void "OB-1662_Reject Global Consent"() {

        //Do Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        doConsentDenyFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertEquals(code, "User denied the consent")

        doStatusRetrieval(consentPath)

        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
    }
}
