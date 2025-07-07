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

package org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.factory;

import org.junit.Test;
import org.testng.Assert;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.AccountServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.AuthorisationServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.FundsConfirmationServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.PaymentServiceHandler;

public class ServiceHandlerFactoryTests {

    @Test
    public void testGetServiceHandler() {

        ServiceHandler serviceHandler;

        // Testing AccountServiceHandler instances
        serviceHandler = ServiceHandlerFactory.getServiceHandler("consents");
        Assert.assertTrue(serviceHandler instanceof AccountServiceHandler);

        // Testing PaymentServiceHandler instances
        serviceHandler = ServiceHandlerFactory.getServiceHandler("payments/{payment-product}");
        Assert.assertTrue(serviceHandler instanceof PaymentServiceHandler);

        serviceHandler = ServiceHandlerFactory.getServiceHandler("bulk-payments/{payment-product}");
        Assert.assertTrue(serviceHandler instanceof PaymentServiceHandler);

        serviceHandler = ServiceHandlerFactory.getServiceHandler("periodic-payments/{payment-product}");
        Assert.assertTrue(serviceHandler instanceof PaymentServiceHandler);

        // Testing FundsConfirmationServiceHandler instances
        serviceHandler = ServiceHandlerFactory.getServiceHandler("consents/confirmation-of-funds");
        Assert.assertTrue(serviceHandler instanceof FundsConfirmationServiceHandler);

        // Testing Explicit authorisation instances
        serviceHandler = ServiceHandlerFactory.getServiceHandler("consents/{consent-id}/authorisations");
        Assert.assertTrue(serviceHandler instanceof AuthorisationServiceHandler);

        // Exceptional scenarios
        serviceHandler = ServiceHandlerFactory.getServiceHandler(null);
        Assert.assertNull(serviceHandler);
    }

}
