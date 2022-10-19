/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.common.constants;

/**
 * Common constants.
 */
public class CommonConstants {

    // Config tag constants
    public static final String OB_CONFIG_FILE = "open-banking-berlin.xml";
    public static final String OB_BERLIN_CONFIG_QNAME = "http://wso2.org/projects/carbon/open-banking-berlin.xml";
    public static final String CONSENT_MGT_CONFIG_TAG = "ConsentManagement";
    public static final String SCA_CONFIG_TAG = "SCA";
    public static final String SUPPORTED_SCA_METHODS_CONFIG_TAG = "SupportedSCAMethods";
    public static final String SUPPORTED_SCA_APPROACHES_CONFIG_TAG = "SupportedSCAApproaches";
    public static final String SCA_REQUIRED = "ConsentManagement.SCA.Required";
    public static final String OAUTH_METADATA_ENDPOINT = "ConsentManagement.SCA.OAuthMetadataEndpoint";
    public static final String FREQ_PER_DAY_ENABLED = "ConsentManagement.FrequencyPerDay.Enable";
    public static final String FREQ_PER_DAY_CONFIG_VALUE = "ConsentManagement.Accounts.FrequencyPerDay.Frequency";
    public static final String VALID_UNTIL_DATE_CAP_ENABLED = "ConsentManagement.Accounts.ValidUntilDateCap.Enabled";
    public static final String VALID_UNTIL_DAYS = "ConsentManagement.Accounts.ValidUntilDateCap.ValidUntilDays";
    public static final String AIS_API_VERSION = "ConsentManagement.APIVersions.AIS";
    public static final String PIS_API_VERSION = "ConsentManagement.APIVersions.PIS";
    public static final String PIIS_API_VERSION = "ConsentManagement.APIVersions.PIIS";
    public static final String SCA_TYPE = "Type";
    public static final String SCA_VERSION = "Version";
    public static final String SCA_ID = "Id";
    public static final String SCA_NAME = "Name";
    public static final String SCA_MAPPED_APPROACH = "MappedApproach";
    public static final String SCA_DESCRIPTION = "Description";
    public static final String SCA_DEFAULT = "Default";
    public static final String SUPPORTED_ACCOUNT_REFERENCE_TYPES_PATH = "ConsentManagement" +
            ".SupportedAccountReferenceTypes.AccountReferenceType";
    public static final String MAX_FUTURE_PAYMENT_DAYS = "ConsentManagement.Payments.BulkPayments" +
            ".MaximumFuturePaymentDays";
    public static final String TRANSACTION_FEE_ENABLED_PATH = "ConsentManagement.Payments.TransactionFee.Enable";
    public static final String TRANSACTION_FEE_AMOUNT = "ConsentManagement.Payments.TransactionFee.Amount";
    public static final String TRANSACTION_FEE_CURRENCY = "ConsentManagement.Payments.TransactionFee.Currency";
    public static final String AUTHORIZE_CANCELLATION = "ConsentManagement.Payments.AuthorizeCancellation";
    public static final String IS_PAYMENT_DEBTOR_ACCOUNT_CURRENCY_VALIDATION_ENABLED = "ConsentManagement.Payments" +
            ".EnableDebtorAccountCurrencyValidation";
    public static final String SUPPORTED_CODE_CHALLENGE_METHODS = "ConsentManagement.SupportedCodeChallengeMethods" +
            ".Method";
    public static final String IS_ACCOUNT_ID_VALIDATION_ENABLED = "ConsentManagement.Accounts." +
            "EnableAccountIDValidation";
    public static final String IS_ACCOUNT_ID_VALIDATION_ENABLED_FOR_COF_CONSENT = "ConsentManagement.COF." +
            "EnableAccountIDValidation";
    public static final String SHAREABLE_ACCOUNTS_RETRIEVAL_ENDPOINT = "ConsentManagement." +
            "ShareableAccountsRetrieveEndpoint";
    public static final String PAYABLE_ACCOUNTS_RETRIEVAL_ENDPOINT = "ConsentManagement." +
            "PayableAccountsRetrieveEndpoint";
    public static final String PAYMENTS_BACKEND_URL = "ConsentManagement.Payments.PaymentsBackendURL";
    public static final String MULTI_CURRENCY_ENABLED = "ConsentManagement.MultiCurrency.Enable";
    public static final String MULTIPLE_RECURRING_CONSENT_ENABLED = "ConsentManagement.Accounts." +
            "EnableMultipleRecurringConsent";
    public static final String SUPPORTED_HASH_ALGORITHMS = "Gateway.SignatureVerification.SupportedHashAlgorithms" +
            ".Algorithm";
    public static final String SUPPORTED_SIGNATURE_ALGORITHMS = "Gateway.SignatureVerification" +
            ".SupportedSignatureAlgorithms.Algorithm";

    public static final String SCA_APPROACH_KEY = "SCA-Approach";
    public static final String SCA_METHODS_KEY = "SCA-Methods";
    public static final String SCA_METHOD_KEY = "SCA-Method";
    public static final String AIS_SCOPE = "ais";
    public static final String PIS_SCOPE = "pis";
    public static final String PIIS_SCOPE = "piis";
    public static final String AUTH_STATUS = "authStatus";
    public static final String CONSENT_ID = "consentId";
    public static final String STATUS_CODE = "statusCode";

    public static final String IDEMPOTENCY_ALLOWED_TIME = "ConsentManagement.Idempotency" +
            ".AllowedDuration";
    public static final String DELIMITER = ":";

    public static final String ORG_ID_VALIDATION_REGEX = "KeyManager.OrgIdValidationRegex";
    public static final String PAYMENT_CONSENT_STATUS_UPDATE_URL = "PaymentConsentStatusUpdateURL";
}
