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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.common_test.Payments_Authorization_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Factory
import org.testng.annotations.Test

/**
 * Tests Explicit Authorisation API with Berlin Payments.
 */
class ExplicitAuthorisationTest extends AbstractPaymentsFlow {

    Map<String, String> map
    String consentPath
    String initiationPayload

    /**
     * Data Factory to Retrieve Explicit Authorization Data from the Data Provider
     * @param maps
     */
    @Factory(dataProvider = "ExplicitAuthorizationData", dataProviderClass = PaymentsDataProviders.class)
    ExplicitAuthorisationTest(Map<String, String> maps) {
        this.map = maps

        consentPath = map.get("consentPath")
        initiationPayload = map.get("initiationPayload")
    }

    final BerlinConstants.SCOPES scopes = BerlinConstants.SCOPES.PAYMENTS

    @Test (groups = ["SmokeTest", "1.3.6"])
    void "Create Payment Consent for Explicit Authorization"() {

        doExplicitAuthInitiation(consentPath, initiationPayload)

        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))
    }

    @Test (groups = ["SmokeTest", "1.3.6"],
            dependsOnMethods = ["Create Payment Consent for Explicit Authorization"])
    void "Create Explicit Authorisation"() {

        createExplicitAuthorization(consentPath)

        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Check the Authorisation status
        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)
    }

    @Test(groups = ["SmokeTest", "1.3.6"], dependsOnMethods = ["Create Explicit Authorisation"])
    void "Check Idempotent Scenario Authorisation"() {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken, requestId)
                .body("{}")
                .post("${consentPath}/${paymentId}/authorisations")

        Assert.assertEquals(authorisationId, authorisationResponse.jsonPath().get("authorisationId"))
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)

    }

    @Test(groups = ["SmokeTest", "1.3.6"], dependsOnMethods = ["Check Idempotent Scenario Authorisation"])
    void "Authenticate PSU on SCA Flow"() {

        doAuthorizationFlow()
        Assert.assertNotNull(code)

        // Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check the Authorisation status
        getAuthorizationStatus(consentPath)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)
    }

    @Test(groups = ["SmokeTest", "1.3.6"], dependsOnMethods = ["Validate the Authorisation IDs List"])
    void "Create authorisation after settlement"() {

        createExplicitAuthorization(consentPath)

        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)
    }

    @Test(groups = ["SmokeTest", "1.3.6"], dependsOnMethods = ["Authenticate PSU on SCA Flow"])
    void "Validate the Authorisation IDs List"() {

        def authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}/authorisations")

        def authorisationsList = authorisationResponse.jsonPath().getList("authorisationIds")

        Assert.assertTrue(authorisationsList.contains(authorisationId))
    }

    @Test(groups = ["1.3.6"], priority = 1)
    void "OB-1487_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true"() {

        //Consent Initiation
        doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources
        createExplicitAuthorization(consentPath)

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Do Authorisation
        doAuthorizationFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Check Consent Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
    }

    @Test(groups = ["1.3.6"], priority = 2,
            dependsOnMethods = "OB-1487_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true")
    void "OB-1492_Get list of all authorisation sub-resource IDs"() {

        getExplicitAuthResources(consentPath)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(authorisationId, authorisationResponse.jsonPath().get("authorisationIds[0]"))
    }

    @Test(groups = ["1.3.6"], priority = 3,
            dependsOnMethods = "OB-1487_Explicit Authorisation when ExplicitAuthorisationPreferred param set to true")
    void "OB-1494_Get SCA status of consent authorisation sub-resource"() {

        getExplicitAuthResourceStatus(consentPath)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(authorisationResponse.jsonPath().get(BerlinConstants.SCA_STATUS_PARAM),
                PaymentsConstants.SCA_STATUS_PSU_AUTHENTICATED)
    }

    @Test(groups = ["1.3.6"], priority = 4)
    void "OB-1488_Consent Initiation when TPP-Redirect Preferred not set in initiation request"() {

        //Consent Initiation
        doDefaultInitiationWithoutRedirectPreffered(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test(groups = ["1.3.6"], priority = 5)
    void "OB-1489_Explicit Authorisation when TPP-Redirect Preferred not set in initiation request"() {

        //Consent Initiation
        doExplicitAuthInitiation(consentPath, initiationPayload)

        //Explicit Authorisation
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .body("{}")
                .post("${consentPath}/${paymentId}/authorisations")

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test(groups = ["1.3.6"], priority = 6)
    void "OB-1490_Explicit Authorisation when PSU reject the consent"() {

        //Consent Initiation
        doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources
        createExplicitAuthorization(consentPath)
        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
                PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Do Authorisation
        doConsentDenyFlow()
        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertEquals(code, "User denied the consent")
    }

    @Test(groups = ["1.3.6"], priority = 7)
    void "OB-1491_Create consent authorisation resource for incorrect consent id"() {

        //Consent Initiation
        doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources
        paymentId = "12345"
        createExplicitAuthorization(consentPath)

        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_UNKNOWN)
    }

    @Test(groups = ["1.3.6"], priority = 8)
    void "OB-1493_Send Get list of all authorisation sub-resource request with invalid consent id"() {

        def paymentId = "1234"

        getExplicitAuthResources(consentPath, paymentId)
        Assert.assertEquals(authorisationResponse.statusCode(), BerlinConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(authorisationResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_UNKNOWN)
    }


    @Test(groups = ["1.3.6"], priority = 1)
    void "OB-360_Explicit Authorisation with same x-request-id and same consent id"() {

        //Consent Initiation
        doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources
        createExplicitAuthorization(consentPath)

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
          PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Create Explicit Authorisation Resources 2
        def  authorisationResponse2 = TestSuite.buildRequest()
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
          .post("${consentPath}/${paymentId}/authorisations")

        def authorisationId2 = authorisationResponse2.jsonPath().get("authorisationId")
        def requestId2 = authorisationResponse2.getHeader(BerlinConstants.X_REQUEST_ID)
        Assert.assertEquals(requestId, requestId2)
        Assert.assertEquals(authorisationId, authorisationId2)
        Assert.assertEquals(authorisationResponse2.jsonPath().get("scaStatus"),
          PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse2.jsonPath().get("_links.scaOAuth.href"))
    }

    @Test(groups = ["1.3.6"], priority = 1)
    void "OB-361_Explicit Authorisation with same x-request-id and different consent id"() {

        //Consent Initiation
        doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(consentResponse.jsonPath().get("_links.startAuthorisationWithPsuIdentification"))

        //Create Explicit Authorisation Resources
        createExplicitAuthorization(consentPath)

        authorisationId = authorisationResponse.jsonPath().get("authorisationId")
        requestId = authorisationResponse.getHeader(BerlinConstants.X_REQUEST_ID)
        Assert.assertNotNull(requestId)
        Assert.assertNotNull(authorisationId)
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"),
          PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.scaOAuth.href"))

        //Consent Initiation 2
        doInitiationWithExplicitAuthPreferred(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(paymentId)

        //Create Explicit Authorisation Resources 2
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
          .post("${consentPath}/${paymentId}/authorisations")

        def authorisationId2 = authorisationResponse2.jsonPath().get("authorisationId")
        def requestId2 = authorisationResponse2.getHeader(BerlinConstants.X_REQUEST_ID)
        Assert.assertEquals(requestId, requestId2)
        Assert.assertNotEquals(authorisationId, authorisationId2)
        Assert.assertEquals(authorisationResponse2.jsonPath().get("scaStatus"),
          PaymentsConstants.SCA_STATUS_RECEIVED)
        Assert.assertNotNull(authorisationResponse2.jsonPath().get("_links.scaOAuth.href"))
    }

}
