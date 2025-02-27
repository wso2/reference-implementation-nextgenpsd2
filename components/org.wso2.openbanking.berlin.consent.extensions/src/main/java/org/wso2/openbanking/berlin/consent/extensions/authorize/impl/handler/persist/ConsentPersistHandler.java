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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;

import java.util.Map;

/**
 * Interface to handle Consent data persistence for Authorize.
 */
public interface ConsentPersistHandler {

    /**
     * Abstract method defined to handle consent persistence based on the consent type.
     *
     * @param consentPersistData    Consent Persist Data Object
     * @param consentResource       Consent Resource Object
     * @throws ConsentManagementException
     */
    void consentPersist(ConsentPersistData consentPersistData, ConsentResource consentResource)
            throws ConsentManagementException;

    /**
     * Abstract method defined to populate data to publish for data reporting in consent persistence.
     *
     * @param consentPersistData   ConsentPersistData Object
     * @return Data map containing values to publish
     */
    Map<String, Object> populateReportingData(ConsentPersistData consentPersistData);
}
