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
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import org.testng.Assert
import org.testng.annotations.Factory
import org.testng.annotations.Test

/**
 * V1.3.0 Accounts With All Available Accounts Tests.
 */
class AllAvailableAccountsEndToEndFlowTest extends AbstractAccountsFlow {

    Map<String, String> map
    String consentPath
    String initiationPayload

    /**
     * Data Factory which Retrieve Consent Path and Payload of All Available Accounts from Data Provider
     * @param maps
     */
    @Factory(dataProvider = "AllAvailableAccounts", dataProviderClass = AccountsDataProviders.class)
    AllAvailableAccountsEndToEndFlowTest(Map<String, String> maps) {
        this.map = maps

        consentPath = map.get("consentPath")
        initiationPayload = map.get("initiationPayload")
    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void "TC0205001_Create Consent Request on Account List of All Accounts"() {

        //Do Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))

        doStatusRetrieval(consentPath)

        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(accountId)
    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "TC0205001_Create Consent Request on Account List of All Accounts")
    void "TC0206001_Authorize Consent of All Accounts"() {

        doAuthorizationFlow()

        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        doStatusRetrieval(consentPath)

        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
        Assert.assertNotNull(accountId)
    }

    @Test(groups = ["1.3.3", "1.3.6"], dependsOnMethods = "TC0206001_Authorize Consent of All Accounts")
    void "Generate User Access Token for Consent with Account List of All Accounts"() {

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
    }

    @Test (groups = ["1.3.3", "1.3.6"],
            dependsOnMethods = "Generate User Access Token for Consent with Account List of All Accounts")
    void "TC0209013_Account Retrieval Request from a Consent with Account List of All Accounts"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertNotNull(response.jsonPath().getJsonObject("com.wso2.openbanking.toolkit.berlin.integration.test.accounts"))
    }

    @Test (groups = ["1.3.3", "1.3.6"],
            dependsOnMethods = "Generate User Access Token for Consent with Account List of All Accounts")
    void "TC0210013_Retrieve Specific Account from a Consent with Account List of All Accounts"(){
        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

        Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
    }

    @Test (groups = ["1.3.3", "1.3.6"],
            dependsOnMethods = "Generate User Access Token for Consent with Account List of All Accounts")
    void "TC0210016_Retrieve Specific Account with Balances from a Consent with Account List of All Accounts"(){

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, accountId)
                .queryParam("withBalance")
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

        Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("account._links.balances"))
    }
}
