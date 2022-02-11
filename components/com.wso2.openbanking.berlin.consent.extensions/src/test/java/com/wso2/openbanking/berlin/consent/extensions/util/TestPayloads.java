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
import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestPayloads {

    public static Map<String, String> getMandatoryInitiationHeadersMap(String isExplicit) {

        Map<String, String> implicitInitiationHeadersMap = new HashMap<>();
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER,
                UUID.randomUUID().toString());
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.PSU_ID_HEADER, "admin@wso2.com");
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER, isExplicit);
        implicitInitiationHeadersMap.put(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER, "127.0.0.1");

        return implicitInitiationHeadersMap;
    }

    public static Map<String, String> getMandatoryStartAuthHeadersMap() {

        Map<String, String> startAuthHeadersMap = new HashMap<>();
        startAuthHeadersMap.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        startAuthHeadersMap.put(ConsentExtensionConstants.PSU_ID_HEADER, "admin@wso2.com");
        startAuthHeadersMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");
        startAuthHeadersMap.put(ConsentExtensionConstants.PSU_IP_ADDRESS_PROPER_CASE_HEADER, "127.0.0.1");

        return startAuthHeadersMap;
    }

    public static JSONObject getMandatoryValidateHeadersMap(String consentId, boolean isWithPsuIpAddress) {

        JSONObject validateHeadersObject = new JSONObject();
        validateHeadersObject.put(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                UUID.randomUUID().toString());
        validateHeadersObject.put(ConsentExtensionConstants.CONSENT_ID_HEADER, consentId);

        if (isWithPsuIpAddress) {
            validateHeadersObject.put(ConsentExtensionConstants.PSU_IP_ADDRESS_PROPER_CASE_HEADER, "127.0.0.1");
        }

        return validateHeadersObject;
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

    public static final String VALID_PAYMENTS_PAYLOAD_BBAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"bban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"bban\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String VALID_PAYMENTS_PAYLOAD_PAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"pan\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"pan\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String VALID_PAYMENTS_PAYLOAD_MASKED_PAN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"maskedPan\": \"DE123**********\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"maskedPan\": \"DE987**********\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String VALID_PAYMENTS_PAYLOAD_MSISDN = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"msisdn\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"msisdn\": \"DE98765432109876543210\"\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n" +
            "}";

    public static final String FULL_VALID_PAYMENTS_PAYLOAD = "{\n" +
            "    \"instructedAmount\": {\n" +
            "        \"currency\": \"EUR\",\n" +
            "        \"amount\": \"123.50\"\n" +
            "    },\n" +
            "    \"debtorAccount\": {\n" +
            "        \"iban\": \"DE12345678901234567890\"\n" +
            "    },\n" +
            "    \"creditorName\": \"Merchant123\",\n" +
            "    \"creditorAgent\": \"Creditor Agent\",\n" +
            "    \"creditorAccount\": {\n" +
            "        \"iban\": \"DE98765432109876543210\",\n" +
            "        \"currency\": \"EUR\",\n" +
            "    },\n" +
            "    \"remittanceInformationUnstructured\": \"Ref Number Merchant\",\n" +
            "    \"endToEndIdentification\": \"end to end identification\"\n" +
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

    public static final String ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_ACCESS_OBJECT = "{\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_RECURRING_INDICATOR = "{\n" +
            "   \"access\":{\n" +
            "      \"allPsd2\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_VALID_UNTIL = "{\n" +
            "   \"access\":{\n" +
            "      \"allPsd2\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_FREQ_PER_DAY = "{\n" +
            "   \"access\":{\n" +
            "      \"allPsd2\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_WITHOUT_MANDATORY_COMBINED_SERVICE = "{\n" +
            "   \"access\":{\n" +
            "      \"allPsd2\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_ACCESS_OBJECT_WITHOUT_ANY_ELEMENTS = "{\n" +
            "   \"access\":{ },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_ACCESS_OBJECT_INVALID_BANK_OFFERED_CONSENT = "{\n" +
            "   \"access\":{\n" +
            "      \"accounts\":[\n" +
            "         {\n" +
            "            \"iban\":\"DE40100100103307118608\"\n" +
            "         }\n" +
            "      ],\n" +
            "      \"balances\":[\n" +
            "         \n" +
            "      ]\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_ACCESS_OBJECT_INVALID_ALL_ACCOUNTS_CONSENT = "{\n" +
            "   \"access\":{\n" +
            "      \"allPsd2\":\"allAccounts\",\n" +
            "      \"availableAccounts\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_ACCESS_OBJECT_INVALID_USE_OF_ADDITIONAL_INFO = "{\n" +
            "   \"access\":{\n" +
            "      \"additionalInformation\":{\n" +
            "         \"ownerName\":[\n" +
            "            {\n" +
            "               \"iban\":\"DE40100100103307118608\"\n" +
            "            }\n" +
            "         ]\n" +
            "      }\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_WITH_FREQ_PER_DAY_LESS_THAN_1 = "{\n" +
            "   \"access\":{\n" +
            "      \"allPsd2\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":0,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_WITH_INVALID_USE_OF_RECURRING_INDICATOR = "{\n" +
            "   \"access\":{\n" +
            "      \"allPsd2\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":false,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String VALID_ACCOUNTS_PAYLOAD_ALL_PSD2 = "{\n" +
            "   \"access\":{\n" +
            "      \"allPsd2\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS = "{\n" +
            "   \"access\":{\n" +
            "      \"availableAccounts\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String VALID_ACCOUNTS_PAYLOAD_AVAILABLE_ACCOUNTS_WITH_BALANCE = "{\n" +
            "   \"access\":{\n" +
            "      \"availableAccountsWithBalance\":\"allAccounts\"\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String VALID_ACCOUNTS_PAYLOAD_BANK_OFFERED_CONSENT = "{\n" +
            "   \"access\":{\n" +
            "      \"accounts\":[\n" +
            "         \n" +
            "      ],\n" +
            "      \"balances\":[\n" +
            "         \n" +
            "      ],\n" +
            "      \"transactions\":[\n" +
            "         \n" +
            "      ]\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String VALID_ACCOUNTS_PAYLOAD_DEDICATED_ACCOUNTS_CONSENT = "{\n" +
            "   \"access\":{\n" +
            "      \"accounts\":[\n" +
            "         {\n" +
            "            \"iban\":\"DE12345678901234567890\"\n" +
            "         }\n" +
            "      ],\n" +
            "      \"balances\":[\n" +
            "         {\n" +
            "            \"iban\":\"DE12345678901234567890\",\n" +
            "            \"currency\":\"EUR\"\n" +
            "         }\n" +
            "      ],\n" +
            "      \"transactions\":[\n" +
            "         {\n" +
            "            \"iban\":\"DE34534456343478667544\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String ACCOUNTS_PAYLOAD_ACCESS_OBJECT_VALID_USE_OF_ADDITIONAL_INFO = "{\n" +
            "   \"access\":{\n" +
            "      \"accounts\":[\n" +
            "         {\n" +
            "            \"iban\":\"DE40100100103307118608\"\n" +
            "         }\n" +
            "      ],\n" +
            "      \"additionalInformation\":{\n" +
            "         \"ownerName\":[\n" +
            "            {\n" +
            "               \"iban\":\"DE40100100103307118608\"\n" +
            "            }\n" +
            "         ]\n" +
            "      }\n" +
            "   },\n" +
            "   \"recurringIndicator\":true,\n" +
            "   \"validUntil\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"frequencyPerDay\":4,\n" +
            "   \"combinedServiceIndicator\":false\n" +
            "}";

    public static final String VALID_FUNDS_CONFIRMATION_PAYLOAD = "{\n" +
            "   \"account\":{\n" +
            "      \"iban\":\"DE73459340345034563141\",\n" +
            "      \"currency\": \"USD\"\n" +
            "   },\n" +
            "   \"cardNumber\":\"1234567891234\",\n" +
            "   \"cardExpiryDate\":\"" + TestUtil.getCurrentDate(2) + "\",\n" +
            "   \"cardInformation\":\"MyMerchant Loyalty Card\",\n" +
            "   \"registrationInformation\":\"Sample info\"\n" +
            "}";

    public static final String VALID_FUNDS_CONFIRMATION_SUBMISSION_PAYLOAD = "{\n" +
            "   \"account\":{\n" +
            "      \"iban\":\"DE73459340345034563141\",\n" +
            "      \"currency\":\"USD\"\n" +
            "   },\n" +
            "   \"cardNumber\":\"1234567891234\",\n" +
            "   \"instructedAmount\":{\n" +
            "      \"currency\":\"EUR\",\n" +
            "      \"amount\":\"123\"\n" +
            "   }\n" +
            "}";
}
