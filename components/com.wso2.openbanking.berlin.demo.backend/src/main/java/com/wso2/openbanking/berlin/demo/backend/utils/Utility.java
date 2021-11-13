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

package com.wso2.openbanking.berlin.demo.backend.utils;

import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import static com.wso2.openbanking.berlin.demo.backend.utils.Constants.AccountService.CODE;
import static com.wso2.openbanking.berlin.demo.backend.utils.Constants.AccountService.MESSAGE;
import static com.wso2.openbanking.berlin.demo.backend.utils.Constants.AccountService.VALID;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

/**
 * Utility methods.
 */
public class Utility {

    private Utility() {

    }

    /**
     * Check if request ID and consent ID are set.
     *
     * @param requestID ID of the request.
     * @param consentID ID gotten after consent transaction.
     * @return JSON response with either an error message or "valid".
     */
    public static JSONObject validateRequestHeader(String requestID, String consentID) {

        JSONObject responseJSON = new JSONObject();
        if (StringUtils.isBlank(requestID)) {
            responseJSON.put(CODE, FORBIDDEN.getStatusCode());
            responseJSON.put(MESSAGE, "X-Request_ID not found");
            return responseJSON;
        } else if (StringUtils.isBlank(consentID)) {
            responseJSON.put(CODE, FORBIDDEN.getStatusCode());
            responseJSON.put(MESSAGE, "Consent-ID not found");
            return responseJSON;
        } else {
            responseJSON.put(MESSAGE, VALID);
            return responseJSON;
        }
    }

}
