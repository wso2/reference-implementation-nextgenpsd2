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
 * Response Validation Tests on Delete Funds Confirmation Consent Request.
 */
class CofDeleteConsentResponseValidationTests extends AbstractCofFlow {

    def consentPath = CofConstants.COF_CONSENT_PATH
    def initiationPayload = CofInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["SmokeTest", "1.3.6"])
    void "OB-1560_Delete consent in received state"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Delete Consent
        deleteCofConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))

        //Check Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_TERMINATED_BY_TPP)
    }

    @Test(groups = ["1.3.6"])
    void "OB-1562_Delete consent request with invalid consent id"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        def invalidConsentId = "1234"

        //Delete Consent
        def consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}/" + invalidConsentId)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE)
                .toString(),BerlinConstants.CONSENT_UNKNOWN)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT)
                .toString(),"Matching consent not found for provided Id")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1565_Send delete consent request without consent id"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Delete Consent
        def consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0603004_Delete the consent with an empty consentId"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Delete Consent
        def consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .delete("${consentPath}/")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))
    }

    @Test (groups = ["1.3.6"])
    void "OB-1564_Send delete consent request for already terminated consent"() {

        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Delete Consent
        deleteCofConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))

        //Check Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_TERMINATED_BY_TPP)

        //Delete Consent which is already terminated
        deleteCofConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT)
                .toString(),"The requested consent is already deleted")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1561_Delete consent in authorised state "() {
        //Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)

        //Authorise the consent
        doCofAuthorizationFlow()

        //Check Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_VALID)

        //Delete Consent
        deleteCofConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))

        //Check Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_TERMINATED_BY_TPP)
    }
}
