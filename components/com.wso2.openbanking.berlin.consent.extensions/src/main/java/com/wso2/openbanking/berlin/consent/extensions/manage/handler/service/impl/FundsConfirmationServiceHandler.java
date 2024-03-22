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
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.factory.RequestHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.FundsConfirmationConsentUtil;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Handle the Funds Confirmations service requests.
 */
public class FundsConfirmationServiceHandler implements ServiceHandler {

    private static final Log log = LogFactory.getLog(FundsConfirmationServiceHandler.class);
    private RequestHandler requestHandler;

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        log.debug("Checking whether the payload is present");
        if (consentManageData.getPayload() == null) {
            log.error(ErrorConstants.PAYLOAD_NOT_PRESENT_ERROR);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_NOT_PRESENT_ERROR));
        }

        log.debug("Checking whether the payload is a JSON");
        Object payload = consentManageData.getPayload();
        if (!(payload instanceof JSONObject)) {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_FORMAT_ERROR));
        }

        // Check Idempotency
        if (CommonConsentUtil.isIdempotent(consentManageData)) {
            return;
        }

        requestHandler = RequestHandlerFactory.getRequestHandler(consentManageData.getRequestPath());

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

        ConsentResource consentResource;
        String requestPath = consentManageData.getRequestPath();
        String consentType = ConsentExtensionUtil.getConsentTypeFromRequestPath(requestPath);
        String consentId = ConsentExtensionUtil
                .getValidatedConsentIdFromRequestPath(consentManageData.getRequest().getMethod(), requestPath,
                        consentType);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Get %s consent using core service", consentType));
        }
        ConsentCoreServiceImpl coreService = getConsentService();
        try {
            consentResource = coreService.getConsent(consentId, false);
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_NOT_FOUND_ERROR, e);
            throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_UNKNOWN,
                    ErrorConstants.CONSENT_NOT_FOUND_ERROR));
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for valid client", consentId));
        }
        ConsentExtensionUtil.validateClient(consentManageData.getClientId(), consentResource.getClientID());

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for correct type", consentId));
        }
        ConsentExtensionUtil.validateConsentType(consentType, consentResource.getConsentType());

        if (StringUtils.contains(requestPath, ConsentExtensionConstants.STATUS)) {
            consentManageData.setResponsePayload(ConsentExtensionUtil.getConsentStatusResponse(consentResource,
                    consentType));
        } else {
            try {
                consentManageData.setResponsePayload(FundsConfirmationConsentUtil
                        .constructFundsConfirmationConsentGetResponse(consentResource));
            } catch (ParseException e) {
                log.error(ErrorConstants.RESPONSE_CONSTRUCT_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.RESPONSE_CONSTRUCT_ERROR);
            }
        }
        consentManageData.setResponseStatus(ResponseStatus.OK);
    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        DetailedConsentResource consentResource;
        String requestPath = consentManageData.getRequestPath();
        String consentType = ConsentExtensionUtil.getConsentTypeFromRequestPath(requestPath);
        String consentId = ConsentExtensionUtil.getValidatedConsentIdFromRequestPath(consentManageData.getRequest()
                .getMethod(), requestPath, consentType);

        log.debug("Get existing consent resource for provided consent Id");
        ConsentCoreServiceImpl coreService = getConsentService();
        try {
            consentResource = coreService.getDetailedConsent(consentId);
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_NOT_FOUND_ERROR, e);
            throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_UNKNOWN,
                    ErrorConstants.CONSENT_NOT_FOUND_ERROR));
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for valid client", consentId));
        }
        ConsentExtensionUtil.validateClient(consentManageData.getClientId(), consentResource.getClientID());

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for correct type", consentId));
        }
        ConsentExtensionUtil.validateConsentType(consentType, consentResource.getConsentType());

        log.debug("Send an error if the consent is already deleted");
        if (StringUtils.equals(ConsentStatusEnum.REVOKED_BY_PSU.toString(), consentResource.getCurrentStatus())
                || StringUtils.equals(ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                consentResource.getCurrentStatus())) {
            log.error(ErrorConstants.CONSENT_ALREADY_DELETED);
            throw new ConsentException(ResponseStatus.UNAUTHORIZED, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.CONSENT_ALREADY_DELETED));
        }

        log.debug("Deleting consent resource and updating status");
        try {
            coreService.revokeConsent(consentId, ConsentStatusEnum.TERMINATED_BY_TPP.toString());

            if (log.isDebugEnabled()) {
                log.debug("Deactivating account mappings of revoked consent: " + consentId);
            }
            ArrayList<ConsentMappingResource> mappingResources = consentResource.getConsentMappingResources();
            ArrayList<String> mappingIds = new ArrayList<>();
            for (ConsentMappingResource mappingResource : mappingResources) {
                mappingIds.add(mappingResource.getMappingID());
            }
            if (CollectionUtils.isNotEmpty(mappingIds)) {
                coreService.deactivateAccountMappings(mappingIds);
            }
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_UPDATE_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_UPDATE_ERROR);
        }
        consentManageData.setResponseStatus(ResponseStatus.NO_CONTENT);
    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {

    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
