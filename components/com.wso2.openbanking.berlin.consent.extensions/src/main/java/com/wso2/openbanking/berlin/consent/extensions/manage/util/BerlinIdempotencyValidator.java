/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.manage.util;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyValidator;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
                return ConsentExtensionConstants.EXPLICIT_AUTH_X_REQUEST_ID;
            case ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END:
                return ConsentExtensionConstants.AUTH_CANCEL_X_REQUEST_ID;
            default:
                return ConsentExtensionConstants.X_REQUEST_ID;
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
        if (ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END.equals(path) &&
                isPresentAsConsentAttribute(consentRequest, ConsentExtensionConstants.AUTH_CANCEL_CREATED_TIME)) {
            return getCreatedTimeFromConsentAttributes(consentRequest,
                    ConsentExtensionConstants.AUTH_CANCEL_CREATED_TIME);
        } else if (ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END.equals(path) &&
                isPresentAsConsentAttribute(consentRequest, ConsentExtensionConstants.EXPLICIT_AUTH_CREATED_TIME)) {
            return getCreatedTimeFromConsentAttributes(consentRequest,
                    ConsentExtensionConstants.EXPLICIT_AUTH_CREATED_TIME);
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
        return consentRequest.getConsentAttributes().containsKey(key);
    }

    /**
     * Method to get created time from consent attributes.
     *
     * @param consentRequest     DetailedConsentResource.
     * @param key                key to be checked.
     * @return created time.
     */
    private long getCreatedTimeFromConsentAttributes(DetailedConsentResource consentRequest, String key) {
        return Long.parseLong(consentRequest.getConsentAttributes().get(key));
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
