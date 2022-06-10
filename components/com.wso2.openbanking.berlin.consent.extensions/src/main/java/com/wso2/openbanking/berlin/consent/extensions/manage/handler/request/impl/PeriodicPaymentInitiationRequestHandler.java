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

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.PaymentConsentUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
