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
import com.wso2.openbanking.berlin.common.utils.ScaApproach;
import com.wso2.openbanking.berlin.common.utils.ScaApproachEnum;
import com.wso2.openbanking.berlin.common.utils.ScaMethod;
import net.minidev.json.JSONObject;

import java.util.List;

/**
 * Links element in response body constructor class.
 */
public class LinksConstructor {

    /**
     * Constructs the links object for initiation responses.
     *
     * @param isTppExplicitAuthorisationPreferred
     * @param currentScaApproach                  current SCA approach
     * @param currentScaMethods                   current SCA methods
     * @param requestPath                         request path of initiation
     * @param status                              consent/payment current status
     * @param scaStatus                           authorisation resource status
     * @param consentId                           consent/payment id
     * @param authorisationId                     authorisation resource id
     * @return constructed links object for initiation response
     */
    public static JSONObject getInitiationLinks(boolean isTppExplicitAuthorisationPreferred,
                                                ScaApproach currentScaApproach, List<ScaMethod> currentScaMethods,
                                                String requestPath, String status, String scaStatus, String consentId,
                                                String authorisationId) {
        JSONObject links = new JSONObject();

        // TODO: get v1 or v2 from config and append it before the links
        links.appendField(ConsentExtensionConstants.SELF, String.format("%s/%s", requestPath, consentId));
        links.appendField(ConsentExtensionConstants.STATUS, status);

        if (!isTppExplicitAuthorisationPreferred) {
            // Implicit authorisation (PSU-ID is mandated during implicit initiation
            // therefore no need to send updatePsuIdentification)
            if (ScaApproachEnum.REDIRECT.equals(currentScaApproach.getApproach())) {
                // Implicit REDIRECT approach
                String wellKnown = CommonConfigParser.getInstance().getOauthMetadataEndpoint();
                links.appendField(ConsentExtensionConstants.SCA_OAUTH, wellKnown);
                links.appendField(ConsentExtensionConstants.SCA_STATUS, scaStatus);
            } else if (ScaApproachEnum.DECOUPLED.equals(currentScaApproach.getApproach())) {
                // TODO: Append proper links for Implicit DECOUPLED approach
            } else {
                // Implicit but SCA approach not decided
                if (currentScaMethods.size() > 1) {
                    // If SCA is required and has more than 1 current SCA method
                    links.appendField(ConsentExtensionConstants.SELECT_AUTH_METHOD, String
                            .format("%s/%s/authorisations/%s", requestPath, consentId, authorisationId));
                }
            }
        } else {
            // Explicit authorisation (PSU-ID is not mandated during explicit initiation
            // therefore can send the startAuthorisationWithPsuIdentification.
            // PSU-ID is mandated during startAuthorisation)

            // NOTE: Optional according to the specification but decided to not consider the PSU-ID sent during
            // explicit initiation and give priority to PSU-ID sent during startAuthorisation therefore making the
            // PSU-ID in startAuthorisation mandatory due to unclear requirement
            if (ScaApproachEnum.REDIRECT.equals(currentScaApproach.getApproach())) {
                // Explicit REDIRECT approach
                links.appendField(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION, String
                        .format("%s/%s/authorisations", requestPath, consentId));
            } else if (ScaApproachEnum.DECOUPLED.equals(currentScaApproach.getApproach())) {
                // TODO: Append proper links for Explicit DECOUPLED approach
            } else {
                // Explicit but SCA approach not decided
                if (currentScaMethods.size() > 1) {
                    // If SCA is required and has more than 1 current SCA method
                    links.appendField(ConsentExtensionConstants.START_AUTH_WITH_AUTH_METHOD_SELECTION,
                            String.format("%s/%s/authorisations", requestPath, consentId));
                }
            }
        }

        return links;
    }
}
