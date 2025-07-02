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

import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.FailedValidationException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ServerException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.EnrichConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PreProcessConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PreProcessConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.SuccessResponseForResponseAlternation;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.SuccessResponsePreProcessConsentCreation;

/**
 * Consent authorisation handler for explicit authorisation.
 */
public class ConsentAuthorisationHandler implements ConsentManagementHandler, ConsentResponseHandler {
    // ToDo: Implement authorisation creation for consents

    /**
     * Handle creation of authorisations for consents.
     *
     * @param requestBody
     * @param validationResponse
     */
    @Override
    public void handleCreation(PreProcessConsentCreationRequestBody requestBody,
                               SuccessResponsePreProcessConsentCreation validationResponse)
            throws FailedValidationException {
        // Throws an error since creating auth object for an existing consent is not supported
        throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
                ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                        TPPMessage.CodeEnum.SERVICE_INVALID_405, ErrorConstants.AUTH_CREATION_NOT_SUPPORTED));
    }

    /**
     * Handles retrieval of consent authorisations.
     *
     * @param requestBody
     * @param validationResponse
     * @throws FailedValidationException
     */
    @Override
    public void handleRetrieval(PreProcessConsentRequestBody requestBody,
                                SuccessResponseForResponseAlternation validationResponse)
            throws FailedValidationException, ServerException {
        // Throws an error since auth resources for a consent cannot be retrieved through consent creation
        throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
                ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                        TPPMessage.CodeEnum.SERVICE_INVALID_405, ErrorConstants.AUTH_CREATION_NOT_SUPPORTED));
    }

    /**
     * Handles consent authorization creation response customization.
     *
     * @param requestBody
     * @param validationResponse
     * @throws FailedValidationException
     */
    @Override
    public void enrichCreationResponse(EnrichConsentCreationRequestBody requestBody,
                                       SuccessResponseForResponseAlternation validationResponse)
            throws ServerException {
        // Throws an error since this should be unreachable
        throw new ServerException(ServerException.ErrorCode.BAD_REQUEST, ErrorUtil.constructBerlinError(
                null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.SERVICE_INVALID_405,
                ErrorConstants.AUTH_CREATION_NOT_SUPPORTED));
    }
}
