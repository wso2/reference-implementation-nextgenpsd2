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

package com.wso2.openbanking.berlin.consent.extensions.common;

import org.apache.commons.lang3.StringUtils;

/**
 * Consent extension utils.
 */
public class ConsentExtensionUtil {

    /**
     * Gets the consent service using the request path.
     *
     * @param requestPath
     * @return
     */
    public static String getServiceDifferentiatingRequestPath(String requestPath) {

        if (requestPath == null) {
            return "";
        }

        String[] requestPathArray = requestPath.split("/");

        if (requestPathArray.length > 1) {
            if (ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH.equals(requestPathArray[1])) {
                return requestPathArray[1];
            } else {
                return requestPathArray[0];
            }
        } else {
            return requestPathArray[0];
        }
    }

    /**
     * Ensures the psu ID is appended with the super tenant domain.
     *
     * @param psuId
     * @return
     */
    public static String appendSuperTenantDomain(String psuId) {

        if (StringUtils.isNotBlank(psuId)) {
            if (psuId.endsWith(ConsentExtensionConstants.SUPER_TENANT_DOMAIN)) {
                return psuId;
            } else {
                return psuId + ConsentExtensionConstants.SUPER_TENANT_DOMAIN;
            }
        } else {
            return null;
        }
    }
}
