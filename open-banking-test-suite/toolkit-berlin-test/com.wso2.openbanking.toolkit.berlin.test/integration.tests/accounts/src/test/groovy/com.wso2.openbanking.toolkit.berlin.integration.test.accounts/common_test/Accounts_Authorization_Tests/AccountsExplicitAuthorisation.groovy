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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Authorization_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Account Explicit Authorisation Flow Tests
 */
class AccountsExplicitAuthorisation extends AbstractAccountsFlow{

    final BerlinConstants.SCOPES scopes = BerlinConstants.SCOPES.PAYMENTS

    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"])
    void "Create Account Consent for Explicit Authorization"() {

        doExplicitAuthInitiation(consentPath, initiationPayload)

        Assert.assertNotNull(accountId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisation"))
    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Create Account Consent for Explicit Authorization"])
    void "Create Explicit Authorisation"() {

        createExplicitAuthorization(consentPath)

        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Create Explicit Authorisation"])
    void "Check Consent Authorisation Sub-Resources Request Ids"() {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken, requestId)
                .body("{}")
                .post("${consentPath}/${accountId}/authorisations")

        Assert.assertEquals(authorisationId, authorisationResponse.jsonPath().get("authorisationId"))

    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Check Consent Authorisation Sub-Resources Request Ids"])
    void "Check the Authorisation status"() {

        getAuthorizationStatus(consentPath)

        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)

    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Check the Authorisation status"])
    void "Authenticate PSU on SCA Flow"() {

        doAuthorizationFlow()
        Assert.assertNotNull(code)

        // Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check the Authorisation status
        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_PSUAUTHENTICATED)
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Authenticate PSU on SCA Flow"])
    void "Validate the Authorisation IDs List"() {

        def authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${accountId}/authorisations/")

        def authorisationsList = authorisationResponse.jsonPath().getList("authorisationIds")

        Assert.assertTrue(authorisationsList.contains(authorisationId))
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], dependsOnMethods = ["Validate the Authorisation IDs List"])
    void "Create authorisation after settlement"() {

        createExplicitAuthorization(consentPath)

        Assert.assertNull(authorisationId)
    }
}
