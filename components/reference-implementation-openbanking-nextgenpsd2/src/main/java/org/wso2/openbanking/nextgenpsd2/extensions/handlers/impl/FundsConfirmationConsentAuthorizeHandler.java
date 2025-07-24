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

package org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ScaStatusEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.BadRequestException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ServerErrorException;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.ConsentAuthorizationHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.AuthorizedResourcesAuthorizedDataInner;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.DetailedConsentResourceDataWithAmendments;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PersistAuthorizedConsent;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PersistAuthorizedConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.Resource;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.StoredAuthorization;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.SuccessResponsePopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.SuccessResponsePopulateConsentAuthorizeScreenDataConsentData;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.AccountConsentUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.CommonConsentValidationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentAuthorizationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.FundsConfirmationConsentUtil;

import java.util.List;

/**
 * Consent handler interface for processing CoF consent authorization related requests.
 */
public class FundsConfirmationConsentAuthorizeHandler implements ConsentAuthorizationHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Return consent data for confirmation of funds consents.
     *
     * @param responseData
     * @param requestData
     * @throws AuthorizationFailureException
     */
    @Override
    public void populateBasicConsentData(SuccessResponsePopulateConsentAuthorizeScreenData responseData,
                                         PopulateConsentAuthorizeScreenData requestData) throws BadRequestException {
        // Add consent data if null
        if (responseData.getConsentData() == null) {
            responseData.setConsentData(new SuccessResponsePopulateConsentAuthorizeScreenDataConsentData());
        }

        // Populate consent data
        responseData.getConsentData().setType(requestData.getConsentResource().getType());
        FundsConfirmationConsentUtil.populateFundsConfirmationBasicConsentData(responseData, requestData);
    }

    /**
     * Return consumer data for confirmation of funds consents.
     *
     * @param responseData
     * @param requestData
     * @throws AuthorizationFailureException
     */
    @Override
    public void populateAccountsData(SuccessResponsePopulateConsentAuthorizeScreenData responseData,
                                     PopulateConsentAuthorizeScreenData requestData)
            throws AuthorizationFailureException, ServerErrorException {

        // Extract account reference from receipt
        JSONObject accountRef;
        try {
            JSONObject receipt =
                    new JSONObject(objectMapper.writeValueAsString(requestData.getConsentResource().getReceipt()));
            accountRef = receipt.getJSONObject(ConsentExtensionConstants.ACCOUNT);
        } catch (JsonProcessingException e) {
            throw new ServerErrorException("Failed to extract receipt from payment consent", e);
        }

        // Populate subject account of the consent to be authorized by user
        CommonConsentValidationUtil.populateConsentInitiatedAccounts(responseData, requestData, accountRef);
    }

    /**
     * Returns authorizations and account mappings for account consent persistence.
     *
     * @param requestBody
     * @param authorizingResource
     * @return
     * @throws AuthorizationFailureException
     * @throws BadRequestException
     * @throws ServerErrorException
     */
    @Override
    public DetailedConsentResourceDataWithAmendments getAmendedConsentResource(
            PersistAuthorizedConsentRequestBody requestBody, StoredAuthorization authorizingResource)
            throws BadRequestException, ServerErrorException, AuthorizationFailureException {

        PersistAuthorizedConsent requestData = requestBody.getData();
        boolean isApproved = requestData.getIsApproved();

        // Get auth status from approval
        String authStatus;
        if (isApproved) {
            authStatus = ScaStatusEnum.FINALISED.toString();
        } else {
            authStatus = ScaStatusEnum.FAILED.toString();
        }

        // Verify that there's only one authorization
        List<AuthorizedResourcesAuthorizedDataInner> authorizedData = requestData.getUserGrantedData()
                .getAuthorizedResources().getAuthorizedData();
        ConsentAuthorizationUtil.verifySingleAuthorizedResource(authorizedData);

        List<Resource> accountMappingResources = AccountConsentUtil.createAccountPermissionMappings(
                requestData.getUserGrantedData().getAuthorizedResources().getAuthorizedData().get(0),
                ConsentExtensionConstants.DEFAULT_PERMISSION
        );

        // Set new consent status
        return ConsentAuthorizationUtil.buildAmendedConsentResource(authorizingResource, isApproved, requestData,
                accountMappingResources, authStatus);
    }
}
