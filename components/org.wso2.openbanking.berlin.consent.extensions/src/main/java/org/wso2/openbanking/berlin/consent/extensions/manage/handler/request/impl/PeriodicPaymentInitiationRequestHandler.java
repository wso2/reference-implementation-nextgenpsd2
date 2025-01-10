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
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.PaymentConsentUtil;

import java.time.LocalDate;

/**
 * Class to handle periodic payments initiation request.
 */
public class PeriodicPaymentInitiationRequestHandler extends PaymentInitiationRequestHandler {

    private static final Log log = LogFactory.getLog(PeriodicPaymentInitiationRequestHandler.class);

    @Override
    protected void validateRequestPayload(JSONObject payload) {

        LocalDate startDate;
        PaymentConsentUtil.validateDebtorAccount(payload);
        PaymentConsentUtil.validateCommonPaymentElements(payload);
        PaymentConsentUtil.validateDayOfExecution(payload);

        log.debug("Validating periodic payments payload for start date");
        if (payload.get(ConsentExtensionConstants.START_DATE) == null
                || StringUtils.isBlank(payload.getAsString(ConsentExtensionConstants.START_DATE))) {
            log.error(ErrorConstants.START_DATE_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.START_DATE_MISSING));
        } else {
            log.debug("Validating start date for correct date format");
            startDate = ConsentExtensionUtil.parseDateToISO((String) payload.get(ConsentExtensionConstants.START_DATE),
                    TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.START_DATE_INVALID);

            log.debug("Validating whether the start date is a future date");
            PaymentConsentUtil.validateFutureDate(startDate, ErrorConstants.START_DATE_NOT_FUTURE);
        }

        log.debug("Validating periodic payments payload for frequency");
        if (payload.get(ConsentExtensionConstants.FREQUENCY) == null
                || StringUtils.isBlank(payload.getAsString(ConsentExtensionConstants.FREQUENCY))) {
            log.error(ErrorConstants.FREQUENCY_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.FREQUENCY_MISSING));
        }

        if (!ConsentExtensionConstants.SUPPORTED_PERIODIC_PAYMENT_FREQUENCY_CODES.contains(
                payload.get(ConsentExtensionConstants.FREQUENCY).toString())) {
            log.error(ErrorConstants.FREQUENCY_UNSUPPORTED);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.FREQUENCY_UNSUPPORTED));
        }

        if (payload.get(ConsentExtensionConstants.END_DATE) != null && StringUtils.isNotBlank(payload.getAsString(
                ConsentExtensionConstants.END_DATE))) {
            log.debug("Validating whether periodic payments end date if a future date");
            LocalDate endDate =
                    ConsentExtensionUtil.parseDateToISO((String) payload.get(ConsentExtensionConstants.END_DATE),
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.END_DATE_NOT_VALID);
            PaymentConsentUtil.validateFutureDate(endDate, ErrorConstants.END_DATE_NOT_FUTURE);
            PaymentConsentUtil.areDatesValid(startDate, endDate);
        }

        if (payload.get(ConsentExtensionConstants.EXECUTION_RULE) != null && StringUtils.isNotBlank(payload.getAsString(
                ConsentExtensionConstants.EXECUTION_RULE))) {
            log.debug("Validating execution rule");
            String executionRule = payload.getAsString(ConsentExtensionConstants.EXECUTION_RULE);
            if (!(StringUtils.equals(ConsentExtensionConstants.FOLLOWING_EXECUTION_RULE, executionRule)
                    || StringUtils.equals(ConsentExtensionConstants.PRECEDING_EXECUTION_RULE, executionRule))) {
                log.error(ErrorConstants.INVALID_EXECUTION_RULE);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.INVALID_EXECUTION_RULE));
            }
        }
    }
}
