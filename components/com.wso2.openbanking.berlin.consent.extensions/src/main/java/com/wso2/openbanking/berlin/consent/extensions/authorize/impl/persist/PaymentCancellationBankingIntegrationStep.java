/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Persist step with sample core banking integration logic for payment cancellation.
 */
public class PaymentCancellationBankingIntegrationStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(PaymentCancellationBankingIntegrationStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        if (consentPersistData.getApproval()) {
            ConsentCoreServiceImpl consentCoreService = getConsentService();
            ConsentData consentData = consentPersistData.getConsentData();
            String paymentId = consentData.getConsentId();
            AuthorizationResource currentAuthResource = consentData.getAuthResource();
            String consentType = consentData.getType();

            try {
                // Execute only for bulk and periodic payments
                if (StringUtils.equals(ConsentExtensionConstants.BULK_PAYMENTS, consentType)
                        || StringUtils.equals(ConsentExtensionConstants.PERIODIC_PAYMENTS, consentType)) {

                    // Execute only if the current authorisation resource is a cancellation resource
                    if (StringUtils.equals(AuthTypeEnum.CANCELLATION.toString(),
                            currentAuthResource.getAuthorizationType())
                            && ConsentAuthUtil.areAllOtherAuthResourcesValid(consentCoreService, paymentId,
                            currentAuthResource)) {

                        String paymentReceipt = consentData.getConsentResource().getReceipt();
                        if (!ConsentAuthUtil.isPaymentResourceSubmitted(paymentId, paymentReceipt,
                                "cancel")) {
                            log.error("Error occurred while cancelling the payment, please retry");
                            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                    ErrorConstants.PAYMENT_CANCELLATION_FAILED);
                        }
                    }
                }
            } catch (OpenBankingException | IOException e) {
                log.error("Exception occurred while cancelling the payment, please retry", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.PAYMENT_CANCELLATION_FAILED);
            }
        }
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
