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

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.factory.RequestHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.AccountConsentUtil;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handle the Accounts service requests.
 */
public class AccountServiceHandler implements ServiceHandler {

    private static final Log log = LogFactory.getLog(AccountServiceHandler.class);
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
        ConsentCoreServiceImpl coreService = new ConsentCoreServiceImpl();
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

        ConsentResource updatedConsentResource = null;
        try {
            if (AccountConsentUtil.isConsentExpired(consentResource, consentResource.getUpdatedTime())) {
                log.debug("The Consent is expired");
                try {
                    updatedConsentResource = coreService.updateConsentStatus(consentId,
                            ConsentStatusEnum.EXPIRED.name());
                } catch (ConsentManagementException e) {
                    log.error(ErrorConstants.CONSENT_UPDATE_ERROR, e);
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            ErrorConstants.CONSENT_UPDATE_ERROR);
                }
            }

            if (updatedConsentResource != null) {
                consentResource = updatedConsentResource;
            }

            if (StringUtils.contains(requestPath, ConsentExtensionConstants.STATUS)) {
                consentManageData.setResponsePayload(ConsentExtensionUtil.getConsentStatusResponse(consentResource,
                        consentType));
            } else {
                consentManageData.setResponsePayload(AccountConsentUtil
                        .constructAccountConsentGetResponse(consentResource));
            }
        } catch (ParseException e) {
            log.error(ErrorConstants.RESPONSE_CONSTRUCT_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.RESPONSE_CONSTRUCT_ERROR);
        }
        consentManageData.setResponseStatus(ResponseStatus.OK);
    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        ConsentResource consentResource;
        String requestPath = consentManageData.getRequestPath();
        String consentType = ConsentExtensionUtil.getConsentTypeFromRequestPath(requestPath);
        String consentId = ConsentExtensionUtil.getValidatedConsentIdFromRequestPath(consentManageData.getRequest()
                .getMethod(), requestPath, ConsentTypeEnum.ACCOUNTS.toString());

        log.debug("Get existing consent resource for provided consent Id");
        ConsentCoreServiceImpl coreService = new ConsentCoreServiceImpl();
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

        log.debug("Send an error if the consent is already deleted");
        if (StringUtils.equals(ConsentStatusEnum.REVOKED_BY_PSU.toString(), consentResource.getCurrentStatus())
                || StringUtils.equals(ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                consentResource.getCurrentStatus())) {
            log.error(ErrorConstants.CONSENT_ALREADY_DELETED);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.INVALID_STATUS_VALUE,
                    ErrorConstants.CONSENT_ALREADY_DELETED));
        }

        log.debug("Deleting consent resource and updating status");
        try {
            // TODO: https://github.com/wso2-enterprise/financial-open-banking/issues/6875
            coreService.revokeConsent(consentId, ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                    "Deleted Account consent");
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
}
