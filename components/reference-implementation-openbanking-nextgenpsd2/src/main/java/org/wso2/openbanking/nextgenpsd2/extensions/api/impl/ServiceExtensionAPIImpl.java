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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ExtensionException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ValidationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.EnrichConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PersistAuthorizedConsent;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PersistAuthorizedConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PopulateConsentAuthorizeScreenRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PreProcessConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PreProcessConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.StoredAuthorization;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.StoredDetailedConsentResourceData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponseConsentRevocation;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponseForResponseAlternation;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePersistAuthorizedConsent;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePersistAuthorizedConsentData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreen;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePreProcessConsentCreation;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.UserGrantedData;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.ConsentAuthorizationHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.ConsentInitiationHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.CommonConsentValidationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentAuthorizationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.PaymentConsentUtil;

import javax.ws.rs.core.Response;

/**
 * Implementation class maintaining the methods for consent management extension APIs.
 */
public class ServiceExtensionAPIImpl {
    public static Log log = LogFactory.getLog(ServiceExtensionAPIImpl.class);

    /**
     * Method for returning the response for enriching consent creation request.
     *
     * @param requestBody request body from enrich consent creation request
     * @return built enrich consent creation response
     */
    public static Response enrichConsentCreationResponse(EnrichConsentCreationRequestBody requestBody) {
        String requestId = requestBody.getRequestId();
        try {
            ConsentInitiationHandler consentHandler = CommonConsentValidationUtil
                    .getConsentInitiationHandler(requestBody.getData().getConsentResourcePath());
            SuccessResponseForResponseAlternation validationResponse = consentHandler
                    .enrichCreationResponse(requestBody);

            return Response.ok().entity(new JSONObject(validationResponse).toString()).build();

        } catch (ExtensionException e) {
            log.error(String.format("[%s] An error occurred enriching consent creation response.", requestId), e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();
        } catch (ValidationFailureException e) {
            // Should be unreachable since resource path is validated in consent creation
            // Thus bad request error is thrown
            log.error(String.format("[%s] Invalid resource path received when enriching consent creation response.",
                    requestId), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorUtil.getErrorResponse(
                    "invalid_request", "Invalid resource path received when enriching " +
                            "consent creation response.")).build();
        }
    }

    /**
     * Method for returning the response for pre-processing consent creation request.
     *
     * @param requestBody request body from pre-process consent creation request
     * @return built pre-processed consent creation response
     */
    public static Response preProcessConsentCreation(PreProcessConsentCreationRequestBody requestBody) {
        String requestId = requestBody.getRequestId();
        try {
            // Validate X-request-ID header
            // Enable forwarding of the specific header in accelerator configurations
            CommonConsentValidationUtil.validateRequestIdentificationHeader(requestBody.getData().getRequestHeaders());

            // Validate consent initiation data format
            CommonConsentValidationUtil.validatePayloadFormat(requestBody);

            // Get validation response for consent creation based on consent type
            ConsentInitiationHandler consentInitiationHandler = CommonConsentValidationUtil
                    .getConsentInitiationHandler(requestBody.getData().getConsentResourcePath());
            SuccessResponsePreProcessConsentCreation validationResponse = consentInitiationHandler
                    .handleCreation(requestBody);

            return Response.ok().entity(new JSONObject(validationResponse).toString()).build();

        } catch (ValidationFailureException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("[%s] Validation failed for consent creation. Returning failed response.",
                        requestId), e);
            }
            return Response.ok().entity(e.getFormattedErrorAsString()).build();

        }  catch (ExtensionException e) {
            log.error(String.format("[%s] An error occurred creating consent.", requestId), e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();
        }
    }

    /**
     * Method for returning the response for pre-processing consent retrieval request.
     *
     * @param requestBody request body from pre-process consent retrieval request
     * @return built pre-processed consent retrieval response
     */
    public static Response preProcessConsentRetrieval(PreProcessConsentRequestBody requestBody) {
        String requestId = requestBody.getRequestId();
        try {
            // Validate X-request-ID header
            CommonConsentValidationUtil.validateRequestIdentificationHeader(requestBody.getData().getRequestHeaders());

            // Get validation response for consent retrieval based on consent type
            ConsentInitiationHandler consentInitiationHandler = CommonConsentValidationUtil
                    .getConsentInitiationHandler(requestBody.getData().getConsentResourcePath());
            SuccessResponseForResponseAlternation validationResponse = consentInitiationHandler
                    .handleRetrieval(requestBody);

            return Response.ok().entity(new JSONObject(validationResponse).toString()).build();

        } catch (ValidationFailureException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("[%s] Validation failed for consent retrieval. Returning failed response.",
                        requestId), e);
            }
            return Response.ok().entity(e.getFormattedErrorAsString()).build();

        }  catch (ExtensionException e) {
            log.error(String.format("[%s] An error occurred retrieving consent.", requestId), e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();
        }
    }

    /**
     * Method for returning response for pre-processing consent revocation request.
     *
     * @param requestBody request body from pre-process consent revocation request
     * @return built pre-processed consent revocation response
     */
    public static Response preProcessConsentRevoke(PreProcessConsentRequestBody requestBody) {
        String requestId = requestBody.getRequestId();
        try {
            // Validate X-request-ID header
            // Enable forwarding of the specific header in accelerator configurations
            CommonConsentValidationUtil.validateRequestIdentificationHeader(requestBody.getData().getRequestHeaders());

            // Get validation response for consent revocation based on consent type
            ConsentInitiationHandler consentInitiationHandler = CommonConsentValidationUtil
                    .getConsentInitiationHandler(requestBody.getData().getConsentResourcePath());
            SuccessResponseConsentRevocation validationResponse = consentInitiationHandler
                    .handleRevocation(requestBody);

            return Response.ok().entity(new JSONObject(validationResponse).toString()).build();

        } catch (ValidationFailureException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("[%s] Validation failed for consent revocation. Returning failed response.",
                        requestId), e);
            }
            return Response.ok().entity(e.getFormattedErrorAsString()).build();

        }  catch (ExtensionException e) {
            log.error(String.format("[%s] An error occurred revoking consent.", requestId), e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();
        }
    }

    /**
     * Method for returning the response for populating consent authorization page.
     *
     * @param requestBody request made to populate-consent-authorize-screen
     * @return payload required to generate the custom consent authorization page
     */
    public static Response populateConsentAuthorizeScreen
    (PopulateConsentAuthorizeScreenRequestBody requestBody) {
        String requestId = requestBody.getRequestId();
        PopulateConsentAuthorizeScreenData data = requestBody.getData();
        StoredDetailedConsentResourceData consentResource = data.getConsentResource();
        String consentType = consentResource.getType();

        try {
            JSONObject queryParams = CommonConsentValidationUtil.convertObjectToJson(data.getRequestParameters());

            // Verify client
            CommonConsentValidationUtil.validateClient(consentResource, queryParams);

            // Validate consent type with consent scopes
            CommonConsentValidationUtil.validateScope(requestId, queryParams, consentType);

            // Check if authorizable
            StoredAuthorization unauthorizedObj = ConsentAuthorizationUtil
                    .getAuthorizableResource(requestId, consentResource.getAuthorizations(), data.getUserId(),
                            consentResource);

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
                CommonConsentValidationUtil.appendAuthorizationToResponse(responseData, unauthorizedObj);

                // Set response data to response
                response.setResponseId(requestBody.getRequestId());
                response.setStatus(SuccessResponsePopulateConsentAuthorizeScreen.StatusEnum.SUCCESS);
                response.setData(responseData);

                return CommonConsentValidationUtil.buildPopulateResponseFromObject(response);
            } else {
                // Should be unreachable since only consent types in ConsentTypes enum are used at initiation
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        ErrorUtil.getFormattedAuthorizationFailureException(requestId,
                                ErrorConstants.INITIATED_CONSENT_TYPE_INVALID, null).toString()).build();
            }
        } catch (AuthorizationFailureException e) {
            log.error(String.format("[%s] Authorization retrieval failed. Redirecting to redirect URL with " +
                    "error description", requestId), e);
            e.setResponseId(requestId);
            return Response.status(Response.Status.OK).entity(e.getFormattedErrorAsString()).build();

        } catch (ExtensionException e) {
            log.error(String.format("[%s] An error occurred populating consent authorize screen.", requestId), e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();

        }
    }

    /**
     * Method for building persist authorized consent response.
     *
     * @param requestBody request made to persist-authorized-consent endpoint
     * @return response containing any modified consent information and account mappings with permissions
     */
    public static Response persistAuthorizedConsent(PersistAuthorizedConsentRequestBody
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
            StoredAuthorization authorizingResource = CommonConsentValidationUtil
                    .extractAuthorizingResource(retrievalMetadata);

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

            return CommonConsentValidationUtil.buildPersistResponseFromObject(response);

        } catch (AuthorizationFailureException e) {
            log.error(String.format("[%s] Authorization persistence failed. Redirecting to redirect URL with " +
                    "error description", requestId), e);
            e.setResponseId(requestId);
            return Response.status(Response.Status.OK).entity(e.getFormattedErrorAsString()).build();

        } catch (ExtensionException e) {
            log.error(String.format("[%s] An error occurred persisting authorized consent.", requestId), e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();

        }
    }

}
