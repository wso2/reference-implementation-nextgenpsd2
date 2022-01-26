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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Common_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofRetrievalPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Funds Confirmations SCA flow tests.
 */
class CofSCAFlowTest extends AbstractCofFlow {

    String consentPath = CofConstants.COF_CONSENT_PATH
    String initiationPayload = CofInitiationPayloads.defaultInitiationPayload
    String retrievalPayload = CofRetrievalPayloads.defaultRetrievalPayload

    @Test (groups = ["SmokeTest", "1.3.6"])
    void "OB-1542_COF consent initiation with valid inputs"() {

        doDefaultCofInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(consentId)

        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaOAuth.href"))
        Assert.assertNotNull(TestUtil.parseResponseBody(consentResponse, "_links.scaStatus.href"))

        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_RECEIVED)
    }

    @Test (groups = ["SmokeTest", "1.3.6"],
            dependsOnMethods = ["OB-1542_COF consent initiation with valid inputs"])
    void "TC0602013_Cof Authorization for SCA implicit accept scenario"() {

        doCofAuthorizationFlow()

        Assert.assertNotNull(automation.currentUrl.get().contains("state"))
        Assert.assertNotNull(code)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_VALID)

        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
    }

    @Test (groups = ["SmokeTest", "1.3.6"],
            dependsOnMethods = ["TC0602013_Cof Authorization for SCA implicit accept scenario"])
    void "TC0605001_Cof Retrieval Request"() {

        def response = BerlinRequestBuilder
                .buildBasicRequest(userAccessToken)
                .body(retrievalPayload)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .post(CofConstants.COF_RETRIEVAL_PATH)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertNotNull(response.jsonPath().getJsonObject("fundsAvailable"))
    }

    @Test (groups = ["SmokeTest", "1.3.6"], priority = 1)
    void "TC0602014_Cof Authorization for SCA implicit deny scenario"() {

        doDefaultCofInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_RECEIVED)

        doConsentDenyFlow()

        Assert.assertEquals(code, "User denied the consent")

        doStatusRetrieval(consentPath)

        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_REJECTED)
    }

    @Test (groups = ["1.3.6"], dependsOnMethods = "TC0602014_Cof Authorization for SCA implicit deny scenario")
    void "OB-1551_Retrieve status of an authorised consent"() {

        getConsentStatus(consentPath, consentId)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_REJECTED)
    }

    @Test (groups = ["1.3.6"], dependsOnMethods = "TC0602014_Cof Authorization for SCA implicit deny scenario")
    void "OB-1553_Consent details retrieval for terminated consent"() {

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_REJECTED)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("account"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardNumber"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardExpiryDate"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("cardInformation"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("consentStatus"))
    }
}
