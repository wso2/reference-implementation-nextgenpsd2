/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.berlin.consent.extensions.manage.util;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.CommonConstants;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.common.models.ScaApproach;
import org.wso2.openbanking.berlin.common.models.ScaMethod;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.CommonUtil;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Map;

/**
 * Contains functions used for funds confirmation consent flow.
 */
public class FundsConfirmationConsentUtil {

    private static final Log log = LogFactory.getLog(FundsConfirmationConsentUtil.class);

    /**
     * Method to validate funds confirmation initiation payload.
     *
     * @param payload
     */
    public static void validateFundsConfirmationInitiationPayload(JSONObject payload) {

        log.debug("Validating mandatory request body elements");
        if (!payload.containsKey(ConsentExtensionConstants.ACCOUNT)
                || payload.get(ConsentExtensionConstants.ACCOUNT) == null) {
            log.error(ErrorConstants.MANDATORY_ELEMENTS_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.MANDATORY_ELEMENTS_MISSING));
        }

        if (payload.containsKey(ConsentExtensionConstants.CARD_EXPIRY_DATE)) {
            log.debug("Validating card expiry date");
            validateCardExpiryDate(payload.getAsString(ConsentExtensionConstants.CARD_EXPIRY_DATE));
        }

        JSONObject accountObject = (JSONObject) payload.get(ConsentExtensionConstants.ACCOUNT);

        log.debug("Validating account reference object");
        CommonConsentUtil.validateAccountRefObject(accountObject);
    }

    /**
     * Validate the requested card expiry date.
     *
     * @param cardExpiryDate requested card expiry date
     */
    public static void validateCardExpiryDate(String cardExpiryDate) {

        LocalDate parsedCardExpiryDate = ConsentExtensionUtil.parseDateToISO(cardExpiryDate,
                TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.CARD_EXPIRY_DATE_INVALID);

        if (parsedCardExpiryDate.isBefore(LocalDate.now(ZoneOffset.UTC))) {
            String errorMessage = String.format("The provided card expiry date %s is a past date",
                    parsedCardExpiryDate);
            log.error(errorMessage);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.TIMESTAMP_INVALID,
                    errorMessage));
        }
    }

    /**
     * Method to construct funds confirmation initiation response.
     *
     * @param consentManageData   consent manage data
     * @param createdConsent      the created consent
     * @param isExplicitAuth      whether explicit authorisation or not
     * @param isRedirectPreferred whether redirect approach is preferred or not
     * @param apiVersion          the configured API version to construct the self links
     * @param isSCARequired       whether SCA is required or not as configured
     * @return the constructed initiation response
     */
    public static JSONObject constructFundsConfirmationInitiationResponse(ConsentManageData consentManageData,
                                                                          DetailedConsentResource createdConsent,
                                                                          boolean isExplicitAuth,
                                                                          boolean isRedirectPreferred,
                                                                          String apiVersion, boolean isSCARequired) {

        String requestPath = consentManageData.getRequestPath();
        String locationString = String.format(ConsentExtensionConstants.SELF_LINK_TEMPLATE,
                apiVersion, requestPath, createdConsent.getConsentID());
        consentManageData.setResponseHeader(ConsentExtensionConstants.LOCATION_HEADER,
                locationString);

        Map<String, Object> scaElements = CommonUtil.getScaApproachAndMethods(isRedirectPreferred,
                isSCARequired);
        ScaApproach scaApproach = (ScaApproach) scaElements.get(CommonConstants.SCA_APPROACH_KEY);
        ArrayList<ScaMethod> scaMethods =
                (ArrayList<ScaMethod>) scaElements.get(CommonConstants.SCA_METHODS_KEY);
        consentManageData.setResponseHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH,
                scaApproach.getApproach().toString());

        JSONObject responseWithoutLinks = FundsConfirmationConsentUtil
                .getFundsConfirmationInitiationResponse(createdConsent, scaMethods);

        String authId = null;

        if (!isExplicitAuth) {
            // Always only one auth resource is created for implicit initiation
            ArrayList<AuthorizationResource> authResources = createdConsent.getAuthorizationResources();
            AuthorizationResource implicitAuthResource = authResources.get(0);
            authId = implicitAuthResource.getAuthorizationID();
        }

        JSONObject links = LinksConstructor.getInitiationLinks(isExplicitAuth, scaApproach,
                scaMethods, requestPath, createdConsent.getConsentID(), authId,
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString());

        return responseWithoutLinks.appendField(ConsentExtensionConstants.LINKS, links);
    }

    /**
     * Method to get the funds confirmation initiation response without links.
     *
     * @param createdConsent the created consent
     * @param scaMethods     decided SCA methods
     * @return the constructed initiation response without links
     */
    public static JSONObject getFundsConfirmationInitiationResponse(DetailedConsentResource createdConsent,
                                                                    ArrayList<ScaMethod> scaMethods) {

        JSONObject responseObject = new JSONObject();
        responseObject.appendField(ConsentExtensionConstants.CONSENT_STATUS, createdConsent.getCurrentStatus());
        responseObject.appendField(ConsentExtensionConstants.CONSENT_ID, createdConsent.getConsentID());

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
     * Method to construct funds confirmation consent get response.
     *
     * @param retrievedConsent consent object
     * @return the constructed funds confirmation consent get response
     * @throws ParseException
     */
    public static JSONObject constructFundsConfirmationConsentGetResponse(ConsentResource retrievedConsent)
            throws ParseException {

        JSONObject consentReceipt =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(retrievedConsent.getReceipt());

        consentReceipt.appendField(ConsentExtensionConstants.CONSENT_STATUS, retrievedConsent.getCurrentStatus());

        return consentReceipt;
    }

}
