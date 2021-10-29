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

package com.wso2.openbanking.berlin.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageHandler;
import com.wso2.openbanking.berlin.common.utils.ErrorConstants;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.common.utils.TPPMessage;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.HeaderValidator;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.factory.ServiceHandlerFactory;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Consent Manage handler implementation for Berlin.
 */
public class BerlinConsentManageHandler implements ConsentManageHandler {

    private static final Log log = LogFactory.getLog(BerlinConsentManageHandler.class);
    private ServiceHandler serviceHandler;

    @Override
    public void handleGet(ConsentManageData consentManageData) throws ConsentException {
        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handleGet(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
        }

    }

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        log.debug("Validating headers map");
        HeaderValidator.validateHeadersMap(consentManageData.getHeaders());

        log.debug("Validating the X-Request-ID header");
        HeaderValidator.validateXRequestId(consentManageData.getHeaders());
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                consentManageData.getHeaders().get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));

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

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handlePost(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
        }
    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {
        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handleDelete(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
        }
    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {
        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handlePut(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorConstants.PATH_INVALID);
        }
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, ErrorConstants.PATCH_NOT_SUPPORTED);
    }
}
