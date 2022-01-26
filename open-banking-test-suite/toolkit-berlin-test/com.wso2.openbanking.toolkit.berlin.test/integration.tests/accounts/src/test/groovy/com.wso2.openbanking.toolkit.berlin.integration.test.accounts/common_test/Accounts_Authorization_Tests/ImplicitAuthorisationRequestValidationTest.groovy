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
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.berlin.common.utils.OAuthAuthorizationRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Implicit Authorisation Flow.
 */
class ImplicitAuthorisationRequestValidationTest extends AbstractAccountsFlow {

    // Static parameters
    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.AllAccessBankOfferedConsentPayload
    String psuId = "${ConfigParser.getInstance().getPSU()}@${ConfigParser.getInstance().getTenantDomain()}"

    @Test (groups = ["1.3.3", "1.3.6"])
    void "PSU authentication on cancelled account consent"() {

        //Account Initiation Request
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Delete Created account consent
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

        //Authorize the cancelled consent
        def authAuthorization = new BerlinOAuthAuthorization(scopes, accountId)
        new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authAuthorization.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertTrue(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim()
                            .equalsIgnoreCase("Current Consent State is not valid for authorisation"))
                }
                .execute()
    }

    @Test (groups = ["1.3.6"])
    void "OB-1409_Implicit Authorisation when ExplicitAuthorisationPreferred param is not set in initiation"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "consentId"))

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1410_Implicit Authorisation when ExplicitAuthorisationPreferred param set to false"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "consentId"))

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1529_Authorisation with undefined PSU_ID when TPP-ExplicitAuthorisationPreferred set to false"() {

        String psuId = "psu1@wso2.com"

        //Consent Initiation
        Response consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, psuId)
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "consentId"))

        //Do Implicit Authorisation
        doAuthorizationFlow()
        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)
        Assert.assertEquals(oauthErrorCode, "invalid_request, User is not similar to the logged in user.")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1530_Authorisation when PSU_ID not define in initiation when TPP-ExplicitAuthorisationPreferred set false"() {

        String psuId = "psu1@wso2.com"

        //Consent Initiation
        Response consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "consentId"))

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }
}
