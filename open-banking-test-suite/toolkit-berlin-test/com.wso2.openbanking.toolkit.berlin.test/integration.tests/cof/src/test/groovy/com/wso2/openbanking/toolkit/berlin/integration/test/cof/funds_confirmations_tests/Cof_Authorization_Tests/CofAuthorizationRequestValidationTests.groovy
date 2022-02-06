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
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import io.restassured.http.ContentType
import io.restassured.response.Response
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
    void "OB-1651_Send Authorisation request without response_type param"() {

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
    void "OB-1646_Send Authorisation request with client id not bound to the consent"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId,
                UUID.randomUUID().toString())

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
    void "OB-1647_Send Authorisation request without scope param"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutScope()
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Scopes are not present or invalid")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602005_Send the Authorisation Request with unsupported scope value"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(BerlinConstants.SCOPES.PAYMENTS,
                consentId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "The provided consent Id mismatches with the scope type " +
                "(\"ais, pis, piis\")")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1648_Send Authorisation request with incorrect consent append to the scope"() {

        def consentId = "1234"

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Matching consent not found for provided Id")
    }

    @Test (groups = ["1.3.6"])
    void "TC0602007_Send the Authorisation Request without consentId in the scope parameter"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, " ")
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "Error while retrieving consent data. No consent Id provided with scope")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1643_Send Authorisation request without state param"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithoutState(scopes, consentId)
        consentAuthorizeErrorFlowValidation(request)

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
    void "OB-1649_Send Authorisation request without code_challenge param"() {

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
    void "OB-1650_Send Authorisation request with incorrect code_challenge param"() {

        CodeChallengeMethod codeChallengeMethod = new CodeChallengeMethod("RS256")

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        try {
            OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId,
                    AppConfigReader.getClientId(), "code", codeChallengeMethod)

        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
        }
    }

    @Test(groups = ["1.3.6"])
    void "TC0602012_Send the Authorisation Request without code_challenge_method value"() {

        CodeChallengeMethod codeChallengeMethod = new CodeChallengeMethod("RS256")

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        try {
            OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId,
                    AppConfigReader.getClientId(), "code", codeChallengeMethod)

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
                .execute()

        def oauthErrorCode = TestUtil.getDecodedUrl(automation.currentUrl.get())

        Assert.assertEquals(oauthErrorCode,"The consent is not in an applicable status for authorization")
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
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        def consentId = TestUtil.parseResponseBody(response, "consentId")

        //Do Authorization
        auth = new BerlinOAuthAuthorization(scopes, consentId)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .execute()

        //Verify the Error
        def oauthErrorCode = TestUtil.getDecodedUrl(automation.currentUrl.get())
        Assert.assertEquals(oauthErrorCode, "The logged in user does not match with the user who initiated the consent")
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
                .baseUri(ConfigParser.getInstance().getBaseURL())
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
                .execute()

        def oauthErrorCode = TestUtil.getDecodedUrl(automation.currentUrl.get())

        Assert.assertEquals(oauthErrorCode,"Provided account references do not exist or not valid")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1658_PSU authentication on cancelled account consent"() {

        //Account Initiation Request
        doDefaultCofInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentId)

        //Delete Created account consent
        deleteCofConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

        //Authorize the cancelled consent
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode, "The consent is not in an applicable status for authorization")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1639_Implicit Authorisation when ExplicitAuthorisationPreferred param is not set in initiation"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentId)

        //Do Implicit Authorisation
        doCofAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1640_Implicit Authorisation when ExplicitAuthorisationPreferred param set to false"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentId)

        //Do Implicit Authorisation
        doCofAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1641_Implicit Authorisation when TPP-Redirect Preferred not set in initiation request"() {

        //Consent Initiation
        Response consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentId)

        //Do Implicit Authorisation
        doCofAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test(groups = ["1.3.6"])
    void "OB-1645_Send Authorisation request without client id param"() {

        //Consent Initiation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, false)
                .body(initiationPayload)
                .post(consentPath)

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentId)

        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId,
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
    void "OB-1656_Authorisation with undefined PSU_ID when TPP-ExplicitAuthorisationPreferred set to false"() {

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

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentId)

        //Do Implicit Authorisation
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

    @Test (groups = ["1.3.6"])
    void "OB-1657_Authorisation when PSU_ID not define in initiation when TPP-ExplicitAuthorisationPreferred set false"() {

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

        consentId = TestUtil.parseResponseBody(consentResponse, "consentId")
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentId)

        //Do Implicit Authorisation
        doCofAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1652_Send Authorisation request with incorrect response_type param"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId,
                AppConfigReader.getClientId(), "id_token&")
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def oauthErrorCode = BerlinTestUtil.getAuthFlowError(authUrl)

        Assert.assertEquals(oauthErrorCode, "invalid_request, Invalid response_type parameter value")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "OB-1655_Send Authorisation request with unsupported code_challenge_method"() {

        CodeChallengeMethod codeChallengeMethod = new CodeChallengeMethod("RS256")

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        try {
            OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId,
                    AppConfigReader.getClientId(), "code", codeChallengeMethod)

        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.message, "Unsupported code challenge method: RS256")
        }
    }

    @Test (groups = ["1.3.6"])
    void "OB-1654_Send Authorisation request with plain value as the code_challenge_method"() {

        //Consent Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Do Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, consentId,
                AppConfigReader.getClientId(), "code", CodeChallengeMethod.PLAIN)
        consentAuthorizeErrorFlow(request)

        String authUrl = automation.currentUrl.get()
        def code = BerlinTestUtil.getCodeFromURL(authUrl)
        Assert.assertNotNull(authUrl.contains("state"))
        Assert.assertNotNull(code)
    }
}
