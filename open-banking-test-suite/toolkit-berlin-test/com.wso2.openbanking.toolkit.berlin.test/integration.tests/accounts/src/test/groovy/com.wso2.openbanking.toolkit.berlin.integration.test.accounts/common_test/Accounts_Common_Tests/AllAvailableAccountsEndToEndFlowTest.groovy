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
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
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

        Assert.assertNotNull(response.jsonPath().getJsonObject("accounts"))
    }

    @Test (groups = ["1.3.3", "1.3.6"], priority = 1)
    void "TC0210013_Retrieve Specific Account from a Consent with Account List of All Accounts"(){

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
    void "TC0210016_Retrieve Specific Account with Balances from a Consent with Account List of All Accounts"(){

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
                .queryParam("withBalance", true)
                .get(AccountsConstants.SPECIFIC_ACCOUNT_PATH)

        Assert.assertNotNull(response.jsonPath().getJsonObject("account"))
        Assert.assertNotNull(response.jsonPath().getJsonObject("account._links.balances"))
    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 1)
    void "OB-1661_Reject All Accounts Consent"() {

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

    @Test (groups = ["1.3.6"])
    void "BG-536_AllAvailableAccounts Initiation request with same x-request-id and same payload"() {

        def xRequestId = UUID.randomUUID().toString()
        def date = getCurrentDate()

        //Consent Initiation - First Time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId1 = TestUtil.parseResponseBody(consentResponse, "consentId").toString()

        //Consent Initiation - Second Time
        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId2 = TestUtil.parseResponseBody(consentResponse2, "consentId").toString()

        Assert.assertEquals(consentId1, consentId2)
    }

    @Test (groups = ["1.3.6"])
    void "BG-537_AllAvailableAccounts Initiation request with same x-request-id and different payload"() {

        def xRequestId = UUID.randomUUID().toString()

        //Consent Initiation - First Time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Consent Initiation - Second Time
        def initiationPayload2 = AccountsInitiationPayloads.initiationPayloadForAllAccountsWithoutRecurringIndicator

        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload2)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse2, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse2, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Payloads are not similar. Hence this is not a valid idempotent request"))
    }

    @Test (groups = ["1.3.6"])
    void "BG-538_AllAvailableAccounts Initiation request with different x-request-id and same payload"() {

        //Consent Initiation - First Time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId1 = TestUtil.parseResponseBody(consentResponse, "consentId").toString()

        //Consent Initiation - Second Time
        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId2 = TestUtil.parseResponseBody(consentResponse2, "consentId").toString()

        Assert.assertNotEquals(consentId1, consentId2)
    }
}
