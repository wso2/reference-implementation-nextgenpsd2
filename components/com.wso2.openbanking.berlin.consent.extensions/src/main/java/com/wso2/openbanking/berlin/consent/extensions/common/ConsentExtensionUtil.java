/**
 * Copyright (c) 2021-2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.common;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.HttpMethod;

/**
 * Consent extension utils.
 */
public class ConsentExtensionUtil {

    private static final Log log = LogFactory.getLog(ConsentExtensionUtil.class);

    /**
     * Returns the extracted account reference type from the account reference object.
     *
     * @param accountRefObject account reference object
     * @return account reference type
     */
    public static String getAccountReferenceType(JSONObject accountRefObject) {

        List<String> configuredAccountReferences = CommonConfigParser.getInstance().getSupportedAccountReferenceTypes();
        for (String accountRef : configuredAccountReferences) {
            if (accountRefObject.containsKey(accountRef)) {
                return accountRef;
            }
        }
        return null;
    }

    public static String getAccountReferenceType(org.json.JSONObject accountRefObject) {

        JSONObject mappedAccountRefObject = new JSONObject();

        for (Object keyObj : accountRefObject.keySet()) {
            String key = (String) keyObj;
            mappedAccountRefObject.appendField(key, accountRefObject.get(key));
        }

        return getAccountReferenceType(mappedAccountRefObject);
    }

    /**
     * Returns the string after appending the account reference type, account id and currency if available.
     *
     * @param accountRefObject account reference object
     * @return account reference type, account id and currency
     */
    public static String getAccountReferenceToPersist(JSONObject accountRefObject) {

        String configuredAccountReference = getAccountReferenceType(accountRefObject);
        String accountReference = String.format("%s%s%s", configuredAccountReference, CommonConstants.DELIMITER,
                accountRefObject.getAsString(configuredAccountReference));
        if (accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY)) {
            accountReference += String.format("%s%s", CommonConstants.DELIMITER, accountRefObject
                    .getAsString(ConsentExtensionConstants.CURRENCY));
        }
        return accountReference;
    }

    /**
     * Returns the part of the path that differentiates the request path into
     * either Accounts, Payments or Funds confirmations.
     *
     * @param requestPath the request path string
     * @return the part to recognize the consent service related to the request
     */
    public static String getServiceDifferentiatingRequestPath(String requestPath) {

        if (requestPath == null) {
            return "";
        }

        String[] requestPathArray = requestPath.split("/");

        if (StringUtils.contains(requestPath, ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END)
                || StringUtils.contains(requestPath,
                ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END)) {
            /*
            Example request paths applicable here:
            1) consents/{consentId}/authorisations
            2) {payment-service}/{payment-product}/{paymentId}/cancellation-authorisations
            3) {payment-service}/{payment-product}/{paymentId}/authorisations
            4) consents/confirmation-of-funds/{consentId}/authorisations
             */
            if (StringUtils.equals(ConsentExtensionConstants.ACCOUNTS_CONSENT_PATH, requestPathArray[0])
                    && !StringUtils.equals(ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH,
                    requestPathArray[1])) {
                /*
                Example request paths applicable here:
                1) consents/{consentId}/authorisations
                 */
                return requestPathArray[2];
            }
            /*
            Example request paths applicable here:
            1) {payment-service}/{payment-product}/{paymentId}/cancellation-authorisations
            2) {payment-service}/{payment-product}/{paymentId}/authorisations
            3) consents/confirmation-of-funds/{consentId}/authorisations
             */
            return requestPathArray[3];
        }

        /*
        Example request paths applicable here:
        1) consents
        2) payments/{payment-product}
        3) consents/confirmation-of-funds
         */
        if (requestPathArray.length > 1) {
            if (ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH.equals(requestPathArray[1])) {
                /*
                Example request paths applicable here:
                1) consents/confirmation-of-funds
                 */
                return requestPathArray[1];
            } else {
                /*
                Example request paths applicable here:
                1) consents
                2) payments/{payment-product}
                 */
                return requestPathArray[0];
            }
        } else {
            /*
            Example request paths applicable here:
            1) consents
             */
            return requestPathArray[0];
        }
    }

    /**
     * Used to get the consent type an authorisation request.
     *
     * @param requestPath authorisation request path string
     * @return returns the relative consent type for the request
     */
    public static String getConsentTypeFromRequestPath(String requestPath) {

        String[] pathElements = requestPath.split("/");
        String authorisationConsentType;

        if (pathElements.length > 1) {
            if (ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH.equals(pathElements[1])) {
                authorisationConsentType = pathElements[1];
            } else {
                authorisationConsentType = pathElements[0];
            }
        } else {
            authorisationConsentType = pathElements[0];
        }

        switch (authorisationConsentType) {
            case ConsentExtensionConstants.PAYMENTS_SERVICE_PATH:
                return ConsentTypeEnum.PAYMENTS.toString();
            case ConsentExtensionConstants.BULK_PAYMENTS_SERVICE_PATH:
                return ConsentTypeEnum.BULK_PAYMENTS.toString();
            case ConsentExtensionConstants.PERIODIC_PAYMENTS_SERVICE_PATH:
                return ConsentTypeEnum.PERIODIC_PAYMENTS.toString();
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH:
                return ConsentTypeEnum.FUNDS_CONFIRMATION.toString();
            default:
                return ConsentTypeEnum.ACCOUNTS.toString();
        }
    }

    /**
     * Ensures the psu ID is appended with the super tenant domain.
     *
     * @param psuId psu Id which is provided with the request
     * @return returns the psu Id with the super tenant ID appended
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
     * Constructs complex consent attribute keys with ":" separator.
     *
     * @param keys array of keys
     * @return constructed consent attribute key
     */
    public static String getConsentAttributeKey(String... keys) {
        StringBuilder consentAttributeKey = new StringBuilder();

        for (int i = 0; i < keys.length; i++) {
            if (i == keys.length - 1) {
                consentAttributeKey.append(keys[i]);
            } else {
                consentAttributeKey.append(keys[i]).append(CommonConstants.DELIMITER);
            }
        }

        return consentAttributeKey.toString();
    }

    /**
     * Returns the consent ID from the request path after validating it.
     *
     * @param requestMethod the http method of the request
     * @param requestPath   the request path string
     * @param consentType   the consent type
     * @return the consent ID from the request path after validating it
     */
    public static String getValidatedConsentIdFromRequestPath(String requestMethod, String requestPath,
                                                              String consentType) {

        String consentId;
        try {
            if (StringUtils.equals(HttpMethod.GET, requestMethod) || StringUtils.equals(HttpMethod.DELETE,
                    requestMethod)) {
                // Consent Id of accounts always situated in 1st position. Consent Id of payments and
                // funds confirmation always situated in 2nd position
                consentId = (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), consentType)) ?
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

                    consentId = (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), consentType)) ?
                            requestPathElements[1] : requestPathElements[2];
                    if (CommonUtil.isValidUuid(consentId)) {
                        return consentId;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.RESOURCE_UNKNOWN, ErrorConstants.PATH_INVALID));
        }
        return StringUtils.EMPTY;
    }

    /**
     * Constructs the consent status GET response.
     *
     * @param consentResource the current consent resource
     * @param consentType     the consent type
     * @return the consent status response
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
     * @param authResources current auth resources of the consent
     * @return the response for the consent authorisation GET request
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
     * @param authResources current auth resources of the consent
     * @param authId        the authorisation Id which is provided with the request
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

    /**
     * Validates the consent client ID with the registered client ID.
     *
     * @param registeredClientId the registered client id
     * @param consentClientId    the client id of the current consent
     */
    public static void validateClient(String registeredClientId, String consentClientId) {

        if (!StringUtils.equals(registeredClientId, consentClientId)) {
            throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.RESOURCE_UNKNOWN,
                    ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR));
        }
    }

    /**
     * Validates the request consent type with the type of the current consent.
     *
     * @param requestConsentType     the consent type which the request belongs to
     * @param typeOfRetrievedConsent the consent type of the current consent
     */
    public static void validateConsentType(String requestConsentType, String typeOfRetrievedConsent) {

        if (!StringUtils.equals(requestConsentType, typeOfRetrievedConsent)) {
            log.error(ErrorConstants.CONSENT_ID_TYPE_MISMATCH);
            throw new ConsentException(ResponseStatus.UNAUTHORIZED, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.CONSENT_ID_TYPE_MISMATCH));
        }
    }

    /**
     * Returns only the consent attribute key-value pairs that were not previously stored.
     *
     * @param oldAttributes already stored consent attributes
     * @param newAttributes new consent attributes to store
     * @return map of filtered consent attributes
     */
    public static Map<String, String> getFinalAttributesToStore(Map<String, String> oldAttributes,
                                                                Map<String, String> newAttributes) {

        Map<String, String> finalAttributesToStore = new HashMap<>();

        Set<Map.Entry<String, String>> oldAttributesEntrySet = oldAttributes.entrySet();
        Set<Map.Entry<String, String>> newAttributesEntrySet = newAttributes.entrySet();

        for (Map.Entry<String, String> newEntry : newAttributesEntrySet) {
            if (newEntry.getKey().contains(ConsentExtensionConstants.EXPLICIT_AUTH) || newEntry.getKey()
                    .contains(ConsentExtensionConstants.AUTH_CANCEL) || !oldAttributesEntrySet.contains(newEntry)) {
                finalAttributesToStore.put(newEntry.getKey(), newEntry.getValue());
            }
        }

        return finalAttributesToStore;
    }

    /**
     * Method to parse a provided date to ISO date. Throws an error is the provided date is invalid.
     *
     * @param dateToParse
     * @param errorCode
     * @param errorMessage
     * @return
     * @throws ConsentException
     */
    public static LocalDate parseDateToISO(String dateToParse, TPPMessage.CodeEnum errorCode, String errorMessage)
            throws ConsentException {

        LocalDate parsedDate;

        try {
            parsedDate = LocalDate.parse(dateToParse, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            log.error(errorMessage, e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, errorCode, errorMessage));
        }
        return parsedDate;
    }

    /**
     * This method checks a provided header key is in the header map without considering the case.
     *
     * @param headersJson headers map
     * @param headerKey header key that need to be checked
     * @return true if present, false otherwise
     */
    public static boolean checkCaseIgnoredHeader(Map<String, String> headersJson, String headerKey) {

        if (log.isDebugEnabled()) {
            log.debug("Validating header: " + headerKey);
        }
        for (String header : headersJson.keySet()) {
            if (header.equalsIgnoreCase(headerKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks a provided header key is in the header json without considering the case.
     *
     * @param headersJson headers json
     * @param headerKey header key that need to be checked
     * @return true if present, false otherwise
     */
    public static boolean checkCaseIgnoredHeader(JSONObject headersJson, String headerKey) {

        for (String header : headersJson.keySet()) {
            if (header.equalsIgnoreCase(headerKey)) {
                return true;
            }
        }
        return false;
    }
}
