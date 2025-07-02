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

package org.wso2.openbanking.nextgenpsd2.extensions.api.shims;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.api.generated.PreProcessConsentCreationApi;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.FailedValidationException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ServerException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.EnrichConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PreProcessConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PreProcessConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.SuccessResponseForResponseAlternation;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.SuccessResponsePreProcessConsentCreation;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.CommonConsentValidationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentManagementHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentResponseHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;

import javax.ws.rs.core.Response;

/**
 * Shim class maintaining the methods for consent management extension APIs.
 */
public class ConsentManagementAPIShim {

    /**
     * Method for returning the response for enriching consent creation request.
     * @param requestBody
     * @return
     */
    public static Response enrichConsentCreationResponse(EnrichConsentCreationRequestBody requestBody) {
        Log log = LogFactory.getLog(PreProcessConsentCreationApi.class);
        SuccessResponseForResponseAlternation validationResponse = new SuccessResponseForResponseAlternation();

        try {
            ConsentResponseHandler consentHandler = CommonConsentValidationUtil
                    .getConsentResponseHandler(requestBody.getData().getConsentResourcePath());

            if (consentHandler != null) {
                consentHandler.enrichCreationResponse(requestBody, validationResponse);
            } else {
                // Server error since if path is invalid consent creation should have failed
                // thus making this unreachable
                throw new ServerException(ServerException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR, null,
                                ErrorConstants.PATH_INVALID));
            }

        } catch (ServerException e) {
            log.error(e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();

        } catch (JSONException e) {
            log.error(e);
            return Response.status(Response.Status.BAD_REQUEST).entity(new JSONObject(
                    ErrorUtil.getErrorResponse(ConsentExtensionConstants.INVALID_REQUEST, e.getMessage())
            ).toString()).build();

        } catch (Exception e) {
            log.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new JSONObject(
                    ErrorUtil.getErrorResponse(ConsentExtensionConstants.SERVER_ERROR, e.getMessage())
            ).toString()).build();
        }

        return Response.ok().entity(new JSONObject(validationResponse).toString()).build();
    }

    /**
     * Method for returning the response for pre-processing consent creation request.
     * @param requestBody
     * @return
     */
    public static Response preProcessConsentCreation(PreProcessConsentCreationRequestBody requestBody) {
        Log log = LogFactory.getLog(PreProcessConsentCreationApi.class);
        SuccessResponsePreProcessConsentCreation validationResponse = new SuccessResponsePreProcessConsentCreation();

        try {
            // Validate X-request-ID header
            // Enable forwarding of the specific header in accelerator configurations
            CommonConsentValidationUtil.validateIdempotencyHeader(requestBody.getData().getRequestHeaders());

            // Extract consent initiation data
            JSONObject consentInitiationDataJSON;
            try {
                consentInitiationDataJSON = CommonConsentValidationUtil.convertObjectToJson(requestBody.getData()
                        .getConsentInitiationData());

                if (consentInitiationDataJSON.isEmpty()) {
                    // If payload is empty
                    throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
                            ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                    TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_NOT_PRESENT_ERROR));
                }

            } catch (JSONException e) {
                // If payload is not JSON
                throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_FORMAT_ERROR));
            }

            ConsentManagementHandler consentManagementHandler = CommonConsentValidationUtil.getConsentHandler(requestBody.getData()
                    .getConsentResourcePath());

            if (consentManagementHandler != null) {
                consentManagementHandler.handleCreation(requestBody, validationResponse);
            } else {
                throw new FailedValidationException(FailedValidationException.ErrorCode.NOT_FOUND,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR, null,
                                ErrorConstants.PATH_INVALID));
            }

        } catch (FailedValidationException e) {
            log.error("Validation failed for consent creation. Returning failed response.", e);
            return Response.ok().entity(e.getFormattedErrorAsString()).build();

        } catch (JSONException e) {
            log.error(e);
            return Response.status(Response.Status.BAD_REQUEST).entity(new JSONObject(
                    ErrorUtil.getErrorResponse(ConsentExtensionConstants.INVALID_REQUEST, e.getMessage())
            ).toString()).build();

        } catch (Exception e) {
            log.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new JSONObject(
                    ErrorUtil.getErrorResponse(ConsentExtensionConstants.SERVER_ERROR, e.getMessage())
            ).toString()).build();
        }

        return Response.ok().entity(new JSONObject(validationResponse).toString()).build();
    }

    /**
     * Method for returning the response for pre-processing consent retrieval request.
     * @param requestBody
     * @return
     */
    public static Response preProcessConsentRetrieval(PreProcessConsentRequestBody requestBody) {
        Log log = LogFactory.getLog(PreProcessConsentCreationApi.class);
        SuccessResponseForResponseAlternation validationResponse = new SuccessResponseForResponseAlternation();

        try {
            // Validate X-request-ID header
            // Enable forwarding of the specific header in accelerator configurations
            CommonConsentValidationUtil.validateIdempotencyHeader(requestBody.getData().getRequestHeaders());

            ConsentManagementHandler consentManagementHandler = CommonConsentValidationUtil.getConsentHandler(requestBody.getData()
                    .getConsentResourcePath());

            if (consentManagementHandler != null) {
                consentManagementHandler.handleRetrieval(requestBody, validationResponse);
            } else {
                throw new FailedValidationException(FailedValidationException.ErrorCode.NOT_FOUND,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR, null,
                                ErrorConstants.PATH_INVALID));
            }

        } catch (FailedValidationException e) {
            log.error("Validation failed for consent creation. Returning failed response.", e);
            return Response.ok().entity(e.getFormattedErrorAsString()).build();

        } catch (ServerException e) {
            log.error(e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();

        } catch (JSONException e) {
            log.error(e);
            return Response.status(Response.Status.BAD_REQUEST).entity(new JSONObject(
                    ErrorUtil.getErrorResponse(ConsentExtensionConstants.INVALID_REQUEST, e.getMessage())
            ).toString()).build();

        } catch (Exception e) {
            log.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new JSONObject(
                    ErrorUtil.getErrorResponse(ConsentExtensionConstants.SERVER_ERROR, e.getMessage())
            ).toString()).build();
        }

        return Response.ok().entity(new JSONObject(validationResponse).toString()).build();
    }
}
