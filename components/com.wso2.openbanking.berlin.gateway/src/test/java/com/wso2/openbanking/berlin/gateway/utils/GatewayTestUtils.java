/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.gateway.utils;

import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.berlin.gateway.test.GatewayTestConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

public class GatewayTestUtils {

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";

    public static final String TEST_TRANSPORT_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIF2DCCBMCgAwIBAgIEWcYJGDANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjAxMjE1MDY1ODMxWhcNMjIwMTE1" +
            "MDcyODMxWjBzMQswCQYDVQQGEwJHQjEaMBgGA1UEChMRV1NPMiAoVUspIExJTUlU" +
            "RUQxKzApBgNVBGETIlBTREdCLU9CLVVua25vd24wMDE1ODAwMDAxSFFRclpBQVgx" +
            "GzAZBgNVBAMTEjAwMTU4MDAwMDFIUVFyWkFBWDCCASIwDQYJKoZIhvcNAQEBBQAD" +
            "ggEPADCCAQoCggEBAN4RybsCYch4OAzJz3bfVAsz04lcuGYz1DE21l6PKkrABU3k" +
            "AYWUw9YtLWDVfA4nemSd5vb9dNJJoY6bvLTBbWBpWqOmq+lzXB4WrGuF5v4BaE8U" +
            "OeuVoIxKg9sV2mHAOaflVX8cz0dZSAbf1h+lvRRzIlX4TgN2ApZACIdtcBZfooOj" +
            "1F070MM9gyLw2A3cOew4MXaaZZFHP0CzQWlRyftaw0mYrx7m2iUK+4d4zEgEjC05" +
            "kdEpkdTtXvuTla/ER9O7DSnx++qKoRcEkqloOF/Rz7uhRhGfQHy6JwrNrZOr9khS" +
            "90pEejBnr8Is9BLqaRwE6COAPq/C+w5ZQ4pd9oMCAwEAAaOCApIwggKOMA4GA1Ud" +
            "DwEB/wQEAwIHgDCBiwYIKwYBBQUHAQMEfzB9MBMGBgQAjkYBBjAJBgcEAI5GAQYD" +
            "MGYGBgQAgZgnAjBcMDUwMwYHBACBmCcBAgwGUFNQX1BJBgcEAIGYJwEDDAZQU1Bf" +
            "QUkGBwQAgZgnAQQMBlBTUF9JQwwbRmluYW5jaWFsIENvbmR1Y3QgQXV0aG9yaXR5" +
            "DAZHQi1GQ0EwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIHgBgNV" +
            "HSAEgdgwgdUwgdIGCysGAQQBqHWBBgFkMIHCMCoGCCsGAQUFBwIBFh5odHRwOi8v" +
            "b2IudHJ1c3Rpcy5jb20vcG9saWNpZXMwgZMGCCsGAQUFBwICMIGGDIGDVXNlIG9m" +
            "IHRoaXMgQ2VydGlmaWNhdGUgY29uc3RpdHV0ZXMgYWNjZXB0YW5jZSBvZiB0aGUg" +
            "T3BlbkJhbmtpbmcgUm9vdCBDQSBDZXJ0aWZpY2F0aW9uIFBvbGljaWVzIGFuZCBD" +
            "ZXJ0aWZpY2F0ZSBQcmFjdGljZSBTdGF0ZW1lbnQwbQYIKwYBBQUHAQEEYTBfMCYG" +
            "CCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rpcy5jb20vb2NzcDA1BggrBgEFBQcw" +
            "AoYpaHR0cDovL29iLnRydXN0aXMuY29tL29iX3BwX2lzc3VpbmdjYS5jcnQwOgYD" +
            "VR0fBDMwMTAvoC2gK4YpaHR0cDovL29iLnRydXN0aXMuY29tL29iX3BwX2lzc3Vp" +
            "bmdjYS5jcmwwHwYDVR0jBBgwFoAUUHORxiFy03f0/gASBoFceXluP1AwHQYDVR0O" +
            "BBYEFN0LLFBaqNtl17Ds7a+4EwedY69oMA0GCSqGSIb3DQEBCwUAA4IBAQBpyV93" +
            "NoWNDg8PhcTWrxQFRLSvNCaDfKQw7MVzK7pl9cFnugZPXUg67KmLiJ+GzI9HHym/" +
            "yfd3Vwx5SNtfQVACmStKsLGv6kRGJcUAIgICV8ZGVlbsWpKam2ck7wR2138QD8s1" +
            "igAIaSWzHyHlkPjy44hRDbLpEYhRf9c2bUYGYnkMUBhmhI3ZhbopR3Zac/1/VBlA" +
            "VR7G0VQiloTHoQUL6OkaTnfdOEjU9Eeo8lQgrGjob5aCWrrPe4ExCyAZdn0NgE69" +
            "womfyrqwLoQpiUGmOSZCuOgWmPe8OrbpGIaodZz2Wk5qgR5xrVkNDfvgM/nXm1r8" +
            "HxriBi5shkweEW6g" +
            "-----END CERTIFICATE-----";

    public static final String TEST_SIGNATURE_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIFuzCCBKOgAwIBAgIEWcYJGTANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjAxMjE1MDY1ODQ3WhcNMjIwMTE1" +
            "MDcyODQ3WjBzMQswCQYDVQQGEwJHQjEaMBgGA1UEChMRV1NPMiAoVUspIExJTUlU" +
            "RUQxKzApBgNVBGETIlBTREdCLU9CLVVua25vd24wMDE1ODAwMDAxSFFRclpBQVgx" +
            "GzAZBgNVBAMTEjAwMTU4MDAwMDFIUVFyWkFBWDCCASIwDQYJKoZIhvcNAQEBBQAD" +
            "ggEPADCCAQoCggEBANCl7XwaW4G4/4lh05O3UHnio6v09ohkkxHxEoVQ8+qqyUM3" +
            "dk/gZYQnVfb1AGaEv54w1iyHXbI9wmB/EDHMISKuxGy6KMrlvraWkzt/6Zm+tJG8" +
            "4o6pC5P76b6fHbMlIRQ+T2nAKyofC/S3X1RYqqUufsc0amTITGG5BPVKizqllGhS" +
            "Khs28A62krlXAeqPwNY572FLyt1CyVnE9YlOiWSv0sVogZxQLTFWn+6nnJUswxp6" +
            "w9UOGDmGDA6KFGVvvKt0+X7aD+FuKRvrDkEr/6eUxURqumiwWslfZb0PDbTgrG5i" +
            "ustUJwzFnKqh8qoIDCSdQCktLQYTvkkbtqBbIQ8CAwEAAaOCAnUwggJxMA4GA1Ud" +
            "DwEB/wQEAwIGwDB6BggrBgEFBQcBAwRuMGwwEwYGBACORgEGMAkGBwQAjkYBBgIw" +
            "VQYGBACBmCcCMEswJDAiBgcEAIGYJwECDAZQU1BfUEkGBwQAgZgnAQMMBlBTUF9B" +
            "SQwbRmluYW5jaWFsIENvbmR1Y3QgQXV0aG9yaXR5DAZHQi1GQ0EwFQYDVR0lBA4w" +
            "DAYKKwYBBAGCNwoDDDCB4AYDVR0gBIHYMIHVMIHSBgsrBgEEAah1gQYBZDCBwjAq" +
            "BggrBgEFBQcCARYeaHR0cDovL29iLnRydXN0aXMuY29tL3BvbGljaWVzMIGTBggr" +
            "BgEFBQcCAjCBhgyBg1VzZSBvZiB0aGlzIENlcnRpZmljYXRlIGNvbnN0aXR1dGVz" +
            "IGFjY2VwdGFuY2Ugb2YgdGhlIE9wZW5CYW5raW5nIFJvb3QgQ0EgQ2VydGlmaWNh" +
            "dGlvbiBQb2xpY2llcyBhbmQgQ2VydGlmaWNhdGUgUHJhY3RpY2UgU3RhdGVtZW50" +
            "MG0GCCsGAQUFBwEBBGEwXzAmBggrBgEFBQcwAYYaaHR0cDovL29iLnRydXN0aXMu" +
            "Y29tL29jc3AwNQYIKwYBBQUHMAKGKWh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vYl9w" +
            "cF9pc3N1aW5nY2EuY3J0MDoGA1UdHwQzMDEwL6AtoCuGKWh0dHA6Ly9vYi50cnVz" +
            "dGlzLmNvbS9vYl9wcF9pc3N1aW5nY2EuY3JsMB8GA1UdIwQYMBaAFFBzkcYhctN3" +
            "9P4AEgaBXHl5bj9QMB0GA1UdDgQWBBSgT/Oyk7RBJYZXZcswLaf25ROiqDANBgkq" +
            "hkiG9w0BAQsFAAOCAQEABqk93SP6d0fKKDoTXkjIo3Cf2sGWCZb8srdLLMe0MkAw" +
            "NdD+86ej5Wjr3KW3DUg7INzRaqyr4dKWpyJt75pBSjVoGFK3UrQ/jJv/+eIDLmZr" +
            "vC3B9Olxhe9ws+Ehs5x+fPWUu7eidmlCnb7RxMWSTCkRJqUJQvFII0f/vRhVWXHT" +
            "lELbATzwwVR2Eg8SoHX8VZ+OL7m0Z0iOVDQE5ozFV4+6ON4X0kx6xwWYi+oFQVXK" +
            "HiZGn5PrFHmJw+wHpytM+fVO1kBrKfLKmjVcHedkwDBx/knNPn/uFpNjHFWFOIR9" +
            "iLa6Q3WH9HyLdXS9tDiBLGqS8jAPkrjDlg5vbzRD6Q==" +
            "-----END CERTIFICATE-----";

    private static final Log log = LogFactory.getLog(GatewayTestUtils.class);
    private static java.security.cert.X509Certificate testEidasCertificate = null;
    private static X509Certificate testClientCertificate = null;

    protected static X509Certificate getCertFromStr(String pemEncodedCert) {
        byte[] decodedTransportCert = Base64.getDecoder().decode(pemEncodedCert
                .replace(CertificateValidationUtils.BEGIN_CERT, "")
                .replace(CertificateValidationUtils.END_CERT, ""));

        InputStream inputStream = new ByteArrayInputStream(decodedTransportCert);
        X509Certificate x509Certificate = null;
        try {
            x509Certificate = X509Certificate.getInstance(inputStream);
        } catch (CertificateException e) {
            log.error("Exception occurred while parsing test certificate. Caused by, ", e);
        }
        return x509Certificate;
    }

    public static synchronized X509Certificate getTestTransportCertificate()
            throws CertificateException {

        byte[] bytes = Base64.getDecoder().decode(GatewayTestUtils.TEST_TRANSPORT_CERT
                .replace(BEGIN_CERT, "").replace(END_CERT, ""));
        return X509Certificate.getInstance(bytes);
    }

    public static Map<String, List<OpenBankingGatewayExecutor>> initExecutors() {

        Map<String, List<OpenBankingGatewayExecutor>> executors = new HashMap<>();
        Map<String, Map<Integer, String>> fullValidatorMap = GatewayTestConstants.FULL_VALIDATOR_MAP;
        for (Map.Entry<String, Map<Integer, String>> stringMapEntry : fullValidatorMap.entrySet()) {
            List<OpenBankingGatewayExecutor> executorList = new ArrayList<>();
            Map<Integer, String> executorNames = stringMapEntry.getValue();
            for (Map.Entry<Integer, String> executorEntity : executorNames.entrySet()) {
                OpenBankingGatewayExecutor object = (OpenBankingGatewayExecutor)
                        OpenBankingUtils.getClassInstanceFromFQN(executorEntity.getValue());
                executorList.add(object);
            }
            executors.put(stringMapEntry.getKey(), executorList);
        }
        return executors;

    }

    public static synchronized X509Certificate getTestSignatureCertificate()
            throws CertificateException {

        byte[] bytes = Base64.getDecoder().decode(GatewayTestUtils.TEST_SIGNATURE_CERT
                .replace(BEGIN_CERT, "").replace(END_CERT, ""));
        return X509Certificate.getInstance(bytes);
    }
}
