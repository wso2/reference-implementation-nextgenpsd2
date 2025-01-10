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

package org.wso2.openbanking.berlin.consent.extensions.authorize.factory;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.authorize.common.AuthorisationStateChangeHook;
import org.wso2.openbanking.berlin.consent.extensions.authorize.common.impl.AccountsStateChangeHook;
import org.wso2.openbanking.berlin.consent.extensions.authorize.common.impl.FundsConfirmationsStateChangeHook;
import org.wso2.openbanking.berlin.consent.extensions.authorize.common.impl.PaymentsStateChangeHook;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.AccountsConsentPersistHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.ConsentPersistHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.FundsConfirmationsConsentPersistHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.PaymentConsentPersistHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.AISAccountListRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.AccountConsentRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.AccountListRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.ConsentRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.FundsConfirmationConsentRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.PIISAccountListRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.PISAccountListRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.PaymentConsentRetrievalHandler;

/**
 * Factory class to get the class based in request type.
 */
public class AuthorizationHandlerFactory {

    /**
     * Method to get the account list authorize handler.
     *
     * @param type consent type of the request
     * @return the selected account list retrieval handler
     */
    public static AccountListRetrievalHandler getAccountListRetrievalHandler(String type) {

        AccountListRetrievalHandler accountListRetrievalHandler = null;

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), type)) {
            accountListRetrievalHandler = new AISAccountListRetrievalHandler();
        } else if (StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), type)) {
            accountListRetrievalHandler = new PISAccountListRetrievalHandler();
        } else if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), type)) {
            accountListRetrievalHandler = new PIISAccountListRetrievalHandler();
        }
        return accountListRetrievalHandler;
    }

    /**
     * Method to get the consent authorize handler.
     *
     * @param type consent type of the request
     * @return the selected consent retrieval handler
     */
    public static ConsentRetrievalHandler getConsentRetrievalHandler(String type) {

        ConsentRetrievalHandler consentRetrievalHandler = null;

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), type)) {
            consentRetrievalHandler = new AccountConsentRetrievalHandler();
        } else if (StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), type)) {
            consentRetrievalHandler = new PaymentConsentRetrievalHandler();
        } else if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), type)) {
            consentRetrievalHandler = new FundsConfirmationConsentRetrievalHandler();
        }
        return consentRetrievalHandler;
    }

    /**
     * Method to get the consent persist handler.
     *
     * @param type consent type of the request
     * @return the selected consent persist handler
     */
    public static ConsentPersistHandler getConsentPersistHandler(String type) {

        ConsentPersistHandler consentPersistHandler = null;

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), type)) {
            consentPersistHandler = new AccountsConsentPersistHandler(getConsentService());
        } else if (StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), type)) {
            consentPersistHandler = new PaymentConsentPersistHandler(getConsentService());
        } else if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), type)) {
            consentPersistHandler = new FundsConfirmationsConsentPersistHandler(getConsentService());
        }
        return consentPersistHandler;
    }

    /**
     * Method to get the authorisation state change hook.
     *
     * @param type consent type of the consent
     * @return the selected authorisation state change hook
     */
    public static AuthorisationStateChangeHook getAuthorisationStateChangeHook(String type) {

        AuthorisationStateChangeHook authorisationStateChangeHook = null;

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), type)) {
            authorisationStateChangeHook = new AccountsStateChangeHook();
        } else if (StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), type)) {
            authorisationStateChangeHook = new PaymentsStateChangeHook();
        } else if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), type)) {
            authorisationStateChangeHook = new FundsConfirmationsStateChangeHook();
        }
        return authorisationStateChangeHook;

    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    public static ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
