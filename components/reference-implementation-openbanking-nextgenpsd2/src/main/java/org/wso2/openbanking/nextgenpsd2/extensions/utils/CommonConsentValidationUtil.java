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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
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
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.AuthorizationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.BadRequestException;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ValidationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.ConsentAuthorizationHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.ConsentManagementValidationHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.AccountConsentAuthorizeHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.AccountConsentManageHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.ConsentAuthorisationManageHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.FundsConfirmationConsentAuthorizeHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.FundsConfirmationConsentManageHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.PaymentConsentAuthorizeHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.handlers.impl.PaymentConsentManageHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.model.AccountReference;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ScaApproach;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ScaMethod;
import org.wso2.openbanking.nextgenpsd2.extensions.model.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.Account;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PopulateConsentAuthorizeScreenData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PreProcessConsentRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.PreProcessConsentRetrievalData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.StoredBasicConsentResourceData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponseConsentRevocation;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponseConsentRevocationData;
import org.wso2.openbanking.nextgenpsd2.extensions.generated.model.SuccessResponsePopulateConsentAuthorizeScreenData;

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

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Common utility class for handling consent operations.
 */
public class CommonConsentValidationUtil {

    private static final Log log = LogFactory.getLog(CommonConsentValidationUtil.class);
    private static final Pattern uuidPattern = Pattern.compile
            ("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    /**
     * This method checks a provided header key is in the header map without considering the case.
     *
     * @param headersJson headers map
     * @param headerKey header key that need to be checked
     * @return true if present, false otherwise
     */
    public static boolean checkCaseIgnoredHeader(String requestId, JSONObject headersJson, String headerKey) {

        if (log.isDebugEnabled()) {
            log.debug("[" + requestId + "] " + "Validating header: " + headerKey);
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
     *
     * @param object json convertible object
     * @return JSONObject of passed object
     * @throws BadRequestException if the passed object cannot be converted to JSON
     */
    public static JSONObject convertObjectToJson(Object object) throws BadRequestException {
        String jsonString;

        try {
            // Convert Object to JSON string
            jsonString = objectMapper.writeValueAsString(object);

            // Parse JSON string to JSONObject
            return new JSONObject(jsonString);
        } catch (JsonProcessingException | JSONException e) {
            throw new BadRequestException(e.getMessage().replaceAll("[\r\n]", ""), e);
        }
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
     * Get api version from consent type.
     * eg: consents for funds-confirmation was introduced in v2 extension of the nextGenPSD2 specification
     *
     * @param consentType type of consent
     * @return api version
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
     * @param isSCARequired whether Strong Customer Authentication is required for consents
     * @param headers request headers
     * @return the authorization status after consent creation (implicit authorization)
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
    public static ConsentManagementValidationHandler getConsentManagementResponseHandler(String requestPath)
            throws BadRequestException, ValidationFailureException {

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
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.NOT_FOUND,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR, null,
                                ErrorConstants.PATH_INVALID));
        }
    }

    /**
     * Method to get the account list authorize handler.
     *
     * @param type consent type of the request
     * @return the selected account list retrieval handler
     */
    public static ConsentAuthorizationHandler getAuthorizationHandler(String type) {

        ConsentAuthorizationHandler authorizationHandler = null;

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), type)) {
            authorizationHandler = new AccountConsentAuthorizeHandler();
        } else if (StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), type)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), type)) {
            authorizationHandler = new PaymentConsentAuthorizeHandler();
        } else if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), type)) {
            authorizationHandler = new FundsConfirmationConsentAuthorizeHandler();
        }
        return authorizationHandler;
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
    public static Optional<Boolean> isTppRedirectPreferred(String requestId, JSONObject headersJSON) {
        log.debug("[" + requestId + "] " + "Determining whether the TPP-Redirect-Preferred header is true or false " +
                "or not present");
        if (checkCaseIgnoredHeader(requestId, headersJSON,
                ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER)) {
            return Optional.of(Boolean.parseBoolean(headersJSON
                    .getString(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER)));
        }

        return Optional.empty();
    }

    /**
     * Validates a format of UUID.
     *
     * @param stringUuid string of UUID format
     * @return whether the sting matches UUID pattern
     */
    public static boolean isValidUuid(String stringUuid) {
        return uuidPattern.matcher(stringUuid.trim()).matches();
    }

    /**
     * Method to parse a provided date to ISO date. Throws an error is the provided date is invalid.
     *
     * @param dateToParse date to parse as a string
     * @param errorCode error code for the error to throw if parsing failed
     * @param errorMessage error message for the error to throw is parsing failed
     * @return string parsed to a LocalData object
     * @throws ValidationFailureException if parsing failed
     * @throws BadRequestException if construction of error in nextGenPSD2 format failed
     */
    public static LocalDate parseDateToISO(String dateToParse, TPPMessage.CodeEnum errorCode,
                                           String errorMessage)
            throws ValidationFailureException, BadRequestException {

        LocalDate parsedDate;

        try {
            parsedDate = LocalDate.parse(dateToParse, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR, errorCode, errorMessage));
        }
        return parsedDate;
    }

    public static void validateRequestIdentificationHeader(Object headers)
            throws ValidationFailureException, BadRequestException {
        // To allow idempotency header validation it needs to be forwarded
        // Therefore X-Request-ID needs to be added to the configuration in the IS deployment.toml
        // [financial_services.consent.manage_extension]
        // allowed_headers = ["X-Request-ID"]

        // Assuming the accelerator only forwards (even empty) JSON object
        JSONObject headersJSON = convertObjectToJson(headers);

        if (!headersJSON.has(ConsentExtensionConstants.X_REQUEST_ID_HEADER)) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.X_REQUEST_ID_MISSING));
        }

        if (!CommonConsentValidationUtil.isValidUuid(headersJSON
                .getString(ConsentExtensionConstants.X_REQUEST_ID_HEADER))) {
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
    public static void validatePsuIpAddress(String requestId, JSONObject headers)
            throws ValidationFailureException, BadRequestException {

        log.debug("[" + requestId + "] " + "Validating PSU-IP-Address header");
        if (headers.has(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER)) {
            String psuIpAddress = headers.getString(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER);

            if (StringUtils.isEmpty(psuIpAddress)) {
                throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                TPPMessage.CodeEnum.FORMAT_ERROR, String.format("Invalid %s header",
                                        ConsentExtensionConstants.PSU_IP_ADDRESS_PROPER_CASE_HEADER)
                ));
            }
        } else {
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
    public static void validateTppRedirectPreferredHeader(String requestId, JSONObject headers)
            throws ValidationFailureException, BadRequestException {

        log.debug("[" + requestId + "] " + "Validating TPP-Redirect-Preferred header according to the specification");
        Optional<Boolean> isRedirectPreferred = isTppRedirectPreferred(requestId, headers);

        if ((isRedirectPreferred.isPresent() && BooleanUtils.isTrue(isRedirectPreferred.get()))
                && getScaApproach(ScaApproachEnum.REDIRECT) == null) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, String.format("%s SCA Approach is not supported",
                                    ScaApproachEnum.REDIRECT)));
        }

        if ((isRedirectPreferred.isPresent() && BooleanUtils.isFalse(isRedirectPreferred.get()))
                && getScaApproach(ScaApproachEnum.DECOUPLED) == null) {

            //ToDo: Since decoupled approach is not supported yet, an error is thrown if the redirect header is false.
            //issue: https://github.com/wso2-enterprise/financial-open-banking/issues/6858
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
            throws ValidationFailureException, BadRequestException {

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
            throws ValidationFailureException, BadRequestException {

        if (!StringUtils.equals(requestConsentType, typeOfRetrievedConsent)) {
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
     * @param requestBody request body from the pre-process consent revocation request
     * @return success response for consent revocation
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
     * @param consentData consent data from request
     * @return whether token revocation is required or not as a string
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
     * @param requestBody consent revocation request body
     * @return success response for consent revocation
     * @throws BadRequestException if the request body is malformed
     * @throws ValidationFailureException if consent revocation request fails validations
     */
    public static SuccessResponseConsentRevocation
    validateRevokeRequestAndReturnResponse(PreProcessConsentRequestBody requestBody) throws BadRequestException,
            ValidationFailureException {
        String requestId = requestBody.getRequestId();

        PreProcessConsentRetrievalData data = requestBody.getData();
        StoredBasicConsentResourceData consentResource = data.getConsentResource();
        String requestPath = data.getConsentResourcePath();
        String consentType = CommonConsentValidationUtil.getConsentTypeFromRequestPath(requestPath);
        String consentId = requestBody.getData().getConsentId();

        // Validate client
        if (log.isDebugEnabled()) {
            log.debug("[" + requestId + "] " + String.format("Validating consent of Id %s for valid client",
                    consentId));
        }

        // Get request client id from the headers
        String requestClientId;
        JSONObject headers;
        try {
            headers = CommonConsentValidationUtil.convertObjectToJson(data.getRequestHeaders());
            requestClientId = headers.getString(CommonConstants.X_WSO2_CLIENT_ID_KEY);
        } catch (JSONException e) {
            // Should be unreachable (since insequence always adds client id header)
            throw new BadRequestException("x-wso2-client-id header not found");
        }
        CommonConsentValidationUtil.validateClient(requestClientId, data.getConsentResource().getClientId());

        // Validate consent type
        if (log.isDebugEnabled()) {
            log.debug("[" + requestId + "] " + String.format("Validating consent of Id %s for correct type",
                    consentId));
        }
        CommonConsentValidationUtil.validateConsentType(consentType, consentResource.getType());

        // Validate consent is revocable (single payments cannot be revoked)
        CommonConsentValidationUtil.validateIfConsentTypeIsRevocable(consentType);

        log.debug("[" + requestId + "] " + "Verify if the consent is already revoked");
        if (StringUtils.equals(ConsentStatusEnum.REVOKED_BY_PSU.toString(), consentResource.getStatus())
                || StringUtils.equals(ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                consentResource.getStatus())) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.UNAUTHORIZED,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.CONSENT_INVALID, ErrorConstants.CONSENT_ALREADY_DELETED));
        }

        // Check whether the consent is already expired before deleting
        if (StringUtils.equals(ConsentStatusEnum.EXPIRED.toString(), consentResource.getStatus())) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.UNAUTHORIZED,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.CONSENT_INVALID, ErrorConstants.CONSENT_ALREADY_EXPIRED));
        }

        return buildConsentRevocationResponse(requestBody);
    }

    /**
     * Validates if the consent type is revocable.
     *
     * @param consentType consent type of consent to revoke
     * @throws ValidationFailureException if consent is irrevocable
     */
    private static void validateIfConsentTypeIsRevocable(String consentType)
            throws ValidationFailureException, BadRequestException {
        if (ConsentTypeEnum.PAYMENTS.toString().equals(consentType)) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.METHOD_NOT_ALLOWED,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.CANCELLATION_INVALID,
                            String.format(ErrorConstants.CANCELLATION_NOT_APPLICABLE)));
        }
    }

    /**
     * Disables default violation and sets built constraint violation.
     *
     * @param context context of the constraint validator
     * @param violationMessage violation message to return
     */
    public static void setConstrainViolation(ConstraintValidatorContext context, String violationMessage) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
    }

    /**
     * Builds constraint violation message with error and error code.
     * Sets FORMAT_ERROR as default.
     * overload method for {@link #buildViolationMessage(TPPMessage.CodeEnum, String)} with default error code.
     *
     * @param error error message to return from violation
     * @return built violation message
     */
    public static String buildViolationMessage(String error) {
        return buildViolationMessage(TPPMessage.CodeEnum.FORMAT_ERROR, error);
    }

    /**
     * Builds constraint violation message with error and error code.
     *
     * @param errorCode error code to return from violation
     * @param error error message to return from violation
     * @return built violation message
     */
    public static String buildViolationMessage(TPPMessage.CodeEnum errorCode, String error) {
        return errorCode.toString() + ":" + error;
    }

    /**
     * Splits retrieved violation message to error and error code.
     *
     * @param violationMessage breaks violation message to get error code and error message
     * @return split error code and error message
     */
    public static String[] splitViolationMessage(String violationMessage) {
        return violationMessage.split(":", 2);
    }

    /**
     * Validates the given JSON payload against a model class using Hibernate Validator.
     * This method expects all violation messages to be built by
     * {@link CommonConsentValidationUtil#buildViolationMessage(TPPMessage.CodeEnum, String)}
     *
     * @param jsonPayload   JSON string representing the model payload
     * @param modelClass    Class object of the model
     * @param <T>   The model type to be validated
     * @return  validated and mapped object
     * @throws ValidationFailureException   if deserialization or validation fails
     */
    public static <T> T validateJSONFromModel(String jsonPayload, Class<T> modelClass)
            throws ValidationFailureException, BadRequestException {
        T mappedObject;

        // Map to object
        try {
            mappedObject = objectMapper.readValue(jsonPayload, modelClass);
        } catch (JsonProcessingException e) {
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_FORMAT_ERROR));
        }

        // Find violations
        Set<ConstraintViolation<T>> violations = validator.validate(mappedObject);

        // Throw first validation error from validation failures
        if (!violations.isEmpty()) {
            String[] codeAndMessage = splitViolationMessage(violations.iterator().next().getMessage());
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                            TPPMessage.CodeEnum.valueOf(codeAndMessage[0]), codeAndMessage[1]));
        }

        return mappedObject;
    }

    /**
     * Returns the extracted account reference type from the account reference object.
     *
     * @param accountRefObject account reference JSON object
     * @return account reference type
     */
    public static String getAccountReferenceType(JSONObject accountRefObject) {

        List<String> configuredAccountReferences = ConfigurationConstants.SUPPORTED_ACC_REFERNCE_TYPES;
        for (String accountRef : configuredAccountReferences) {
            if (accountRefObject.has(accountRef)) {
                return accountRef;
            }
        }
        return null;
    }

    /**
     * Returns the extracted account reference type from the account reference object.
     *
     * @param accountRefObject account reference map object
     * @return account reference type
     */
    public static String getAccountReferenceType(Map<String, ?> accountRefObject) {

        List<String> configuredAccountReferences = ConfigurationConstants.SUPPORTED_ACC_REFERNCE_TYPES;
        for (String accountRef : configuredAccountReferences) {
            if (accountRefObject.containsKey(accountRef)) {
                return accountRef;
            }
        }
        return null;
    }

    /**
     * Populates consent initiated accounts for both payment and funds confirmation consents.
     *
     * @param responseData
     * @param requestData
     * @param accountRefJSON
     * @throws AuthorizationFailureException
     */
    public static void populateConsentInitiatedAccounts(SuccessResponsePopulateConsentAuthorizeScreenData responseData,
                                                        PopulateConsentAuthorizeScreenData requestData,
                                                        JSONObject accountRefJSON)
            throws AuthorizationFailureException {
        String payableAccountsEndpoint = ConfigurationConstants.PAYABLE_ACCOUNTS_RETRIEVAL_ENDPOINT;
        JSONArray userAccountsArray = DataRetrievalUtil.getAccountsFromEndpoint(requestData.getUserId(),
                payableAccountsEndpoint, new HashMap<>(), new HashMap<>());

        if (userAccountsArray == null || userAccountsArray.isEmpty()) {
            throw new AuthorizationFailureException(ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER);
        }

        Account validatedAccountRefObject;
        if (accountRefJSON.has(ConsentExtensionConstants.MASKED_PAN)) {
            // Skipping validation for maskedPan based account reference types and this needs to be validated
            // from the bank back end since there might be scenarios where there are 2 similar maskedPans
            // for a single user therefore we are not sure which account to validate it against
            // Eg: 123456xxxxxx1234, 123456xxxxxx1234 -> Both these maskedPans can belong to the same user
            validatedAccountRefObject = ConsentAuthorizationUtil.getAccountFromAccountRef(accountRefJSON);
        } else {
            JSONArray accountRefsArray = new JSONArray();
            accountRefsArray.put(accountRefJSON);
            List<Account> validatedAccountList = ConsentAuthorizationUtil.getValidatedAccountObjects(accountRefsArray,
                    userAccountsArray);

            // Validate account existence under user
            if (validatedAccountList == null || validatedAccountList.isEmpty()) {
                throw new AuthorizationFailureException(ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER);
            }

            validatedAccountRefObject = validatedAccountList.get(0);
        }

        if (validatedAccountRefObject == null) {
            throw new AuthorizationFailureException(ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER);
        }

        // Add to consent page to be displayed
        responseData.getConsentData().setInitiatedAccountsForConsent(List.of(validatedAccountRefObject));
    }

    /**
     * Extracts account reference object from authorized account objects.
     *
     * @param accounts
     * @return
     */
    public static List<AccountReference> extractAccountRef(List<Account> accounts) {
        List<AccountReference> accountRefs = new ArrayList<>();

        for (Account authorizedAccount: accounts) {
            AccountReference accountRef = new AccountReference();
            if (authorizedAccount.getAdditionalProperties().containsKey(ConsentExtensionConstants.CURRENCY)) {
                accountRef.setAdditionalProperties(ConsentExtensionConstants.CURRENCY,
                        (String) authorizedAccount.getAdditionalProperties().get(ConsentExtensionConstants.CURRENCY));
            }
            String accountRefType = CommonConsentValidationUtil
                    .getAccountReferenceType(authorizedAccount.getAdditionalProperties());
            accountRef.setAdditionalProperties(accountRefType,
                    (String) authorizedAccount.getAdditionalProperties().get(accountRefType));

            accountRefs.add(accountRef);
        }

        return accountRefs;
    }
}
