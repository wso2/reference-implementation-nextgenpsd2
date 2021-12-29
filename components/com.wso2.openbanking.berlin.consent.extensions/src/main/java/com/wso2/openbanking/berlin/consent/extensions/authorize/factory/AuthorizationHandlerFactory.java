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

package com.wso2.openbanking.berlin.consent.extensions.authorize.factory;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.authorize.common.AuthorisationStateChangeHook;
import com.wso2.openbanking.berlin.consent.extensions.authorize.common.impl.AccountsStateChangeHook;
import com.wso2.openbanking.berlin.consent.extensions.authorize.common.impl.FundsConfirmationsStateChangeHook;
import com.wso2.openbanking.berlin.consent.extensions.authorize.common.impl.PaymentsStateChangeHook;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.AccountsConsentPersistHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.ConsentPersistHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.FundsConfirmationsConsentPersistHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.PaymentConsentPersistHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.AccountAccountListRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.AccountConsentRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.AccountListRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.FundsConfirmationAccountListRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.FundsConfirmationConsentRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.ConsentRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.PaymentAccountListRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.PaymentConsentRetrievalHandler;
import org.apache.commons.lang3.StringUtils;

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
            accountListRetrievalHandler = new AccountAccountListRetrievalHandler();
        } else if (StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), type)) {
            accountListRetrievalHandler = new PaymentAccountListRetrievalHandler();
        } else if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), type)) {
            accountListRetrievalHandler = new FundsConfirmationAccountListRetrievalHandler();
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
