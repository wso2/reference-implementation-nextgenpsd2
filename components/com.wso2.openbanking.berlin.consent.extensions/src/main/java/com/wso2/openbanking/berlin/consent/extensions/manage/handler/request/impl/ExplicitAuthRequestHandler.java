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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.ScaApproach;
import com.wso2.openbanking.berlin.common.models.ScaMethod;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.HeaderValidator;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handle Explicit Authorisation creation request.
 */
public class ExplicitAuthRequestHandler implements RequestHandler {

    private static final Log log = LogFactory.getLog(ExplicitAuthRequestHandler.class);

    @Override
    public void handle(ConsentManageData consentManageData) throws ConsentException {

        DetailedConsentResource detailedConsentResource;
        AuthorizationResource createdAuthorizationResource;

        CommonConfigParser configParser = CommonConfigParser.getInstance();
        ConsentCoreService consentCoreService = getConsentService();

        Map<String, String> headersMap = consentManageData.getHeaders();
        JSONObject requestPayload = (JSONObject) consentManageData.getPayload();
        boolean isSCARequired = configParser.isScaRequired();
        String requestPath = consentManageData.getRequestPath();
        String consentType = ConsentExtensionUtil.getConsentTypeFromRequestPath(requestPath);
        String consentId = ConsentExtensionUtil.getValidatedConsentIdFromRequestPath(consentManageData.getRequest()
                .getMethod(), requestPath, consentType);

        validateRequestHeaders(headersMap);
        validateRequestPayload(requestPayload);

        Optional<Boolean> isRedirectPreferred = HeaderValidator.isTppRedirectPreferred(headersMap);
        String psuIdOfRequest = consentManageData.getHeaders().get(ConsentExtensionConstants.PSU_ID_HEADER);
        String tenantEnsuredPSUId = ConsentExtensionUtil.appendSuperTenantDomain(psuIdOfRequest);

        log.debug("Get detailed consent for the provided id");
        ConsentCoreServiceImpl coreService = new ConsentCoreServiceImpl();
        try {
            detailedConsentResource = coreService.getDetailedConsent(consentId);
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_NOT_FOUND_ERROR, e);
            throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_UNKNOWN,
                    ErrorConstants.CONSENT_NOT_FOUND_ERROR));
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for valid client", consentId));
        }
        ConsentExtensionUtil.validateClient(consentManageData.getClientId(), detailedConsentResource.getClientID());

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for correct type", consentId));
        }
        ConsentExtensionUtil.validateConsentType(consentType, detailedConsentResource.getConsentType());

        log.debug("Check whether there is an already created authorisation resource for the same user");
        boolean isRetrievedConsentExplicit =
                Boolean.parseBoolean(detailedConsentResource.getConsentAttributes()
                        .get(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER));
        String apiVersion = configParser.getApiVersion(detailedConsentResource.getConsentType());

        boolean shouldCreateAuthorisationResource = false;
        AuthorizationResource authorizationResource = null;
        if (isRetrievedConsentExplicit) {
            log.debug("The consent is explicit");

            if (!isRedirectPreferred.isPresent() || BooleanUtils.isTrue(isRedirectPreferred.get())) {
                log.debug("SCA approach is Redirect SCA (OAuth2)");

                List<AuthorizationResource> explicitAuthResources = detailedConsentResource.getAuthorizationResources();

                if (explicitAuthResources.size() != 0) {
                    for (AuthorizationResource authResource : explicitAuthResources) {
                        if (StringUtils.equals(tenantEnsuredPSUId, authResource.getUserID())) {
                            // Handling explicit idempotency scenario
                            if (log.isDebugEnabled()) {
                                log.debug(String.format("Authorisation resource for user %s already exists",
                                        tenantEnsuredPSUId));
                            }
                            authorizationResource = authResource;
                            break;
                        }
                    }
                    if (authorizationResource == null) {
                        log.debug("Authorisation resource does not exists");
                        shouldCreateAuthorisationResource = true;
                    }
                } else {
                    log.debug("Authorisation resource does not exists");
                    shouldCreateAuthorisationResource = true;
                }
            }
        } else {
            log.error(ErrorConstants.IMPLICIT_CONSENT_START_AUTHORISATION);
            throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_UNKNOWN,
                    ErrorConstants.IMPLICIT_CONSENT_START_AUTHORISATION));
        }

        if (shouldCreateAuthorisationResource) {
            log.debug("Creating new authorisation resource");

            String authType;
            if (StringUtils.contains(ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END,
                    requestPath)) {
                authType = AuthTypeEnum.CANCELLATION.toString();
            } else {
                authType = AuthTypeEnum.AUTHORISATION.toString();
            }

            authorizationResource = new AuthorizationResource();
            authorizationResource.setConsentID(consentId);
            authorizationResource.setAuthorizationType(authType);
            authorizationResource.setUserID(tenantEnsuredPSUId);
            authorizationResource.setAuthorizationStatus(ScaStatusEnum.RECEIVED.toString());

            try {
                createdAuthorizationResource = consentCoreService.createConsentAuthorization(authorizationResource);
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.START_AUTHORISATION_RESOURCE_CREATION_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            try {
                log.debug("Getting SCA information from the config");
                Map<String, Object> scaInfoMap = CommonUtil.getScaApproachAndMethods(true,
                        isSCARequired);

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Storing consent attributes against consent: %s", consentId));
                }
                Map<String, String> newConsentAttributes = getConsentAttributesToPersist(consentManageData,
                        createdAuthorizationResource, scaInfoMap);
                detailedConsentResource.getConsentAttributes().putAll(newConsentAttributes);
                consentCoreService.storeConsentAttributes(consentId, detailedConsentResource.getConsentAttributes());
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.CONSENT_ATTRIBUTE_INITIATION_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            consentManageData.setResponsePayload(CommonConsentUtil
                    .constructStartAuthorisationResponse(consentManageData, authorizationResource,
                            true, apiVersion, isSCARequired));
            consentManageData.setResponseStatus(ResponseStatus.CREATED);

        } else {

            consentManageData.setResponsePayload(CommonConsentUtil
                    .constructStartAuthorisationResponse(consentManageData, authorizationResource,
                            true, apiVersion, isSCARequired));
            consentManageData.setResponseStatus(ResponseStatus.CREATED);
        }
    }

    /**
     * Sets necessary attributes to be persisted as consent attributes. This method can be overridden to persist
     * needed consent attributes by different Berlin based specifications.
     *
     * @param consentManageData            consent manage data
     * @param createdAuthorizationResource authorization resource
     * @param scaInfoMap                   SCA details
     * @return map of consent attributes to store
     */
    protected Map<String, String> getConsentAttributesToPersist(ConsentManageData consentManageData,
                                                                AuthorizationResource createdAuthorizationResource,
                                                                Map<String, Object> scaInfoMap) {

        Map<String, String> consentAttributesMap = new HashMap<>();

        String authId = createdAuthorizationResource.getAuthorizationID();
        ScaApproach scaApproach = (ScaApproach) scaInfoMap.get(CommonConstants.SCA_APPROACH_KEY);

        String scaApproachKey = ConsentExtensionUtil
                .getConsentAttributeKey(CommonConstants.SCA_APPROACH_KEY, authId);
        consentAttributesMap.put(scaApproachKey, scaApproach.getApproach().toString());

        ScaMethod scaMethod = CommonUtil.getScaMethod(scaApproach.getApproach());
        if (scaMethod != null) {
            String scaMethodKey = ConsentExtensionUtil
                    .getConsentAttributeKey(CommonConstants.SCA_METHOD_KEY, authId);
            consentAttributesMap.put(scaMethodKey, scaMethod.getAuthenticationMethodId());
        }

        return consentAttributesMap;
    }

    /**
     * Validates start authorisation request headers. This method can be overridden to do header
     * validations by different Berlin based specifications.
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
     * Validates start authorisation payloads. This method can be overridden to validate payloads by different Berlin
     * based specifications.
     *
     * @param payload request payload
     */
    protected void validateRequestPayload(JSONObject payload) {

    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
