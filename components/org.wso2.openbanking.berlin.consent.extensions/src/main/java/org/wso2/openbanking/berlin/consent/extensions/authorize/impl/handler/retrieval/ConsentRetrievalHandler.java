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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import net.minidev.json.JSONObject;

import java.util.Map;

/**
 * Interface to handle Consent data retrieval for Authorize.
 */
public interface ConsentRetrievalHandler {

    /**
     * Abstract method defined to retrieve the consent related data in the authorization flow to send them to the
     * consent page to get PSU consent.
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved from database
     * @return
     * @throws ConsentException
     */
    JSONObject getConsentData(ConsentResource consentResource) throws ConsentException;

    /**
     * Abstract method defined to validate the authorization status.
     *
     * @param consentResource
     * @param authType
     * @return
     */
    boolean validateAuthorizationStatus(ConsentResource consentResource, String authType);

    /**
     * Abstract method defined to populate data to publish for data reporting in consent retrieval.
     *
     * @param consentData   ConsentData
     * @return Data map containing values to publish
     */
    Map<String, Object> populateReportingData(ConsentData consentData);
}
