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

package com.wso2.openbanking.berlin.consent.extensions.common;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ScaApproachEnum;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Header validations class.
 */
public class HeaderValidator {

    private static final Log log = LogFactory.getLog(HeaderValidator.class);

    /**
     * Validates the PSU-IP-Address request header.
     *
     * @param headers request headers
     */
    public static void validatePsuIpAddress(Map<String, String> headers) {

        log.debug("Validating PSU-IP-Address header");
        if (headers.containsKey(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER)) {
            String psuIpAddress = headers.get(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER);

            if (StringUtils.isEmpty(psuIpAddress)) {
                log.error(String.format("Invalid %s header", ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER));
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                        null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        String.format("Invalid %s header", ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER)
                ));
            }
        } else {
            log.error(ErrorConstants.PSU_IP_ADDRESS_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.PSU_IP_ADDRESS_MISSING));
        }
    }

    /**
     * Validates the PSU-IP-Address request header sent in a
     * JSON object during consent validation.
     *
     * @param headersObject headers object
     */
    public static void validatePsuIpAddress(JSONObject headersObject) {

        Map<String, String> headerMap = new HashMap<>();

        if (headersObject.containsKey(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER)) {
            headerMap.put(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER,
                    headersObject.getAsString(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER));
        }
        validatePsuIpAddress(headerMap);
    }

    /**
     * Validates the X-Request-ID request header.
     *
     * @param headers request headers
     */
    public static void validateXRequestId(Map<String, String> headers) {
        log.debug("Validating the X-Request-ID header");
        if (headers.containsKey(ConsentExtensionConstants.X_REQUEST_ID_HEADER)) {
            String xRequestId = headers.get(ConsentExtensionConstants.X_REQUEST_ID_HEADER);

            if (StringUtils.isEmpty(xRequestId) || !CommonUtil.isValidUuid(xRequestId)) {
                log.error(ErrorConstants.X_REQUEST_ID_INVALID);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                        null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.X_REQUEST_ID_INVALID));
            }
        } else {
            log.error(ErrorConstants.X_REQUEST_ID_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.X_REQUEST_ID_MISSING));
        }
    }

    /**
     * Validates the X-Request-ID request header sent in a
     * JSON object during consent validation.
     *
     * @param headersObject headers object
     */
    public static void validateXRequestId(JSONObject headersObject) {

        Map<String, String> headerMap = new HashMap<>();

        if (headersObject.containsKey(ConsentExtensionConstants.X_REQUEST_ID_HEADER)) {
            headerMap.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER,
                    headersObject.getAsString(ConsentExtensionConstants.X_REQUEST_ID_HEADER));
        }
        validateXRequestId(headerMap);
    }

    /**
     * Validates the TPP-Redirect-Preferred request header.
     *
     * @param headers request headers
     */
    public static void validateTppRedirectPreferredHeader(Map<String, String> headers) {

        log.debug("Validating TPP-Redirect-Preferred header according to the specification");
        Optional<Boolean> isRedirectPreferred = isTppRedirectPreferred(headers);

        if ((isRedirectPreferred.isPresent() && BooleanUtils.isTrue(isRedirectPreferred.get()))
                && CommonUtil.getScaApproach(ScaApproachEnum.REDIRECT) == null) {
            log.error(String.format("%s SCA Approach is not supported", ScaApproachEnum.REDIRECT));
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    String.format("%s SCA Approach is not supported", ScaApproachEnum.REDIRECT)));
        }

        if ((isRedirectPreferred.isPresent() && BooleanUtils.isFalse(isRedirectPreferred.get()))
                && CommonUtil.getScaApproach(ScaApproachEnum.DECOUPLED) == null) {

            //todo: Since decoupled approach is not supported yet, an error is thrown if the redirect header is false.
            //issue: https://github.com/wso2-enterprise/financial-open-banking/issues/6858
            log.error(String.format("%s SCA Approach is not supported", ScaApproachEnum.DECOUPLED));
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    String.format("%s SCA Approach is not supported", ScaApproachEnum.DECOUPLED)));
        }
    }

    /**
     * Validates the TPP-Explicit-Authorisation-Preferred request header.
     *
     * @param headers request headers
     * @return
     */
    public static boolean isTppExplicitAuthorisationPreferred(Map<String, String> headers) {
        log.debug("Determining whether the consent request is implicit or explicit");
        if (headers.containsKey(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER)) {
            return Boolean.parseBoolean(headers.get(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER));
        }

        return false;
    }

    /**
     * Validates the TPP-Redirect-Preferred request header.
     *
     * @param headers request headers
     * @return if redirect approach preferred or not
     */
    public static Optional<Boolean> isTppRedirectPreferred(Map<String, String> headers) {
        log.debug("Determining whether the TPP-Redirect-Preferred header is true or false or not present");
        if (headers.containsKey(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER)) {
            return Optional.of(Boolean.parseBoolean(headers
                    .get(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER)));
        }

        return Optional.empty();
    }

    /**
     * Mandates any header and returns an error if not present.
     *
     * @param headers request headers
     * @param header  header to mandate
     */
    public static void mandateHeader(Map<String, String> headers, String header) {
        if (!headers.containsKey(header)) {
            log.error(header + " header is missing");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    header + " header is missing"));
        }
    }

    /**
     * Checks if the Consent-ID header is present and in proper format.
     *
     * @param headersObject headers object
     */
    public static void validateConsentId(JSONObject headersObject) {
        log.debug("Validating the Consent-ID header");
        if (headersObject.containsKey(ConsentExtensionConstants.CONSENT_ID_HEADER)) {
            String consentId = headersObject.getAsString(ConsentExtensionConstants.CONSENT_ID_HEADER);

            if (StringUtils.isEmpty(consentId) || !CommonUtil.isValidUuid(consentId)) {
                log.error(ErrorConstants.CONSENT_ID_INVALID);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                        null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.CONSENT_ID_INVALID));
            }
        } else {
            log.error(ErrorConstants.CONSENT_ID_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.CONSENT_ID_MISSING));
        }
    }

}
