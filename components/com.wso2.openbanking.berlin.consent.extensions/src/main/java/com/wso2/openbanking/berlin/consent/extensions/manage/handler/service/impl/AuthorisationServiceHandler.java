/**
 * Copyright (c) 2021-2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.factory.RequestHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.PaymentConsentUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Handles authorisation service related requests.
 */
public class AuthorisationServiceHandler implements ServiceHandler {

    private static final Log log = LogFactory.getLog(AuthorisationServiceHandler.class);

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        RequestHandler requestHandler = RequestHandlerFactory.getRequestHandler(consentManageData.getRequestPath());

        // Check Idempotency
        if (CommonConsentUtil.isIdempotent(consentManageData)) {
            return;
        }

        if (requestHandler != null) {
            requestHandler.handle(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

    @Override
    public void handleGet(ConsentManageData consentManageData) throws ConsentException {

        DetailedConsentResource detailedConsentResource;
        String requestPath = consentManageData.getRequestPath();
        String consentType = ConsentExtensionUtil.getConsentTypeFromRequestPath(requestPath);
        String consentId = ConsentExtensionUtil
                .getValidatedConsentIdFromRequestPath(consentManageData.getRequest().getMethod(), requestPath,
                        consentType);
        boolean isCancellationRequest =
                StringUtils.contains(requestPath,
                        ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Get %s detailed consent", consentType));
        }
        ConsentCoreServiceImpl coreService = getConsentService();
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

        if (log.isDebugEnabled()) {
            log.debug(String.format("Get authorization resources belong to consent of Id: %s", consentId));
        }
        ArrayList<AuthorizationResource> authorizationResources = detailedConsentResource.getAuthorizationResources();
        if (authorizationResources == null || authorizationResources.size() == 0) {
            log.error(ErrorConstants.AUTHORISATIONS_NOT_FOUND);
            throw new ConsentException(ResponseStatus.UNAUTHORIZED, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.AUTHORISATIONS_NOT_FOUND));
        }

        if (isCancellationRequest) {
            authorizationResources = PaymentConsentUtil.filterAuthorizations(authorizationResources,
                    AuthTypeEnum.CANCELLATION);
        } else {
            authorizationResources = PaymentConsentUtil.filterAuthorizations(authorizationResources,
                    AuthTypeEnum.AUTHORISATION);
        }

        // Flag to check if request is a auth sub-resource status request
        boolean isAuthSubResourceIdsListPath = StringUtils.endsWith(requestPath,
                ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END)
                || StringUtils.endsWith(requestPath,
                ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END);
        if (isAuthSubResourceIdsListPath) {
            consentManageData.setResponsePayload(ConsentExtensionUtil
                    .getAuthorisationGetResponse(authorizationResources));
        } else {
            String[] requestPathArray = requestPath.split("/");
            String providedAuthId = requestPathArray[requestPathArray.length - 1];

            if (log.isDebugEnabled()) {
                log.debug(String.format("The provided auth ID is %s", providedAuthId));
            }

            // Validating if the authorisation Id belongs to the consent
            boolean isAuthIdValid = false;
            for (AuthorizationResource authorizationResource: authorizationResources) {
                if (StringUtils.equals(authorizationResource.getAuthorizationID(), providedAuthId)) {
                    isAuthIdValid = true;
                    break;
                }
            }

            if (!isAuthIdValid) {
                log.error(ErrorConstants.INVALID_AUTHORISATION_ID);
                throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_UNKNOWN,
                        ErrorConstants.INVALID_AUTHORISATION_ID));
            }

            consentManageData.setResponsePayload(ConsentExtensionUtil
                    .getAuthorisationGetStatusResponse(authorizationResources, providedAuthId));
        }
        consentManageData.setResponseStatus(ResponseStatus.OK);
    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {
        log.error(ErrorConstants.DELETE_NOT_SUPPORTED);
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, ErrorConstants.DELETE_NOT_SUPPORTED);
    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {

    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
