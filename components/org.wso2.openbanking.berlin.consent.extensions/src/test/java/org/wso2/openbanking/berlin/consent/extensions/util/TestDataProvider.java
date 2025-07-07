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

package org.wso2.openbanking.berlin.consent.extensions.util;

import org.testng.annotations.DataProvider;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.PermissionEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;

/**
 * Data Provider for Berlin Account Tests.
 */
public class TestDataProvider {

    @DataProvider(name = "ValidAccReferencePayloadTestDataProvider")
    Object[][] getValidAccReferencePayloadTestDataProvider() {

        return new Object[][]{
                {TestPayloads.VALID_ACCOUNT_REFERENCE_1},
                {TestPayloads.VALID_ACCOUNT_REFERENCE_2},
                {TestPayloads.VALID_ACCOUNT_REFERENCE_3},
                {TestPayloads.VALID_ACCOUNT_REFERENCE_4},
                {TestPayloads.VALID_ACCOUNT_REFERENCE_5}
        };
    }

    @DataProvider(name = "InvalidAccReferencePayloadTestDataProvider")
    Object[][] getInvalidAccReferencePayloadTestDataProvider() {

        return new Object[][]{
                {TestPayloads.UNSUPPORTED_ACCOUNT_REFERENCE_1},
                {TestPayloads.UNSUPPORTED_ACCOUNT_REFERENCE_2},
                {TestPayloads.UNSUPPORTED_ACCOUNT_REFERENCE_3},
                {TestPayloads.UNSUPPORTED_ACCOUNT_REFERENCE_4},
                {TestPayloads.UNSUPPORTED_ACCOUNT_REFERENCE_5},
                {TestPayloads.UNSUPPORTED_ACCOUNT_REFERENCE_6}
        };
    }

    @DataProvider(name = "InvalidAccountInitiationPayloadTestDataProvider")
    Object[][] getInvalidAccountInitiationPayloadTestDataProvider() {

        // payload, configuredFreqPerDay(minimum value), isValidUntilDateCapEnabled,
        // validUntilDays(configured valid until days)
        return new Object[][]{
                {TestPayloads.ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_ACCESS_OBJECT, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_RECURRING_INDICATOR, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_VALID_UNTIL, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_FREQ_PER_DAY, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_COMBINED_SERVICE, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_ACCESS_OBJECT_WITHOUT_ANY_ELEMENTS, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_ACCESS_OBJECT_INVALID_BANK_OFFERED_CONSENT, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_ACCESS_OBJECT_INVALID_ALL_ACCOUNTS_CONSENT, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_ACCESS_OBJECT_INVALID_USE_OF_ADDITIONAL_INFO, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_WITH_FREQ_PER_DAY_LESS_THAN_1, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_WITH_INVALID_USE_OF_RECURRING_INDICATOR, 4, false, 0},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2, 6, false, 0}
        };
    }

    @DataProvider(name = "ValidAccountInitiationPayloadTestDataProvider")
    Object[][] getValidAccountInitiationPayloadTestDataProvider() {

        // payload, configuredFreqPerDay(minimum value), isValidUntilDateCapEnabled,
        // validUntilDays(configured valid until days)
        return new Object[][]{
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2, 4, false, 0},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS, 4, false, 0},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS_WITH_BALANCE, 4, false, 0},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_BANK_OFFERED_CONSENT, 4, false, 0},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_DEDICATED_ACCOUNTS_CONSENT, 4, false, 0},
                {TestPayloads.ACCOUNTS_PAYLOAD_ACCESS_OBJECT_VALID_USE_OF_ADDITIONAL_INFO, 4, false, 0}
        };
    }

    @DataProvider(name = "PermissionTestDataProvider")
    Object[][] getPermissionTestDataProvider() {

        return new Object[][]{
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2, PermissionEnum.ALL_PSD2.toString()},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS, PermissionEnum.AVAILABLE_ACCOUNTS.toString()},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS_WITH_BALANCE,
                        PermissionEnum.AVAILABLE_ACCOUNTS_WITH_BALANCES.toString()},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_BANK_OFFERED_CONSENT, PermissionEnum.BANK_OFFERED.toString()},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_DEDICATED_ACCOUNTS_CONSENT,
                        PermissionEnum.DEDICATED_ACCOUNTS.toString()}
        };
    }

    @DataProvider(name = "ValidatedValidUntilTestDataProvider")
    Object[][] getValidatedValidUntilTestDataProvider() {

        return new Object[][]{
                {TestUtil.getCurrentDate(0), false, 0, TestUtil.getCurrentDate(0)},
                {TestUtil.getCurrentDate(4), false, 0, TestUtil.getCurrentDate(4)},
                {"9999-12-31", false, 0, "9999-12-31"},
                {TestUtil.getCurrentDate(4), true, 1, TestUtil.getCurrentDate(1)},
                {TestUtil.getCurrentDate(10), true, 2, TestUtil.getCurrentDate(2)}
        };
    }

    @DataProvider(name = "ValidAccountHandlePostTestDataProvider")
    Object[][] getValidAccountHandlePostTestDataProvider() {

        return new Object[][]{
                {TestPayloads.getMandatoryInitiationHeadersMap("false"),
                        TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2, "consents"}
        };
    }

    @DataProvider(name = "HandleValidStartAuthTestDataProvider")
    Object[][] getHandleValidStartAuthTestDataProvider() {

        return new Object[][]{
                {
                        "consents/%s/authorisations",
                        ConsentTypeEnum.ACCOUNTS.toString(),
                        ConsentStatusEnum.RECEIVED.toString(),
                        AuthTypeEnum.AUTHORISATION.toString(),
                        false
                },
                {
                        "consents/%s/authorisations",
                        ConsentTypeEnum.ACCOUNTS.toString(),
                        ConsentStatusEnum.RECEIVED.toString(),
                        AuthTypeEnum.AUTHORISATION.toString(),
                        true
                },
                {
                        "bulk-payments/sepa-credit-transfers/%s/authorisations",
                        ConsentTypeEnum.BULK_PAYMENTS.toString(),
                        TransactionStatusEnum.RCVD.name(),
                        AuthTypeEnum.AUTHORISATION.toString(),
                        false
                },
                {
                        "periodic-payments/sepa-credit-transfers/%s/authorisations",
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(),
                        TransactionStatusEnum.RCVD.name(),
                        AuthTypeEnum.AUTHORISATION.toString(),
                        false
                },
                {
                        "bulk-payments/sepa-credit-transfers/%s/cancellation-authorisations",
                        ConsentTypeEnum.BULK_PAYMENTS.toString(),
                        TransactionStatusEnum.ACTC.name(),
                        AuthTypeEnum.CANCELLATION.toString(),
                        false
                },
                {
                        "periodic-payments/sepa-credit-transfers/%s/cancellation-authorisations",
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(),
                        TransactionStatusEnum.ACTC.name(),
                        AuthTypeEnum.CANCELLATION.toString(),
                        false
                },
                {
                        "bulk-payments/sepa-credit-transfers/%s/authorisations",
                        ConsentTypeEnum.BULK_PAYMENTS.toString(),
                        TransactionStatusEnum.RCVD.name(),
                        AuthTypeEnum.AUTHORISATION.toString(),
                        true
                },
                {
                        "periodic-payments/sepa-credit-transfers/%s/authorisations",
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(),
                        TransactionStatusEnum.RCVD.name(),
                        AuthTypeEnum.AUTHORISATION.toString(),
                        true
                },
                {
                        "bulk-payments/sepa-credit-transfers/%s/cancellation-authorisations",
                        ConsentTypeEnum.BULK_PAYMENTS.toString(),
                        TransactionStatusEnum.ACTC.name(),
                        AuthTypeEnum.CANCELLATION.toString(),
                        true
                },
                {
                        "periodic-payments/sepa-credit-transfers/%s/cancellation-authorisations",
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(),
                        TransactionStatusEnum.ACTC.name(),
                        AuthTypeEnum.CANCELLATION.toString(),
                        true
                }
        };
    }

    @DataProvider(name = "HandleInvalidStartAuthTestDataProvider")
    Object[][] getHandleInvalidStartAuthTestDataProvider() {

        return new Object[][]{
                {
                        "/payments/sepa-credit-transfers/%s/cancellation-authorisations",
                        ConsentTypeEnum.PAYMENTS.toString(),
                        TransactionStatusEnum.ACTC.name(),
                        AuthTypeEnum.CANCELLATION.toString(),
                        false
                },
                {
                        "/bulk-payments/sepa-credit-transfers/%s/cancellation-authorisations",
                        ConsentTypeEnum.BULK_PAYMENTS.toString(),
                        TransactionStatusEnum.RCVD.name(),
                        AuthTypeEnum.CANCELLATION.toString(),
                        false
                },
                {
                        "/periodic-payments/sepa-credit-transfers/%s/cancellation-authorisations",
                        ConsentTypeEnum.PERIODIC_PAYMENTS.toString(),
                        TransactionStatusEnum.RCVD.name(),
                        AuthTypeEnum.CANCELLATION.toString(),
                        false
                }
        };
    }

    @DataProvider(name = "BulkAccountsSubmissionTestDataProvider")
    Object[][] getBulkAccountsSubmissionTestDataProvider() {

        return new Object[][]{
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2, "/accounts"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS_WITH_BALANCE, "/accounts?withBalance"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS, "/card-accounts"}
        };
    }

    @DataProvider(name = "SingleAccountSubmissionTestDataProvider")
    Object[][] getSingleAccountSubmissionTestDataProvider() {

        return new Object[][]{
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/accounts/DE12345678901234567890"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/accounts/DE12345678901234567890?withBalance"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/accounts/DE12345678901234567891/balances"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/accounts/DE12345678901234567892/transactions"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/accounts/DE12345678901234567892/transactions?withBalance"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/accounts/DE12345678901234567892/transactions/1234"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/card-accounts/DE12345678901234567890"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/card-accounts/DE12345678901234567891/balances"},
                {TestPayloads.VALID_ACCOUNTS_PAYLOAD_ALL_PSD2,
                        "/card-accounts/DE12345678901234567892/transactions"}
        };
    }

}
