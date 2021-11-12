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

package com.wso2.openbanking.berlin.consent.extensions.authservlet.impl;

import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.authservlet.util.AuthServletUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

/**
 * The Berlin implementation of servlet extension that handles Berlin scenarios.
 */
public class OBBerlinAuthServletImpl  implements OBAuthServletInterface {

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
        return null;
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
