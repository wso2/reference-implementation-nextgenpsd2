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

    public static final String CONFIGURATION_BUILD_ERROR =  "Error occurred while building the configurations using" +
            " open-banking.xml";
    public static final String CONFIG_INPUT_STREAM_ERROR = "Error in closing the input stream for open-banking-berlin" +
            ".xml";
    public static final String CONFIG_NOT_FOUND = "open-banking configuration not found in: ";

    // Error messages
    public static final String X_REQUEST_ID_MISSING = "X-Request-ID header is missing in the request";
    public static final String PATCH_NOT_SUPPORTED = "The PATCH method is not supported";
    public static final String PATH_INVALID = "Invalid request path";
    public static final String PAYLOAD_FORMAT_ERROR = "Incorrect JSON format in the Request Payload";
    public static final String PAYLOAD_NOT_PRESENT_ERROR = "Request payload is not present";
    public static final String PSU_IP_ADDRESS_MISSING = "The PSU-IP-Address mandatory header is missing in the request";
    public static final String PSU_ID_MISSING = "PSU-ID mandatory header is missing in the request";
    public static final String X_REQUEST_ID_INVALID = "Invalid X-Request-ID header. Needs to be in UUID " +
            "format";
    public static final String DECOUPLED_FLOW_NOT_SUPPORTED = "Decoupled SCA Approach is not supported";
    public static final String DECOUPLED_FLOW_NOT_SUPPORTED_FOR_CANCELLATION = "Decoupled SCA Approach is not " +
            "supported for payment cancellation";
    public static final String NO_CONSENT_FOR_CLIENT_ERROR = "No valid consent found for given client id";
    public static final String AUTHORISATIONS_NOT_FOUND = "No authorisations found for provided consent id";

    // Payments related error messages
    public static final String DEBTOR_ACCOUNT_MISSING = "Debtor account is missing in payments payload";
    public static final String CREDITOR_ACCOUNT_MISSING = "Creditor account is missing in payments payload";
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
    public static final String AUTHORISATION_RESOURCE_NOT_FOUND_ERROR = "Matching authorisation resource/s not found " +
            "for provided consent Id";
    public static final String CONSENT_ID_TYPE_MISMATCH = "The provided consent ID valid but belongs to a " +
            "different consent type";
    public static final String RESPONSE_CONSTRUCT_ERROR = "Error occurred while constructing response";
    public static final String CONSENT_ALREADY_DELETED = "The requested consent is already deleted";
    public static final String CONSENT_UPDATE_ERROR = "Error while updating the consent status";
    public static final String REQUESTED_EXECUTION_DATE_INVALID = "Requested execution date is invalid";
    public static final String NO_PAYMENTS_IN_BODY = "No payments found in payments request body";
    public static final String EMPTY_PAYMENTS_ELEMENT = "Empty payment element found in request body";

    // Error object related constants
    public static final String PATH = "path";
    public static final String CATEGORY = "category";
    public static final String CODE = "code";
    public static final String TEXT = "text";

}
