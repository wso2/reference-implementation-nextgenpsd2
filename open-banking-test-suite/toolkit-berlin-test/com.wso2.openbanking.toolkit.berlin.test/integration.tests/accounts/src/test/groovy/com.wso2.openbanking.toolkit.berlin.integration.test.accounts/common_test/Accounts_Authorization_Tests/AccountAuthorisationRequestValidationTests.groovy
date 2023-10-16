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

import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod
import com.wso2.openbanking.berlin.common.utils.AuthAutomationSteps
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.berlin.common.utils.OAuthAuthorizationRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.WaitForRedirectAutomationStep
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Authorization Request Validation Tests of the Account Flow.
 */
class AccountAuthorisationRequestValidationTests extends AbstractAccountsFlow {

    def consentPath = AccountsConstants.CONSENT_PATH
    def initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1421_Send the Authorisation Request without response_type attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutResponseType(scopes, accountId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "Missing response_type parameter value")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1416_Send Authorisation request with client id not bound to the consent"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
                UUID.randomUUID().toString())

        new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .addStep {driver, context ->
                    WebElement lblErrorResponse = driver.findElement(By.xpath(BerlinConstants.LBL_AUTH_PAGE_CLIENT_INVALID_ERROR_200))
                    Assert.assertTrue(lblErrorResponse.getText().trim().contains("Cannot find an application associated " +
                            "with the given consumer key"))
                }
                .execute()
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0202003_Send the Authorisation Request without scope attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutScope()
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "Scopes are not present or invalid")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1418_Send Authorisation request with incorrect consent append to the scope"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(BerlinConstants.SCOPES.PAYMENTS,
                accountId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "The provided consent Id mismatches with the scope type (\"ais, pis, piis\")")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0202005_Send the Authorisation Request with invalid accountId in the scope parameter"() {

        def accountId = "1234"

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Matching consent not found for provided Id")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0202006_Send the Authorisation Request without accountId in the scope parameter"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, " ")
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Error while retrieving consent data. No consent Id provided with scope")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0202007_Send the Authorisation Request without state attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutState(scopes, accountId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "'state' parameter is required")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0202008_Send the Authorisation Request without redirect_uri attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutRedirectionURI(scopes, accountId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = URLDecoder.decode(authUrl.split("&")[1].split("=")[1].toString(),"UTF8")

        Assert.assertEquals(oauthErrorCode, "Redirect URI is not present in the authorization request")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1419_Send the Authorisation Request without code_challenge attribute"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutCodeChallenge(scopes, accountId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "PKCE is mandatory for this application. PKCE " +
                "Challenge is not provided or is not upto RFC 7636 specification.")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1420_Send Authorisation request with incorrect code_challenge param"() {

        CodeChallengeMethod codeChallengeMethod = new CodeChallengeMethod("RS256")

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        try {
            OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
                    AppConfigReader.getClientId(), "code", codeChallengeMethod)

        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
        }
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0202015_Authorize a Consent in terminatedByTpp Status"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete the created Consent
        deleteConsent(consentPath)

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, accountId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .execute()

        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")

        Assert.assertEquals(oauthErrorCode,"The consent is not in an applicable status for authorization")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0202016_Authorize the consent by a different PSU when the PSU-ID is defined in the initiation request"() {

        def xRequestId = UUID.randomUUID().toString()

        //Consent Initiation
        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(BerlinConstants.PSU_ID, BerlinConstants.PSU_EMAIL_ID)
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        def consentId = TestUtil.parseResponseBody(response, "consentId")

        //Do Authorization
        auth = new BerlinOAuthAuthorization(scopes, consentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                            "The logged in user does not match with the user who initiated the consent")
                }
                .execute()

        //Verify the Error
        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")
        Assert.assertEquals(oauthErrorCode, "The logged in user does not match with the user who initiated the consent")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0202017_Authorize the consent by a different PSU when the PSU-ID is not defined in the initiation request"() {

        def xRequestId = UUID.randomUUID().toString()

        //Consent Initiation
        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        accountId = TestUtil.parseResponseBody(response, "consentId")

        //Do Authorization
        doAuthorizationFlow()

        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1613_PSU authentication on cancelled account consent"() {

        //Account Initiation Request
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Delete Created account consent
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

        //Authorize the cancelled consent
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId.toString())
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "The consent is not in an applicable status for authorization")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1409_Implicit Authorisation when ExplicitAuthorisationPreferred param is not set in initiation"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

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

        accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1411_Implicit Authorisation when TPP-Redirect Preferred not set in initiation request"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test(groups = ["1.3.6"])
    void "OB-1415_Send Authorisation request without client id param"() {

        //Consent Initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .body(initiationPayload)
                .post(consentPath)

        accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
                UUID.randomUUID().toString())
        new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .addStep { driver, context ->
                    WebElement lblErrorResponse = driver.findElement(By.xpath(BerlinConstants.LBL_AUTH_PAGE_CLIENT_INVALID_ERROR_200))
                    Assert.assertTrue(lblErrorResponse.getText().trim().contains("Cannot find an application associated " +
                            "with the given consumer key"))
                }
                .execute()
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

        accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Do Implicit Authorisation
        auth = new BerlinOAuthAuthorization(scopes, accountId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                            "The logged in user does not match with the user who initiated the consent")
                }
                .execute()

        //Verify the Error
        def oauthErrorCode = URLDecoder.decode(automation.currentUrl.get().split("&")[1].split("=")[1].toString(),
                "UTF8")
        Assert.assertEquals(oauthErrorCode, "The logged in user does not match with the user who initiated the consent")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1530_Authorisation when PSU_ID not define in initiation when TPP-ExplicitAuthorisationPreferred set false"() {

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

        accountId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Do Implicit Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1422_Send Authorisation request with incorrect response_type param"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
                AppConfigReader.getClientId(), "id_token&")
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "Invalid response_type parameter value")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1425_Send the Authorisation Request with unsupported code_challenge_method value"() {

        CodeChallengeMethod codeChallengeMethod = new CodeChallengeMethod("RS256")

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        try {
            OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
                    AppConfigReader.getClientId(), "code", codeChallengeMethod)

        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
        }
    }

    @Test (groups = ["1.3.6"])
    void "OB-1424_Send Authorisation request with plain value as the code_challenge_method"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId,
                AppConfigReader.getClientId(), "code", CodeChallengeMethod.PLAIN)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def code = BerlinTestUtil.getCodeFromURL(authUrl)
        Assert.assertNotNull(authUrl.contains("state"))
        Assert.assertNotNull(code)
    }

    @Test(groups = ["1.3.6"],
            dataProvider = "BankOfferedConsentData", dataProviderClass = AccountsDataProviders.class)
    void "OB-1659_Reject bank offered consent authorisation without selecting accounts"(String title, List<String>
            fields, String payload) {

        //Consent Initiation
        doDefaultInitiation(consentPath, payload)

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, accountId)

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_DENY_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get()).split("=")[0]
                .replace("+", " ")
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertEquals(code, "User denied the consent")

        doConsentRetrieval(consentPath)

        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
    }

    @Test(groups = ["1.3.6"],
            dataProvider = "BankOfferedConsentData", dataProviderClass = AccountsDataProviders.class)
    void "OB-1660_Reject bank offered consent authorisation by selecting accounts"(String title, List<String>
            fields, String payload) {

        //Consent Initiation
        doDefaultInitiation(consentPath, payload)

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, accountId)

        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    fields.forEach{ value ->
                        driver.findElement(By.xpath(value)).click()
                    }
                    driver.findElement(By.xpath(BerlinConstants.ACCOUNTS_DENY_XPATH)).click()
                }
                .addStep(new WaitForRedirectAutomationStep())
                .execute()

        code = BerlinTestUtil.getCodeFromURL(automation.currentUrl.get()).split("=")[0]
                .replace("+", " ")
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertEquals(code, "User denied the consent")

        doConsentRetrieval(consentPath)

        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
    }

    @Test
    void "OB-1665_Generate user access token without PKCE code verifier"() {

        //Consent Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertNotNull(accountId)

        //Do Authorization
        doAuthorizationFlow()
        Assert.assertNotNull(code)

       //Get User Access Token
        def userAccessTokenResponse = BerlinRequestBuilder.getUserTokenWithoutCodeVerifier(BerlinConstants
                .AUTH_METHOD.PRIVATE_KEY_JWT, scopes, code.toString())

        Assert.assertEquals(userAccessTokenResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(userAccessTokenResponse, "error_description"),
                "No PKCE code verifier found.PKCE is mandatory for this oAuth 2.0 application.")
    }
}
