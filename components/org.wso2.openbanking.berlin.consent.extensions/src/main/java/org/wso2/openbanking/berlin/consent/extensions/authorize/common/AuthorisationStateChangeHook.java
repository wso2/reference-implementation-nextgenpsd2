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

package org.wso2.openbanking.berlin.consent.extensions.authorize.common;

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.openbanking.berlin.consent.extensions.authorize.enums.AuthorisationAggregateStatusEnum;

/**
 * Interface with the method to implement authorisation state change logics for consent services.
 */
public interface AuthorisationStateChangeHook {

    /**
     * Used to implement the aggregated status change logic for consent services.
     *
     * @param consentId Id of the current consent
     * @param authType type of current authorisation
     * @param aggregatedStatus the aggregated status of the consent
     * @param currentAuthorisation the current authorisation
     */
    String onAuthorisationStateChange(String consentId, String authType,
                                    AuthorisationAggregateStatusEnum aggregatedStatus,
                                    AuthorizationResource currentAuthorisation);
}
