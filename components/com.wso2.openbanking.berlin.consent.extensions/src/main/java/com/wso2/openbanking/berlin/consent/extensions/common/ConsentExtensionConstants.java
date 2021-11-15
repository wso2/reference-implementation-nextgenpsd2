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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Consent extension constants.
 */
public class ConsentExtensionConstants {

    // Constants that are common to all request types
    public static final String X_REQUEST_ID_HEADER = "x-request-id";
    public static final String CONSENT_ID_HEADER = "consent-id";
    public static final String EXPLICIT_AUTHORISATION_PATH_END = "authorisations";
    public static final String PSU_IP_ADDRESS_HEADER = "psu-ip-address";
    public static final String PSU_ID_HEADER = "psu-id";
    public static final String TPP_EXPLICIT_AUTH_PREFERRED_HEADER = "tpp-explicit-authorisation-preferred";
    public static final String TPP_REDIRECT_PREFERRED_HEADER = "tpp-redirect-preferred";
    public static final String LAST_ACTION_DATE = "lastActionDate";
    public static final String CONSENT_STATUS = "consentStatus";
    public static final String CONSENT_ID = "consentId";
    public static final String AUTH_ID = "authorisationId";
    public static final String CHOSEN_SCA_METHOD = "chosenScaMethod";
    public static final String SCA_METHODS = "scaMethods";
    public static final String LINKS = "_links";
    public static final String HREF = "href";
    public static final String CONSENT_ATTR_KEY_DELIMITER = ":";
    public static final String SELF_LINK_TEMPLATE = "/%s/%s/%s";
    public static final String STATUS_LINK_TEMPLATE = "/%s/%s/%s/status";
    public static final String AUTH_RESOURCE_LINK_TEMPLATE = "/%s/%s/%s/authorisations/%s";
    public static final String START_AUTH_RESOURCE_LINK_TEMPLATE = "/%s/%s/%s";
    public static final String START_AUTH_LINK_TEMPLATE = "/%s/%s/%s/authorisations";
    public static final String AUTHORISATION_IDS = "authorisationIds";
    public static final String IS_ERROR = "isError";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String UTC = "UTC";
    public static final String IMPLICIT = "implicit";
    public static final String EXPLICIT = "explicit";
    public static final String CONSENT_DATA = "consentData";
    public static final String TITLE = "title";
    public static final String PAYMENT_TYPE_TITLE = "Payment Type";
    public static final String DATA_SIMPLE = "data";
    public static final String SINGLE_PAYMENT_TITLE = "Single Payments";
    public static final String INSTRUCTED_AMOUNT_TITLE = "Instructed Amount";
    public static final String CURRENCY_TITLE = "Instructed Currency";
    public static final String DEBTOR_REFERENCE_TITLE = "Debtor Account %s Reference";
    public static final String CREDITOR_NAME_TITLE = "Creditor Name";
    public static final String CREDITOR_REFERENCE_TITLE = "Creditor Account %s Reference";
    public static final String REMITTANCE_INFORMATION_UNSTRUCTURED_TITLE = "Remittance Information Unstructured";
    public static final String END_TO_END_IDENTIFICATION_TITLE = "End to End Identification";
    public static final String DEBTOR_ACCOUNT_TITLE = "Debtor Account";
    public static final String START_DATE_TITLE = "Start Date";
    public static final String END_DATE_TITLE = "End Date";
    public static final String FREQUENCY_TITLE = "Frequency";
    public static final String EXECUTION_RULE_TITLE = "Execution Rule";
    public static final String REQUESTED_DATA_TITLE = "Requested Data: ";
    public static final String CONSENT_DETAILS_TITLE = "Consent Details: ";
    public static final String CREDITOR_AGENT_TITLE = "Creditor Agent: ";
    public static final String PAYMENT_TITLE = "Payment ";
    public static final String DATA_REQUESTED = "data_requested";
    public static final String CONSENT_TYPE = "consent_type";
    public static final String TYPE = "type";
    public static final String DEFAULT_PERMISSION = "n/a";
    public static final String AIS_SCOPE = "ais";
    public static final String PIS_SCOPE = "pis";
    public static final String PIIS_SCOPE = "piis";

    // Original proper case header constants
    public static final String LOCATION_PROPER_CASE_HEADER = "Location";
    public static final String X_REQUEST_ID_PROPER_CASE_HEADER = "X-Request-ID";
    public static final String ASPSP_SCA_APPROACH_PROPER_CASE_HEADER = "ASPSP-SCA-Approach";
    public static final String SUPER_TENANT_DOMAIN = "@carbon.super";
    public static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F" +
            "]{12}";

    // Constants that are used by accounts service
    public static final String ACCOUNTS_CONSENT_PATH = "consents";
    public static final String ACCOUNTS_SUBMISSION_PATH_IDENTIFIER = "accounts";
    public static final String CARD_ACCOUNTS_SUBMISSION_PATH_IDENTIFIER = "card-accounts";
    public static final String MAXIMUM_VALID_DATE = "9999-12-31";
    public static final String ACCESS = "access";
    public static final String ACCESS_OBJECT = "accessObject";
    public static final String ACCOUNT_DETAILS = "accountDetails";
    public static final String RECURRING_INDICATOR = "recurringIndicator";
    public static final String RECURRING_INDICATOR_TITLE = "Recurring Indicator";
    public static final String VALID_UNTIL = "validUntil";
    public static final String VALID_UNTIL_TITLE = "Consent Expiry Date";
    public static final String FREQUENCY_PER_DAY = "frequencyPerDay";
    public static final String FREQUENCY_PER_DAY_TITLE = "Frequency Per Day";
    public static final String COMBINED_SERVICE_INDICATOR = "combinedServiceIndicator";
    public static final String COMBINED_SERVICE_INDICATOR_TITLE = "Combined Service Indicator";
    public static final String CARD_ACCOUNTS = "card-accounts";
    public static final String AVAILABLE_ACCOUNTS = "availableAccounts";
    public static final String AVAILABLE_ACCOUNTS_WITH_BALANCE = "availableAccountsWithBalance";
    public static final String ALL_PSD2 = "allPsd2";
    public static final String ADDITIONAL_INFORMATION = "additionalInformation";
    public static final String ALL_ACCOUNTS = "allAccounts";
    public static final String ALL_ACCOUNTS_WITH_OWNER_NAME = "allAccountsWithOwnerName";
    public static final String ACCOUNT = "account";
    public static final String WITH_BALANCE = "withBalance";
    public static final String ACCOUNTS_LINK_TEMPLATE = "/%s/accounts";
    public static final String ACCOUNT_ID = "accountId";
    public static final String IS_BALANCE_PERMISSION = "isBalancePermission";
    public static final String ACCOUNT_LIST = "accountList";
    public static final String PERMISSION = "Permission";
    public static final String PERMISSIONS = "permissions";
    public static final String VALIDATION_RESPONSE_PERMISSION = "permission";
    public static final String ACCESS_METHOD = "accessMethod";
    public static final String ACCESS_METHODS = "accessMethods";
    public static final String ACCOUNT_NUMBERS = "accountNumbers";
    public static final String ACCOUNT_CONSENT_INFO = "accountConsentInfo";
    public static final List<String> BULK_ACCOUNT_ACCESS_METHODS_REGEX_LIST = Collections
            .unmodifiableList(Arrays.asList(
            "accounts",
            "accounts?withBalance",
            "card-accounts"));
    public static final List<String> SINGLE_ACCOUNT_ACCESS_METHODS_REGEX_LIST = Collections
            .unmodifiableList(Arrays.asList(
            "accounts/[^/?]*",
            "accounts/[^/?]*?withBalance",
            "accounts/[^/?]*/balances",
            "accounts/[^/?]*/transactions",
            "accounts/[^/?]*/transactions?withBalance",
            "accounts/[^/?]*/transactions/[^/?]*",
            "card-accounts/[^/?]*",
            "card-accounts/[^/?]*/balances",
            "card-accounts/[^/?]*/transactions"));

    // Constants that are used in accounts authorize flow
    public static final String ACCOUNTS_PERMISSION = "Read Account Information";
    public static final String BALANCES_PERMISSION = "Read Account Balance Information";
    public static final String TRANSACTIONS_PERMISSION = "Read Account Transaction Information";
    public static final String ACCOUNTS_ACC_NUMBER_SET = "accountsAccNumberSet";
    public static final String BALANCES_ACC_NUMBER_SET = "balancesAccNumberSet";
    public static final String TRANSACTIONS_ACC_NUMBER_SET = "transactionsAccNumberSet";
    public static final String ACCOUNT_TYPE = "accountType";
    // Account types that are displayed in the consent page
    public static final String STATIC_DEFAULT = "static-default";
    public static final String SELECT_BALANCE = "select-balance";
    public static final String STATIC_BALANCE = "static-balance";
    public static final String SELECT_ACCOUNT = "select-account";
    public static final String STATIC_ACCOUNT = "static-account";
    public static final String SELECT_TRANSACTION = "select-transaction";
    public static final String STATIC_TRANSACTION = "static-transaction";

    // Constants that are used by payments service
    public static final String PAYMENTS_SERVICE_PATH = "payments";
    public static final String BULK_PAYMENTS_SERVICE_PATH = "bulk-payments";
    public static final String PERIODIC_PAYMENTS_SERVICE_PATH = "periodic-payments";
    public static final String PAYMENT_EXPLICIT_CANCELLATION_AUTHORISATION_PATH_END = "cancellation-authorisations";
    public static final String FOLLOWING_EXECUTION_RULE = "following";
    public static final String PRECEDING_EXECUTION_RULE = "preceding";
    public static final String IBAN = "iban";
    public static final String BBAN = "bban";
    public static final String PAN = "pan";
    public static final String MASKED_PAN = "maskedPan";
    public static final String MSISDN = "msisdn";
    public static final String PAYMENT_PRODUCT = "payment-product";
    public static final String PAYMENT_SERVICE = "payment-service";
    public static final String TRANSACTION_STATUS = "transactionStatus";
    public static final String PAYMENT_ID = "paymentId";
    public static final String ASPSP_SCA_APPROACH = "ASPSP-SCA-Approach";
    public static final String TRANSACTION_FEE_INDICATOR = "transactionFeeIndicator";
    public static final String TRANSACTION_FEES = "transactionFees";
    public static final String AMOUNT = "amount";
    public static final String CURRENCY = "currency";
    public static final String START_DATE = "startDate";
    public static final String FREQUENCY = "frequency";
    public static final String END_DATE = "endDate";
    public static final String EXECUTION_RULE = "executionRule";
    public static final String REQUESTED_EXECUTION_DATE = "requestedExecutionDate";
    public static final String DEBTOR_ACCOUNT = "debtorAccount";
    public static final String INSTRUCTED_AMOUNT = "instructedAmount";
    public static final String CREDITOR_ACCOUNT = "creditorAccount";
    public static final String CREDITOR_NAME = "creditorName";
    public static final String CREDITOR_AGENT = "creditorAgent";
    public static final String DAY_OF_EXECUTION = "dayOfExecution";
    public static final String END_TO_END_IDENTIFICATION = "endToEndIdentification";
    public static final String CREDITOR_ADDRESS = "creditorAddress";
    public static final String REMITTANCE_INFO_UNSTRUCTURED = "remittanceInformationUnstructured";
    public static final String BATCH_BOOKING_PREFERRED = "batchBookingPreferred";

    // Payment services
    public static final String PAYMENTS = "payments";
    public static final String BULK_PAYMENTS = "bulk-payments";
    public static final String PERIODIC_PAYMENTS = "periodic-payments";

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
    public static final String START_AUTHORISATION = "startAuthorisation";
    public static final String START_AUTH_WITH_PSU_IDENTIFICATION = "startAuthorisationWithPsuIdentification";
    public static final String START_AUTH_WITH_AUTH_METHOD_SELECTION
            = "startAuthorisationWithAuthenticationMethodSelection";

}
