/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.util;

import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Common validation util class.
 */
public class CommonValidationUtil {

    /**
     * Checks if the there is any active mapping resource.
     *
     * @param mappingResources mapping resources
     * @return true if there is any active mapping resource
     */
    public static boolean hasAnyActiveMappingResource(ArrayList<ConsentMappingResource> mappingResources) {

        if (mappingResources == null || mappingResources.isEmpty()) {
            return false;
        }

        for (ConsentMappingResource mappingResource : mappingResources) {
            if (StringUtils.equals(mappingResource.getMappingStatus(), ConsentExtensionConstants.ACTIVE)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method sets error details of consent validation errors to consent validation result.
     *
     * @param consentValidationResult consent validation result object
     * @param statusCode status code of the error
     * @param errorCode error code (Berlin error code)
     * @param errorMessage error message
     */
    public static void handleConsentValidationError(ConsentValidationResult consentValidationResult, int statusCode,
                                                    String errorCode, String errorMessage) {

        consentValidationResult.setHttpCode(statusCode);
        consentValidationResult.setErrorCode(errorCode);
        consentValidationResult.setErrorMessage(errorMessage);
    }

}
