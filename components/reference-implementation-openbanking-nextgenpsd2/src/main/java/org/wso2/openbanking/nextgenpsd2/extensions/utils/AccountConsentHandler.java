/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.nextgenpsd2.extensions.utils;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.nextgenpsd2.extensions.datamodels.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.AuthTypeEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ConsentStatusEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ConsentTypeEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.FailedValidationException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ServerException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.Authorization;
import org.wso2.openbanking.nextgenpsd2.extensions.model.DetailedConsentResourceData;
import org.wso2.openbanking.nextgenpsd2.extensions.model.EnrichConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.PreProcessConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.PreProcessConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.PreProcessConsentRetrievalData;
import org.wso2.openbanking.nextgenpsd2.extensions.model.StoredBasicConsentResourceData;
import org.wso2.openbanking.nextgenpsd2.extensions.model.SuccessResponseForResponseAlternation;
import org.wso2.openbanking.nextgenpsd2.extensions.model.SuccessResponseForResponseAlternationData;
import org.wso2.openbanking.nextgenpsd2.extensions.model.SuccessResponsePreProcessConsentCreation;
import org.wso2.openbanking.nextgenpsd2.extensions.model.SuccessResponseWithDetailedConsentData;

import java.util.Optional;

/**
 * Consent handler for account consents.
 */
public class AccountConsentHandler implements ConsentHandler, ConsentResponseHandler {
    private static final Log log = LogFactory.getLog(AccountConsentHandler.class);

    /**
     * Handles creation of account consents.
     *
     * @param requestBody
     * @param validationResponse
     */
    @Override
    public void handleCreation(PreProcessConsentCreationRequestBody requestBody,
                               SuccessResponsePreProcessConsentCreation validationResponse)
            throws FailedValidationException {
        // Skipping idempotency check as it's handled by the accelerator
        // ToDo: Add explicit authorisation support

        boolean isSCARequired = Boolean.parseBoolean(ConfigurableProperties.IS_SCA_REQUIRED);

        JSONObject headersJSON = CommonConsentValidationUtil.convertObjectToJson(requestBody.getData()
                .getRequestHeaders());

        // Validate headers
        CommonConsentValidationUtil.validateTppRedirectPreferredHeader(headersJSON);
        CommonConsentValidationUtil.validatePsuIpAddress(headersJSON);

        // Validate payload
        JSONObject requestPayload;
        try {
            requestPayload = CommonConsentValidationUtil
                    .convertObjectToJson(requestBody.getData().getConsentInitiationData());
        } catch (JSONException e) {
            throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_FORMAT_ERROR));
        }
        String permission = AccountConsentUtil.validateAccountInitiationPayloadAndGetPermission(requestPayload);

        Optional<Boolean> isRedirectPreferred = CommonConsentValidationUtil.isTppRedirectPreferred(headersJSON);

        if (!isRedirectPreferred.isPresent() || BooleanUtils.isTrue(isRedirectPreferred.get())) {
            log.debug("SCA approach is Redirect SCA (OAuth2)");
            String authStatus = CommonConsentValidationUtil.getAuthorizationStatus(isSCARequired, headersJSON);

            // Response body
            validationResponse.setResponseId(requestBody.getRequestId());
            validationResponse.setStatus(SuccessResponsePreProcessConsentCreation.StatusEnum.SUCCESS);

            // Response data, contains consentResource
            SuccessResponseWithDetailedConsentData data = new SuccessResponseWithDetailedConsentData();

            // Consent resource
            DetailedConsentResourceData consentResource = new DetailedConsentResourceData();
            consentResource.setReceipt(requestPayload);
            consentResource.setType(ConsentTypeEnum.ACCOUNTS.toString());
            consentResource.setStatus(ConsentStatusEnum.RECEIVED.toString());

            // Setting additional properties to consent resource
            boolean recurringIndicator = requestPayload.getBoolean(ConsentExtensionConstants.RECURRING_INDICATOR);
            consentResource.setRecurringIndicator(recurringIndicator);
            consentResource.setFrequency(requestPayload
                    .getInt(ConsentExtensionConstants.FREQUENCY_PER_DAY));

            String validUntilString = requestPayload.getString(ConsentExtensionConstants.VALID_UNTIL);
            if (recurringIndicator) {
                consentResource.setValidityTime(AccountConsentUtil.convertToUtcTimestamp(validUntilString));
            } else {
                // setting null for one off consent's validity period
                consentResource.setValidityTime(0L);
            }

            // Set consent attributes
            JSONObject attributesJSON = new JSONObject();
            attributesJSON.put(ConsentExtensionConstants.PERMISSION, permission);
            consentResource.setAttributes(attributesJSON);

            // Build auth resource for implicit authorisation
            // ToDo: Revisit once explicit authorisation is supported
            Authorization authObj = new Authorization();
            if (headersJSON.has(ConsentExtensionConstants.PSU_ID_HEADER)) {
                authObj.setUserId(headersJSON.getString(ConsentExtensionConstants.PSU_ID_HEADER));
            }
            authObj.setType(AuthTypeEnum.AUTHORISATION.toString());
            authObj.setStatus(authStatus);

            // Append auth resource to consent
            consentResource.addAuthorizationsItem(authObj);

            // Envelop consent in response data
            data.setConsentResource(consentResource);

            // Append response data to response
            validationResponse.setData(data);
        }
    }

    /**
     * Handles retrieval of account requests.
     *
     * @param requestBody
     * @param validationResponse
     * @throws FailedValidationException
     */
    @Override
    public void handleRetrieval(PreProcessConsentRequestBody requestBody,
                                SuccessResponseForResponseAlternation validationResponse)
            throws FailedValidationException, ServerException {

        PreProcessConsentRetrievalData data = requestBody.getData();
        String requestPath = data.getConsentResourcePath();
        String consentType = CommonConsentValidationUtil.getConsentTypeFromRequestPath(requestPath);
        String consentId = data.getConsentId();
        StoredBasicConsentResourceData consentResource = data.getConsentResource();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for valid client", consentId));
        }

        // Get request client id from the headers
        String requestClientId;
        JSONObject headers;
        try {
            headers = CommonConsentValidationUtil.convertObjectToJson(data.getRequestHeaders());
            requestClientId = headers.getString(CommonConstants.X_WSO2_CLIENT_ID_KEY);
        } catch (JSONException e) {
            // Should be unreachable (since insequence always adds client id header)
            throw new ServerException(ServerException.ErrorCode.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR,
                    "x-wso2-client-id header not found"));
        }

        // Validate client
        CommonConsentValidationUtil.validateClient(requestClientId, data.getConsentResource().getClientId());

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for correct type", consentId));
        }
        CommonConsentValidationUtil.validateConsentType(consentType, consentResource.getType());

        if ((consentResource.getRecurringIndicator() && AccountConsentUtil.isConsentExpired(
                consentResource.getValidityTime()))
                && !(StringUtils.equals(consentResource.getStatus(),
                ConsentStatusEnum.TERMINATED_BY_TPP.toString())
                || StringUtils.equals(consentResource.getStatus(),
                ConsentStatusEnum.REVOKED_BY_PSU.toString()))) {
            log.debug("The Consent is expired");
            consentResource.setStatus(ConsentStatusEnum.EXPIRED.toString());
        }

        JSONObject payloadToSend = new JSONObject();

        if (StringUtils.contains(requestPath, ConsentExtensionConstants.STATUS)) {
            CommonConsentValidationUtil.appendConsentStatusResponse(consentResource, consentType, payloadToSend);
        } else {
            payloadToSend = CommonConsentValidationUtil.convertObjectToJson(consentResource.getReceipt());
            AccountConsentUtil.extendAccountConsentGetResponse(consentResource, payloadToSend);
        }

        validationResponse.setResponseId(requestBody.getRequestId());
        validationResponse.setStatus(SuccessResponseForResponseAlternation.StatusEnum.SUCCESS);
        validationResponse.setData(new SuccessResponseForResponseAlternationData()
                .modifiedResponse(payloadToSend)
                .responseHeaders(CommonConsentValidationUtil.getIdempotencyHeaderJSON(
                        headers.getString(ConsentExtensionConstants.X_REQUEST_ID_HEADER))));
    }

    /**
     * Handles account consent creation response customization.
     *
     * @param requestBody
     * @param validationResponse
     * @throws FailedValidationException
     */
    @Override
    public void enrichCreationResponse(EnrichConsentCreationRequestBody requestBody,
                                       SuccessResponseForResponseAlternation validationResponse)
            throws ServerException {
        ConsentInitiationUtil.buildResponseAlterationResponseForConsentCreation(requestBody, validationResponse,
                ConsentTypeEnum.ACCOUNTS.toString());
    }
}
