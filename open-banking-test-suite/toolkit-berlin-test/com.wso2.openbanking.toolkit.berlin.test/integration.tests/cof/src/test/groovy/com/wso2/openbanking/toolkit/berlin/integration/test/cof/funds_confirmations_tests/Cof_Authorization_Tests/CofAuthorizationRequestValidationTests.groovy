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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Authorization_Tests

import com.wso2.openbanking.berlin.common.utils.AuthAutomationSteps
import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.berlin.common.utils.OAuthAuthorizationRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import io.restassured.http.ContentType
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Authorization Request Validation Tests of the Confirmation of Funds Flow.
 */
class CofAuthorizationRequestValidationTests extends AbstractCofFlow {

    def consentPath = CofConstants.COF_CONSENT_PATH
    def initiationPayload = CofInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["1.3.6"])
    void "TC0602002_Send the Authorisation Request without response_type attribute"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutResponseType(scopes, consentId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "invalid_request, Missing response_type parameter value")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602003_Send the Authorisation Request with invalid client_id value"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithInvalidClientId(scopes, consentId)

        new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new AuthAutomationSteps(request.toURI().toString()))
                .addStep {driver, context -> WebElement lblErrorResponse = driver.findElement(By.xpath
                        (BerlinConstants.LBL_AUTH_PAGE_CLIENT_INVALID_ERROR_200))
                    Assert.assertTrue(lblErrorResponse.getText().trim().contains("Cannot find an application " +
                            "associated with the given consumer key"))
                }
                .execute()
    }

    @Test (groups = ["1.3.6"])
    void "TC0602004_Send the Authorisation Request without scope attribute"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutScope()
        consentAuthorizeErrorFlowToValidateScopes(request)

        Assert.assertEquals(oauthErrorCode, "Scopes are not present or invalid")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602005_Send the Authorisation Request with unsupported scope value"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedScope(BerlinConstants.SCOPES.PAYMENTS,
                consentId)
        consentAuthorizeErrorFlowToValidateScopes(request)

        Assert.assertEquals(oauthErrorCode, "Requested consent not found for this TPP-Unique-ID")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602006_Send the Authorisation Request with invalid consentId in the scope parameter"() {

        def consentId = "1234"

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedScope(scopes, consentId)
        consentAuthorizeErrorFlowToValidateScopes(request)

        Assert.assertEquals(oauthErrorCode, "Requested consent not found for this TPP-Unique-ID")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602007_Send the Authorisation Request without consentId in the scope parameter"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedScope(scopes, " ")
        consentAuthorizeErrorFlowToValidateScopes(request)

        Assert.assertEquals(oauthErrorCode, "Error while retrieving funds confirmation data. No consent ID " +
                "provided with scope.")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602008_Send the Authorisation Request without state attribute"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutState(scopes, consentId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "invalid_request, 'state' parameter is required")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602009_Send the Authorisation Request without redirect_uri attribute"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutRedirectionURI(scopes, consentId)
        consentAuthorizeErrorFlow(request)

        def oauthErrorCode = TestUtil.getDecodedUrl(automation.currentUrl.get())

        Assert.assertEquals(oauthErrorCode, "Redirect URI is not present in the authorization request")
    }

    @Test (groups = ["1.3.6"])
    void "TC060210_Send the Authorisation Request without code_challenge attribute"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutCodeChallenge(scopes, consentId)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "PKCE is mandatory for this application. PKCE " +
                "Challenge is not provided or is not upto RFC 7636 specification.")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602011_Send the Authorisation Request with unsupported code_challenge value"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        try {
            OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedCodeChallengeMethod(scopes, consentId)

        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
        }
    }

    @Test(groups = ["1.3.6"])
    void "TC0602012_Send the Authorisation Request without code_challenge_method value"() {
        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        try {
            OAuthAuthorizationRequestBuilder.OAuthRequestWithUnsupportedCodeChallengeMethod(scopes, consentId)

        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
        }
    }

    @Test (groups = ["1.3.6"])
    void "TC0602015_Authorize a Consent in terminatedByTpp Status"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Delete the created Consent
        deleteCofConsent(consentPath)

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, consentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                            "Current Consent State is not valid for authorisation")
                }
                .execute()

        def oauthErrorCode = TestUtil.getDecodedUrl(automation.currentUrl.get())

        Assert.assertEquals(oauthErrorCode,"Current Consent State is not valid for authorisation")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602016_Authorize the consent by a different PSU when the PSU-ID is defined in the initiation request"() {

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
                .post(consentPath)

        def consentId = TestUtil.parseResponseBody(response, "consentId")

        //Do Authorization
        auth = new BerlinOAuthAuthorization(scopes, consentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim(),
                            "PSU-ID of the consent does not match with the logged in user")
                }
                .execute()

        //Verify the Error
        def oauthErrorCode = TestUtil.getDecodedUrl(automation.currentUrl.get())
        Assert.assertEquals(oauthErrorCode, "PSU-ID of the consent does not match with the logged in user")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602017_Authorize the consent by a different PSU when the PSU-ID is not defined in the initiation request"() {

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
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(response, "consentId")

        //Do Authorization
        doCofAuthorizationFlow()

        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test(groups = "1.3.6")
    void "TC0602018_Authorize a Consent which contains a different account ID"() {

        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(CofInitiationPayloads.initiationPayloadWithDifferentAccountId)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")

        //Consent Authorization
        auth = new BerlinOAuthAuthorization(scopes, consentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertEquals(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText()
                            .trim(), "Invalid debtor account in consent")
                }
                .execute()
    }
}
