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

package org.wso2.openbanking.nextgenpsd2.extensions.handlers;

import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ExtensionException;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.DetailedConsentResourceDataWithAmendments;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PersistAuthorizedConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.StoredAuthorization;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreenData;

/**
 * Consent handler interface for processing consent authorization related requests.
 */
public interface ConsentAuthorizationHandler {
    void populateBasicConsentData(SuccessResponsePopulateConsentAuthorizeScreenData responseData,
                                  PopulateConsentAuthorizeScreenData requestData)
            throws AuthorizationFailureException, ExtensionException;
    void populateAccountsData(SuccessResponsePopulateConsentAuthorizeScreenData responseData,
                              PopulateConsentAuthorizeScreenData requestData)
            throws AuthorizationFailureException, ExtensionException;
    DetailedConsentResourceDataWithAmendments getAmendedConsentResource
            (PersistAuthorizedConsentRequestBody requestBody, StoredAuthorization authorizingResource)
            throws AuthorizationFailureException, ExtensionException;
}
