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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Response Validation Tests on Get Funds Confirmation Consent Request.
 */
class CofGetConsentResponseValidationTests extends AbstractCofFlow {

    def consentPath = CofConstants.COF_CONSENT_PATH
    def initiationPayload = CofInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["SmokeTest", "1.3.6"])
    void "TC0604001_Get the Consent with Valid Consent Id"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("account"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardNumber"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardExpiryDate"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardInformation"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("consentStatus"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0604002_Get Consent With Empty Consent Id"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
    }

    @Test (groups = ["1.3.6"])
    void "TC0604003_Get Consent Without Consent Id Parameter"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
    }

    @Test (groups = ["1.3.6"])
    void "TC0604004_Get Already Terminated Consent"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent
        deleteCofConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_TERMINATED_BY_TPP)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_TERMINATED_BY_TPP)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("account"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardNumber"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardExpiryDate"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardInformation"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("consentStatus"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0604005_Get an Authorized Consent"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent
        doCofAuthorizationFlow()
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_VALID)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_VALID)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("account"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardNumber"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardExpiryDate"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardInformation"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("consentStatus"))
    }
}
