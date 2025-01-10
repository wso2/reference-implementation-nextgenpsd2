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

import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.AccountServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.AuthorisationServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.FundsConfirmationServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl.PaymentServiceHandler;

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
