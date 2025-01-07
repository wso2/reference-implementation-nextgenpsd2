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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.consent.extensions.authorize.common.AuthorisationStateChangeHook;
import org.wso2.openbanking.berlin.consent.extensions.authorize.enums.AuthorisationAggregateStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;

/**
 * Authorisation state change hook for payments service.
 */
public class PaymentsStateChangeHook implements AuthorisationStateChangeHook {

    private static final Log log = LogFactory.getLog(PaymentsStateChangeHook.class);

    @Override
    public String onAuthorisationStateChange(String consentId, String authType,
                                           AuthorisationAggregateStatusEnum aggregatedStatus,
                                           AuthorizationResource currentAuthorisation) {

        String transactionStatus = null;

        if (StringUtils.equals(AuthTypeEnum.AUTHORISATION.toString(), authType)) {
            switch (aggregatedStatus) {
                case FULLY_AUTHORISED:
                    transactionStatus = TransactionStatusEnum.ACCP.name();
                    break;
                case REJECTED:
                    transactionStatus = TransactionStatusEnum.RJCT.name();
                    break;
                case PARTIALLY_AUTHORISED:
                    transactionStatus = TransactionStatusEnum.PATC.name();
                    break;
                default:
                    log.warn(String.format("Unbindable authorisation state (%s) offered", aggregatedStatus));
            }
        } else {
            switch (aggregatedStatus) {
                case FULLY_AUTHORISED:
                    transactionStatus = TransactionStatusEnum.CANC.name();
                    break;
                case REJECTED:
                    transactionStatus = TransactionStatusEnum.ACCP.name();
                    break;
                default:
                    log.warn(String.format("Unbindable authorisation state (%s) offered", aggregatedStatus));
            }
        }
        return transactionStatus;
    }
}
