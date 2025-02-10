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

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyValidationException;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyValidationResult;
import com.wso2.openbanking.accelerator.consent.extensions.common.idempotency.IdempotencyValidator;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.CommonConstants;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.ScaApproach;
import org.wso2.openbanking.berlin.common.models.ScaMethod;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.CommonUtil;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.HeaderValidator;
import org.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;
import org.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains functions used for common consent flow.
 */
public class CommonConsentUtil {

    private static final Log log = LogFactory.getLog(CommonConsentUtil.class);
    private static final IdempotencyValidator idempotencyValidator = getIdempotencyValidator();

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

    /**
     * X-Request-ID validation util method. Throws appropriate error when detected.
     *
     * @param headers headers map
     */
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

    public static String getAuthorizationStatus(boolean isSCARequired, boolean isExplicitAuth,
                                                Map<String, String> headers) {
        String authStatus = (isSCARequired) ?
                ScaStatusEnum.SCA_METHOD_SELECTED.toString() :
                (isExplicitAuth) ?
                        ScaStatusEnum.RECEIVED.toString() :
                        headers.containsKey(ConsentExtensionConstants.PSU_ID_HEADER) ?
                                ScaStatusEnum.PSU_IDENTIFIED.toString() : ScaStatusEnum.RECEIVED.toString();
        return authStatus;
    }

    /**
     * Method to check whether the request is a valid idempotent request.
     *
     * @param consentManageData  Consent Manage Data object
     * @return whether the request is idempotent
     */
    public static boolean isIdempotent(ConsentManageData consentManageData) {

        try {
            IdempotencyValidationResult result = idempotencyValidator.validateIdempotency(consentManageData);
            if (result.isIdempotent()) {
                if (result.isValid()) {
                    log.debug("Idempotent request. Returning the previous response.");
                    appendResponsePayload(consentManageData, result.getConsent());
                    return true;
                } else {
                    log.error(ErrorConstants.X_REQUEST_ID_FRAUDULENT);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                            TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.X_REQUEST_ID_FRAUDULENT));
                }
            }
        } catch (IdempotencyValidationException e) {
            log.error(ErrorConstants.X_REQUEST_ID_FRAUDULENT, e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR, e.getMessage()));
        }
        return false;
    }

    /**
     * Method to append the idempotent response payload to the consent manage data.
     *
     * @param consentManageData   Consent Manage Data object
     * @param consent             Detailed Consent Resource object retrieved from DB
     */
    private static void appendResponsePayload(ConsentManageData consentManageData, DetailedConsentResource consent) {

        CommonConfigParser configParser = CommonConfigParser.getInstance();
        String apiVersion = configParser.getApiVersion(consent.getConsentType());
        boolean isSCARequired = configParser.isScaRequired();
        boolean isExplicitAuth = HeaderValidator.isTppExplicitAuthorisationPreferred(consentManageData.getHeaders());

        switch (ConsentExtensionUtil.getServiceDifferentiatingRequestPath(consentManageData.getRequestPath())) {

            case ConsentExtensionConstants.ACCOUNTS_CONSENT_PATH:
                consentManageData.setResponsePayload(AccountConsentUtil
                        .constructAccountInitiationResponse(consentManageData, consent, isExplicitAuth,
                                true, apiVersion, isSCARequired));
                consentManageData.setResponseHeader(ConsentExtensionConstants.ASPSP_MULTIPLE_CONSENT_SUPPORTED,
                        String.valueOf(configParser.isMultipleRecurringConsentEnabled()));
                consentManageData.setResponseStatus(ResponseStatus.CREATED);
                break;
            case ConsentExtensionConstants.PAYMENTS_SERVICE_PATH:
            case ConsentExtensionConstants.BULK_PAYMENTS_SERVICE_PATH:
            case ConsentExtensionConstants.PERIODIC_PAYMENTS_SERVICE_PATH:
                consentManageData.setResponsePayload(PaymentConsentUtil
                        .constructPaymentInitiationResponse(consentManageData, consent, isExplicitAuth,
                                true, apiVersion, isSCARequired));
                consentManageData.setResponseStatus(ResponseStatus.CREATED);
                break;
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH:
                consentManageData.setResponsePayload(FundsConfirmationConsentUtil
                        .constructFundsConfirmationInitiationResponse(consentManageData, consent, isExplicitAuth,
                                true, apiVersion, isSCARequired));
                consentManageData.setResponseStatus(ResponseStatus.CREATED);
                break;
            case ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END:
            case ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END:
                consentManageData.setResponsePayload(CommonConsentUtil
                        .constructStartAuthorisationResponse(consentManageData,
                                getCancelAuthResource(consent.getAuthorizationResources()), true,
                                apiVersion, isSCARequired));
                consentManageData.setResponseStatus(ResponseStatus.CREATED);
                break;
            default:
                return;
        }

    }

    /**
     * This method accepts a number of strings as arguments and outputs them according to the provided order
     * joined with underscore characters.
     *
     * @param strings a number of strings
     * @return joined strings using underscores
     */
    public static String constructAttributeKey(String... strings) {

        return StringUtils.join(strings, "_");
    }

    /**
     * Method to filter the authorizations based on the cancellation authorization type.
     * @param authResources   List of authorization resources
     * @return  List of authorization resources filtered based on the cancellation authorization type
     */
    private static AuthorizationResource getCancelAuthResource(ArrayList<AuthorizationResource> authResources) {
        for (AuthorizationResource authResource : authResources) {
            if (StringUtils.equals(AuthTypeEnum.CANCELLATION.toString(), authResource.getAuthorizationType())) {
                return authResource;
            }
        }
        return authResources.get(0);
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    public static IdempotencyValidator getIdempotencyValidator() {

        return new BerlinIdempotencyValidator();
    }
}
