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

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.PaymentConsentUtil;

/**
 * Class to handle bulk payments initiation request handler.
 */
public class BulkPaymentInitiationRequestHandler extends PaymentInitiationRequestHandler {

    private static final Log log = LogFactory.getLog(BulkPaymentInitiationRequestHandler.class);

    @Override
    protected void validateRequestPayload(JSONObject payload) {

        CommonConfigParser configParser = CommonConfigParser.getInstance();
        String maxPaymentExecutionDays = configParser.getMaxFuturePaymentDays();
        PaymentConsentUtil.validateDebtorAccount(payload);

        if (payload.get(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE) != null
                && payload.get(ConsentExtensionConstants.REQUESTED_EXECUTION_TIME) != null) {
            log.error(ErrorConstants.EXECUTION_DATE_TIME_ERROR);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.EXECUTION_DATE_TIME_ERROR));
        }

        PaymentConsentUtil.validateRequestedExecutionDate(payload, maxPaymentExecutionDays);

        JSONArray payments;

        log.debug("Validate presence of payment objects");
        if (payload.get(ConsentExtensionConstants.PAYMENTS) == null) {
            log.error(ErrorConstants.NO_PAYMENTS_IN_BODY);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.NO_PAYMENTS_IN_BODY));
        } else {
            payments = (JSONArray) payload.get(ConsentExtensionConstants.PAYMENTS);
            if (payments.size() == 0) {
                log.error(ErrorConstants.EMPTY_PAYMENTS_ELEMENT);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.EMPTY_PAYMENTS_ELEMENT));
            }
        }

        // Validate each payment object present in bulk payments payload
        PaymentConsentUtil.validatePaymentElements(payments);
    }
}
