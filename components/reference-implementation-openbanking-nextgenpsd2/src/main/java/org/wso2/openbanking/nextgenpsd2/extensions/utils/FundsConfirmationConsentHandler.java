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

import org.wso2.openbanking.nextgenpsd2.extensions.configurations.ConfigurableProperties;
import org.wso2.openbanking.nextgenpsd2.extensions.datamodels.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.AuthTypeEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ConsentStatusEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ConsentTypeEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.FailedValidationException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ServerException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Consent handler for account consents.
 */
public class FundsConfirmationConsentHandler implements ConsentHandler, ConsentResponseHandler {
    private static final Log log = LogFactory.getLog(FundsConfirmationConsentUtil.class);

    /**
     * Handles creation of confirmation of funds consents.
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

        JSONObject headersJSON =
                CommonConsentValidationUtil.convertObjectToJson(requestBody.getData().getRequestHeaders());

        // Validate headers
        CommonConsentValidationUtil.validateTppRedirectPreferredHeader(headersJSON);

        // Validate payload
        JSONObject requestPayload;
        try {
            requestPayload =
                    CommonConsentValidationUtil.convertObjectToJson(requestBody.getData().getConsentInitiationData());
        } catch (JSONException e) {
            throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_FORMAT_ERROR));
        }
        FundsConfirmationConsentUtil.validateFundsConfirmationInitiationPayload(requestPayload);

        Optional<Boolean> isRedirectPreferred = CommonConsentValidationUtil.isTppRedirectPreferred(headersJSON);

        if (!isRedirectPreferred.isPresent() || BooleanUtils.isTrue(isRedirectPreferred.get())) {
            log.debug("SCA approach is Redirect SCA (OAuth2)");

            // Response body
            validationResponse.setResponseId(requestBody.getRequestId());
            validationResponse.setStatus(SuccessResponsePreProcessConsentCreation.StatusEnum.SUCCESS);

            // Response data, contains consentResource
            SuccessResponseWithDetailedConsentData data = new SuccessResponseWithDetailedConsentData();

            // Consent resource
            DetailedConsentResourceData consentResource = new DetailedConsentResourceData();
            consentResource.setReceipt(requestPayload);
            consentResource.setType(ConsentTypeEnum.FUNDS_CONFIRMATION.toString());
            consentResource.setStatus(ConsentStatusEnum.RECEIVED.toString());

            // Setting inapplicable consent parameters
            consentResource.setFrequency(0);
            consentResource.setValidityTime(0L);
            consentResource.setRecurringIndicator(false);

            // Build auth resource for implicit authorisation
            // ToDo: Revisit once explicit authorisation is supported
            Authorization authObj = new Authorization();
            if (headersJSON.has(ConsentExtensionConstants.PSU_ID_HEADER)) {
                authObj.setUserId(headersJSON.getString(ConsentExtensionConstants.PSU_ID_HEADER));
            }
            authObj.setType(AuthTypeEnum.AUTHORISATION.toString());
            String authStatus = CommonConsentValidationUtil.getAuthorizationStatus(isSCARequired, headersJSON);
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
     * Handles retrieval of funds confirmation consents.
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
        StoredBasicConsentResourceData consentResource = requestBody.getData().getConsentResource();
        String consentId = consentResource.getId();

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
        CommonConsentValidationUtil.validateConsentType(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(),
                consentResource.getType());

        // Build empty response to send since no additional attributes are added
        validationResponse.setStatus(SuccessResponseForResponseAlternation.StatusEnum.SUCCESS);
        validationResponse.setResponseId(requestBody.getRequestId());

        SuccessResponseForResponseAlternationData responseData = new SuccessResponseForResponseAlternationData();

        // Build response body
        JSONObject payloadToSend = new JSONObject();
        if(!requestBody.getData().getConsentResourcePath().contains(ConsentExtensionConstants.STATUS)) {
            payloadToSend = CommonConsentValidationUtil.convertObjectToJson(consentResource.getReceipt());
        }

        CommonConsentValidationUtil.appendConsentStatusResponse(consentResource,
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), payloadToSend);
        responseData.setModifiedResponse(payloadToSend);
        responseData.setResponseHeaders(CommonConsentValidationUtil.getIdempotencyHeaderJSON(
                headers.getString(ConsentExtensionConstants.X_REQUEST_ID_HEADER)
        ));

        validationResponse.setData(responseData);
    }

    /**
     * Handles CoF consent creation response customization.
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
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString());
    }
}
