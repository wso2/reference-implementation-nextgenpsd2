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
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
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

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 1)
    void "Create Payment Consent for Explicit Authorization"() {

        doExplicitAuthInitiation(consentPath, initiationPayload)

        Assert.assertNotNull(paymentId)
        Assert.assertNotNull(authorisationResponse.jsonPath().get("_links.startAuthorisation"))
    }

    @Test (groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 2,
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

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 3, dependsOnMethods = ["Create Explicit Authorisation"])
    void "Check Idempotent Scenario Authorisation"() {

        authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken, requestId)
                .body("{}")
                .post("${consentPath}/${paymentId}/authorisations")

        Assert.assertEquals(authorisationId, authorisationResponse.jsonPath().get("authorisationId"))
        Assert.assertEquals(authorisationResponse.jsonPath().get("scaStatus"), PaymentsConstants.SCA_STATUS_RECEIVED)

    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 4, dependsOnMethods = ["Create Explicit Authorisation"])
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

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 5, dependsOnMethods = ["Validate the Authorisation IDs List"])
    void "Create authorisation after settlement"() {

        createExplicitAuthorization(consentPath)

        Assert.assertNull(authorisationId)
    }

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"], priority = 6, dependsOnMethods = ["Authenticate PSU on SCA Flow"])
    void "Validate the Authorisation IDs List"() {

        def authorisationResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/${paymentId}/authorisations/")

        def authorisationsList = authorisationResponse.jsonPath().getList("authorisationIds")

        Assert.assertTrue(authorisationsList.contains(authorisationId))
    }
}
