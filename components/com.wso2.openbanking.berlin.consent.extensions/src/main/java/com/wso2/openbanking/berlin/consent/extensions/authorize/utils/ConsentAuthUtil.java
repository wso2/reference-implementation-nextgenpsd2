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

package com.wso2.openbanking.berlin.consent.extensions.authorize.utils;

import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;

/**
 * Contains util methods need for authorization process.
 */
public class ConsentAuthUtil {

    private static final Log log = LogFactory.getLog(ConsentAuthUtil.class);

    /**
     * Returns the consent Id sent in the scope string of authorization request.
     *
     * @param scopeString the scope string of the request
     * @return the consent Id
     */
    public static String getConsentId(String scopeString) {

        String[] scopeArray = null;
        if (scopeString != null) {
            // The scopes string is split by a space here because the scopes are sent as space separated string
            scopeArray = scopeString.split(" ");
        }

        if (scopeArray != null) {
            for (String parameter : scopeArray) {
                if (StringUtils.startsWithIgnoreCase(parameter, "ais:") ||
                        StringUtils.startsWithIgnoreCase(parameter, "pis:") ||
                        StringUtils.startsWithIgnoreCase(parameter, "piis:")) {
                    if (parameter.split(":").length == 2) {
                        return parameter.split(":")[1];
                    } else {
                        return StringUtils.EMPTY;
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Validates the consent Id with provided consent type in scope string. If not valid, an error is sent to the
     * redirect URI of the request.
     *
     * @param consentType the consent type
     * @param scopeString the scope string sent in request
     * @param redirectUri the redirect URI of the request
     * @param state the state of the request
     * @throws ConsentException thrown if a validation failure happen
     */
    public static void validateConsentTypeWithId(String consentType, String scopeString, URI redirectUri,
                                                 String state)
            throws ConsentException {

        log.debug("Validating whether the provided consent Id matches with the scope type");

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), consentType)
                && !StringUtils.contains(scopeString, ConsentExtensionConstants.AIS_SCOPE)) {
            log.error(ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_SCOPE,
                            ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH, redirectUri, state));
        }

        if ((StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), consentType)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), consentType)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), consentType))
                && !StringUtils.contains(scopeString, ConsentExtensionConstants.PIS_SCOPE)) {
            log.error(ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_SCOPE,
                            ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH, redirectUri, state));
        }

        if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), consentType)
                && !StringUtils.contains(scopeString, ConsentExtensionConstants.PIIS_SCOPE)) {
            log.error(ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_SCOPE,
                            ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH, redirectUri, state));
        }
    }

    /**
     * Method to construct TPP errors which needs to send as a redirect.
     *
     * @param state state parameter of the request
     * @return the constructed error JSON object
     */
    public static JSONObject constructRedirectErrorJson(AuthErrorCode authErrorCode,
                                                        String errorDescription, URI redirectUri, String state) {

        JSONObject errorObject = new JSONObject();
        errorObject.appendField("error", authErrorCode.toString());
        errorObject.appendField("error_description", errorDescription);
        errorObject.appendField("redirect_uri", redirectUri.toString());
        errorObject.appendField("state", state);
        return errorObject;
    }
}
