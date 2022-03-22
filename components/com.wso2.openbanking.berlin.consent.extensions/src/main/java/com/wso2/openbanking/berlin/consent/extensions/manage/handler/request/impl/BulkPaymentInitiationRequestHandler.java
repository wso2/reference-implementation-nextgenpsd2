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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
