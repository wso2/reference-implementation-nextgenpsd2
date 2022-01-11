/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import net.minidev.json.JSONObject;

import java.util.Map;

/**
 * Interface to handle account list retrieval for Authorize.
 */
public interface AccountListRetrievalHandler {

    /**
     * Abstract method defined to retrieve the accounts related data in the authorization flow to send them to the
     * consent page to get PSU consent.
     *
     * @param consentData     consent related data
     * @param consentDataJSON consent data appended from the previous steps
     * @return accounts details to be shown in the consent page
     * @throws ConsentException
     */
    JSONObject getAccountData(ConsentData consentData, JSONObject consentDataJSON) throws ConsentException;

    /**
     * Appends the account related data to the metadata map to retrieve during persisting steps.
     *
     * @param metaDataMap     meta data map
     * @param accountDataJSON account data to store in the metadata map
     */
    void appendAccountDetailsToMetadata(Map<String, Object> metaDataMap, JSONObject accountDataJSON);

}
