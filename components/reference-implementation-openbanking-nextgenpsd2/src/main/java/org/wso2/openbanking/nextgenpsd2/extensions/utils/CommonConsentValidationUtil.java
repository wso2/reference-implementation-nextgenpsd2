/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.nextgenpsd2.extensions.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.configurations.ConfigurationConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.CommonConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ConsentStatusEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ConsentTypeEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ScaApproachEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.ScaStatusEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.enums.TransactionStatusEnum;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.BadRequestException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ValidationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.ConsentManagementResponseHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.AccountConsentManageHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.ConsentAuthorisationManageHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.FundsConfirmationConsentManageHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.PaymentConsentManageHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ScaApproach;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ScaMethod;
import org.wso2.openbanking.nextgenpsd2.extensions.model.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PreProcessConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.PreProcessConsentRetrievalData;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.StoredBasicConsentResourceData;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.SuccessResponseConsentRevocation;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.SuccessResponseConsentRevocationData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Common utility class for handling consent operations.
 */
public class CommonConsentValidationUtil {

    private static final Log log = LogFactory.getLog(CommonConsentValidationUtil.class);
    private static final Pattern uuidPattern = Pattern.compile
            ("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);

    /**
     * This method checks a provided header key is in the header map without considering the case.
     *
     * @param headersJson headers map
     * @param headerKey header key that need to be checked
     * @return true if present, false otherwise
     */
    public static boolean checkCaseIgnoredHeader(JSONObject headersJson, String headerKey) {

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
     * Convert an object to a JSON object.
     * @param object
     * @return
     * @throws Exception
     */
    public static JSONObject convertObjectToJson(Object object) throws JSONException {
        String jsonString;

        try {
            // Convert Object to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            jsonString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JSONException(e);
        }

        // Parse JSON string to JSONObject
        return new JSONObject(jsonString);
    }

    /**
     * Extracts the account reference number from the provided account reference object.
     *
     * @param accountRefObject account reference object
     * @return account reference number
     */
    public static String getAccountReference(JSONObject accountRefObject) {

        String accountReference = "";

        if (accountRefObject.has(ConsentExtensionConstants.IBAN)) {
            accountReference = accountRefObject.getString(ConsentExtensionConstants.IBAN);
        } else if (accountRefObject.has(ConsentExtensionConstants.BBAN)) {
            accountReference = accountRefObject.getString(ConsentExtensionConstants.BBAN);
        } else if (accountRefObject.has(ConsentExtensionConstants.PAN)) {
            accountReference = accountRefObject.getString(ConsentExtensionConstants.PAN);
        } else if (accountRefObject.has(ConsentExtensionConstants.MASKED_PAN)) {
            accountReference = accountRefObject.getString(ConsentExtensionConstants.MASKED_PAN);
        } else if (accountRefObject.has(ConsentExtensionConstants.MSISDN)) {
            accountReference = accountRefObject.getString(ConsentExtensionConstants.MSISDN);
        }

        return accountReference;
    }

    /**
     *
     * @param consentType
     * @return
     */
    public static String getApiVersion(String consentType) {
        if (ConsentTypeEnum.ACCOUNTS.toString().equals(consentType)) {
            return ConfigurationConstants.AIS_API_VERSION;
        }

        if (ConsentTypeEnum.PAYMENTS.toString().equals(consentType)
                || ConsentTypeEnum.BULK_PAYMENTS.toString().equals(consentType)
                || ConsentTypeEnum.PERIODIC_PAYMENTS.toString().equals(consentType)) {
            return ConfigurationConstants.PIS_API_VERSION;
        }

        if (ConsentTypeEnum.FUNDS_CONFIRMATION.toString().equals(consentType)) {
            return ConfigurationConstants.PIIS_API_VERSION;
        }

        return "";
    }

    /**
     * Returns authorisation status based on preference.
     * @param isSCARequired
     * @param headers
     * @return
     */
    public static String getAuthorizationStatus(boolean isSCARequired, JSONObject headers) {
        return (isSCARequired) ?
                ScaStatusEnum.SCA_METHOD_SELECTED.toString() :
                headers.has(ConsentExtensionConstants.PSU_ID_HEADER) ?
                        ScaStatusEnum.PSU_IDENTIFIED.toString() : ScaStatusEnum.RECEIVED.toString();
    }

    /**
     * Method to get the Consent Manage Service Handler.
     *
     * @param requestPath Request path of the request
     * @return ServiceHandler
     */
    public static ConsentManagementResponseHandler getConsentManagementResponseHandler(String requestPath) {

        switch (getServiceDifferentiatingRequestPath(requestPath)) {
            case ConsentExtensionConstants.ACCOUNTS_CONSENT_PATH:
                return new AccountConsentManageHandler();
            case ConsentExtensionConstants.PAYMENTS_SERVICE_PATH:
            case ConsentExtensionConstants.BULK_PAYMENTS_SERVICE_PATH:
            case ConsentExtensionConstants.PERIODIC_PAYMENTS_SERVICE_PATH:
                return new PaymentConsentManageHandler();
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SERVICE_PATH:
                return new FundsConfirmationConsentManageHandler();
            case ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END:
            case ConsentExtensionConstants.PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END:
                return new ConsentAuthorisationManageHandler();
            default:
                return null;
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
     * Constructs the consent status GET response.
     *
     * @param consentResource the current consent resource
     * @param consentType     the consent type
     * @param payloadToSend   the response payload
     */
    public static void appendConsentStatusResponse(StoredBasicConsentResourceData consentResource,
                                                   String consentType, JSONObject payloadToSend) {

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), consentType)
                || StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), consentType)) {
            payloadToSend.put(ConsentExtensionConstants.CONSENT_STATUS,
                    consentResource.getStatus());
        } else {
            payloadToSend.put(ConsentExtensionConstants.TRANSACTION_STATUS,
                    consentResource.getStatus());
        }
    }

    /**
     * Gets the configured default SCA approach.
     *
     * @return default SCA approach
     */
    public static ScaApproach getDefaultScaApproach() {
        List<ScaApproach> scaApproaches = getSupportedScaApproaches();

        for (ScaApproach scaApproach : scaApproaches) {
            if (scaApproach.isDefault()) {
                return scaApproach;
            }
        }

        return null;
    }

    /**
     * Gets the configured default SCA method.
     *
     * @return default SCA method
     */
    public static ScaMethod getDefaultScaMethod() {
        List<ScaMethod> scaMethods = getSupportedScaMethods();

        for (ScaMethod scaMethod : scaMethods) {
            if (scaMethod.isDefault()) {
                return scaMethod;
            }
        }

        return null;
    }

    /**
     * Gets the SCA approach from the list of approaches.
     *
     * @param scaApproachEnum the SCA approach to find
     * @return found SCA approach
     */
    public static ScaApproach getScaApproach(ScaApproachEnum scaApproachEnum) {
        List<ScaApproach> scaApproaches = getSupportedScaApproaches();

        for (ScaApproach scaApproach : scaApproaches) {
            if (scaApproachEnum.equals(scaApproach.getApproach())) {
                return scaApproach;
            }
        }

        return null;
    }

    /**
     * Determines the current SCA approach and the SCA methods based on the parameters.
     *
     * @param isTppRedirectPreferred TPP-Redirect-Preferred header
     * @param isScaRequired          to know if SCA is required or not
     * @return returns the current SCA approach and methods
     */
    public static Map<String, Object> getScaApproachAndMethods(Boolean isTppRedirectPreferred, boolean isScaRequired) {
        List<ScaMethod> supportedScaMethods = getSupportedScaMethods();

        ScaApproach currentScaApproach = new ScaApproach();
        List<ScaMethod> currentScaMethods = new ArrayList<>();

        if (Boolean.TRUE.equals(isTppRedirectPreferred)) {
            currentScaApproach = getScaApproach(ScaApproachEnum.REDIRECT);
            if (isScaRequired) {
                currentScaMethods.add(getScaMethod(ScaApproachEnum.REDIRECT));
            }
        } else if (Boolean.FALSE.equals(isTppRedirectPreferred)) {
            currentScaApproach = getScaApproach(ScaApproachEnum.DECOUPLED);
            if (isScaRequired) {
                currentScaMethods.add(getScaMethod(ScaApproachEnum.DECOUPLED));
            }
        } else {
            // When TPP-Redirect-Preferred header is not sent
            if (isScaRequired) {
                if (supportedScaMethods.size() == 1) {
                    // If SCA is required and there is only a single supported SCA method, it becomes the selected
                    // SCA method and the mapped approach of the SCA method becomes the SCA approach
                    currentScaApproach = getScaApproach(supportedScaMethods.get(0).getMappedApproach());
                    currentScaMethods = supportedScaMethods;
                } else {
                    // If a default SCA method is configured then that becomes the SCA approach and method
                    ScaMethod defaultScaMethod = getDefaultScaMethod();

                    if (defaultScaMethod != null) {
                        currentScaApproach = getDefaultScaApproach();
                        currentScaMethods.add(defaultScaMethod);
                    } else {
                        // Approach not finalised and the TPP/PSU will be given the choice to select
                        currentScaMethods = supportedScaMethods;
                    }
                }
            } else {
                currentScaApproach = getDefaultScaApproach();
            }
        }

        Map<String, Object> scaApproachAndMethods = new HashMap<>();
        scaApproachAndMethods.put(CommonConstants.SCA_APPROACH_KEY, currentScaApproach);
        scaApproachAndMethods.put(CommonConstants.SCA_METHODS_KEY, currentScaMethods);

        return scaApproachAndMethods;
    }

    /**
     * Gets the SCA method for the specified approach from the list of methods.
     *
     * @param scaApproachEnum the SCA method to find
     * @return found SCA method
     */
    public static ScaMethod getScaMethod(ScaApproachEnum scaApproachEnum) {
        List<ScaMethod> scaMethods = getSupportedScaMethods();

        for (ScaMethod scaMethod : scaMethods) {
            if (scaApproachEnum.equals(scaMethod.getMappedApproach())) {
                return scaMethod;
            }
        }

        return null;
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
     * To get a list of ScaMethod.
     *
     * @return a list of configured SCA methods after mapping it to the ScaMethod class.
     */
    public static List<ScaMethod> getSupportedScaMethods() {
        List<ScaMethod> supportedScaMethods = new ArrayList<>();

        for (Map<String, String> supportedScaMethod : ConfigurationConstants.SUPPORTED_SCA_METHODS) {
            ScaMethod scaMethod = new ScaMethod();

            scaMethod.setAuthenticationType(supportedScaMethod.get(CommonConstants.SCA_TYPE));
            scaMethod.setAuthenticationVersion(supportedScaMethod.get(CommonConstants.SCA_VERSION));
            scaMethod.setAuthenticationMethodId(supportedScaMethod.get(CommonConstants.SCA_ID));
            scaMethod.setName(supportedScaMethod.get(CommonConstants.SCA_NAME));
            scaMethod.setMappedApproach(ScaApproachEnum.fromValue(supportedScaMethod
                    .get(CommonConstants.SCA_MAPPED_APPROACH)));
            scaMethod.setDescription(supportedScaMethod.get(CommonConstants.SCA_DESCRIPTION));
            scaMethod.setDefault(Boolean.parseBoolean(supportedScaMethod.get(CommonConstants.SCA_DEFAULT)));

            supportedScaMethods.add(scaMethod);
        }

        return supportedScaMethods;
    }

    /**
     * To get a list of ScaApproach.
     *
     * @return a list of configured SCA approaches after mapping it to the ScaApproach class.
     */
    public static List<ScaApproach> getSupportedScaApproaches() {
        List<ScaApproach> supportedScaApproaches = new ArrayList<>();

        for (Map<String, String> supportedScaApproach : ConfigurationConstants.SUPPORTED_SCA_APPROACHES) {
            ScaApproach scaApproach = new ScaApproach();

            scaApproach.setApproach(ScaApproachEnum.fromValue(supportedScaApproach.get(CommonConstants.SCA_NAME)));
            scaApproach.setDefault(Boolean.parseBoolean(supportedScaApproach.get(CommonConstants.SCA_DEFAULT)));

            supportedScaApproaches.add(scaApproach);
        }

        return supportedScaApproaches;
    }

    /**
     * Checks if the account refs has an unsupported account ref.
     *
     * @param accountRefKeys account reference types sent in the initiation payload
     * @return true if account references has un supported account ref type
     */
    private static boolean hasUnSupportedAccountRefTypes(Set<String> accountRefKeys) {

        List<String> configuredAccountRefTypes = ConfigurationConstants.SUPPORTED_ACC_REFERNCE_TYPES;
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
     * Validates the TPP-Redirect-Preferred request header.
     *
     * @param headersJSON request headers
     * @return if redirect approach preferred or not
     */
    public static Optional<Boolean> isTppRedirectPreferred(JSONObject headersJSON) {
        log.debug("Determining whether the TPP-Redirect-Preferred header is true or false or not present");
        if (checkCaseIgnoredHeader(headersJSON,
                ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER)) {
            return Optional.of(Boolean.parseBoolean(headersJSON
                    .getString(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER)));
        }

        return Optional.empty();
    }

    /**
     * Validates a format of UUID.
     *
     * @param stringUuid
     * @return
     */
    public static boolean isValidUuid(String stringUuid) {
        return uuidPattern.matcher(stringUuid.trim()).matches();
    }

    /**
     * Method to parse a provided date to ISO date. Throws an error is the provided date is invalid.
     *
     * @param dateToParse
     * @param errorCode
     * @param errorMessage
     * @return
     * @throws ValidationFailureException
     */
    public static LocalDate parseDateToISO(String dateToParse, TPPMessage.CodeEnum errorCode, String errorMessage)
            throws ValidationFailureException {

        LocalDate parsedDate;

        try {
            parsedDate = LocalDate.parse(dateToParse, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            log.error(errorMessage, e);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR, errorCode, errorMessage));
        }
        return parsedDate;
    }

    /**
     * Validating the account reference object.
     * A valid account reference object can have a single supported account reference
     * type attribute and optionally a currency attribute.
     *
     * @param accountRefObject account reference object
     */
    public static void validateAccountRefObject(JSONObject accountRefObject) throws ValidationFailureException {

        if (accountRefObject == null) {
            log.error(ErrorConstants.ACCOUNT_REFERENCE_OBJECT_MISSING);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.ACCOUNT_REFERENCE_OBJECT_MISSING));
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
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.INVALID_ACCOUNT_REFERENCE));
        }

        String accountReference = getAccountReference(accountRefObject);
        if (StringUtils.isBlank(accountReference)) {
            log.error(ErrorConstants.ACCOUNT_REFERENCE_IS_EMPTY);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.ACCOUNT_REFERENCE_IS_EMPTY));
        }
    }

    public static void validateIdempotencyHeader(Object headers) throws ValidationFailureException {
        // To allow idempotency header validation it needs to be forwarded
        // Therefore X-Request-ID needs to be added to the configuration in the IS deployment.toml
        // [financial_services.consent.manage_extension]
        // allowed_headers = ["X-Request-ID"]

        // Assuming the accelerator only forwards (even empty) JSON object
        JSONObject headersJSON = convertObjectToJson(headers);

        if (!headersJSON.has(ConsentExtensionConstants.X_REQUEST_ID_HEADER)) {
            log.error(ErrorConstants.X_REQUEST_ID_MISSING);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.X_REQUEST_ID_MISSING));
        }

        if (!CommonConsentValidationUtil.isValidUuid(headersJSON
                .getString(ConsentExtensionConstants.X_REQUEST_ID_HEADER))) {
            log.error(ErrorConstants.X_REQUEST_ID_INVALID);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.X_REQUEST_ID_INVALID));
        }
    }

    /**
     * Validates the PSU-IP-Address request header.
     *
     * @param headers request headers
     */
    public static void validatePsuIpAddress(JSONObject headers) throws ValidationFailureException {

        log.debug("Validating PSU-IP-Address header");
        if (headers.has(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER)) {
            String psuIpAddress = headers.getString(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER);

            if (StringUtils.isEmpty(psuIpAddress)) {
                log.error(String.format("Invalid %s header",
                        ConsentExtensionConstants.PSU_IP_ADDRESS_PROPER_CASE_HEADER));
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                TPPMessage.CodeEnum.FORMAT_ERROR, String.format("Invalid %s header",
                                        ConsentExtensionConstants.PSU_IP_ADDRESS_PROPER_CASE_HEADER)
                ));
            }
        } else {
            log.error(ErrorConstants.PSU_IP_ADDRESS_MISSING);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PSU_IP_ADDRESS_MISSING));
        }
    }

    /**
     * Validates the TPP-Redirect-Preferred request header.
     *
     * @param headers request headers
     */
    public static void validateTppRedirectPreferredHeader(JSONObject headers) throws ValidationFailureException {

        log.debug("Validating TPP-Redirect-Preferred header according to the specification");
        Optional<Boolean> isRedirectPreferred = isTppRedirectPreferred(headers);

        if ((isRedirectPreferred.isPresent() && BooleanUtils.isTrue(isRedirectPreferred.get()))
                && getScaApproach(ScaApproachEnum.REDIRECT) == null) {
            log.error(String.format("%s SCA Approach is not supported", ScaApproachEnum.REDIRECT));
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, String.format("%s SCA Approach is not supported",
                                    ScaApproachEnum.REDIRECT)));
        }

        if ((isRedirectPreferred.isPresent() && BooleanUtils.isFalse(isRedirectPreferred.get()))
                && getScaApproach(ScaApproachEnum.DECOUPLED) == null) {

            //ToDo: Since decoupled approach is not supported yet, an error is thrown if the redirect header is false.
            //issue: https://github.com/wso2-enterprise/financial-open-banking/issues/6858
            log.error(String.format("%s SCA Approach is not supported", ScaApproachEnum.DECOUPLED));
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, String.format("%s SCA Approach is not supported",
                                    ScaApproachEnum.DECOUPLED)));
        }
    }

    /**
     * Validates the consent client ID with the registered client ID.
     *
     * @param registeredClientId the registered client id
     * @param consentClientId    the client id of the current consent
     */
    public static void validateClient(String registeredClientId, String consentClientId)
            throws ValidationFailureException {

        if (!StringUtils.equals(registeredClientId, consentClientId)) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.FORBIDDEN,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.RESOURCE_UNKNOWN, ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR));
        }
    }

    /**
     * Validates the request consent type with the type of the current consent.
     *
     * @param requestConsentType     the consent type which the request belongs to
     * @param typeOfRetrievedConsent the consent type of the current consent
     */
    public static void validateConsentType(String requestConsentType, String typeOfRetrievedConsent)
            throws ValidationFailureException {

        if (!StringUtils.equals(requestConsentType, typeOfRetrievedConsent)) {
            log.error(ErrorConstants.CONSENT_ID_TYPE_MISMATCH);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.UNAUTHORIZED,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.CONSENT_INVALID, ErrorConstants.CONSENT_ID_TYPE_MISMATCH));
        }
    }

    public static JSONObject getIdempotencyHeaderJSON(String xRequestID) {
        JSONObject idempotencyHeader = new JSONObject();
        idempotencyHeader.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER, xRequestID);
        return idempotencyHeader;
    }

    /**
     * Builds consent revocation response.
     *
     * @param requestBody
     * @return
     */
    public static SuccessResponseConsentRevocation buildConsentRevocationResponse(
            PreProcessConsentRequestBody requestBody) {
        SuccessResponseConsentRevocation validationResponse = new SuccessResponseConsentRevocation();
        validationResponse.setResponseId(requestBody.getRequestId());
        validationResponse.setStatus(SuccessResponseConsentRevocation.StatusEnum.SUCCESS);

        // Set revocation response data
        StoredBasicConsentResourceData consentData = requestBody.getData().getConsentResource();
        SuccessResponseConsentRevocationData responseData = new SuccessResponseConsentRevocationData();
        if (consentData.getType().contains(ConsentExtensionConstants.PAYMENTS)) {
            responseData.setRevocationStatusName(TransactionStatusEnum.CANC.name());
        } else {
            responseData.setRevocationStatusName(ConsentStatusEnum.TERMINATED_BY_TPP.toString());
        }
        responseData.setRequireTokenRevocation(getIfRequireTokenRevocation(consentData));

        validationResponse.setData(responseData);
        return validationResponse;
    }

    /**
     * Decide if token revocation is necessary given the status of consent.
     *
     * @param consentData
     * @return
     */
    private static String getIfRequireTokenRevocation(StoredBasicConsentResourceData consentData) {
        // Check if consent is authorized
        if (ConsentStatusEnum.VALID.toString().equals(consentData.getStatus())) {
            return "true";
        }

        // Check if a valid token can exist for transaction
        //ToDo: Verify that these are the only statuses of transaction where a token revocation would be necessary
        if (TransactionStatusEnum.ACCP.name().equals(consentData.getStatus())) {
            return "true";
        }

        return "false";
    }

    /**
     * Validates revoke request for payment, account and funds confirmation consents and returns built response.
     *
     * @param requestBody
     * @return
     * @throws BadRequestException
     * @throws ValidationFailureException
     */
    public static SuccessResponseConsentRevocation
    validateRevokeRequestAndReturnResponse(PreProcessConsentRequestBody requestBody) throws BadRequestException,
            ValidationFailureException {

        PreProcessConsentRetrievalData data = requestBody.getData();
        StoredBasicConsentResourceData consentResource = data.getConsentResource();
        String requestPath = data.getConsentResourcePath();
        String consentType = CommonConsentValidationUtil.getConsentTypeFromRequestPath(requestPath);
        String consentId = requestBody.getData().getConsentId();

        // Validate client
        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for valid client", consentId));
        }

        // Get request client id from the headers
        String requestClientId;
        JSONObject headers;
        try {
            headers = CommonConsentValidationUtil.convertObjectToJson(data.getRequestHeaders());
            requestClientId = headers.getString(CommonConstants.X_WSO2_CLIENT_ID_KEY);
        } catch (JSONException e) {
            // Should be unreachable (since insequence always adds client id header)
            throw new BadRequestException(ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR,
                    "x-wso2-client-id header not found"));
        }
        CommonConsentValidationUtil.validateClient(requestClientId, data.getConsentResource().getClientId());

        // Validate consent type
        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating consent of Id %s for correct type", consentId));
        }
        CommonConsentValidationUtil.validateConsentType(consentType, consentResource.getType());

        // Validate consent is revocable (single payments cannot be revoked)
        CommonConsentValidationUtil.validateIfConsentTypeIsRevocable(consentType);

        log.debug("Send an error if the consent is already deleted");
        if (StringUtils.equals(ConsentStatusEnum.REVOKED_BY_PSU.toString(), consentResource.getStatus())
                || StringUtils.equals(ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                consentResource.getStatus())) {
            log.error(ErrorConstants.CONSENT_ALREADY_DELETED);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.UNAUTHORIZED,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.CONSENT_INVALID, ErrorConstants.CONSENT_ALREADY_DELETED));
        }

        // Check whether the consent is already expired before deleting
        if (StringUtils.equals(ConsentStatusEnum.EXPIRED.toString(), consentResource.getStatus())) {
            log.error(ErrorConstants.CONSENT_ALREADY_EXPIRED);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.UNAUTHORIZED,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.CONSENT_INVALID, ErrorConstants.CONSENT_ALREADY_EXPIRED));
        }

        return buildConsentRevocationResponse(requestBody);
    }

    /**
     * Validates if the consent type is revocable.
     *
     * @param consentType
     */
    private static void validateIfConsentTypeIsRevocable(String consentType) throws ValidationFailureException {
        if (ConsentTypeEnum.PAYMENTS.toString().equals(consentType)) {
            log.error(String.format(ErrorConstants.CANCELLATION_NOT_APPLICABLE));
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.METHOD_NOT_ALLOWED,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.CANCELLATION_INVALID,
                            String.format(ErrorConstants.CANCELLATION_NOT_APPLICABLE)));
        }
    }
}
