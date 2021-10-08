/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.factory.tests;

import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.factory.ServiceHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.AccountServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.FundsConfirmationServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.PaymentServiceHandler;
import org.junit.Test;
import org.testng.Assert;

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

        // Exceptional scenarios
        serviceHandler = ServiceHandlerFactory.getServiceHandler(null);
        Assert.assertNull(serviceHandler);
    }

}
