/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.identity.response.type.handlers;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler.OBResponseTypeHandler;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.identity.response.type.constants.IdentityConstants;
import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;

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
