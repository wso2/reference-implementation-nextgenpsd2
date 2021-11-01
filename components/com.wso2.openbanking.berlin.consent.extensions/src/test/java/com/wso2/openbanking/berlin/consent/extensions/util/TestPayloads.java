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

package com.wso2.openbanking.berlin.consent.extensions.util;

import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestPayloads {

    public static Map<String, String> getMandatoryInitiationHeadersMap(String isExplicit) {

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER, UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.PSU_ID_HEADER, "admin@wso2.com");
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER, isExplicit);
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER, "127.0.0.1");

        return implicitInitiationHeadersMap;
    }

    public static final String VALID_PAYMENTS_PAYLOAD = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_INVALID_REFERENCE = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"invalid\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITHOUT_DEBTOR_ACCOUNT = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_EMPTY_IBAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_EMPTY_BBAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_EMPTY_PAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_NULL_BBAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_NULL_PAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_NULL_IBAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_INVALID_IBAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"invalid_iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_NULL_INSTRUCTED_AMOUNT = "{\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_EMPTY_INSTRUCTED_AMOUNT = "{\n" +
            "    \"instructedAmount\": {\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITHOUT_CURRENCY = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_EMPTY_CURRENCY = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITHOUT_AMOUNT = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_EMPTY_AMOUNT = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"amount\": \"\",\n" +
            "        \"currency\": \"EUR\",\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITHOUT_CREDITOR_NAME = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITH_EMPTY_CREDITOR_NAME = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String PAYMENTS_PAYLOAD_WITHOUT_CREDITOR_ACCOUNT = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String VALID_PERIODICAL_PAYMENT_PAYLOAD = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"2025-03-01\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String VALID_PERIODICAL_PAYMENT_PAYLOAD_WITH_INVALID_START_DATE = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"20250-03-01\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITHOUT_START_DATE = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITH_EMPTY_START_DATE = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITHOUT_FREQUENCY = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"2025-03-01\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITH_EMPTY_FREQUENCY = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"2025-03-01\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"frequency\": \"\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITHOUT_EXECUTION_RULE = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"2025-03-01\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITH_INVALID_EXECUTION_RULE = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"2025-03-01\",\n" +
            "  \"executionRule\": \"invalid_rule\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITH_PAST_END_DATE = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"2099-03-01\",\n" +
            "  \"endDate\": \"2019-03-01\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITH_INVALID_END_DATE = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"2099-03-01\",\n" +
            "  \"endDate\": \"20190-03-01\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String PERIODICAL_PAYMENT_PAYLOAD_WITH_INCONSISTENT_DATES = "{\n" +
            "  \"instructedAmount\": {\n" +
            "    \"currency\": \"EUR\",\n" +
            "    \"amount\": \"123\"\n" +
            "  },\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"creditorName\": \"Merchant123\",\n" +
            "  \"creditorAccount\": {\n" +
            "    \"iban\": \"DE23100120020123456789\"\n" +
            "  },\n" +
            "  \"remittanceInformationUnstructured\": \"Ref Number Abonnement\",\n" +
            "  \"startDate\": \"2030-03-01\",\n" +
            "  \"endDate\": \"2025-03-01\",\n" +
            "  \"executionRule\": \"preceding\",\n" +
            "  \"frequency\": \"Monthly\",\n" +
            "  \"dayOfExecution\": \"01\"\n" +
            "}";

    public static final String VALID_BULK_PAYMENTS_PAYLOAD = "{\n" +
            "  \"batchBookingPreferred\": \"true\",\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"paymentInformationId\": \"my-bulk-identification-1234\",\n" +
            "  \"requestedExecutionDate\": \"2025-08-01\",\n" +
            "  \"payments\": [\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant123\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"DE02100100109307118603\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"34.10\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant456\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"FR7612345987650123456789014\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 2\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static final String BULK_PAYMENTS_PAYLOAD_WITHOUT_EXECUTION_DATE = "{\n" +
            "  \"batchBookingPreferred\": \"true\",\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"paymentInformationId\": \"my-bulk-identification-1234\",\n" +
            "  \"payments\": [\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant123\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"DE02100100109307118603\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"34.10\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant456\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"FR7612345987650123456789014\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 2\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static final String BULK_PAYMENTS_PAYLOAD_WITH_EMPTY_EXECUTION_DATE = "{\n" +
            "  \"batchBookingPreferred\": \"true\",\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"paymentInformationId\": \"my-bulk-identification-1234\",\n" +
            "  \"requestedExecutionDate\": \"\",\n" +
            "  \"payments\": [\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant123\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"DE02100100109307118603\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"34.10\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant456\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"FR7612345987650123456789014\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 2\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static final String BULK_PAYMENTS_PAYLOAD_WITH_INVALID_EXECUTION_DATE = "{\n" +
            "  \"batchBookingPreferred\": \"true\",\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"paymentInformationId\": \"my-bulk-identification-1234\",\n" +
            "  \"requestedExecutionDate\": \"20250-08-01\",\n" +
            "  \"payments\": [\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant123\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"DE02100100109307118603\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"34.10\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant456\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"FR7612345987650123456789014\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 2\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static final String BULK_PAYMENTS_PAYLOAD_WITH_PAST_EXECUTION_DATE = "{\n" +
            "  \"batchBookingPreferred\": \"true\",\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"paymentInformationId\": \"my-bulk-identification-1234\",\n" +
            "  \"requestedExecutionDate\": \"2020-08-01\",\n" +
            "  \"payments\": [\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant123\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"DE02100100109307118603\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 1\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"34.10\"\n" +
            "      },\n" +
            "      \"creditorName\": \"Merchant456\",\n" +
            "      \"creditorAccount\": {\n" +
            "        \"iban\": \"FR7612345987650123456789014\"\n" +
            "      },\n" +
            "      \"remittanceInformationUnstructured\": \"Ref Number Merchant 2\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static final String BULK_PAYMENTS_PAYLOAD_WITHOUT_PAYMENTS = "{\n" +
            "  \"batchBookingPreferred\": \"true\",\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"paymentInformationId\": \"my-bulk-identification-1234\",\n" +
            "  \"requestedExecutionDate\": \"2025-08-01\",\n" +
            "}";

    public static final String BULK_PAYMENTS_PAYLOAD_WITH_EMPTY_PAYMENTS = "{\n" +
            "  \"batchBookingPreferred\": \"true\",\n" +
            "  \"debtorAccount\": {\n" +
            "    \"iban\": \"DE40100100103307118608\"\n" +
            "  },\n" +
            "  \"paymentInformationId\": \"my-bulk-identification-1234\",\n" +
            "  \"requestedExecutionDate\": \"2025-08-01\",\n" +
            "  \"payments\": []\n" +
            "}";
}
