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

package org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.factory;

import org.junit.Test;
import org.testng.Assert;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.AccountInitiationRequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.ExplicitAuthRequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.FundsConfirmationInitiationRequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.PaymentInitiationRequestHandler;

public class RequestHandlerFactoryTests {

    @Test
    public void testGetRequestHandler() {

        RequestHandler requestHandler;

        // Testing ExplicitAuthRequestHandler instances
        requestHandler = RequestHandlerFactory
                .getRequestHandler("{payment-service}/{payment-product}/{paymentId}/cancellation-authorisations");
        Assert.assertTrue(requestHandler instanceof ExplicitAuthRequestHandler);

        requestHandler = RequestHandlerFactory
                .getRequestHandler("consents/{consentId}/authorisations");
        Assert.assertTrue(requestHandler instanceof ExplicitAuthRequestHandler);

        requestHandler = RequestHandlerFactory
                .getRequestHandler("{payment-service}/{payment-product}/{paymentId}/authorisations");
        Assert.assertTrue(requestHandler instanceof ExplicitAuthRequestHandler);

        requestHandler = RequestHandlerFactory
                .getRequestHandler("consents/confirmation-of-funds/{consentId}/authorisations");
        Assert.assertTrue(requestHandler instanceof ExplicitAuthRequestHandler);

        // Testing AccountInitiationRequestHandler instances
        requestHandler = RequestHandlerFactory.getRequestHandler("consents");
        Assert.assertTrue(requestHandler instanceof AccountInitiationRequestHandler);

        // Testing PaymentInitiationRequestHandlers instances
        requestHandler = RequestHandlerFactory.getRequestHandler("payments/{payment-product}");
        Assert.assertTrue(requestHandler instanceof PaymentInitiationRequestHandler);

        requestHandler = RequestHandlerFactory.getRequestHandler("bulk-payments/{payment-product}");
        Assert.assertTrue(requestHandler instanceof PaymentInitiationRequestHandler);

        requestHandler = RequestHandlerFactory.getRequestHandler("periodic-payments/{payment-product}");
        Assert.assertTrue(requestHandler instanceof PaymentInitiationRequestHandler);

        // Testing FundsConfirmationInitiationRequestHandler instances
        requestHandler = RequestHandlerFactory.getRequestHandler("consents/confirmation-of-funds");
        Assert.assertTrue(requestHandler instanceof FundsConfirmationInitiationRequestHandler);
    }

}
