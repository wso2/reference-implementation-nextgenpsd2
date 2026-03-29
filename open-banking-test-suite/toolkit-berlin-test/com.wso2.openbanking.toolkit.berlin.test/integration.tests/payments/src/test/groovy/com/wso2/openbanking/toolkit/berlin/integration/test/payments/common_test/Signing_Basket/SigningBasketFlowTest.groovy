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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.common_test.Signing_Basket

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Factory
import org.testng.annotations.Test

/**
 * Tests for Signing Basket Flow.
 */
class SigningBasketFlowTest extends AbstractPaymentsFlow {

    Map<String, String> map
    String consentPath
    String initiationPayload
    String paymentType
    String paymentId1, paymentId2

    @Factory(dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    SigningBasketFlowTest(Map<String, String> maps) {
        this.map = maps

        consentPath = map.get("consentPath")
        initiationPayload = map.get("initiationPayload")
        paymentType = map.get("paymentType")
    }

    @Test (groups = ["SmokeTest", "1.3.6"])
    void "Create Multiple Payment Consent"() {

        //Create Payment Consent 1
        doDefaultInitiation(consentPath, initiationPayload)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertEquals(consentResponse.jsonPath().get("transactionStatus"),
                PaymentsConstants.TRANSACTION_STATUS_RECEIVED)

        Assert.assertNotNull(paymentId)
        paymentId1 = TestUtil.parseResponseBody(consentResponse, "paymentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")

        //Create Payment Consent 2
        consentResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        paymentId2 = TestUtil.parseResponseBody(consentResponse, "paymentId")
        consentStatus = TestUtil.parseResponseBody(consentResponse, "transactionStatus")
    }

    @Test (dependsOnMethods = ["Create Payment Consent"], groups = ["1.3.6"])
    void "BG-745_Initiate Signing Basket Request"() {

        String payload = PaymentsInitiationPayloads.signingBasketInitiationPayloadBuilder(paymentId1, paymentId2)

        doSigningBasketInitiation(payload)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(basketId)
        Assert.assertEquals(consentStatus, PaymentsConstants.SCA_STATUS_RECEIVED)

        Assert.assertEquals(signingBasketResponse.jsonPath().get("_links.self.href"),
                "/v1" + PaymentsConstants.SIGNING_BASKET_PATH + basketId)
        Assert.assertEquals(signingBasketResponse.jsonPath().get("_links.status.href"),
                "/v1" + PaymentsConstants.SIGNING_BASKET_PATH + basketId + "/status")
        Assert.assertEquals(signingBasketResponse.jsonPath().get("_links.startAuthorisation.href"),
                "/v1" + PaymentsConstants.SIGNING_BASKET_PATH + basketId + "/authorisations")
    }

    @Test (dependsOnMethods = ["BG-745_Initiate Signing Basket Request"], groups = ["1.3.6"])
    void "BG-746_Get Signing Basket Request"() {

        doSigningBasketRetrieval(basketId)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_200)

        List<String> payments = signingBasketResponse.jsonPath().getList("payments")

        Assert.assertNotNull(payments, "Payments array is null")
        Assert.assertFalse(payments.isEmpty(), "Payments array is empty")

        Assert.assertEquals(consentStatus, PaymentsConstants.SCA_STATUS_RECEIVED)
    }

    @Test (dependsOnMethods = ["BG-746_Get Signing Basket Request"], groups = ["1.3.6"])
    void "BG-747_Get Signing Basket Status Request"() {

        getStatusOfSigningBasket(basketId)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, PaymentsConstants.SCA_STATUS_RECEIVED)
    }

    @Test (dependsOnMethods = ["BG-747_Get Signing Basket Status Request"], groups = ["1.3.6"])
    void "BG-753_Send signing basket cancellation request"() {

        doSigningBasketCancellation(basketId)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_204)
    }

    @Test (groups = ["1.3.6"])
    void "BG-748_Initiate Signing Basket with Empty array of PaymentIds"() {

        String payload = PaymentsInitiationPayloads.signingBasketInitiationPayloadBuilder("", "")

        doSigningBasketInitiation(payload)

        Assert.assertEquals(signingBasketResponse.statusCode(), BerlinConstants.STATUS_CODE_400)
    }
}
