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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.common_test.Payments_Common_Tests


import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.AbstractPaymentsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.payments.util.PaymentsDataProviders
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Payments V133 OAuth Redirect Flow Tests.
 */
class PaymentsSCAFlowTest extends AbstractPaymentsFlow {

    @Test(groups = ["SmokeTest", "1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0302001_Payment implicit SCA accept scenario"(String consentPath, List<String> paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation RequestPaymentProduct
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertNotNull(paymentId)

            // Initiate SCA flow.
            doAuthorizationFlow()

            // Get User Access Token
            generateUserAccessToken()
            Assert.assertNotNull(userAccessToken)

            // Check consent received status
            doStatusRetrieval(paymentConsentPath)

            Assert.assertEquals(consentStatus, PaymentsConstants.TRANSACTION_STATUS_ACCP)
        }
    }

    @Test(groups = ["1.3.3", "1.3.6"],
            dataProvider = "PaymentsTypes", dataProviderClass = PaymentsDataProviders.class)
    void "TC0302002_Payment implicit SCA deny scenario"(String consentPath, List<String> paymentProducts, String payload) {

        paymentProducts.each { value ->
            String paymentConsentPath = consentPath + "/" + value

            //Make Payment Initiation Request
            doDefaultInitiation(paymentConsentPath, payload)
            Assert.assertNotNull(paymentId)

            // Deny the Consent
            doConsentDenyFlow()

            Assert.assertEquals(code, "User denied the consent")
        }
    }
}
