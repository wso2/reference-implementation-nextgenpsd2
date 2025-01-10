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

package org.wso2.openbanking.berlin.consent.extensions.authorize.worker;

import org.apache.commons.lang3.StringUtils;
import org.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;

/**
 * Class to update authorization object to PSU Authenticated status.
 */
public class PSUAuthenticatedStatusUpdateWorker extends BerlinAuthStatusUpdateWorker {

    @Override
    boolean isCurrentAuthorizationStatusEligible(String authStatus) {
        return StringUtils.equals(ScaStatusEnum.RECEIVED.toString(), authStatus) ||
                StringUtils.equals(ScaStatusEnum.SCA_METHOD_SELECTED.toString(), authStatus) ||
                StringUtils.equals(ScaStatusEnum.PSU_IDENTIFIED.toString(), authStatus);
    }

    @Override
    String getNewAuthStatusValue() {
        return ScaStatusEnum.PSU_AUTHENTICATED.toString();
    }
}
