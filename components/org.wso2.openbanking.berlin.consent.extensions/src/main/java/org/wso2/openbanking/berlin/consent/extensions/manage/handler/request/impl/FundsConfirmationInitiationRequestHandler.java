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

package org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.common.utils.CommonUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.HeaderValidator;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.FundsConfirmationConsentUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handle Funds Confirmation initiation request.
 */
public class FundsConfirmationInitiationRequestHandler implements RequestHandler {

    private static final Log log = LogFactory.getLog(FundsConfirmationInitiationRequestHandler.class);

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
                    ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), ConsentStatusEnum.RECEIVED.toString());

            String authStatus = CommonConsentUtil.getAuthorizationStatus(isSCARequired, isExplicitAuth, headersMap);
            try {
                createdConsent = consentCoreService.createAuthorizableConsent(consentResource,
                        headersMap.get(ConsentExtensionConstants.PSU_ID_HEADER),
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

            consentManageData.setResponsePayload(FundsConfirmationConsentUtil
                    .constructFundsConfirmationInitiationResponse(consentManageData, createdConsent, isExplicitAuth,
                            true, apiVersion, isSCARequired));
            consentManageData.setResponseStatus(ResponseStatus.CREATED);
        }
    }

    /**
     * Validates funds confirmation consent request headers. This method can be overridden to do header validations
     * by different Berlin based specifications.
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
    }

    /**
     * Validates funds confirmation consent payloads. This method can be overridden to validate payloads by
     * different Berlin based specifications.
     *
     * @param payload request payload
     */
    protected void validateRequestPayload(JSONObject payload) {

        FundsConfirmationConsentUtil.validateFundsConfirmationInitiationPayload(payload);
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

        if (!isExplicitAuth) {
            CommonConsentUtil.storeInitiationScaInfoToConsentAttributes(consentAttributesMap, createdConsent,
                    scaInfoMap);
        }

        if (headersMap.containsKey(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER)) {
            consentAttributesMap.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER,
                    headersMap.get(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER));
        }
        consentAttributesMap.put(CommonConsentUtil.constructAttributeKey(consentManageData.getRequestPath(),
                ConsentExtensionConstants.X_REQUEST_ID), headersMap.get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));
        return consentAttributesMap;
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
