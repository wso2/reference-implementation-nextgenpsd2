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

package org.wso2.openbanking.berlin.consent.extensions.validate.validator.util;

import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.apache.commons.lang3.StringUtils;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;

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
