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
import com.wso2.openbanking.berlin.common.utils.OAuthAuthorizationRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Account Explicit Authorisation Flow Tests
 */
class AccountsExplicitAuthorisation extends AbstractAccountsFlow{

    final BerlinConstants.SCOPES scopes = BerlinConstants.SCOPES.ACCOUNTS

    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["SmokeTest", "1.3.6"])
    void "Create Account Consent for Explicit Authorization"() {

        doExplicitAuthInitiation(consentPath, initiationPayload)

        Assert.assertNotNull(accountId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))
    }

    @Test (groups = ["SmokeTest", "1.3.6"], dependsOnMethods = ["Create Account Consent for Explicit Authorization"])
    void "BG-308_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true"() {

        createExplicitAuthorization(consentPath)

        //TODO: Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/7187
        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test(groups = ["SmokeTest", "1.3.6"],
            dependsOnMethods = ["BG-308_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true"])
    void "OB-1457_Get SCA status of consent authorisation sub-resource"() {

        getAuthorizationStatus(consentPath)

        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
    }

    @Test(groups = ["SmokeTest", "1.3.6"],
            dependsOnMethods = ["OB-1457_Get SCA status of consent authorisation sub-resource"])
    void "Authenticate PSU on SCA Flow"() {

        doAuthorizationFlow()
        Assert.assertNotNull(code)

        // Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check the Authorisation status
        getAuthorizationStatus(consentPath)
        String scaStatus = authorisationResponse.jsonPath().get(BerlinConstants.SCA_STATUS_PARAM)
        Assert.assertEquals(scaStatus, AccountsConstants.CONSENT_STATUS_PSUAUTHENTICATED)
    }

    @Test(groups = ["SmokeTest", "1.3.6"], dependsOnMethods = ["Authenticate PSU on SCA Flow"])
    void "BG-313_Get list of all authorisation sub-resource IDs"() {

        def authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${accountId}/authorisations")

        def authorisationsList = authorisationResponse.jsonPath().getList("authorisationIds")

        Assert.assertTrue(authorisationsList.contains(authorisationId))
    }

    @Test(groups = ["SmokeTest", "1.3.6"],
            dependsOnMethods = ["BG-313_Get list of all authorisation sub-resource IDs"])
    void "Create authorisation after settlement"() {

        createExplicitAuthorization(consentPath, requestId)

        Assert.assertNotNull(authorisationId)

        //Consent Authorization
        def request = OAuthAuthorizationRequestBuilder.OAuthRequestWithConfigurableParams(scopes, accountId)
        consentAuthorizeErrorFlowValidation(request)

        Assert.assertEquals(oauthErrorCode,"The consent is not in an applicable status for authorization")
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dependsOnMethods = "Create authorisation after settlement")
    void "BG-315_Get SCA status of consent authorisation sub-resource"() {

        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_200)

        String scaStatus = authorisationResponse.jsonPath().get(BerlinConstants.SCA_STATUS_PARAM)
        Assert.assertTrue(scaStatus.equalsIgnoreCase(AccountsConstants.CONSENT_STATUS_PSUAUTHENTICATED) ||
                scaStatus.equalsIgnoreCase(AccountsConstants.SCA_STATUS_FINALISED))
    }

    @Test(groups = ["1.3.6"], priority = 1)
    void "BG-309_Consent Initiation when TPP-Redirect Preferred not set in initiation request"() {

        //Consent Initiation
        doDefaultInitiationWithoutRedirectPreffered(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test(groups = ["1.3.6"], priority = 1)
    void "BG-310_Explicit Authorisation when TPP-Redirect Preferred not set in initiation request"() {

        //Consent Initiation
        doExplicitAuthInitiation(consentPath, initiationPayload)

        //Explicit Authorisation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .body("{}")
                .post("${consentPath}/${accountId}/authorisations")

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test(groups = ["1.3.6"], priority = 1)
    void "BG-311_Explicit Authorisation when PSU reject the consent"() {

        //Consent Initiation
        doExplicitAuthInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Create Explicit Authorisation Resources
        createExplicitAuthorization(consentPath)
        authorisationId = authorisationResponse.jsonPath().get("authorisationId")

        //TODO: Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/7187
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Do Authorisation
        doConsentDenyFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertEquals(code, "User denied the consent")

        //Check consent status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_REJECTED)
    }

    @Test(groups = ["1.3.6"], priority = 1)
    void "BG-312_Create consent authorisation resource for incorrect consent id"() {

        //Consent Initiation
        doExplicitAuthInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Create Explicit Authorisation Resources
        accountId = "12345"
        createExplicitAuthorization(consentPath)

        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_UNKNOWN)
    }

    @Test(groups = ["1.3.6"], priority = 1)
    void "BG-314_Send Get list of all authorisation sub-resource request with invalid consent id"() {

        def accountId = "1234"

        getExplicitAuthResources(consentPath, accountId)

        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_UNKNOWN)
    }

    @Test (priority = 3)
    void "BG-627_Authorisation with undefined PSU_ID when TPP-ExplicitAuthorisationPreferred set to true"() {

        //Consent Initiation
        authorisationResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        accountId = TestUtil.parseResponseBody(authorisationResponse, "consentId")

        //Create Authorisation Sub-resource
        createExplicitAuthorization(consentPath)

        //TODO: Issue: https://github.com/wso2-enterprise/financial-open-banking/issues/7187
        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Authorise consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_200)

        String scaStatus = authorisationResponse.jsonPath().get(BerlinConstants.SCA_STATUS_PARAM)
        Assert.assertTrue(scaStatus.equalsIgnoreCase(AccountsConstants.CONSENT_STATUS_PSUAUTHENTICATED) ||
                scaStatus.equalsIgnoreCase(AccountsConstants.SCA_STATUS_FINALISED))
    }

    @Test
    void "BG_545_Explicit Authorisation without x-request-id header"() {

        //Consent Initiation
        doExplicitAuthInitiation(consentPath, initiationPayload)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources without x-request-id header
        def authorisationResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body("{}")
                .post("${consentPath}/${accountId}/authorisations")

        Assert.assertEquals(authorisationResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse2, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse2, BerlinConstants.TPPMESSAGE_TEXT),
                "X-Request-ID header is missing in the request")
    }

    @Test
    void "BG_317_Explicit Authorisation with same x-request-id and same consent id"() {

        //Consent Initiation
        doExplicitAuthInitiation(consentPath, initiationPayload)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources
        createExplicitAuthorization(consentPath)

        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Create Explicit Authorisation Resources with same x-request-id and same consent id
        def authorisationResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, requestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body("{}")
                .post("${consentPath}/${accountId}/authorisations")

        String xRequestId2 = authorisationResponse2.getHeader(BerlinConstants.X_REQUEST_ID)
        String authorisationId2 = authorisationResponse2.jsonPath().get("authorisationId")

        Assert.assertEquals(authorisationResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(xRequestId2, requestId.toString())
        Assert.assertEquals(authorisationId2, authorisationId.toString())
    }

    @Test
    void "BG_318_Explicit Authorisation with same x-request-id and different consent id"() {

        //Consent Initiation
        doExplicitAuthInitiation(consentPath, initiationPayload)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources
        createExplicitAuthorization(consentPath)

        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), AccountsConstants.CONSENT_STATUS_RECEIVED)

        // Create New Consent
        def initiationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(initiationResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        def accountId2 = TestUtil.parseResponseBody(initiationResponse, "consentId")

        //Create Explicit Authorisation Resources with same x-request-id and different consent id
        def authorisationResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, requestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body("{}")
                .post("${consentPath}/${accountId2}/authorisations")

        String xRequestId2 = authorisationResponse2.getHeader(BerlinConstants.X_REQUEST_ID)
        String authorisationId2 = authorisationResponse2.jsonPath().get("authorisationId")

        Assert.assertEquals(requestId, xRequestId2)
        Assert.assertNotEquals(authorisationId, authorisationId2)
        Assert.assertEquals(authorisationResponse2.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse2.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test
    void "BG_546_Explicit Authorisation with different x-request-id and same consent id"() {

        //Consent Initiation
        doExplicitAuthInitiation(consentPath, initiationPayload)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources
        createExplicitAuthorization(consentPath)

        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Create Explicit Authorisation Resources with different x-request-id and same consent id
        def authorisationResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .header(BerlinConstants.TPP_REDIRECT_PREFERRED, true)
                .body("{}")
                .post("${consentPath}/${accountId}/authorisations")

        Assert.assertEquals(authorisationResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse2, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue (TestUtil.parseResponseBody (authorisationResponse2, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Cannot use different unique identifier for the same consent ID when the request does not " +
                        "contain a payload."))
    }
}
