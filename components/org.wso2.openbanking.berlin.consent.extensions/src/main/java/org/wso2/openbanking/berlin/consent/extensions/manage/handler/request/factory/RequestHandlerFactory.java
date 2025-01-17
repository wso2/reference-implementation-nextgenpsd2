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

import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.AccountInitiationRequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.BulkPaymentInitiationRequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.ExplicitAuthRequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.FundsConfirmationInitiationRequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.PaymentInitiationRequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.PeriodicPaymentInitiationRequestHandler;

/**
 * Factory for deciding the type of request.
 */
public class RequestHandlerFactory {

    /**
     * Method to get the Consent Manage Request Handler.
     *
     * @param requestPath Request path of the request
     * @return RequestHandler
     */
    public static RequestHandler getRequestHandler(String requestPath) {

        if (requestPath == null) {
            return null;
        }

        String[] requestPathArray = requestPath.split("/");

        String lastElement = requestPathArray[requestPathArray.length - 1];
        if (ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END
                .equals(lastElement)
                || ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END
                .equals(lastElement)) {
            return new ExplicitAuthRequestHandler();
        }

        switch (ConsentExtensionUtil.getServiceDifferentiatingRequestPath(requestPath)) {
            case ConsentExtensionConstants.ACCOUNTS_CONSENT_PATH:
                return new AccountInitiationRequestHandler();
            case ConsentExtensionConstants.PAYMENTS_SERVICE_PATH:
                return new PaymentInitiationRequestHandler();
            case ConsentExtensionConstants.BULK_PAYMENTS_SERVICE_PATH:
                return new BulkPaymentInitiationRequestHandler();
            case ConsentExtensionConstants.PERIODIC_PAYMENTS_SERVICE_PATH:
                return new PeriodicPaymentInitiationRequestHandler();
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH:
                return new FundsConfirmationInitiationRequestHandler();
            default:
                return null;
        }

    }

}
