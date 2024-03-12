/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com/). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.worker;

import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * Class to update authorization object to failed status.
 */
public class FailedStatusUpdateWorker extends BerlinAuthStatusUpdateWorker {

    @Override
    boolean isCurrentAuthorizationStatusEligible(String authStatus) {
        //to address the basic auth failures
        return StringUtils.equals(ScaStatusEnum.RECEIVED.toString(), authStatus) ||
                StringUtils.equals(ScaStatusEnum.SCA_METHOD_SELECTED.toString(), authStatus) ||
                StringUtils.equals(ScaStatusEnum.PSU_IDENTIFIED.toString(), authStatus) ||
                // to address the sca failures
                StringUtils.equals(ScaStatusEnum.STARTED.toString(), authStatus) ||
                StringUtils.equals(ScaStatusEnum.PSU_AUTHENTICATED.toString(), authStatus);
    }

    @Override
    String getNewAuthStatusValue() {
        return ScaStatusEnum.FAILED.toString();
    }
}
