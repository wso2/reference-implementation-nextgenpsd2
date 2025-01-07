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

package org.wso2.openbanking.berlin.identity.response.type.handlers;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.OBResponseTypeHandler;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.openbanking.berlin.common.constants.CommonConstants;
import org.wso2.openbanking.berlin.identity.response.type.constants.IdentityConstants;

import java.util.Locale;

/**
 * Berlin response type handler implementation.
 */
public class BerlinResponseTypeHandlerImpl implements OBResponseTypeHandler {

    @Override
    public long updateRefreshTokenValidityPeriod(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        return oAuthAuthzReqMessageContext.getRefreshTokenvalidityPeriod();
    }

    @Override
    public String[] updateApprovedScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        String[] scopes;
        if (oAuthAuthzReqMessageContext.getAuthorizationReqDTO() != null) {
            scopes = oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getScopes();
        } else {
            scopes = new String[0];
        }

        String[] updatedScopes;

        for (String scope : scopes) {

            String newScope;

            if (scope.toLowerCase(Locale.ENGLISH).startsWith(CommonConstants.AIS_SCOPE)) {
                newScope = IdentityConstants.ACCOUNTS_SCOPE;
            } else if (scope.toLowerCase(Locale.ENGLISH).startsWith(CommonConstants.PIS_SCOPE)) {
                newScope = IdentityConstants.PAYMENTS_SCOPE;
            } else if (scope.toLowerCase(Locale.ENGLISH).startsWith(CommonConstants.PIIS_SCOPE)) {
                newScope = IdentityConstants.FUNDS_CONFIRMATION_SCOPE;
            } else {
                continue;
            }

            String consentIdClaim = OpenBankingConfigParser.getInstance().getConfiguration().get(
                    IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();
            String consentId = scope.split(CommonConstants.DELIMITER)[1];
            String consentScope = consentIdClaim + consentId;
            updatedScopes = (String[]) ArrayUtils.add(scopes, consentScope);
            updatedScopes = (String[]) ArrayUtils.add(updatedScopes, newScope);
            return updatedScopes;
        }
        return scopes;
    }
}
