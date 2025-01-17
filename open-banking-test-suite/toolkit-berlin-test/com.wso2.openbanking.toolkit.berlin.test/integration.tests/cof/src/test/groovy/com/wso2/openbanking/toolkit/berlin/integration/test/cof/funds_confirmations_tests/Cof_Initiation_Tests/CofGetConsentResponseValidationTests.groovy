/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
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

    @Test (groups = ["SmokeTest", "1.3.6"], priority = 1)
    void "OB-1543_Retrieve COF consent details"() {

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

    @Test (groups = ["SmokeTest", "1.3.6"], dependsOnMethods = "OB-1543_Retrieve COF consent details", priority = 1)
    void "OB-1547_Retrieve status of a valid consent"() {

        getConsentStatus(consentPath, consentId)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_RECEIVED)
    }

    @Test (groups = ["1.3.6"])
    void "TC0604002_Get Consent With Empty Consent Id"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.SERVICE_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Method not allowed for given API resource")
    }

    @Test (groups = ["1.3.6"])
    void "TC0604003_Get Consent Without Consent Id Parameter"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.SERVICE_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Method not allowed for given API resource")
    }

    @Test (groups = ["1.3.6"], priority = 2)
    void "OB-1546_Consent details retrieval for a deleted consent"() {

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

    @Test (groups = ["1.3.6"], dependsOnMethods = "OB-1546_Consent details retrieval for a deleted consent",
            priority = 2)
    void "OB-1549_Retrieve status of a deleted consent"() {
        getConsentStatus(consentPath, consentId)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_TERMINATED_BY_TPP)
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

    @Test (groups = ["1.3.6"], dependsOnMethods = "TC0604005_Get an Authorized Consent")
    void "OB-1551_Retrieve status of an authorised consent"() {

        getConsentStatus(consentPath, consentId)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_VALID)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1545_Consent detail retrieval for invalid consent id"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/1234")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_UNKNOWN)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Matching consent not found for provided Id")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1548_Retrieve status of a invalid consent"() {

        String invalidConsentId = 1234

        getConsentStatus(consentPath, invalidConsentId)

        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.CONSENT_UNKNOWN)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Matching consent not found for provided Id")
    }
}
