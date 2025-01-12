/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.factory;

import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.AccountServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.AuthorisationServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.FundsConfirmationServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.PaymentServiceHandler;

/**
 * Factory for deciding the banking service.
 */
public class ServiceHandlerFactory {

    /**
     * Method to get the Consent Manage Service Handler.
     *
     * @param requestPath Request path of the request
     * @return ServiceHandler
     */
    public static ServiceHandler getServiceHandler(String requestPath) {

        switch (ConsentExtensionUtil.getServiceDifferentiatingRequestPath(requestPath)) {
            case ConsentExtensionConstants.ACCOUNTS_CONSENT_PATH:
                return new AccountServiceHandler();
            case ConsentExtensionConstants.PAYMENTS_SERVICE_PATH:
            case ConsentExtensionConstants.BULK_PAYMENTS_SERVICE_PATH:
            case ConsentExtensionConstants.PERIODIC_PAYMENTS_SERVICE_PATH:
            case ConsentExtensionConstants.PAYMENT_CONSENT_UPDATE_PATH:
                return new PaymentServiceHandler();
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH:
                return new FundsConfirmationServiceHandler();
            case ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END:
            case ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END:
                return new AuthorisationServiceHandler();
            default:
                return null;
        }
    }

}
