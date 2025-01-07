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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval;

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
