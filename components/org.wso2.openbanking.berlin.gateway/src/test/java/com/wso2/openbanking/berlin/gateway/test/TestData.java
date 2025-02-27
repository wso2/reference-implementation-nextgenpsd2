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

package org.wso2.openbanking.berlin.gateway.test;

import org.wso2.openbanking.berlin.gateway.utils.GatewayTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains test data needed for executor tests.
 */
public class TestData {

    public static final List<String> SUPPORTED_HASH_ALGORITHMS = Stream.of("SHA-256", "SHA-512")
            .collect(Collectors.toList());
    public static final List<String> UNSUPPORTED_ALGORITHMS = Stream.of("invalidAlgorithm")
            .collect(Collectors.toList());
    public static final List<String> SUPPORTED_SIGNATURE_ALGORITHMS = Stream.of("SHA1withRSA")
            .collect(Collectors.toList());


    public static final String VALID_ACCOUNT_INITIATION_PAYLOAD = "{\n" +
            "    \"access\": {\n" +
            "        \"accounts\": [\n" +
            "            {\n" +
            "                \"iban\": \"DE98765432109876543210\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"balances\": [\n" +
            "            {\n" +
            "                \"iban\": \"DE98765432109876543210\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"transactions\": [\n" +
            "            {\n" +
            "                \"iban\": \"DE98765432109876543210\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"recurringIndicator\": true,\n" +
            "    \"validUntil\": \"2022-02-13\",\n" +
            "    \"frequencyPerDay\": 4,\n" +
            "    \"combinedServiceIndicator\": false\n" +
            "}";

    public static final Map<String, String> VALID_ACCOUNTS_REQUEST_HEADERS_MAP = new HashMap<String, String>() { {
       put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
       put("Date", "6 Feb 2022 21:06:45 IST");
       put("PSU-ID", "admin@wso2.com");
       put("PSU-ID-Type", "email");
       put("Digest", "SHA-256=2whNzouALY3qVnIK/7qzhf8qXUQf101mrjTasBqshHg=");
       put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840." +
               "113549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
               "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"," +
               "algorithm=\"rsa-sha256\", headers=\"digest x-request-id date psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
               "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
               "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
               "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
               "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
       put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);
       put("Authorization", GatewayTestUtils.SAMPLE_JWT);

    }};

    public static final Map<String, String> INVALID_PAYMENTS_REQUEST_HEADERS_MAP = new HashMap<String, String>() { {
        put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "SHA-256=2whNzouALY3qVnIK/7qzhf8qXUQf101mrjTasBqshHg=");
        put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840." +
                "113549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
                "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"," +
                "algorithm=\"rsa-sha256\", headers=\"digest x-request-id psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
                "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
                "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
                "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
                "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
        put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);
        put("Authorization", GatewayTestUtils.SAMPLE_JWT);

    }};

    public static final Map<String, String> INVALID_ACCOUNTS_REQUEST_HEADERS_MAP_2 = new HashMap<String, String>() { {
        put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
        put("Date", "6 Feb 2022 21:06:45 IST");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "SHA-256=ANsITc6LgC2N6lZyCv+6s4X/Kl1EH9dNZq402rAarIR4");
        put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840." +
                "113549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
                "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"," +
                "algorithm=\"rsa-sha256\", headers=\"digest psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
                "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
                "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
                "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
                "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
        put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);

    }};

    public static final Map<String, String> INVALID_ACCOUNTS_REQUEST_HEADERS_MAP_3 = new HashMap<String, String>() { {
        put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
        put("Date", "6 Feb 2022 21:06:45 IST");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "SHA-256=ANsITc6LgC2N6lZyCv+6s4X/Kl1EH9dNZq402rAarIR4");
        put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840." +
                "113549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
                "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"," +
                "algorithm=\"rsa-sha256\", headers=\"digest x-request-id DATE psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
                "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
                "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
                "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
                "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
        put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);

    }};

    public static final Map<String, String> INVALID_ACCOUNTS_REQUEST_HEADERS_MAP = new HashMap<String, String>() { {
        put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
        put("Date", "6 Feb 2022 21:06:45 IST");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "SHA-256=ANsITc6LgC2N6lZyCv+6s4X/Kl1EH9dNZq402rAarIR4");
        put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840" +
                ".113549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
                "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"=123," +
                "algorithm=\"rsa-sha256\", headers=\"digest x-request-id date psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
                "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
                "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
                "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
                "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
        put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);

    }};

    public static final Map<String, String> HEADERS_MAP_WITHOUT_SIGNATURE = new HashMap<String, String>() { {
        put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
        put("Date", "6 Feb 2022 21:06:45 IST");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "SHA-256=AKrcGWs+pPjbIzkHSoUqx+OWk53mGS0Se72hW4I5O8ag");
        put("Signature", "");
        put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);

    }};

    public static final Map<String, String> HEADERS_MAP_WITHOUT_DIGEST = new HashMap<String, String>() { {
        put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
        put("Date", "6 Feb 2022 21:06:45 IST");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "");
        put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840." +
                "113549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
                "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"," +
                "algorithm=\"rsa-sha256\", headers=\"digest x-request-id date psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
                "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
                "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
                "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
                "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
        put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);

    }};

    public static final Map<String, String> HEADERS_MAP_WITHOUT_CERT_HEADER = new HashMap<String, String>() { {
        put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
        put("Date", "6 Feb 2022 21:06:45 IST");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "SHA-256=AKrcGWs+pPjbIzkHSoUqx+OWk53mGS0Se72hW4I5O8ag");
        put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840.1" +
                "13549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
                "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"," +
                "algorithm=\"rsa-sha256\", headers=\"digest x-request-id date psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
                "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
                "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
                "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
                "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
        put("TPP-Signature-Certificate", "");

    }};

    public static final Map<String, String> HEADERS_MAP_WITHOUT_X_REQUEST_ID = new HashMap<String, String>() { {
        put("X-Request-ID", "");
        put("Date", "6 Feb 2022 21:06:45 IST");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "SHA-256=AKrcGWs+pPjbIzkHSoUqx+OWk53mGS0Se72hW4I5O8ag");
        put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840." +
                "113549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
                "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"," +
                "algorithm=\"rsa-sha256\", headers=\"digest x-request-id date psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
                "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
                "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
                "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
                "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
        put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);
    }};

    public static final Map<String, String> HEADERS_MAP_WITHOUT_DATE_HEADER = new HashMap<String, String>() { {
        put("X-Request-ID", "f0688201-b2b4-419e-b854-774cdd684bae");
        put("Date", "");
        put("PSU-ID", "admin@wso2.com");
        put("PSU-ID-Type", "email");
        put("Digest", "SHA-256=AKrcGWs+pPjbIzkHSoUqx+OWk53mGS0Se72hW4I5O8ag");
        put("Signature", "keyId=\"SN=de730ec5733ead1b,CA=1.2.840." +
                "113549.1.9.1=#16116d616c7368616e694077736f322e636f6d," +
                "CN=OpenBanking Pre-Production Issuing CA,OU=OpenBanking,O=WSO2 (UK) LIMITED,L=COL,ST=WP,C=LK\"," +
                "algorithm=\"rsa-sha256\", headers=\"digest x-request-id date psu-id\",signature=T+SYL8poZMs0o0EGpkG" +
                "XmAyVPuznmgVOT9SAl+XGc+G/4IUs8x3x1pWJkZBsPrxmUKvbP6marHavZTK4baIis8c9HenQJ7t80bpX4NebMIF6ntJuggce/" +
                "tOf/ygU1LoDlPgPbceIH1gZsBTyyFhiJq2TEbg4HxkRi8IagY+dPSfXoUd3JFpzUPa1vdVZCGUzWq0p6nZOIcdNHPHglZXInF" +
                "XkodfViHlLcQntGhj3l6KWAtLvbgbWDKRfs/iFph4IJDasL2n4uAR0Uunn/vIiTgDw2yFHJWS9tx3GOWD6xA/OnISM6lVn6kYU" +
                "euOVtQSFHRzbWo++2isi8iXiEOXaRw==");
        put("TPP-Signature-Certificate", GatewayTestUtils.TEST_SIGNATURE_CERT);
    }};
}
