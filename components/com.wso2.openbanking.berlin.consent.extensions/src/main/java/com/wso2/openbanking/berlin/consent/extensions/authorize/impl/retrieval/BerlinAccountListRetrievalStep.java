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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.authorize.factory.AuthorizationHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.AccountListRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle account list retrieval for authorize.
 */
public class BerlinAccountListRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(BerlinAccountListRetrievalStep.class);
    AccountListRetrievalHandler accountListRetrievalHandler;

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }

        JSONObject consentDataJSON = (JSONObject) jsonObject.get(ConsentExtensionConstants.CONSENT_DATA);
        if (consentDataJSON == null || consentDataJSON.size() == 0) {
            log.error(ErrorConstants.INCORRECT_CONSENT_DATA);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.SERVER_ERROR,
                            ErrorConstants.INCORRECT_CONSENT_DATA, consentData.getRedirectURI(),
                            consentData.getState()));
        }

        JSONObject accountDataJSON = getAccountData(consentData, consentDataJSON);

        jsonObject.appendField(ConsentExtensionConstants.ACCOUNT_DATA, accountDataJSON);
        accountListRetrievalHandler.appendAccountDetailsToMetadata(consentData.getMetaDataMap(), accountDataJSON);
    }

    /**
     * Method to retrieve account related data from the initiation payload.
     *
     * @param consentData
     * @param consentDataJSON
     * @return
     * @throws ConsentException
     */
    public JSONObject getAccountData(ConsentData consentData, JSONObject consentDataJSON)
            throws ConsentException {

        String type = consentData.getConsentResource().getConsentType();
        accountListRetrievalHandler = AuthorizationHandlerFactory.getAccountListRetrievalHandler(type);
        return accountListRetrievalHandler.getAccountData(consentData, consentDataJSON);
    }
}
