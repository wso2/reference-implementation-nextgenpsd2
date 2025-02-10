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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyValidationException;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyValidationResult;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyValidator;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Class to handle idempotency related operations.
 */
public class BerlinIdempotencyValidator extends IdempotencyValidator {

    private static final Log log = LogFactory.getLog(BerlinIdempotencyValidator.class);



    @Override
    public IdempotencyValidationResult validateIdempotency(ConsentManageData consentManageData)
            throws IdempotencyValidationException {

        if (!OpenBankingConfigParser.getInstance().isIdempotencyValidationEnabled()) {
            return new IdempotencyValidationResult(false, false);
        }

        String requestPath = consentManageData.getRequestPath();

        if (requestPath.contains(ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END)
                || requestPath.contains(ConsentExtensionConstants
                .PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END)) {

            if (isPreIdempotencyValidationsFail(consentManageData)) {
                return new IdempotencyValidationResult(false, false);
            }

            String idempotencyKeyValue = consentManageData.getHeaders().get(getIdempotencyHeaderName());
            String idempotencyKeyName = CommonConsentUtil
                    .constructAttributeKey(getIdempotencyAttributeName(requestPath), idempotencyKeyValue);

            if (IdempotencyConstants.EMPTY_OBJECT.equals(consentManageData.getPayload().toString())) {
                try {
                    // Start authorisation and cancellation authorisation requests do not contain a payload.
                    IdempotencyValidationResult result = validateIdempotencyWithoutPayload(consentManageData,
                            idempotencyKeyName, idempotencyKeyValue);
                    if (!result.isIdempotent()) {
                        return result;
                    }
                    // Creating unique idempotency and creation time attributes.
                    String uniqueAttributeName = getUniqueCreatedTimeAttributeNameForExplicitRequest(requestPath,
                            idempotencyKeyValue);
                    long createdTime = getCreatedTimeFromConsentAttributes(result.getConsent(), uniqueAttributeName);
                    return validateIdempotencyConditions(consentManageData, result.getConsent(), createdTime);
                } catch (IOException e) {
                    log.error(IdempotencyConstants.JSON_COMPARING_ERROR, e);
                    throw new IdempotencyValidationException(IdempotencyConstants.JSON_COMPARING_ERROR);
                } catch (ConsentManagementException e) {
                    log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
                    return new IdempotencyValidationResult(true, false);
                }
            }
            return new IdempotencyValidationResult(false, false);
        } else {
            return super.validateIdempotency(consentManageData);
        }
    }



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

    @Override
    protected void checkSameConsentIdRelatedToDifferentRequestId(ConsentManageData consentManageData,
                                                                 Map.Entry<String, String> entry,
                                                                 String idempotencyKeyValue)
            throws IdempotencyValidationException {

        String requestPath = consentManageData.getRequestPath();
        if (!(requestPath.contains(ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END)
                || requestPath.contains(ConsentExtensionConstants
                .PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END))) {

            /* The NextGenPSD2 specification requires creating multiple authorisation resources for the same
               consent. Therefore, not performing this check for explicit authorisation/cancellation requests. */
            super.checkSameConsentIdRelatedToDifferentRequestId(consentManageData, entry, idempotencyKeyValue);
        }
    }

    /**
     * Returns the attribute name for the explicit authorisation request.
     *
     * The format of the attribute name would be in this format: resourcePath_parameterName_requestId
     *
     * @param resourcePath path of the request
     * @param requestId request ID
     * @return the unique attribute name for the current explicit request type
     */
    private String getUniqueCreatedTimeAttributeNameForExplicitRequest(String resourcePath, String requestId) {

        String path = ConsentExtensionUtil.getServiceDifferentiatingRequestPath(resourcePath);
        String attributeKey;

        if (ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END.equals(path)) {
            attributeKey = CommonConsentUtil.constructAttributeKey(resourcePath,
                    ConsentExtensionConstants.AUTH_CANCEL_CREATED_TIME, requestId);
            return attributeKey;
        } else if (ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END.equals(path)) {
            attributeKey = CommonConsentUtil.constructAttributeKey(resourcePath,
                    ConsentExtensionConstants.EXPLICIT_AUTH_CREATED_TIME, requestId);
            return attributeKey;
        }
        return null;
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
