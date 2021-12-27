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

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.util;

import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Map;

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
     * @param payload JSON payload
     * @return true if payload is valid
     */
    public static boolean isPayloadValid(DetailedConsentResource detailedConsentResource,
                                         ConsentValidationResult consentValidationResult, JSONObject payload) {

        log.debug("Checking whether the payload is present");
        if (payload == null) {
            log.error(ErrorConstants.PAYLOAD_NOT_PRESENT_ERROR);
            consentValidationResult.setHttpCode(ResponseStatus.BAD_REQUEST.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.PAYLOAD_NOT_PRESENT_ERROR));
            return false;
        }

        log.debug("Validating mandatory request body elements");
        if (!payload.containsKey(ConsentExtensionConstants.ACCOUNT)
                || !payload.containsKey(ConsentExtensionConstants.INSTRUCTED_AMOUNT)) {
            log.error(ErrorConstants.MANDATORY_ELEMENTS_MISSING);
            consentValidationResult.setHttpCode(ResponseStatus.BAD_REQUEST.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.MANDATORY_ELEMENTS_MISSING));
            return false;
        }

        log.debug("Validating account");
        boolean isAccountMappingValid = false;
        JSONObject accountRefObject = (JSONObject) payload.get(ConsentExtensionConstants.ACCOUNT);
        Map<String, String> consentAttributes = detailedConsentResource.getConsentAttributes();
        ArrayList<ConsentMappingResource> mappingResources = detailedConsentResource.getConsentMappingResources();
        if (CommonValidationUtil.hasAnyActiveMappingResource(mappingResources)) {
            ConsentMappingResource mappingResource = mappingResources.get(0);
            if (isAccountMappingValid(accountRefObject, mappingResource, consentAttributes)) {
                isAccountMappingValid = true;
            }
        }

        if (!isAccountMappingValid) {
            log.error(ErrorConstants.NO_VALID_ACCOUNTS_FOR_CONSENT);
            consentValidationResult.setHttpCode(ResponseStatus.UNAUTHORIZED.getStatusCode());
            consentValidationResult.setModifiedPayload(ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.NO_VALID_ACCOUNTS_FOR_CONSENT));
            return false;
        }

        return true;
    }

    private static boolean isAccountMappingValid(JSONObject accountRefObject, ConsentMappingResource mappingResource,
                                                 Map<String, String> consentAttributes) {

        if (!StringUtils.equals(mappingResource.getMappingStatus(), ConsentExtensionConstants.ACTIVE)) {
            return false;
        }

        String configuredAccReferenceType = CommonConfigParser.getInstance().getAccountReferenceType();
        String accountNumber = accountRefObject.getAsString(configuredAccReferenceType);
        String accountCurrency = accountRefObject.getAsString(ConsentExtensionConstants.CURRENCY);
        boolean hasCurrency = StringUtils.isNotBlank(accountCurrency);
        boolean isAccountNumberMatching = StringUtils.equals(accountNumber, mappingResource.getAccountID());

        if (hasCurrency) {
            String mappedCurrency = consentAttributes.get(mappingResource.getMappingID());
            boolean isMappedCurrencyMatching = StringUtils.equals(accountCurrency, mappedCurrency);
            return isAccountNumberMatching && isMappedCurrencyMatching;
        } else {
            return isAccountNumberMatching;
        }
    }

}
