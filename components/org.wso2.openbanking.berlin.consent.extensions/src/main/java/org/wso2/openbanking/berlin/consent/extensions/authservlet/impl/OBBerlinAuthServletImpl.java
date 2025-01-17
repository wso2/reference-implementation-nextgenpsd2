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

package org.wso2.openbanking.berlin.consent.extensions.authservlet.impl;

import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.authservlet.util.AuthServletUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

/**
 * The Berlin implementation of servlet extension that handles Berlin scenarios.
 */
public class OBBerlinAuthServletImpl implements OBAuthServletInterface {

    @Override
    public Map<String, Object> updateRequestAttribute(HttpServletRequest httpServletRequest, JSONObject jsonObject,
                                                      ResourceBundle resourceBundle) {

        String consentType = jsonObject.getString(ConsentExtensionConstants.TYPE);

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), consentType)) {
            return AuthServletUtil.populateAccountsData(httpServletRequest, jsonObject);
        } else if (StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), consentType)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), consentType)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), consentType)) {
            return AuthServletUtil.populatePaymentsData(httpServletRequest, jsonObject);
        } else if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), consentType)) {
            return AuthServletUtil.populateFundsConfirmationData(httpServletRequest, jsonObject);
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> updateSessionAttribute(HttpServletRequest httpServletRequest, JSONObject jsonObject,
                                                      ResourceBundle resourceBundle) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> updateConsentData(HttpServletRequest httpServletRequest) {

        Map<String, Object> returnMaps = new HashMap<>();

        String[] checkedAccountsRefs = httpServletRequest
                .getParameterValues(ConsentExtensionConstants.CHECKED_ACCOUNTS_ACCOUNT_REFS);
        String[] checkedBalancesRefs = httpServletRequest
                .getParameterValues(ConsentExtensionConstants.CHECKED_BALANCES_ACCOUNT_REFS);
        String[] checkedTransactionsRefs = httpServletRequest
                .getParameterValues(ConsentExtensionConstants.CHECKED_TRANSACTIONS_ACCOUNT_REFS);

        if (checkedAccountsRefs != null) {
            returnMaps.put(ConsentExtensionConstants.CHECKED_ACCOUNTS_ACCOUNT_REFS, AuthServletUtil
                    .convertToAccountRefObjectsArray(checkedAccountsRefs));
        }
        if (checkedBalancesRefs != null) {
            returnMaps.put(ConsentExtensionConstants.CHECKED_BALANCES_ACCOUNT_REFS, AuthServletUtil
                    .convertToAccountRefObjectsArray(checkedBalancesRefs));
        }
        if (checkedTransactionsRefs != null) {
            returnMaps.put(ConsentExtensionConstants.CHECKED_TRANSACTIONS_ACCOUNT_REFS, AuthServletUtil
                    .convertToAccountRefObjectsArray(checkedTransactionsRefs));
        }

        return returnMaps;
    }

    @Override
    public Map<String, String> updateConsentMetaData(HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public String getJSPPath() {

        return "/ob_berlin_default.jsp";
    }
}
