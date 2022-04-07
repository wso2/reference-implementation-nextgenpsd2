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
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.factory.ServiceHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;
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

        log.debug("Validating the X-Request-ID header");
        CommonConsentUtil.validateXRequestId(consentManageData.getHeaders());
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                consentManageData.getHeaders().get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handleGet(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }

    }

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        log.debug("Validating the X-Request-ID header");
        CommonConsentUtil.validateXRequestId(consentManageData.getHeaders());
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                consentManageData.getHeaders().get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handlePost(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        log.debug("Validating the X-Request-ID header");
        CommonConsentUtil.validateXRequestId(consentManageData.getHeaders());
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                consentManageData.getHeaders().get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handleDelete(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {
        log.error(ErrorConstants.PUT_NOT_SUPPORTED);
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, ErrorConstants.PUT_NOT_SUPPORTED);
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {
        log.error(ErrorConstants.PATCH_NOT_SUPPORTED);
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, ErrorConstants.PATCH_NOT_SUPPORTED);
    }

}
