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

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.factory;

import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.AccountInitiationRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.ExplicitAuthRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.FundsConfirmationInitiationRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.PaymentInitiationRequestHandler;
import org.junit.Test;
import org.testng.Assert;

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
