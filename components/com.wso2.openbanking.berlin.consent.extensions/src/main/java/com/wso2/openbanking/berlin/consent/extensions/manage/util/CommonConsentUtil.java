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

import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.models.ScaApproach;
import com.wso2.openbanking.berlin.common.models.ScaMethod;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Contains functions used for common consent flow.
 */
public class CommonConsentUtil {

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
            responseObject.appendField(ConsentExtensionConstants.CHOSEN_SCA_METHOD, chosenSCAMethods);
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

}
