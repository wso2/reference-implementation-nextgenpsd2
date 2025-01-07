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

import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;

import java.util.ArrayList;

/**
 * Funds confirmation validation util class.
 */
public class FundsConfirmationValidationUtil {

    private static final Log log = LogFactory.getLog(FundsConfirmationValidationUtil.class);

    /**
     * Validates the payload.
     *
     * @param detailedConsentResource detailed consent resource
     * @param consentValidationResult validation result
     * @param payload                 JSON payload
     * @return true if payload is valid
     */
    public static boolean isPayloadValid(DetailedConsentResource detailedConsentResource,
                                         ConsentValidationResult consentValidationResult, JSONObject payload) {

        if (payload == null) {
            log.error(ErrorConstants.PAYLOAD_NOT_PRESENT_ERROR);
            consentValidationResult.setHttpCode(ResponseStatus.BAD_REQUEST.getStatusCode());
            consentValidationResult.setErrorCode(TPPMessage.CodeEnum.FORMAT_ERROR.toString());
            consentValidationResult.setErrorMessage(ErrorConstants.PAYLOAD_NOT_PRESENT_ERROR);
            return false;
        }

        if (!payload.containsKey(ConsentExtensionConstants.ACCOUNT)
                || !payload.containsKey(ConsentExtensionConstants.INSTRUCTED_AMOUNT)) {
            log.error(ErrorConstants.MANDATORY_ELEMENTS_MISSING);
            consentValidationResult.setHttpCode(ResponseStatus.BAD_REQUEST.getStatusCode());
            consentValidationResult.setErrorCode(TPPMessage.CodeEnum.FORMAT_ERROR.toString());
            consentValidationResult.setErrorMessage(ErrorConstants.MANDATORY_ELEMENTS_MISSING);
            return false;
        }

        boolean isAccountMappingValid = false;
        JSONObject accountRefObject = (JSONObject) payload.get(ConsentExtensionConstants.ACCOUNT);
        ArrayList<ConsentMappingResource> mappingResources = detailedConsentResource.getConsentMappingResources();
        if (CommonValidationUtil.hasAnyActiveMappingResource(mappingResources)) {
            if (CommonConfigParser.getInstance().isAccountIdValidationEnabledForCofConsent()) {
                ConsentMappingResource mappingResource = mappingResources.get(0);
                if (isAccountMappingValid(accountRefObject, mappingResource)) {
                    isAccountMappingValid = true;
                }
            } else {
                isAccountMappingValid = true;
            }
        }

        if (!isAccountMappingValid) {
            log.error(ErrorConstants.NO_VALID_ACCOUNTS_FOR_CONSENT);
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setErrorCode(TPPMessage.CodeEnum.CONSENT_INVALID.toString());
            consentValidationResult.setErrorMessage(ErrorConstants.NO_VALID_ACCOUNTS_FOR_CONSENT);
            return false;
        }

        return true;
    }

    private static boolean isAccountMappingValid(JSONObject accountRefObject, ConsentMappingResource mappingResource) {

        if (!StringUtils.equals(mappingResource.getMappingStatus(), ConsentExtensionConstants.ACTIVE)) {
            return false;
        }

        String accountReference = ConsentExtensionUtil.getAccountReferenceToPersist(accountRefObject);
        String mappingResourceAccountId = mappingResource.getAccountID();

        // Skipping maskedPan validation since this account reference type must be validated by the bank
        if (mappingResourceAccountId.contains(ConsentExtensionConstants.MASKED_PAN)) {
            return true;
        }

        return StringUtils.equals(mappingResourceAccountId, accountReference);
    }

}
