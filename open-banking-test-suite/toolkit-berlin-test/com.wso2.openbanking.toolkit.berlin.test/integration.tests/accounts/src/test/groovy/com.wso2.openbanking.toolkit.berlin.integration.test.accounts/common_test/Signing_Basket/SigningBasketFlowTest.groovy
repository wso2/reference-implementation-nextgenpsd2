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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Signing_Basket

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Factory
import org.testng.annotations.Test

/**
 * Tests for Signing Basket Flow.
 */
class SigningBasketFlowTest extends AbstractAccountsFlow {

    Map<String, String> map
    String consentPath
    String initiationPayload
    String consentId1, consentId2

    @BeforeClass
    void "initAccountsConsent"() {
        consentPath = AccountsConstants.CONSENT_PATH
        initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload
    }

    @Test (groups = ["SmokeTest", "1.3.6"])
    void "Create Multiple Accounts Consents"() {

        //Create Accounts Consent 1
        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                AccountsConstants.CONSENT_STATUS_RECEIVED)

        consentId1 = TestUtil.parseResponseBody(consentResponse, "consentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")

        //Create Accounts Consent 2
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        consentId2 = TestUtil.parseResponseBody(consentResponse, "consentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")
    }

    @Test (dependsOnMethods = ["Create Multiple Accounts Consents"], groups = ["1.3.6"])
    void "BG-749_Initiate Signing Basket Request"() {

        String payload = AccountsInitiationPayloads.signingBasketInitiationPayloadBuilder(consentId1, consentId2)

        doSigningBasketInitiation(payload)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(basketId)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        Assert.assertEquals(signingBasketResponse.jsonPath().get("_links.self.href"),
                "/v1" + AccountsConstants.SIGNING_BASKET_PATH + basketId)
        Assert.assertEquals(signingBasketResponse.jsonPath().get("_links.status.href"),
                "/v1" + AccountsConstants.SIGNING_BASKET_PATH + basketId + "/status")
        Assert.assertEquals(signingBasketResponse.jsonPath().get("_links.startAuthorisation.href"),
                "/v1" + AccountsConstants.SIGNING_BASKET_PATH + basketId + "/authorisations")
    }

    @Test (dependsOnMethods = ["BG-745_Initiate Signing Basket Request"], groups = ["1.3.6"])
    void "BG-750_Get Signing Basket Request"() {

        doSigningBasketRetrieval(basketId)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_200)

        List<String> payments = signingBasketResponse.jsonPath().getList("payments")

        Assert.assertNotNull(payments, "Payments array is null")
        Assert.assertFalse(payments.isEmpty(), "Payments array is empty")

        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)
    }

    @Test (dependsOnMethods = ["BG-746_Get Signing Basket Request"], groups = ["1.3.6"])
    void "BG-751_Get Signing Basket Status Request"() {

        getStatusOfSigningBasket(basketId)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)
    }

    @Test (dependsOnMethods = ["BG-747_Get Signing Basket Status Request"], groups = ["1.3.6"])
    void "BG-753_Send signing basket cancellation request"() {

        doSigningBasketCancellation(basketId)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_204)
    }

    @Test (groups = ["1.3.6"])
    void "BG-752_Initiate Signing Basket with Empty array of ConsentIds"() {

        String payload = AccountsInitiationPayloads.signingBasketInitiationPayloadBuilder("", "")

        doSigningBasketInitiation(payload)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_400)
    }
}
