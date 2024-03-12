/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
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

    /**
     * Check if request ID is set.
     *
     * @param requestID ID of the request.
     * @return JSON response with either an error message or "valid".
     */
    public static JSONObject validatePaymentRequestHeader(String requestID) {

        JSONObject responseJSON = new JSONObject();
        if (StringUtils.isBlank(requestID)) {
            responseJSON.put(CODE, FORBIDDEN.getStatusCode());
            responseJSON.put(MESSAGE, "X-Request_ID not found");
        } else {
            responseJSON.put(MESSAGE, VALID);
        }
        return responseJSON;
    }

}
