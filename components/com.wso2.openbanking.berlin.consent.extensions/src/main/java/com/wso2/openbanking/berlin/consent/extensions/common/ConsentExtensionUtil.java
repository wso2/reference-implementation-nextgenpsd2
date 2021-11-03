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

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import javax.ws.rs.HttpMethod;

/**
 * Consent extension utils.
 */
public class ConsentExtensionUtil {

    /**
     * Gets the consent service using the request path.
     *
     * @param requestPath
     * @return
     */
    public static String getServiceDifferentiatingRequestPath(String requestPath) {

        if (requestPath == null) {
            return "";
        }

        String[] requestPathArray = requestPath.split("/");

        if (StringUtils.contains(requestPath, ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END)
                || StringUtils.contains(requestPath,
                ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END)) {

            return requestPathArray[3];
        }

        if (requestPathArray.length > 1) {
            if (ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH.equals(requestPathArray[1])) {
                return requestPathArray[1];
            } else {
                return requestPathArray[0];
            }
        } else {
            return requestPathArray[0];
        }
    }

    /**
     * Used to get the consent type an authorisation request.
     *
     * @param requestPath
     * @return
     */
    public static String getAuthorisationConsentType(String requestPath) {

        String[] pathElements = requestPath.split("/");
        return pathElements[0];
    }

    /**
     * Ensures the psu ID is appended with the super tenant domain.
     *
     * @param psuId
     * @return
     */
    public static String appendSuperTenantDomain(String psuId) {

        if (StringUtils.isNotBlank(psuId)) {
            if (psuId.endsWith(ConsentExtensionConstants.SUPER_TENANT_DOMAIN)) {
                return psuId;
            } else {
                return psuId + ConsentExtensionConstants.SUPER_TENANT_DOMAIN;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the consent ID from the request path after validating it.
     *
     * @param requestMethod
     * @param requestPath
     * @param consentType
     * @return
     */
    public static String getValidatedConsentIdFromRequestPath(String requestMethod, String requestPath,
                                                              String consentType) {

        String consentId;

        if (StringUtils.equals(HttpMethod.GET, requestMethod) || StringUtils.equals(HttpMethod.DELETE, requestMethod)) {
            // Consent Id of accounts always situated in 1nd position. Consent Id of payments and funds confirmation
            // always situated in 2st position
            consentId = (StringUtils.equals(ConsentExtensionConstants.ACCOUNTS, consentType)) ?
                    requestPath.split("/")[1] : requestPath.split("/")[2];
            if (CommonUtil.isValidUuid(consentId)) {
                return consentId;
            }
        } else if (StringUtils.equals(HttpMethod.POST, requestMethod)) {
            String[] requestPathElements = requestPath.split("/");
            String lastElement = requestPathElements[requestPathElements.length - 1];
            if (StringUtils.equals(ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END, lastElement)
                    || StringUtils
                    .equals(ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END,
                            lastElement)) {

                consentId = (StringUtils.equals(ConsentExtensionConstants.ACCOUNTS, consentType)) ?
                        requestPathElements[1] : requestPathElements[2];
                if (CommonUtil.isValidUuid(consentId)) {
                    return consentId;
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Constructs the consent status GET response.
     *
     * @param consentResource
     * @param consentType
     * @return
     */
    public static JSONObject getConsentStatusResponse(ConsentResource consentResource, String consentType) {

        JSONObject consentStatusResponse = new JSONObject();

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), consentType)
                || StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), consentType)) {
            consentStatusResponse.appendField(ConsentExtensionConstants.CONSENT_STATUS,
                    consentResource.getCurrentStatus());
        } else {
            consentStatusResponse.appendField(ConsentExtensionConstants.TRANSACTION_STATUS,
                    consentResource.getCurrentStatus());
        }
        return consentStatusResponse;
    }

    /**
     * Constructs the consent authorisation GET response.
     *
     * @param authResources
     * @return
     */
    public static JSONObject getAuthorisationGetResponse(ArrayList<AuthorizationResource> authResources) {

        JSONObject authorisationsGetResponse = new JSONObject();
        JSONArray authIdsArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(authResources)) {
            for (AuthorizationResource authResource : authResources) {
                authIdsArray.add(authResource.getAuthorizationID());
            }
        }
        authorisationsGetResponse.appendField(ConsentExtensionConstants.AUTHORISATION_IDS, authIdsArray);
        return authorisationsGetResponse;
    }

    /**
     * Constructs the consent authorisation status GET request.
     *
     * @param authResources
     * @param authId
     * @return
     */
    public static JSONObject getAuthorisationGetStatusResponse(ArrayList<AuthorizationResource> authResources,
                                                               String authId) {

        JSONObject authorisationGetStatusResponse = new JSONObject();
        if (CollectionUtils.isNotEmpty(authResources)) {
            for (AuthorizationResource authResource : authResources) {
                if (StringUtils.equals(authId, authResource.getAuthorizationID())) {
                    authorisationGetStatusResponse.appendField(ConsentExtensionConstants.SCA_STATUS,
                            authResource.getAuthorizationStatus());
                    break;
                }
            }
        }
        return authorisationGetStatusResponse;
    }
}
