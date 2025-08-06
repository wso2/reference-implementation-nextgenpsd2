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

package org.wso2.openbanking.nextgenpsd2.extensions.api.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.CommonConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.BadRequestException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ServerErrorException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ValidationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.ConsentAuthorizationHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PersistAuthorizedConsent;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PersistAuthorizedConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PopulateConsentAuthorizeScreenRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.StoredAuthorization;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.StoredDetailedConsentResourceData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePersistAuthorizedConsent;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePersistAuthorizedConsentData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreen;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.UserGrantedData;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.CommonConsentValidationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentAuthorizationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.PaymentConsentUtil;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

/**
 * Implementation class maintaining the methods for consent authorization extension APIs.
 */
public class ConsentAuthorizationAPIImpl {
    public static Log log = LogFactory.getLog(ConsentAuthorizationAPIImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Method for returning the response for populating consent authorization page.
     *
     * @param requestBody
     * @return
     */
    public static Response populateConsentAuthorizeScreen
    (@Valid PopulateConsentAuthorizeScreenRequestBody requestBody) {
        String requestId = requestBody.getRequestId();
        PopulateConsentAuthorizeScreenData data = requestBody.getData();
        StoredDetailedConsentResourceData consentResource = data.getConsentResource();
        String consentType = consentResource.getType();

        try {
            // Verify client
            JSONObject queryParams;
            String clientId;
            try {
                queryParams = CommonConsentValidationUtil.convertObjectToJson(data.getRequestParameters());
                clientId = queryParams.getString(CommonConstants.CLIENT_ID_PARAM);
                CommonConsentValidationUtil.validateClient(clientId, consentResource
                        .getClientId());
            } catch (JSONException e) {
                log.debug("[" + requestId + "] " + "Client id not found in request", e);
                return Response.ok().entity(ErrorUtil
                                .getFormattedAuthorizationFailureException(requestId,
                                        "Client id not found in request", null)).build();
            } catch (ValidationFailureException e) {
                log.debug("[" + requestId + "] " + ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR, e);
                return Response.ok().entity(ErrorUtil.getFormattedAuthorizationFailureException(requestId,
                                ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR, null)).build();
            }

            // Validate consent type with consent scopes
            String scopes;
            scopes = queryParams.optString(CommonConstants.SCOPE_PARAM);
            if (!scopes.isEmpty()) {
                ConsentAuthorizationUtil.validateConsentTypeWithScopes(
                        consentType, scopes);
            } else {
                log.debug("[" + requestId + "] " + "Scope not found in request");
                return Response.ok().entity(ErrorUtil.getFormattedAuthorizationFailureException(requestId,
                                "Scope not found in request", null)).build();
            }

            // Check if authorizable
            StoredAuthorization unauthorizedObj = ConsentAuthorizationUtil
                    .getAuthorizableResource(consentResource.getAuthorizations(), data.getUserId(), consentResource);

            ConsentAuthorizationHandler authorizationHandler = CommonConsentValidationUtil.getAuthorizationHandler(
                    consentResource.getType());
            if (authorizationHandler != null) {
                SuccessResponsePopulateConsentAuthorizeScreen response =
                        new SuccessResponsePopulateConsentAuthorizeScreen();
                SuccessResponsePopulateConsentAuthorizeScreenData responseData =
                        new SuccessResponsePopulateConsentAuthorizeScreenData();

                // Add basic consent data to display
                authorizationHandler.populateBasicConsentData(responseData, data);

                // Add account data to display
                authorizationHandler.populateAccountsData(responseData, data);

                // Append authorizable consent as consent metadata
                Map<String, Object> consentMetadata;
                if (responseData.getConsentData().getConsentMetadata() == null) {
                    consentMetadata = new HashMap<>();
                } else {
                    consentMetadata = (HashMap) responseData.getConsentData().getConsentMetadata();
                }
                consentMetadata.put(ConsentExtensionConstants.AUTHORIZING_AUTHORIZATION, unauthorizedObj);
                responseData.getConsentData().setConsentMetadata(consentMetadata);

                // Set response data to response
                response.setResponseId(requestBody.getRequestId());
                response.setStatus(SuccessResponsePopulateConsentAuthorizeScreen.StatusEnum.SUCCESS);
                response.setData(responseData);

                try {
                    return Response.ok().entity(objectMapper.writeValueAsString(response)).build();
                } catch (JsonProcessingException e) {
                    log.debug("[" + requestId + "] " + "Failed to parse built populate response object to JSON.", e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(ErrorUtil.getErrorResponse("invalid_request",
                            "Failed to parse built populate response object to JSON.")).build();
                }
            } else {
                // Should be unreachable since only consent types in ConsentTypes enum are used at initiation
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        ErrorUtil.getFormattedAuthorizationFailureException(requestId,
                                ErrorConstants.INITIATED_CONSENT_TYPE_INVALID, null).toString()).build();
            }
        } catch (AuthorizationFailureException e) {
            log.error("[" + requestId + "] " + "Authorization retrieval failed. " +
                    "Redirecting to redirect URL with error description", e);
            e.setResponseId(requestId);
            return Response.status(Response.Status.OK).entity(e.getFormattedErrorAsString()).build();

        } catch (BadRequestException | ServerErrorException e) {
            log.error("[" + requestId + "] " + "An error occurred populating consent authorize screen.", e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();

        }
    }

    /**
     * Method for building persist authorized consent response.
     *
     * @param requestBody
     * @return
     */
    public static Response persistAuthorizedConsent(@Valid @NotNull PersistAuthorizedConsentRequestBody
                                                            requestBody) {
        String requestId = requestBody.getRequestId();
        PersistAuthorizedConsent persistData = requestBody.getData();
        boolean isApproved = persistData.getIsApproved();
        UserGrantedData userGrantedData = persistData.getUserGrantedData();
        StoredDetailedConsentResourceData consentResource = persistData.getConsentResource();
        String consentType = consentResource.getType();

        try {
            JSONObject retrievalMetadata = CommonConsentValidationUtil
                    .convertObjectToJson(userGrantedData.getAuthorizedResources().getMetadata());

            // Restore authorizing authorization
            StoredAuthorization authorizingResource;
            try {
                authorizingResource = objectMapper.readValue(retrievalMetadata
                        .getJSONObject(ConsentExtensionConstants.AUTHORIZING_AUTHORIZATION).toString(),
                        StoredAuthorization.class);
            } catch (JsonProcessingException e) {
                throw new ServerErrorException("Authorization resource being authorized is invalid", e);
            }

            // Banking backend integration for payments
            if (isApproved) {
                PaymentConsentUtil.handleBackendPayment(authorizingResource, consentResource);
            }

            // Build success response for consent persistence
            SuccessResponsePersistAuthorizedConsent response = new SuccessResponsePersistAuthorizedConsent();
            response.setResponseId(requestId);
            response.setStatus(SuccessResponsePersistAuthorizedConsent.StatusEnum.SUCCESS);
            SuccessResponsePersistAuthorizedConsentData responseData =
                    new SuccessResponsePersistAuthorizedConsentData();

            ConsentAuthorizationHandler authorizationHandler =
                    CommonConsentValidationUtil.getAuthorizationHandler(consentType);
            responseData.setConsentResource(authorizationHandler.getAmendedConsentResource(requestBody,
                    authorizingResource));

            response.setData(responseData);

            try {
                return Response.ok().entity(objectMapper.writeValueAsString(response)).build();
            } catch (JsonProcessingException e) {
                log.debug("[" + requestId + "] " + "Failed to parse built populate response object to JSON.", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ErrorUtil.getErrorResponse("invalid_request",
                                "Failed to parse built populate response object to JSON.")).build();
            }
        } catch (AuthorizationFailureException e) {
            log.error("[" + requestId + "] " + "Authorization persistence failed. " +
                    "Redirecting to redirect URL with error description", e);
            e.setResponseId(requestId);
            return Response.status(Response.Status.OK).entity(e.getFormattedErrorAsString()).build();

        } catch (BadRequestException | ServerErrorException e) {
            log.error("[" + requestId + "] " + "An error occurred persisting authorized consent.", e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();

        }
    }
}
