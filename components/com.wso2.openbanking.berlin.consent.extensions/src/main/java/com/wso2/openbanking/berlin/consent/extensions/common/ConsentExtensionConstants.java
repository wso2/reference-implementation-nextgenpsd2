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

/**
 * Consent extension constants.
 */
public class ConsentExtensionConstants {

    // Constants that are common to all request types
    public static final String X_REQUEST_ID_HEADER = "x-request-id";
    public static final String EXPLICIT_AUTHORISATION_PATH_END = "authorisations";
    public static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    public static final String PSU_ID_HEADER = "psu-id";
    public static final String TPP_EXPLICIT_AUTH_PREFERRED_HEADER = "tpp-explicit-authorisation-preferred";
    public static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    public static final String EXPLICIT_AUTH_PATH_IDENTIFIER = "authorisations";
    public static final String CONSENT_STATUS = "consentStatus";
    public static final String CONSENT_ID = "consentId";
    public static final String CHOSEN_SCA_METHOD = "chosenScaMethod";
    public static final String SCA_METHODS = "scaMethods";
    public static final String LINKS = "_links";
    public static final String CONSENT_ATTR_KEY_DELIMITER = ":";

    // Original proper case header constants
    public static final String LOCATION_PROPER_CASE_HEADER = "Location";
    public static final String X_REQUEST_ID_PROPER_CASE_HEADER = "X-Request-ID";
    public static final String ASPSP_SCA_APPROACH_PROPER_CASE_HEADER = "ASPSP-SCA-Approach";

    // Constants that are used by accounts service
    public static final String ACCOUNTS_CONSENT_PATH = "consents";
    public static final String ACCOUNTS_SUBMISSION_PATH_IDENTIFIER = "accounts";
    public static final String CARD_ACCOUNTS_SUBMISSION_PATH_IDENTIFIER = "card-accounts";
    public static final String MAXIMUM_VALID_DATE = "9999-12-31";
    public static final String ACCESS = "access";
    public static final String RECURRING_INDICATOR = "recurringIndicator";
    public static final String VALID_UNTIL = "validUntil";
    public static final String FREQUENCY_PER_DAY = "frequencyPerDay";
    public static final String COMBINED_SERVICE_INDICATOR = "combinedServiceIndicator";
    public static final String ACCOUNTS = "accounts";
    public static final String BALANCES = "balances";
    public static final String TRANSACTIONS = "transactions";
    public static final String AVAILABLE_ACCOUNTS = "availableAccounts";
    public static final String AVAILABLE_ACCOUNTS_WITH_BALANCE = "availableAccountsWithBalance";
    public static final String ALL_PSD2 = "allPsd2";
    public static final String ADDITIONAL_INFORMATION = "additionalInformation";
    public static final String ALL_ACCOUNTS = "allAccounts";
    public static final String ALL_ACCOUNTS_WITH_OWNER_NAME = "allAccountsWithOwnerName";

    // Constants that are used by payments service
    public static final String PAYMENTS_SERVICE_PATH = "payments";
    public static final String BULK_PAYMENTS_SERVICE_PATH = "bulk-payments";
    public static final String PERIODIC_PAYMENTS_SERVICE_PATH = "periodic-payments";
    public static final String PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END = "cancellation-authorisations";

    // Payment products
    public static final String SEPA_CREDIT_TRANSFERS = "sepa-credit-transfers";
    public static final String INSTANT_SEPA_CREDIT_TRANSFERS = "instant-sepa-credit-transfers";
    public static final String TARGET_TWO_PAYMENTS = "target-2-payments";
    public static final String CROSS_BORDER_CREDIT_TRANSFERS = "cross-border-credit-transfers";

    // Constants that are used by funds confirmation service
    public static final String FUNDS_CONFIRMATIONS_SERVICE_PATH = "confirmation-of-funds";
    public static final String FUNDS_CONFIRMATIONS_SUBMISSION_PATH_IDENTIFIER = "funds-confirmations";

    // Response related constants
    public static final String SELF = "self";
    public static final String STATUS = "status";
    public static final String SCA_OAUTH = "scaOAuth";
    public static final String SCA_STATUS = "scaStatus";
    public static final String SELECT_AUTH_METHOD = "selectAuthenticationMethod";
    public static final String START_AUTH = "startAuthorisation";
    public static final String START_AUTH_WITH_PSU_IDENTIFICATION = "startAuthorisationWithPsuIdentification";
    public static final String START_AUTH_WITH_AUTH_METHOD_SELECTION
            = "startAuthorisationWithAuthenticationMethodSelection";

}
