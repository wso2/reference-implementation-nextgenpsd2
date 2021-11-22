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

import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.enums.ScaApproachEnum;
import com.wso2.openbanking.berlin.common.models.ScaApproach;
import com.wso2.openbanking.berlin.common.models.ScaMethod;
import net.minidev.json.JSONObject;

import java.util.List;

/**
 * Links element in response body constructor class.
 */
public class LinksConstructor {

    /**
     * Constructs the links object for initiation responses.
     *
     * @param isTppExplicitAuthorisationPreferred is explicit authorisation
     * @param currentScaApproach                  current SCA approach
     * @param currentScaMethods                   current SCA methods
     * @param requestPath                         request path of initiation
     * @param consentId                           consent/payment consentId
     * @param authorisationId                     authorisation resource consentId
     * @param consentType                         type of consent
     * @return constructed links object for initiation response
     */
    public static JSONObject getInitiationLinks(boolean isTppExplicitAuthorisationPreferred,
                                                ScaApproach currentScaApproach, List<ScaMethod> currentScaMethods,
                                                String requestPath, String consentId, String authorisationId,
                                                String consentType) {
        JSONObject links = new JSONObject();

        String apiVersion = CommonConfigParser.getInstance().getApiVersion(consentType);

        String selfLink = String.format(ConsentExtensionConstants.SELF_LINK_TEMPLATE,
                apiVersion, requestPath, consentId);
        links.appendField(ConsentExtensionConstants.SELF, new JSONObject()
                .appendField(ConsentExtensionConstants.HREF, selfLink));

        String statusLink = String.format(ConsentExtensionConstants.STATUS_LINK_TEMPLATE,
                apiVersion, requestPath, consentId);
        links.appendField(ConsentExtensionConstants.STATUS, new JSONObject()
                .appendField(ConsentExtensionConstants.HREF, statusLink));

        if (!isTppExplicitAuthorisationPreferred) {
            // Implicit authorisation
            String authResourceLink = String.format(ConsentExtensionConstants.AUTH_RESOURCE_LINK_TEMPLATE,
                    apiVersion, requestPath, consentId, authorisationId);
            if (ScaApproachEnum.REDIRECT.equals(currentScaApproach.getApproach())) {
                // Implicit REDIRECT approach
                String wellKnown = CommonConfigParser.getInstance().getOauthMetadataEndpoint();
                links.appendField(ConsentExtensionConstants.SCA_OAUTH, new JSONObject()
                        .appendField(ConsentExtensionConstants.HREF, wellKnown));

                links.appendField(ConsentExtensionConstants.SCA_STATUS, new JSONObject()
                        .appendField(ConsentExtensionConstants.HREF, authResourceLink));
            } else {
                // Implicit but SCA approach not decided
                if (currentScaMethods.size() > 1) {
                    // If SCA is required and has more than 1 current SCA method
                    links.appendField(ConsentExtensionConstants.SELECT_AUTH_METHOD, new JSONObject()
                            .appendField(ConsentExtensionConstants.HREF, authResourceLink));
                }
            }
        } else {
            // Explicit authorisation
            String startAuthorisationsLink = String.format(ConsentExtensionConstants.START_AUTH_LINK_TEMPLATE,
                    apiVersion, requestPath, consentId);
            if (ScaApproachEnum.REDIRECT.equals(currentScaApproach.getApproach())) {
                // Explicit REDIRECT approach
                links.appendField(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION, new JSONObject()
                        .appendField(ConsentExtensionConstants.HREF, startAuthorisationsLink));
            } else {
                // Explicit but SCA approach not decided
                if (currentScaMethods.size() > 1) {
                    // If SCA is required and has more than 1 current SCA method
                    links.appendField(ConsentExtensionConstants.START_AUTH_WITH_AUTH_METHOD_SELECTION, new JSONObject()
                            .appendField(ConsentExtensionConstants.HREF, startAuthorisationsLink));
                }
            }
        }

        return links;
    }

    /**
     * Constructs the links object for start authorisation responses.
     *
     * @param currentScaApproach current SCA approach
     * @param currentScaMethods  current SCA methods
     * @param requestPath        request path of initiation
     * @param authorisationId    authorisation resource consentId
     * @param consentType        type of consent
     * @return constructed links object for start authorisation response
     */
    public static JSONObject getStartAuthorisationLinks(ScaApproach currentScaApproach,
                                                        List<ScaMethod> currentScaMethods,
                                                        String requestPath, String authorisationId,
                                                        String consentType) {

        JSONObject links = new JSONObject();

        String apiVersion = CommonConfigParser.getInstance().getApiVersion(consentType);

        String authResourceLink = String.format(ConsentExtensionConstants.START_AUTH_RESOURCE_LINK_TEMPLATE,
                apiVersion, requestPath, authorisationId);
        if (ScaApproachEnum.REDIRECT.equals(currentScaApproach.getApproach())) {
            // REDIRECT approach
            String wellKnown = CommonConfigParser.getInstance().getOauthMetadataEndpoint();
            links.appendField(ConsentExtensionConstants.SCA_OAUTH, new JSONObject()
                    .appendField(ConsentExtensionConstants.HREF, wellKnown));

            links.appendField(ConsentExtensionConstants.SCA_STATUS, new JSONObject()
                    .appendField(ConsentExtensionConstants.HREF, authResourceLink));
        } else {
            // SCA approach not decided
            if (currentScaMethods.size() > 1) {
                // If SCA is required and has more than 1 current SCA method
                links.appendField(ConsentExtensionConstants.SELECT_AUTH_METHOD, new JSONObject()
                        .appendField(ConsentExtensionConstants.HREF, authResourceLink));
            }
        }

        return links;
    }

    /**
     * Method to construct payment cancellation explicit authorisation response links.
     *
     * @param requestPath request path of initiation
     * @param paymentId   payment id
     * @param consentType type of consent
     * @return constructed links object for payment cancellation explicit authorisation response
     */
    public static JSONObject getCancellationLinks(String requestPath, String paymentId, String consentType) {

        JSONObject links = new JSONObject();

        String apiVersion = CommonConfigParser.getInstance().getApiVersion(consentType);

        String selfLink = String.format(ConsentExtensionConstants.SELF_LINK_TEMPLATE,
                apiVersion, requestPath, paymentId);
        links.appendField(ConsentExtensionConstants.SELF, new JSONObject()
                .appendField(ConsentExtensionConstants.HREF, selfLink));

        String statusLink = String.format(ConsentExtensionConstants.STATUS_LINK_TEMPLATE,
                apiVersion, requestPath, paymentId);
        links.appendField(ConsentExtensionConstants.STATUS, new JSONObject()
                .appendField(ConsentExtensionConstants.HREF, statusLink));

        String startAuthorisationsLink = String.format(ConsentExtensionConstants.START_CANCELLATION_AUTH_LINK_TEMPLATE,
                apiVersion, requestPath, paymentId);
        links.appendField(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION, new JSONObject()
                .appendField(ConsentExtensionConstants.HREF, startAuthorisationsLink));
        return links;
    }

    /**
     * Constructs the links object for account consent get responses.
     *
     * @return constructed links for account consent get responses
     */
    public static JSONObject getAccountConsentGetLinks() {

        JSONObject links = new JSONObject();

        String apiVersion = CommonConfigParser.getInstance().getApiVersion(ConsentTypeEnum.ACCOUNTS.toString());

        links.appendField(ConsentExtensionConstants.ACCOUNT, new JSONObject()
                .appendField(ConsentExtensionConstants.HREF,
                        String.format(ConsentExtensionConstants.ACCOUNTS_LINK_TEMPLATE, apiVersion)));

        return links;
    }

}
