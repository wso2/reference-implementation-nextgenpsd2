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

    // Constants that are used by accounts service
    public static final String ACCOUNTS_CONSENT_PATH = "consents";
    public static final String ACCOUNTS_SUBMISSION_PATH_IDENTIFIER = "accounts";
    public static final String CARD_ACCOUNTS_SUBMISSION_PATH_IDENTIFIER = "card-accounts";

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

}
