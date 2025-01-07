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

package org.wso2.openbanking.berlin.consent.extensions.manage.util;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyValidator;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;

/**
 * Class to handle idempotency related operations.
 */
public class BerlinIdempotencyValidator extends IdempotencyValidator {

    private static final Log log = LogFactory.getLog(BerlinIdempotencyValidator.class);

    /**
     * Method to get the Idempotency Key Name store in consent Attributes.
     *
     * @param resourcePath     Resource Path
     * @return idempotency key Name.
     */
    @Override
    public String getIdempotencyAttributeName(String resourcePath) {
        String path = ConsentExtensionUtil.getServiceDifferentiatingRequestPath(resourcePath);
        switch (path) {
            case ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END:
                return CommonConsentUtil.constructAttributeKey(resourcePath,
                        ConsentExtensionConstants.EXPLICIT_AUTH_X_REQUEST_ID);
            case ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END:
                return CommonConsentUtil.constructAttributeKey(resourcePath,
                        ConsentExtensionConstants.AUTH_CANCEL_X_REQUEST_ID);
            default:
                return CommonConsentUtil.constructAttributeKey(resourcePath, ConsentExtensionConstants.X_REQUEST_ID);
        }
    }

    /**
     * Method to get the Idempotency Key Header Name according to the request.
     *
     * @return idempotency key Header Name.
     */
    @Override
    public String getIdempotencyHeaderName() {

        return ConsentExtensionConstants.X_REQUEST_ID_HEADER;
    }

    /**
     * Method to get created time from the Detailed Consent Resource.
     *
     * @param resourcePath     Resource Path
     * @param consentId             ConsentId
     * @return Created Time.
     */
    @Override
    public long getCreatedTimeOfPreviousRequest(String resourcePath, String consentId) {

        DetailedConsentResource consentRequest = getConsent(consentId);
        if (consentRequest == null) {
            return 0L;
        }

        String path = ConsentExtensionUtil.getServiceDifferentiatingRequestPath(resourcePath);
        if (ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END.equals(path)) {
            String attributeKey = CommonConsentUtil.constructAttributeKey(resourcePath,
                    ConsentExtensionConstants.AUTH_CANCEL_CREATED_TIME);
            return getCreatedTimeFromConsentAttributes(consentRequest, attributeKey);
        } else if (ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END.equals(path)) {
            String attributeKey = CommonConsentUtil.constructAttributeKey(resourcePath,
                    ConsentExtensionConstants.EXPLICIT_AUTH_CREATED_TIME);
            return getCreatedTimeFromConsentAttributes(consentRequest, attributeKey);
        } else {
            return consentRequest.getCreatedTime();
        }
    }

    /**
     * Method to get payload from previous request.
     *
     * @param resourcePath     Resource Path
     * @param consentId             ConsentId
     * @return Map containing the payload.
     */
    @Override
    public String getPayloadOfPreviousRequest(String resourcePath, String consentId) {

        DetailedConsentResource consentRequest = getConsent(consentId);
        if (consentRequest == null) {
            return null;
        }

        String path = ConsentExtensionUtil.getServiceDifferentiatingRequestPath(resourcePath);
        if ((ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END).equals(path) ||
                ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END.equals(path)) {
            // Payload for these APIs are {} hence returning empty JSON object.
            return new JSONObject().toString();
        } else {
            return consentRequest.getReceipt();
        }
    }

    /**
     * Method to check whether key exist as a consent attribute.
     *
     * @param consentRequest     DetailedConsentResource.
     * @param key                key to be checked.
     * @return true if key is present as a consent attribute.
     */
    private boolean isPresentAsConsentAttribute(DetailedConsentResource consentRequest, String key) {
        return consentRequest.getConsentAttributes() != null &&
                consentRequest.getConsentAttributes().containsKey(key);
    }

    /**
     * Method to get created time from consent attributes.
     *
     * @param consentRequest     DetailedConsentResource.
     * @param key                key to be checked.
     * @return created time.
     */
    private long getCreatedTimeFromConsentAttributes(DetailedConsentResource consentRequest, String key) {
        if (isPresentAsConsentAttribute(consentRequest, key)) {
            return Long.parseLong(consentRequest.getConsentAttributes().get(key));
        }
        return 0L;
    }

    /**
     * Method to get the consent from the consent ID.
     *
     * @param consentId   Consent ID
     * @return  DetailedConsentResource
     */
    private DetailedConsentResource getConsent(String consentId) {
        try {
            return getConsentService().getDetailedConsent(consentId);
        } catch (ConsentManagementException e) {
            log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
            return null;
        }
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
