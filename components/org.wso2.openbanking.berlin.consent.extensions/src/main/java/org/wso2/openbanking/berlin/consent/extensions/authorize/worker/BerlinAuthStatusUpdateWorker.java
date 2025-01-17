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

package org.wso2.openbanking.berlin.consent.extensions.authorize.worker;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.identity.auth.extensions.adaptive.function.OpenBankingAuthenticationWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

/**
 * Class containing the common logic to update berlin authorization status from adaptive auth script.
 */
public abstract class BerlinAuthStatusUpdateWorker implements OpenBankingAuthenticationWorker {

    private static final Log log = LogFactory.getLog(BerlinAuthStatusUpdateWorker.class);
    private static ConsentCoreService instance = new ConsentCoreServiceImpl();

    @Override
    public JSONObject handleRequest(JsAuthenticationContext context, Map<String, String> map) {
        String consentId = ConsentAuthUtil.getConsentId(context.getContext()
                .getAuthenticationRequest()
                .getRequestQueryParam("scope")[0]);

        AuthenticatedUser user = context.getContext().getLastAuthenticatedUser();
        String userId = user.getUserName();
        try {
            DetailedConsentResource consent = instance.getDetailedConsent(consentId);
            AuthorizationResource authResource = this.getAuthorizationResourceFromConsent(consent, userId);
            instance.updateAuthorizationStatus(authResource.getAuthorizationID(), getNewAuthStatusValue());
        } catch (ConsentManagementException e) {
            JSONObject errorResponse =  new JSONObject().put("Error", true);
            errorResponse.put("Reason", e.getMessage());
            log.info(errorResponse);
        }
        return new JSONObject().put("Success", true);
    }

    private AuthorizationResource getAuthorizationResourceFromConsent(DetailedConsentResource consentResource,
                                                                      String userId) throws ConsentManagementException {

        ArrayList<AuthorizationResource> authorizationResources = consentResource.getAuthorizationResources();
        String authType;
        authType = StringUtils.equals(consentResource.getCurrentStatus(), TransactionStatusEnum.ACTC.name()) ?
                AuthTypeEnum.CANCELLATION.toString() : AuthTypeEnum.AUTHORISATION.toString();

        Optional<AuthorizationResource> unauthorizedAuthResource = authorizationResources.stream()
                .filter(authorization -> StringUtils.equals(userId,
                        authorization.getUserID())
                        && StringUtils.equals(authType, authorization.getAuthorizationType()))
                .findFirst();

        if (unauthorizedAuthResource.isPresent()) {
            return unauthorizedAuthResource.get();
        } else {
            unauthorizedAuthResource = authorizationResources.stream()
                    .filter(authorization -> isCurrentAuthorizationStatusEligible(authorization
                            .getAuthorizationStatus()) &&
                            StringUtils.equals(authType, authorization.getAuthorizationType()))
                    .findFirst();
            if (unauthorizedAuthResource.isPresent()) {
                return unauthorizedAuthResource.get();
            } else {
                throw new ConsentManagementException("Failed to find an eligible authorization resource for the " +
                        "consentID :" + consentResource.getConsentID());
            }
        }
    }

    abstract boolean isCurrentAuthorizationStatusEligible(String authStatus);

    abstract String getNewAuthStatusValue();
}
