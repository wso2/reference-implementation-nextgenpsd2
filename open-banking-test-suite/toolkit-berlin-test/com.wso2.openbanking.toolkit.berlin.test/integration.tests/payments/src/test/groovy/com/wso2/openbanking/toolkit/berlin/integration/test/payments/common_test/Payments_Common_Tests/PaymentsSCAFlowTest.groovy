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
