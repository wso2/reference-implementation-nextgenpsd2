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

package com.wso2.openbanking.berlin.common.constants;

/**
 * Constants related to errors.
 */
public class ErrorConstants {

    public static final String CONFIGURATION_BUILD_ERROR = "Error occurred while building the configurations using" +
            " open-banking.xml";
    public static final String CONFIG_INPUT_STREAM_ERROR = "Error in closing the input stream for open-banking-berlin" +
            ".xml";
    public static final String CONFIG_NOT_FOUND = "open-banking configuration not found in: ";

    // Error messages
    public static final String X_REQUEST_ID_MISSING = "X-Request-ID header is missing in the request";
    public static final String CONSENT_ID_MISSING = "Consent-ID header is missing in the request";
    public static final String PATCH_NOT_SUPPORTED = "The PATCH method is not supported";
    public static final String DELETE_NOT_SUPPORTED = "The DELETE method is not supported";
    public static final String PUT_NOT_SUPPORTED = "The PUT method is not supported";
    public static final String PATH_INVALID = "Invalid request path";
    public static final String PAYLOAD_FORMAT_ERROR = "Incorrect JSON format in the Request Payload";
    public static final String PAYLOAD_NOT_PRESENT_ERROR = "Request payload is not present";
    public static final String PSU_IP_ADDRESS_MISSING = "The PSU-IP-Address mandatory header is missing in the request";
    public static final String PSU_ID_MISSING = "PSU-ID mandatory header is missing in the request";
    public static final String X_REQUEST_ID_INVALID = "Invalid X-Request-ID header. Needs to be in UUID " +
            "format";
    public static final String CONSENT_ID_INVALID = "Invalid Consent-ID header. Needs to be in UUID " +
            "format";
    public static final String DECOUPLED_FLOW_NOT_SUPPORTED = "Decoupled SCA Approach is not supported";
    public static final String DECOUPLED_FLOW_NOT_SUPPORTED_FOR_CANCELLATION = "Decoupled SCA Approach is not " +
            "supported for payment cancellation";
    public static final String NO_CONSENT_FOR_CLIENT_ERROR = "No valid consent found for given client id";
    public static final String AUTHORISATIONS_NOT_FOUND = "No authorisations found for provided consent id";
    public static final String INVALID_AUTHORISATION_ID = "No valid consent found for given authorisation id";
    public static final String START_AUTHORISATION_RESOURCE_CREATION_ERROR = "Error occurred while creating " +
            "authorisation resource";
    public static final String IMPLICIT_CONSENT_START_AUTHORISATION = "The %s is implicit therefore cannot " +
            "create authorisation resource";
    public static final String CONSENT_DATA_RETRIEVE_ERROR = "Exception occurred while getting consent data";
    public static final String LOGGED_IN_USER_MISMATCH = "The logged in user does not match with the " +
            "user who initiated the consent";
    public static final String INVALID_PAYMENT_CONSENT_STATUS_UPDATE = "Invalid payment consent update request for " +
            "consent Id: %s";
    public static final String CONSENT_PERSIST_ERROR = "Unable to persist authorisation";
    public static final String CONSENT_ID_SCOPE_MISSING_ERROR = "Error while retrieving consent data. No consent " +
            "Id provided with scope";
    public static final String CONSENT_ID_AND_SCOPE_MISMATCH = "The provided consent Id mismatches with the scope " +
            "type (\"ais, pis, piis\")";
    public static final String NO_MATCHING_USER_FOR_CONSENT = "No matching user for consent";
    public static final String NO_MATCHING_ACCOUNTS_FOR_PERMISSIONS = "No matching accounts found for " +
            "requested permissions";
    public static final String NO_MATCHING_ACCOUNT_FOR_ACCOUNT_ID = "No matching account found for " +
            "requested account id";
    public static final String ACCOUNT_ID_CANNOT_BE_EMPTY = "Account id cannot be empty";
    public static final String NO_VALID_ACCOUNTS_FOR_CONSENT = "No valid accounts for this consent";
    public static final String CONSENT_EXPIRED = "The consent is expired";
    public static final String CONSENT_INVALID_STATE = "Consent is not in a valid state";
    public static final String INCORRECT_CONSENT_DATA = "Received invalid consent data";
    public static final String ACCOUNTS_NOT_FOUND_FOR_USER = "Provided account references do not exist or not valid";

    // Payments related error messages
    public static final String DEBTOR_ACCOUNT_MISSING = "Debtor account is missing in payments payload";
    public static final String CREDITOR_ACCOUNT_MISSING = "Creditor account is missing in payments payload";
    public static final String CREDITOR_ACCOUNT_REFERENCE_MISSING = "Creditor account reference is missing in " +
            "payments payload";
    public static final String INSTRUCTED_AMOUNT_MISSING = "Instructed amount is missing in payments payload";
    public static final String AMOUNT_IS_MISSING = "Amount is missing in instructed amount";
    public static final String CURRENCY_CODE_MISSING = "Currency code is missing in instructed amount";
    public static final String CREDITOR_NAME_MISSING = "Creditor name is missing in payments payload";
    public static final String START_DATE_MISSING = "Start date is missing in periodic payments payload";
    public static final String START_DATE_INVALID = "Invalid start date";
    public static final String FREQUENCY_MISSING = "Frequency is missing in periodic payments payload";
    public static final String END_DATE_NOT_FUTURE = "End date must be a future date";
    public static final String END_DATE_NOT_VALID = "Invalid end date";
    public static final String DATES_INCONSISTENT = "End date must be greater than start date";
    public static final String INVALID_EXECUTION_RULE = "Execution rule should be either \"following\" or " +
            "\"preceding\"";
    public static final String INVALID_ACCOUNT_REFERENCE_TYPE = "Provided account reference type is not supported";
    public static final String ACCOUNT_REFERENCE_TYPE_MISSING = "Account reference type is missing";
    public static final String EXECUTION_DATE_NOT_FUTURE = "The execution date must be a future date";
    public static final String PAYMENT_EXECUTION_DATE_EXCEEDED = "The execution date provided in the request is after" +
            " the execution date supported by the ASPSP";
    public static final String CONSENT_INITIATION_ERROR = "Error occurred while initiating payment consent";
    public static final String CONSENT_ATTRIBUTE_INITIATION_ERROR = "Error occurred while storing consent " +
            "attributes";
    public static final String CONSENT_NOT_FOUND_ERROR = "Matching consent not found for provided Id";
    public static final String CONSENT_ID_NOT_FOUND_ERROR = "The consent Id is not found in consent persist data";
    public static final String AUTHORISATION_RESOURCE_NOT_FOUND_ERROR = "The authorisation resource is not found in " +
            "consent persist data";
    public static final String CONSENT_ID_TYPE_MISMATCH = "The provided consent ID valid but belongs to a " +
            "different consent type";
    public static final String AUTH_ID_CONSENT_ID_MISMATCH = "No available authorisation resource with given " +
            "authorisation Id matched with the requested different consent type";
    public static final String APPROVE_WITH_NO_ACCOUNTS_ERROR = "Cannot approve the consent without any account " +
            "related data";
    public static final String RESPONSE_CONSTRUCT_ERROR = "Error occurred while constructing response";
    public static final String JSON_PARSE_ERROR = "Error occurred while parsing JSON";
    public static final String BANK_OFFERED_CONSENT_UPDATE_ERROR = "Error while trying to update consent receipt " +
            "with bank offered accounts";
    public static final String CONSENT_ALREADY_DELETED = "The requested consent is already deleted";
    public static final String CONSENT_UPDATE_ERROR = "Error while updating the consent status";
    public static final String REQUESTED_EXECUTION_DATE_INVALID = "Requested execution date is invalid";
    public static final String NO_PAYMENTS_IN_BODY = "No payments found in payments request body";
    public static final String EMPTY_PAYMENTS_ELEMENT = "Empty payment element found in request body";
    public static final String CANNOT_CREATE_PAYMENT_CANCELLATION = "Cannot create cancellation for payment in " +
            "status %s";
    public static final String CANCELLATION_NOT_APPLICABLE = "Cancellation not applicable for Single payments";

    // Accounts related error messages
    public static final String MANDATORY_ELEMENTS_MISSING = "Invalid request payload, mandatory elements are missing";
    public static final String ACCESS_OBJECT_MANDATORY_ELEMENTS_MISSING = "\"access\" object has missing " +
            "required attributes";
    public static final String INVALID_USE_OF_ADDITIONAL_INFO_ATTRIBUTE = "The \"additionalInformation\" attribute " +
            "can only be used together with at least one of the major \"access\" attributes";
    public static final String INVALID_FREQ_PER_DAY = "Frequency per day have to be greater than zero";
    public static final String INVALID_FREQ_PER_DAY_COUNT = "Set frequency per day attribute as 1 for one time " +
            "account access";
    public static final String COMBINED_SERVICE_INDICATOR_NOT_SUPPORTED = "Sessions: Combination of AIS and PIS " +
            "Services are not supported";
    public static final String INVALID_PERMISSION = "Requested permissions in the Payload are invalid";
    public static final String VALID_UNTIL_DATE_INVALID = "Valid until date is invalid";

    // Funds confirmation related error messages
    public static final String CARD_EXPIRY_DATE_INVALID = "Card expiry date is invalid";

    // Error object related constants
    public static final String PATH = "path";
    public static final String CATEGORY = "category";
    public static final String CODE = "code";
    public static final String TEXT = "text";

    // Executors related error messages
    public static final String SIGNATURE_HEADER_MISSING = "Signature header not passed through the request";
    public static final String DIGEST_HEADER_MISSING = "Digest header not passed through the request";
    public static final String SIGNING_CERT_MISSING = "TPP signing certificate header not passed through the request";
    public static final String SIGNING_CERT_INVALID = "Signature certificate header is invalid";
    public static final String SIGNING_CERT_REVOKED = "Signature certificate is revoked";
    public static final String DIGEST_VALIDATION_ERROR = "Message digest validation failed";
    public static final String DATE_HEADER_MISSING = "Date header not passed through the request";
    public static final String INVALID_DIGEST_HEADER = "Invalid Digest header";
    public static final String INVALID_SIGNATURE_HEADER = "Invalid Signature header";
    public static final String INVALID_DIGEST_ALGORITHM = "Unsupported Digest algorithm";
    public static final String INVALID_SIGNATURE_ALGORITHM = "Unsupported Signature algorithm";
    public static final String SIGNATURE_CERTIFICATE_EXPIRED = "The certificate provided in TPP-Signature-Certificate" +
            " header is expired";
    public static final String SIGNATURE_VERIFICATION_FAIL = "Signature verification failed";
    public static final String CERT_PARSE_EROR = "Error while parsing signature certificate";
}
