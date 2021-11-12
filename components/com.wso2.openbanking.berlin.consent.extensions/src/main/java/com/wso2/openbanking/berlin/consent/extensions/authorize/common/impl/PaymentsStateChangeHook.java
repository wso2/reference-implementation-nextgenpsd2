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

package com.wso2.openbanking.berlin.consent.extensions.authorize.common.impl;

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.berlin.consent.extensions.authorize.common.AuthorisationStateChangeHook;
import com.wso2.openbanking.berlin.consent.extensions.authorize.enums.AuthorisationAggregateStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
                    log.warn("Unbindable authorisation state offered");
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
                    log.warn("Unbindable authorisation state offered");
            }
        }
        return transactionStatus;
    }
}
