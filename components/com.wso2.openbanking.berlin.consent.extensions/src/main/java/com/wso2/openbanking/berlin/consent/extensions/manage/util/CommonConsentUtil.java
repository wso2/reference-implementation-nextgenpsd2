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

package com.wso2.openbanking.berlin.consent.extensions.manage.util;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.ScaApproach;
import com.wso2.openbanking.berlin.common.models.ScaMethod;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.HeaderValidator;
import com.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains functions used for common consent flow.
 */
public class CommonConsentUtil {

    private static final Log log = LogFactory.getLog(CommonConsentUtil.class);

    /**
     * Validating the account reference object.
     * A valid account reference object can have a single supported account reference
     * type attribute and optionally a currency attribute.
     *
     * @param accountRefObject account reference object
     */
    public static void validateAccountRefObject(JSONObject accountRefObject) {

        if (accountRefObject == null) {
            log.error(ErrorConstants.ACCOUNT_REFERENCE_OBJECT_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.ACCOUNT_REFERENCE_OBJECT_MISSING));
        }

        Set<String> accountRefKeys = accountRefObject.keySet();
        boolean isAccountReferenceValid = true;
        if (accountRefKeys.size() == 1) {
            if (hasUnSupportedAccountRefTypes(accountRefKeys)) {
                isAccountReferenceValid = false;
            }
        } else if (accountRefKeys.size() == 2) {
            if (!accountRefKeys.contains(ConsentExtensionConstants.CURRENCY)
                    || hasUnSupportedAccountRefTypes(accountRefKeys)) {
                isAccountReferenceValid = false;
            }
        } else {
            isAccountReferenceValid = false;
        }

        if (!isAccountReferenceValid) {
            log.error(ErrorConstants.INVALID_ACCOUNT_REFERENCE);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.INVALID_ACCOUNT_REFERENCE));
        }

        String accountReference = getAccountReference(accountRefObject);
        if (StringUtils.isBlank(accountReference)) {
            log.error(ErrorConstants.ACCOUNT_REFERENCE_IS_EMPTY);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.ACCOUNT_REFERENCE_IS_EMPTY));
        }
    }

    /**
     * Checks if the account refs has an unsupported account ref.
     *
     * @param accountRefKeys account reference types sent in the initiation payload
     * @return true if account references has un supported account ref type
     */
    private static boolean hasUnSupportedAccountRefTypes(Set<String> accountRefKeys) {

        List<String> configuredAccountRefTypes = CommonConfigParser.getInstance().getSupportedAccountReferenceTypes();
        for (String accountRef : accountRefKeys) {
            // Skipping currency since it is not an account reference type
            if (StringUtils.equals(accountRef, ConsentExtensionConstants.CURRENCY)) {
                continue;
            }
            if (!configuredAccountRefTypes.contains(accountRef)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the account reference number from the provided account reference object.
     *
     * @param accountRefObject account reference object
     * @return account reference number
     */
    public static String getAccountReference(JSONObject accountRefObject) {

        String accountReference = "";

        if (accountRefObject.containsKey(ConsentExtensionConstants.IBAN)) {
            accountReference = accountRefObject.getAsString(ConsentExtensionConstants.IBAN);
        } else if (accountRefObject.containsKey(ConsentExtensionConstants.BBAN)) {
            accountReference = accountRefObject.getAsString(ConsentExtensionConstants.BBAN);
        } else if (accountRefObject.containsKey(ConsentExtensionConstants.PAN)) {
            accountReference = accountRefObject.getAsString(ConsentExtensionConstants.PAN);
        } else if (accountRefObject.containsKey(ConsentExtensionConstants.MASKED_PAN)) {
            accountReference = accountRefObject.getAsString(ConsentExtensionConstants.MASKED_PAN);
        } else if (accountRefObject.containsKey(ConsentExtensionConstants.MSISDN)) {
            accountReference = accountRefObject.getAsString(ConsentExtensionConstants.MSISDN);
        }

        return accountReference;
    }

    /**
     * Method to construct start authorisation response.
     *
     * @param consentManageData            consent manage data
     * @param createdAuthorizationResource the created authorization resource
     * @param isRedirectPreferred          whether redirect approach is preferred or not
     * @param apiVersion                   the configured API version to construct the links
     * @param isSCARequired                whether SCA is required or not as configured
     * @return the constructed start authorisation response
     */
    public static JSONObject constructStartAuthorisationResponse(ConsentManageData consentManageData,
                                                                 AuthorizationResource createdAuthorizationResource,
                                                                 boolean isRedirectPreferred, String apiVersion,
                                                                 boolean isSCARequired) {

        String requestPath = consentManageData.getRequestPath();
        String consentType = ConsentExtensionUtil.getConsentTypeFromRequestPath(requestPath);
        String locationString = String.format(ConsentExtensionConstants.SELF_LINK_TEMPLATE,
                apiVersion, requestPath, createdAuthorizationResource.getAuthorizationID());
        consentManageData.setResponseHeader(ConsentExtensionConstants.LOCATION_HEADER,
                locationString);

        Map<String, Object> scaElements = CommonUtil.getScaApproachAndMethods(isRedirectPreferred,
                isSCARequired);
        ScaApproach scaApproach = (ScaApproach) scaElements.get(CommonConstants.SCA_APPROACH_KEY);
        ArrayList<ScaMethod> scaMethods =
                (ArrayList<ScaMethod>) scaElements.get(CommonConstants.SCA_METHODS_KEY);
        consentManageData.setResponseHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH,
                scaApproach.getApproach().toString());

        JSONObject responseWithoutLinks = CommonConsentUtil.getStartAuthorisationResponse(createdAuthorizationResource,
                scaMethods);

        JSONObject links = LinksConstructor.getStartAuthorisationLinks(scaApproach, scaMethods, requestPath,
                createdAuthorizationResource.getAuthorizationID(), consentType);

        return responseWithoutLinks.appendField(ConsentExtensionConstants.LINKS, links);
    }

    /**
     * Method to get the account start authorisation response without links.
     *
     * @param createdAuthorizationResource the created authorization resource
     * @param scaMethods                   decided SCA methods
     * @return the constructed start authorisation response without links
     */
    public static JSONObject getStartAuthorisationResponse(AuthorizationResource createdAuthorizationResource,
                                                           ArrayList<ScaMethod> scaMethods) {

        JSONObject responseObject = new JSONObject();
        responseObject.appendField(ConsentExtensionConstants.SCA_STATUS, createdAuthorizationResource
                .getAuthorizationStatus());
        responseObject.appendField(ConsentExtensionConstants.AUTH_ID, createdAuthorizationResource
                .getAuthorizationID());

        JSONArray chosenSCAMethods = new JSONArray();
        for (ScaMethod scaMethod : scaMethods) {
            chosenSCAMethods.add(CommonUtil.convertObjectToJson(scaMethod));
        }

        if (scaMethods.size() > 1) {
            responseObject.appendField(ConsentExtensionConstants.SCA_METHODS, chosenSCAMethods);
        } else {
            responseObject.appendField(ConsentExtensionConstants.CHOSEN_SCA_METHOD, chosenSCAMethods.get(0));
        }

        return responseObject;
    }

    /**
     * Stores the SCA related info to consent attributes during initiation.
     *
     * @param consentAttributesMap map of consent attributes
     * @param createdConsent       created consent
     * @param scaInfoMap           SCA details
     */
    public static void storeInitiationScaInfoToConsentAttributes(Map<String, String> consentAttributesMap,
                                                                 DetailedConsentResource createdConsent,
                                                                 Map<String, Object> scaInfoMap) {

        String authId = createdConsent.getAuthorizationResources().get(0).getAuthorizationID();
        ScaApproach scaApproach = (ScaApproach) scaInfoMap.get(CommonConstants.SCA_APPROACH_KEY);

        String scaApproachKey = ConsentExtensionUtil
                .getConsentAttributeKey(CommonConstants.SCA_APPROACH_KEY, authId);
        consentAttributesMap.put(scaApproachKey, scaApproach.getApproach().toString());

        ScaMethod scaMethod = CommonUtil.getScaMethod(scaApproach.getApproach());
        if (scaMethod != null) {
            String scaMethodKey = ConsentExtensionUtil
                    .getConsentAttributeKey(CommonConstants.SCA_METHOD_KEY, authId);
            consentAttributesMap.put(scaMethodKey, scaMethod.getAuthenticationMethodId());
        }
    }

    public static void validateXRequestId(Map<String, String> headers) {

        if (!HeaderValidator.isHeaderStringPresent(headers, ConsentExtensionConstants.X_REQUEST_ID_HEADER)) {
            log.error(ErrorConstants.X_REQUEST_ID_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.X_REQUEST_ID_MISSING));
        }

        if (!HeaderValidator.isHeaderValidUUID(headers, ConsentExtensionConstants.X_REQUEST_ID_HEADER)) {
            log.error(ErrorConstants.X_REQUEST_ID_INVALID);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.X_REQUEST_ID_INVALID));
        }
    }
}
