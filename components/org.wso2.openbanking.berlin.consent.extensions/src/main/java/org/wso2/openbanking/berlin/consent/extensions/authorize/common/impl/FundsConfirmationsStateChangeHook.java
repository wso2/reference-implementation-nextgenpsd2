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

package org.wso2.openbanking.berlin.consent.extensions.authorize.common.impl;

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.consent.extensions.authorize.common.AuthorisationStateChangeHook;
import org.wso2.openbanking.berlin.consent.extensions.authorize.enums.AuthorisationAggregateStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;

/**
 * Authorisation state change hook for funds confirmations service.
 */
public class FundsConfirmationsStateChangeHook implements AuthorisationStateChangeHook {

    private static final Log log = LogFactory.getLog(FundsConfirmationsStateChangeHook.class);

    @Override
    public String onAuthorisationStateChange(String consentId, String authType,
                                           AuthorisationAggregateStatusEnum aggregatedStatus,
                                           AuthorizationResource currentAuthorisation) {

        String consentStatus = null;

        switch (aggregatedStatus) {
            case FULLY_AUTHORISED:
                consentStatus = ConsentStatusEnum.VALID.toString();
                break;
            case REJECTED:
                consentStatus = ConsentStatusEnum.REJECTED.toString();
                break;
            case PARTIALLY_AUTHORISED:
                consentStatus = ConsentStatusEnum.PARTIALLY_AUTHORISED.toString();
                break;
            default:
                log.warn(String.format("Unbindable authorisation state (%s) offered", aggregatedStatus));
        }
        return consentStatus;
    }
}
