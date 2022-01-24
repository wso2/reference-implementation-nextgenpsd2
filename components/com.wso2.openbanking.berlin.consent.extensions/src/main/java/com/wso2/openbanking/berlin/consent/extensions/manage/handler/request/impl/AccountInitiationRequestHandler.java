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

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.HeaderValidator;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.AccountConsentUtil;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handle Accounts initiation request.
 */
public class AccountInitiationRequestHandler implements RequestHandler {

    private static final Log log = LogFactory.getLog(AccountInitiationRequestHandler.class);

    @Override
    public void handle(ConsentManageData consentManageData) throws ConsentException {

        DetailedConsentResource createdConsent;

        CommonConfigParser configParser = CommonConfigParser.getInstance();
        ConsentCoreService consentCoreService = getConsentService();

        Map<String, String> headersMap = consentManageData.getHeaders();
        JSONObject requestPayload = (JSONObject) consentManageData.getPayload();
        String clientId = consentManageData.getClientId();
        boolean isSCARequired = configParser.isScaRequired();

        validateRequestHeaders(headersMap);
        validateRequestPayload(requestPayload);

        boolean isExplicitAuth = HeaderValidator.isTppExplicitAuthorisationPreferred(headersMap);

        Optional<Boolean> isRedirectPreferred = HeaderValidator.isTppRedirectPreferred(headersMap);

        if (log.isDebugEnabled()) {
            log.debug(String.format("The consent initiation is an %s initiation",
                    isExplicitAuth ? ConsentExtensionConstants.EXPLICIT : ConsentExtensionConstants.IMPLICIT));
        }
        if (!isRedirectPreferred.isPresent() || BooleanUtils.isTrue(isRedirectPreferred.get())) {
            log.debug("SCA approach is Redirect SCA (OAuth2)");

            ConsentResource consentResource = new ConsentResource(clientId, requestPayload.toJSONString(),
                    ConsentTypeEnum.ACCOUNTS.toString(), ConsentStatusEnum.RECEIVED.toString());

            // Setting additional properties to consent resource
            consentResource.setRecurringIndicator(Boolean.parseBoolean(requestPayload
                    .getAsString(ConsentExtensionConstants.RECURRING_INDICATOR)));
            consentResource.setConsentFrequency((Integer) requestPayload
                    .getAsNumber(ConsentExtensionConstants.FREQUENCY_PER_DAY));

            String validUntilString = requestPayload.getAsString(ConsentExtensionConstants.VALID_UNTIL);
            consentResource.setValidityPeriod(AccountConsentUtil.convertToUtcTimestamp(validUntilString));

            String tenantEnsuredPSUId = ConsentExtensionUtil
                    .appendSuperTenantDomain(headersMap.get(ConsentExtensionConstants.PSU_ID_HEADER));
            String authStatus = isExplicitAuth ? null : ScaStatusEnum.RECEIVED.toString();
            try {
                createdConsent = consentCoreService.createAuthorizableConsent(consentResource, tenantEnsuredPSUId,
                        authStatus, AuthTypeEnum.AUTHORISATION.toString(),
                        !isExplicitAuth);
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.CONSENT_INITIATION_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            try {
                log.debug("Getting SCA information from the config");
                Map<String, Object> scaInfoMap = CommonUtil.getScaApproachAndMethods(true,
                        isSCARequired);

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Storing consent attributes against consent: %s",
                            consentResource.getConsentID()));
                }
                consentCoreService.storeConsentAttributes(createdConsent.getConsentID(),
                        getConsentAttributesToPersist(consentManageData, createdConsent, scaInfoMap, isExplicitAuth));
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.CONSENT_ATTRIBUTE_INITIATION_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            String apiVersion = configParser.getApiVersion(consentResource.getConsentType());

            consentManageData.setResponsePayload(AccountConsentUtil
                    .constructAccountInitiationResponse(consentManageData, createdConsent, isExplicitAuth,
                            true, apiVersion, isSCARequired));
            consentManageData.setResponseStatus(ResponseStatus.CREATED);
        }
    }

    /**
     * Validates account consent request headers. This method can be overridden to do header validations by different
     * Berlin based specifications.
     *
     * @param headersMap headers map
     * @throws ConsentException
     */
    protected void validateRequestHeaders(Map<String, String> headersMap) throws ConsentException {

        /*
         * 1). If the header is true, the REDIRECT SCA Approach must be configured in open-banking.xml as the
         * supported SCA approach.
         *
         * 2). If the header is false, the DECOUPLED SCA Approach must be configured in open-banking.xml (Currently
         * not supported by the toolkit).
         *
         * 3) If the header is not present, the TPP/PSU should decide what is the SCA Approach based on the
         * configured SCA methods by the ASPSP (Currently only one SCA method is supported to be configured in
         * open-banking.xml). Therefore SCA method choosing is not yet supported by the toolkit.
         */
        HeaderValidator.validateTppRedirectPreferredHeader(headersMap);

        HeaderValidator.validatePsuIpAddress(headersMap);
    }

    /**
     * Validates account consent payloads. This method can be overridden to validate payloads by different Berlin
     * based specifications.
     *
     * @param payload request payload
     */
    protected void validateRequestPayload(JSONObject payload) {

        CommonConfigParser configParser = CommonConfigParser.getInstance();
        AccountConsentUtil.validateAccountInitiationPayload(payload, configParser.getConfiguredMinimumFreqPerDay(),
                configParser.isValidUntilDateCapEnabled(), configParser.validUntilDaysCap());
    }

    /**
     * Sets necessary attributes to be persisted as consent attributes. This method can be overridden to persist
     * needed consent attributes by different Berlin based specifications.
     *
     * @param consentManageData consent manage data
     * @param scaInfoMap        SCA details
     * @param isExplicitAuth    if explicit is preferred or not
     * @return map of consent attributes to store
     */
    protected Map<String, String> getConsentAttributesToPersist(ConsentManageData consentManageData,
                                                                DetailedConsentResource createdConsent,
                                                                Map<String, Object> scaInfoMap,
                                                                boolean isExplicitAuth) {

        Map<String, String> headersMap = consentManageData.getHeaders();

        Map<String, String> consentAttributesMap = new HashMap<>();

        // Storing permission
        consentAttributesMap.put(ConsentExtensionConstants.PERMISSION, AccountConsentUtil.getPermission());

        // Storing consent expire attribute to expire in background
        consentAttributesMap.put(ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE,
                Long.toString(createdConsent.getValidityPeriod()));

        if (!isExplicitAuth) {
            CommonConsentUtil.storeInitiationScaInfoToConsentAttributes(consentAttributesMap, createdConsent,
                    scaInfoMap);
        }

        if (headersMap.containsKey(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER)) {
            consentAttributesMap.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER,
                    headersMap.get(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER));
        }
        return consentAttributesMap;
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
