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
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
