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

package org.wso2.openbanking.berlin.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.factory.ServiceHandlerFactory;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;

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

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handlePut(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {
        log.error(ErrorConstants.PATCH_NOT_SUPPORTED);
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, ErrorConstants.PATCH_NOT_SUPPORTED);
    }

}
