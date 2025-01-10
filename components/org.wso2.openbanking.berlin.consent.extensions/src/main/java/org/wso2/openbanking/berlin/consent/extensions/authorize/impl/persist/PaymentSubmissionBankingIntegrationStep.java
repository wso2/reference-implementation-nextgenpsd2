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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;

import java.io.IOException;

/**
 * Persist step with sample core banking integration logic for payment submission.
 */
public class PaymentSubmissionBankingIntegrationStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(PaymentSubmissionBankingIntegrationStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        if (consentPersistData.getApproval()) {
            ConsentCoreServiceImpl consentCoreService = getConsentService();
            ConsentData consentData = consentPersistData.getConsentData();
            String paymentId = consentData.getConsentId();
            AuthorizationResource currentAuthResource = consentData.getAuthResource();
            String consentType = consentData.getType();

            try {
                // Execute only for instant, bulk and periodic payments
                if (StringUtils.equals(ConsentExtensionConstants.PAYMENTS, consentType)
                        || StringUtils.equals(ConsentExtensionConstants.BULK_PAYMENTS, consentType)
                        || StringUtils.equals(ConsentExtensionConstants.PERIODIC_PAYMENTS, consentType)) {

                    // Execute only if the current authorisation resource is a submission auth resource
                    if (StringUtils.equals(AuthTypeEnum.AUTHORISATION.toString(),
                            currentAuthResource.getAuthorizationType())
                            && ConsentAuthUtil.areAllOtherAuthResourcesValid(consentCoreService, paymentId,
                            currentAuthResource)) {

                        String paymentReceipt = consentData.getConsentResource().getReceipt();
                        if (!ConsentAuthUtil.isPaymentResourceSubmitted(paymentId, paymentReceipt,
                                "submit")) {
                            log.error("Error occurred while submitting the payment, please retry");
                            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                    ErrorConstants.PAYMENT_SUBMISSION_FAILED);
                        }
                    }
                }
            } catch (OpenBankingException | IOException e) {
                log.error("Exception occurred while submitting the payment, please retry", e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.PAYMENT_SUBMISSION_FAILED);
            }
        }
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }

}
