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

import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.AccountInitiationRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.BulkPaymentInitiationRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.CreateExplicitAuthorisationRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.FundsConfirmationInitiationRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.PaymentCreateExplicitCancellationAuthorisationRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.PaymentInitiationRequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl.PeriodicPaymentInitiationRequestHandler;

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

        if (ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END
                .equals(requestPathArray[requestPathArray.length - 1])) {
            return new PaymentCreateExplicitCancellationAuthorisationRequestHandler();
        }

        if (ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END
                .equals(requestPathArray[requestPathArray.length - 1])) {
            return new CreateExplicitAuthorisationRequestHandler();
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
