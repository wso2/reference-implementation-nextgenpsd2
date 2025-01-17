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

package org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.openbanking.berlin.common.constants.CommonConstants;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.request.factory.RequestHandlerFactory;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;

/**
 * Handle the Payments service requests.
 */
public class PaymentServiceHandler implements ServiceHandler {

    private static final Log log = LogFactory.getLog(PaymentServiceHandler.class);
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

    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {

        if (StringUtils.equals(ConsentExtensionConstants.PAYMENT_CONSENT_UPDATE_PATH,
                consentManageData.getRequestPath())) {
            ConsentCoreServiceImpl coreService = getConsentService();
            String consentId = ((JSONObject) consentManageData.getPayload())
                    .getAsString(CommonConstants.CONSENT_ID);
            int deleteResponseStatusCode = Integer.parseInt(((JSONObject) consentManageData.getPayload())
                    .getAsString(CommonConstants.STATUS_CODE));

            try {
                if (deleteResponseStatusCode == HttpStatus.SC_ACCEPTED) {
                    coreService.updateConsentStatus(consentId, TransactionStatusEnum.ACTC.name());
                    consentManageData.setResponseStatus(ResponseStatus.OK);
                    consentManageData.setResponsePayload("{}");
                } else if (deleteResponseStatusCode == HttpStatus.SC_NO_CONTENT) {
                    coreService.updateConsentStatus(consentId, TransactionStatusEnum.CANC.name());
                    consentManageData.setResponseStatus(ResponseStatus.OK);
                    consentManageData.setResponsePayload("{}");
                } else {
                    log.debug("Consent status not updated during payment delete request");
                }
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.CONSENT_UPDATE_ERROR, e);
            }
        }
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
