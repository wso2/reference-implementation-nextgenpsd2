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
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.PaymentConsentUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Class to handle bulk payments initiation request handler.
 */
public class BulkPaymentInitiationRequestHandler extends PaymentInitiationRequestHandler {

    private static final Log log = LogFactory.getLog(BulkPaymentInitiationRequestHandler.class);

    @Override
    protected void validateRequestPayload(JSONObject payload) {

        CommonConfigParser configParser = CommonConfigParser.getInstance();
        String maxPaymentExecutionDays = configParser.getMaxFuturePaymentDays();
        PaymentConsentUtil.validateDebtorAccount(payload, configParser.getAccountReferenceType());

        log.debug("Validating requested execution date");
        if (payload.get(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE) != null
                && StringUtils.isNotBlank(payload.getAsString(ConsentExtensionConstants.REQUESTED_EXECUTION_DATE))) {

            LocalDate requestedExecutionDate =
                    PaymentConsentUtil.parseDateToISO((String) payload.get(ConsentExtensionConstants
                                    .REQUESTED_EXECUTION_DATE), TPPMessage.CodeEnum.EXECUTION_DATE_INVALID,
                            ErrorConstants.REQUESTED_EXECUTION_DATE_INVALID);

            LocalDate today = LocalDate.now();

            if (!requestedExecutionDate.isAfter(LocalDate.now())) {
                log.error(ErrorConstants.EXECUTION_DATE_NOT_FUTURE);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                        TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.EXECUTION_DATE_NOT_FUTURE));
            } else if (StringUtils.isNotBlank(maxPaymentExecutionDays)) {
                long maxNumberPaymentExecutionDays = Long.parseLong(maxPaymentExecutionDays);
                if (ChronoUnit.DAYS.between(today.plusDays(maxNumberPaymentExecutionDays),
                        requestedExecutionDate) > 0) {
                    log.error(ErrorConstants.PAYMENT_EXECUTION_DATE_EXCEEDED);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                            TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.PAYMENT_EXECUTION_DATE_EXCEEDED));
                }
            }
        }

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

        log.debug("Iterating and validating payment objects");
        for (Object payment : payments) {
            PaymentConsentUtil.validateCommonPaymentElements((JSONObject) payment);
        }
    }
}
