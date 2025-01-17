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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.consent.extensions.authorize.factory.AuthorizationHandlerFactory;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.AccountListRetrievalHandler;
import org.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;

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
