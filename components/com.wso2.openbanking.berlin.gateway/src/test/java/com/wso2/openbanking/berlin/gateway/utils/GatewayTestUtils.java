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

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.berlin.gateway.test.GatewayTestConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
    private static final String X509_CERT_INSTANCE_NAME = "X.509";

    private static java.security.cert.X509Certificate expiredSelfCertificate = null;
    private static java.security.cert.X509Certificate testClientCertificateIssuer = null;

    public static final String SAMPLE_JWT_2 = "eyJhbGciOiJSUzI1NiJ9.eyJjbGllbnRJZCI6IlBTREdCLU9CLVVua25vd24wMDE1ODAw" +
            "MDAxSFFRclpBQVgiLCJjdXJyZW50U3RhdHVzIjoiQUNDUCIsImNyZWF0ZWRUaW1lc3RhbXAiOjE2NjU5OTgwMzMsInJlY3VycmluZ" +
            "0luZGljYXRvciI6ZmFsc2UsImF1dGhvcml6YXRpb25SZXNvdXJjZXMiOlt7InVwZGF0ZWRUaW1lIjoxNjY1OTk4MTI1LCJjb25zZW" +
            "50SWQiOiJmYzI4YWQ0NS0wZjQ0LTQwMWYtOTJiZS0yYjk1ZTQzYTA2NmQiLCJhdXRob3JpemF0aW9uSWQiOiIzMzQ5MmU1NC1mOTF" +
            "mLTRiM2QtOGVmZS02ZWU5ZWQ2Y2IyZDUiLCJhdXRob3JpemF0aW9uVHlwZSI6ImF1dGhvcmlzYXRpb24iLCJ1c2VySWQiOiJhZG1p" +
            "bkB3c28yLmNvbSIsImF1dGhvcml6YXRpb25TdGF0dXMiOiJwc3VBdXRoZW50aWNhdGVkIn1dLCJ1cGRhdGVkVGltZXN0YW1wIjox" +
            "NjY1OTk4MTI1LCJ2YWxpZGl0eVBlcmlvZCI6MCwiY29uc2VudEF0dHJpYnV0ZXMiOnsicGF5bWVudC1zZXJ2aWNlIjoiYnVsay1wY" +
            "XltZW50cyIsIlNDQS1BcHByb2FjaDozMzQ5MmU1NC1mOTFmLTRiM2QtOGVmZS02ZWU5ZWQ2Y2IyZDUiOiJSRURJUkVDVCIsIlgtUm" +
            "VxdWVzdC1JRCI6IjhmNzk0Mjk2LWMyMTctNGEwYS1iYzFkLTMwYzJhZjM4MWQzZCIsIlNDQS1NZXRob2Q6MzM0OTJlNTQtZjkxZi0" +
            "0YjNkLThlZmUtNmVlOWVkNmNiMmQ1Ijoic21zLW90cCIsInBheW1lbnQtcHJvZHVjdCI6InNlcGEtY3JlZGl0LXRyYW5zZmVycyJ9" +
            "LCJjb25zZW50SWQiOiJmYzI4YWQ0NS0wZjQ0LTQwMWYtOTJiZS0yYjk1ZTQzYTA2NmQiLCJjb25zZW50TWFwcGluZ1Jlc291cmNlc" +
            "yI6W3sibWFwcGluZ0lkIjoiMmQ4NTY1MDctYzRmMy00ZjE2LWE0NmUtODUyNTQ4NTNkNzdiIiwibWFwcGluZ1N0YXR1cyI6ImFjdG" +
            "l2ZSIsImFjY291bnRJZCI6ImliYW46REU0MDEwMDEwMDEwMzMwNzExODYwOCIsImF1dGhvcml6YXRpb25JZCI6IjMzNDkyZTU0LWY" +
            "5MWYtNGIzZC04ZWZlLTZlZTllZDZjYjJkNSIsInBlcm1pc3Npb24iOiJuXC9hIn1dLCJhZGRpdGlvbmFsQ29uc2VudEluZm8iOnsiW" +
            "C1SZXF1ZXN0LUlEIjoiY2YxM2QwMDctMTgyYS00YzBjLWJiZTUtOTQ5ZjVlNjU5YTRlIiwiYXV0aFN0YXR1cyI6InBhcnRpYWwifS" +
            "wiY29uc2VudFR5cGUiOiJidWxrLXBheW1lbnRzIiwicmVjZWlwdCI6eyJkZWJ0b3JBY2NvdW50Ijp7ImliYW4iOiJERTQwMTAwMTAw" +
            "MTAzMzA3MTE4NjA4In0sInJlcXVlc3RlZEV4ZWN1dGlvbkRhdGUiOiIyMDI1LTA4LTAxIiwicGF5bWVudEluZm9ybWF0aW9uSWQiOi" +
            "JteS1idWxrLWlkZW50aWZpY2F0aW9uLTEyMzQiLCJwYXltZW50cyI6W3siY3JlZGl0b3JOYW1lIjoiTWVyY2hhbnQxMjMiLCJjcmVk" +
            "aXRvckFjY291bnQiOnsiaWJhbiI6IkRFMDIxMDAxMDAxMDkzMDcxMTg2MDMifSwiaW5zdHJ1Y3RlZEFtb3VudCI6eyJhbW91bnQiO" +
            "iIxMjMuNTAiLCJjdXJyZW5jeSI6IkVVUiJ9LCJyZW1pdHRhbmNlSW5mb3JtYXRpb25VbnN0cnVjdHVyZWQiOiJSZWYgTnVtYmVyIE1" +
            "lcmNoYW50IDEifSx7ImNyZWRpdG9yTmFtZSI6Ik1lcmNoYW50NDU2IiwiY3JlZGl0b3JBY2NvdW50Ijp7ImliYW4iOiJGUjc2MTIzN" +
            "DU5ODc2NTAxMjM0NTY3ODkwMTQifSwiaW5zdHJ1Y3RlZEFtb3VudCI6eyJhbW91bnQiOiIzNC4xMCIsImN1cnJlbmN5IjoiRVVSIn0s" +
            "InJlbWl0dGFuY2VJbmZvcm1hdGlvblVuc3RydWN0dXJlZCI6IlJlZiBOdW1iZXIgTWVyY2hhbnQgMiJ9XSwiYmF0Y2hCb29raW5nUHJ" +
            "lZmVycmVkIjp0cnVlfSwiY29uc2VudEZyZXF1ZW5jeSI6MH0.piGFAXuiuL6sCJoUCGyph_EyO9wqtrfZ6HGkTvdPwZHY7VvfVhHRwEC" +
            "nsklZBwjflp-CXacLZPkBYz27-hMc2lz6dFXWgN49mNVPXI8g-wsu3PhfzJKC9OuVvDcw6cCtslf10ePnC7a4QyvCPsM5_lrC2mIifkS" +
            "OcQbELwSp5sHHsqcibzyuFOBg-wb6ZCrKMt1c6gBSSucAwBDGvBDayEK3njcZ7FNYGBYtrmCJC-s5FXHwVRtM-R49M1fepK3BVx_yyoL" +
            "DEjuIgevJlQhCmYXM2-eaCPs1U9Ubt7YOK8JkfPY4R2eneaTbTlxtBhP7XGOPazft687aZkQItWSJdQ";
    public static final String TEST_TRANSPORT_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIFljCCA36gAwIBAgIJAN5zDsVzPq0aMA0GCSqGSIb3DQEBBQUAMIGsMQswCQYD" +
            "VQQGEwJMSzELMAkGA1UECAwCV1AxDDAKBgNVBAcMA0NPTDEaMBgGA1UECgwRV1NP" +
            "MiAoVUspIExJTUlURUQxFDASBgNVBAsMC09wZW5CYW5raW5nMS4wLAYDVQQDDCVP" +
            "cGVuQmFua2luZyBQcmUtUHJvZHVjdGlvbiBJc3N1aW5nIENBMSAwHgYJKoZIhvcN" +
            "AQkBFhFtYWxzaGFuaUB3c28yLmNvbTAeFw0yMjAxMTgwNzI3NDJaFw0yNDAxMTgw" +
            "NzI3NDJaMHMxCzAJBgNVBAYTAkdCMRowGAYDVQQKDBFXU08yIChVSykgTElNSVRF" +
            "RDErMCkGA1UEYQwiUFNER0ItT0ItVW5rbm93bjAwMTU4MDAwMDFIUVFyWkFBWDEb" +
            "MBkGA1UEAwwSMDAxNTgwMDAwMUhRUXJaQUFYMIIBIjANBgkqhkiG9w0BAQEFAAOC" +
            "AQ8AMIIBCgKCAQEA59+TouW8sLFWk7MUht40v+DDglinjL2qmQ+wP3YNtvza/7Ue" +
            "KZ+gWw92jd0v99xZz7c5KOgtTgctAmIU1qjGLwzHzn/fl/ZrO4spGLIbU7RwGHA7" +
            "BSpB4k0vGdpCBigaaILHhBrAczDJ1BLYMS4lg69+6fYTeY2s0Khv92NWl8TXorAH" +
            "W0D8KrbZ3chWIynZamNu8KN6s+GL5jyu6pzJpXVNOXiUdRr4U9fLctw7qPw4RbBM" +
            "edXohmVFwMTQ7lMKax+wHOjfQDQW7KuZxRRYiUqB3hjyhrKlIpjjWtnxLclymTAI" +
            "TRMqFlH8KFq/rVBGQ8F3SnDp90E25RbSWdfNRwIDAQABo4HyMIHvMA4GA1UdDwEB" +
            "/wQEAwIHgDAdBgNVHSUEFjAUBggrBgEFBQcDAgYIKwYBBQUHAwEwHQYDVR0OBBYE" +
            "FNxNxhzaeU3VdIMlXkNiYbnjheOnMIGeBggrBgEFBQcBAwSBkTCBjjATBgYEAI5G" +
            "AQYwCQYHBACORgEGAzB3BgYEAIGYJwIwbTBGMEQGBwQAgZgnAQEMBlBTUF9BUwYH" +
            "BACBmCcBAgwGUFNQX1BJBgcEAIGYJwEDDAZQU1BfQUkGBwQAgZgnAQQMBlBTUF9J" +
            "QwwbRmluYW5jaWFsIENvbmR1Y3QgQXV0aG9yaXR5DAZHQi1GQ0EwDQYJKoZIhvcN" +
            "AQEFBQADggIBABBM63bCwANVRR44wFCZysbppYAT4mms3dUqoP3XCUXaO3+7zNWa" +
            "siZ90cje3fuiTD5SAyykm/I/mlgVx92ZbYFW0VG7IVkuC7Fid5iPywHX7Bm1xmEY" +
            "bL1AtAm4sBzE1Kw5dnB1L30do7sp9fuJCdom5/fhrh2GyLBd0iA62qQ+F9uALrC0" +
            "bub0KnGaEf9g1UltgxuqguoYoHb46ICJ03kMGZMC5BcjDDEbDQQ3kT+g9evaBUBm" +
            "3A3cNJURF7/07iLEfHNYrMxDLIw6aC4svbcx+IquO81xpTCefhTU4UFSLN1/DXWW" +
            "qrjCqkvHE53mb33QCXmnsooTP8pABG2q2+w5EC9yeX6Fln6M8VwZL5P2stELWXZE" +
            "876kCo0LkmoP3s6Z62bF4u9hJvM9mQRvmDVqN2Y7eLMty4qmGEmAYYiHOG+FXNKo" +
            "io9MXbB3B7tdeM4g2HlQGfRIrTrfAOu2cH1l1ZwHZgx7oCXN1nuZgE3r07kJx4Bn" +
            "DXCRpXoZq4pB3AlzcWEPh51/SS8Wsz52CNSDGoMB7HPkNnoDrYoibb1LFrOwJ3IM" +
            "VUKCSnt1QdnrKtMVMTd0iI4uk7kCKt7QFeiizN+oW6BI/MNm6mHEWd9CKWmrZT56" +
            "wU3ZM7vgwugq9tAs+oi8Lf3ZODuXAsiSpgcd6dceatoqeyB4E+6kp0Ge" +
            "-----END CERTIFICATE-----";

    public static final String TEST_SIGNATURE_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIFdzCCA1+gAwIBAgIJAN5zDsVzPq0bMA0GCSqGSIb3DQEBBQUAMIGsMQswCQYD" +
            "VQQGEwJMSzELMAkGA1UECAwCV1AxDDAKBgNVBAcMA0NPTDEaMBgGA1UECgwRV1NP" +
            "MiAoVUspIExJTUlURUQxFDASBgNVBAsMC09wZW5CYW5raW5nMS4wLAYDVQQDDCVP" +
            "cGVuQmFua2luZyBQcmUtUHJvZHVjdGlvbiBJc3N1aW5nIENBMSAwHgYJKoZIhvcN" +
            "AQkBFhFtYWxzaGFuaUB3c28yLmNvbTAeFw0yMjAxMTgwNzI4MzdaFw0yNDAxMTgw" +
            "NzI4MzdaMHMxCzAJBgNVBAYTAkdCMRowGAYDVQQKDBFXU08yIChVSykgTElNSVRF" +
            "RDErMCkGA1UEYQwiUFNER0ItT0ItVW5rbm93bjAwMTU4MDAwMDFIUVFyWkFBWDEb" +
            "MBkGA1UEAwwSMDAxNTgwMDAwMUhRUXJaQUFYMIIBIjANBgkqhkiG9w0BAQEFAAOC" +
            "AQ8AMIIBCgKCAQEA1rhjeBrT0+nb/VaHYrkgNodKUlShQvXHL8/sb1q+sVt459Lq" +
            "X8SDYemp01OWWKv45TBv9WtUVl8T08Zc2n5Q6ha7BKVjXaSsDhkXj7TrOAwBxRWn" +
            "NjNivvyb4n4Gxek+AcgozEUFj1MHeIkgywEzP3DUNkbZiQjw7LFxLOy9Bh00f089" +
            "DrCRHxEnUPka9BdRS9M0GWIuMVxn+FxmbZIl7wVxc8t2xFd4uWGtvBX1EbWLYJr1" +
            "/j5fgjHCSmkj23Ar+6oqv2AKpp9HJ2aeYJozHRy+4asSjTuYeobULGIaSxgPGOJg" +
            "4fprs+2UKpV2z2B6RMDf78WQJoGn+lm4wLtKWwIDAQABo4HTMIHQMA4GA1UdDwEB" +
            "/wQEAwIGwDAdBgNVHQ4EFgQUIprePFwGvrdfcP6t14Y5TzPPgJowgZ4GCCsGAQUF" +
            "BwEDBIGRMIGOMBMGBgQAjkYBBjAJBgcEAI5GAQYCMHcGBgQAgZgnAjBtMEYwRAYH" +
            "BACBmCcBAQwGUFNQX0FTBgcEAIGYJwECDAZQU1BfUEkGBwQAgZgnAQMMBlBTUF9B" +
            "SQYHBACBmCcBBAwGUFNQX0lDDBtGaW5hbmNpYWwgQ29uZHVjdCBBdXRob3JpdHkM" +
            "BkdCLUZDQTANBgkqhkiG9w0BAQUFAAOCAgEAsuTEC6j8u7lGEpSeVekcUXby9DPR" +
            "aI6He7MW2K9f6nz12nleyUnvQVQqoFUF+kywRgGpIP/9HsI12NYwLVfgm9LJWR+O" +
            "bBMWDH1OUqgxgy+Pj4+udiR8FJQsEdY6eF4lgfQq6SU+naMFCcJhuIFEVjItiC71" +
            "nVg2PPdaXRwxOlXhuIpfW3DdIaELNffoVKYxnx/wpRGRAbTFI87pDdqZ/2sbkbnt" +
            "4Lq+7VIQmdqqrMCIvPqh8n6/I5xotq8kPbBcf7/1wjUDClqX5chBh89sxZhIyRtO" +
            "q3HiVzQ2c6sYgA640LsxHB4yPWFDv5tPnzohmDmfCYcvbSZhsGm+hlyjaf8/gN8c" +
            "G+ui7PFKuyI0NUnR3rD88gIzl2lT1Wtw9f2eFkS8NSnB3ENhRYPLcvZDePktOjkj" +
            "5PEqo4OTZgGnoMzXNvzWaABrcpzw7a1WF7swvvZavUg9bvFbbeVloDZj4w9ODICo" +
            "e/Sy+2nZBRTVzIHGJ4kNzscXXSMsSD9sXZMT/JmmD+cs/X/9SJzAsunXqpHbTB9P" +
            "iGWdR7rhk6e0ddwwTa7wLs/h2MOP8LyTVaHQtNXe8x54YwDCP98dZ1G76iLKIZoU" +
            "84aYcGp7E8tlQl05UhunlTNe+WxWvBt9tWeotaOomSqGRWDk+4j5z91NoFuFQ81a" +
            "DAJxB8RYI2I1sk8=" +
            "-----END CERTIFICATE-----";

    public static final String EXPIRED_SELF_CERT = "-----BEGIN CERTIFICATE-----" +
            "MIIDiTCCAnGgAwIBAgIENx3SZjANBgkqhkiG9w0BAQsFADB1MQswCQYDVQQGEwJs" +
            "azEQMA4GA1UECBMHd2VzdGVybjEQMA4GA1UEBxMHY29sb21ibzENMAsGA1UEChME" +
            "d3NvMjEUMBIGA1UECxMLb3BlbmJhbmtpbmcxHTAbBgNVBAMTFG9wZW5iYW5raW5n" +
            "LndzbzIuY29tMB4XDTIwMDMwMTEyMjE1MVoXDTIwMDUzMDEyMjE1MVowdTELMAkG" +
            "A1UEBhMCbGsxEDAOBgNVBAgTB3dlc3Rlcm4xEDAOBgNVBAcTB2NvbG9tYm8xDTAL" +
            "BgNVBAoTBHdzbzIxFDASBgNVBAsTC29wZW5iYW5raW5nMR0wGwYDVQQDExRvcGVu" +
            "YmFua2luZy53c28yLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB" +
            "AKWMb1mhSthxi5vmQcvEnt0rauYv8uFWjGyiuCkk5wQbArybGXyC8rrZf5qNNY4s" +
            "RG2+Yimxph2Z8MWWPFBebTIABPuRcVDquX7fL4+8FZJTH3JLwfT+slunAA4473mZ" +
            "9s2fAVu6CmQf1V09+fEbMGI9WWh53g19wg5WdlToOX4g5lh4QtGRpbWpEWaYrKzS" +
            "B5EWOUI7lroFtv6s9OpEO59VAkXWKUbT98T8TCYqiDH+nMy3k+GbVawxXeHYHQr+" +
            "XlbcChPaCwhMXspqKG49xaJmrOuRMoAWCBGUW8r2RDhQ+FP5V/sTRMqKmBv9gTe6" +
            "RJwoKPlDt+0aX9vaFjKpjPcCAwEAAaMhMB8wHQYDVR0OBBYEFGH0gyeHIz1+ONGI" +
            "PuGnAhrS3apoMA0GCSqGSIb3DQEBCwUAA4IBAQCVEakh1SLnZOz2IK0ISbAV5UBb" +
            "nerLNDl+X+YSYsCQM1SBcXDjlkSAeP3ErJEO3RW3wdRQjLRRHomwSCSRE84SUfSL" +
            "VPIbeR7jm4sS9x5rnlGF6iqhYh2MlZD/hFxdrGoYv8g/JN4FFFMXRmmaQ8ouYJwc" +
            "4ZoxRdCXszeI5Zp2+b14cs/nf4geYliHtcDr/w7fkvQ0hn+c1lTihbW0/eE32aUK" +
            "SULAmjx0sCDfDAQItP79CC7jCW0TFN0CMORw/+fzp/dnVboSZ2MgcuRIH1Ez+6/1" +
            "1QJD2SrkkaRSEaXI6fe9jgHVhnqK9V3y3WAuzEKjaKw6jV8BjkXAA4dQj1Re" +
            "-----END CERTIFICATE-----";

    public static final String TEST_CLIENT_CERT_ISSUER = "-----BEGIN CERTIFICATE-----" +
            "MIIF1jCCA74CCQCv4eZ5xH5itDANBgkqhkiG9w0BAQsFADCBrDELMAkGA1UEBhMC" +
            "TEsxCzAJBgNVBAgMAldQMQwwCgYDVQQHDANDT0wxGjAYBgNVBAoMEVdTTzIgKFVL" +
            "KSBMSU1JVEVEMRQwEgYDVQQLDAtPcGVuQmFua2luZzEuMCwGA1UEAwwlT3BlbkJh" +
            "bmtpbmcgUHJlLVByb2R1Y3Rpb24gSXNzdWluZyBDQTEgMB4GCSqGSIb3DQEJARYR" +
            "bWFsc2hhbmlAd3NvMi5jb20wHhcNMjIwMTE4MDY1NDI5WhcNMjMwMTE4MDY1NDI5" +
            "WjCBrDELMAkGA1UEBhMCTEsxCzAJBgNVBAgMAldQMQwwCgYDVQQHDANDT0wxGjAY" +
            "BgNVBAoMEVdTTzIgKFVLKSBMSU1JVEVEMRQwEgYDVQQLDAtPcGVuQmFua2luZzEu" +
            "MCwGA1UEAwwlT3BlbkJhbmtpbmcgUHJlLVByb2R1Y3Rpb24gSXNzdWluZyBDQTEg" +
            "MB4GCSqGSIb3DQEJARYRbWFsc2hhbmlAd3NvMi5jb20wggIiMA0GCSqGSIb3DQEB" +
            "AQUAA4ICDwAwggIKAoICAQC89m56sxvPa/3Qx0cT3Eu1Ze4dbMk1Kh59Pz1yKe+z" +
            "me9+/WSue8gxRoPlJjPB1XmVqe+LuBAiS032p87pOcOS4Yd7m3fIDHAf9Im3GLIs" +
            "9A9lzmJwE7kjztmlpSZF8LVEVDLQMlOxi7YfK5ZCv2j31VS+NdtjsEiRbDYy8W+Z" +
            "706pKvfywJNH12bCg/h2AjQM3tnBhdHatk4kWB/IMObfYgVNjy9PJBxS94ZLqUnX" +
            "LUhTQXVqfJMERwgTqCuThzVIT1VdfT//x2g+DkgaY+ZR9MLIX0M0leta5F1BjF+G" +
            "T1qDxRhsYlzRPJ9sZui8FPT7CKXx8vNmongWduT2Zu6HXvTbMZu4z7GP4Pas4gxF" +
            "HpjuPkU9TC6XY5BPihEWVX187vyTIWUyF53tTmEnMdlgSjOzo2KZ/wxxXhaXU01Z" +
            "SgxrpIt/6YjwKk42Slk7kCxnLSEhOdrcYyfq3c5/fIw2N1aKVp/mP1V7Sjq6no+j" +
            "1SxO1Gcmyg61GVlQq2g7saop7SR5H829izaSgG++nvRoWL8O5xeMS59moPokAQhe" +
            "/msEt5ZcTqSmICssDSZn3/Bdb2A8FsQMxXYIrtA7tn4DtKY8u7enV/zAeAEY4odV" +
            "0uKUaEfA1ktV/n3gkH3ovbz8sqnuw4Rvjc/gVEU+i+ZyWqiNeoufw31yMco5Bb1t" +
            "9wIDAQABMA0GCSqGSIb3DQEBCwUAA4ICAQCHu8htykTryNgBZ9aFVZHr+EcgvLhD" +
            "we+zfk1M+x3A0bCasGFhsi9zh4OBcx9sPcMBFNoC2K4mYGPc2rtVk5G6+Jdo3eJP" +
            "m+rC+5OfYF9Hpzv3XtQR8MNpGrmpYd+c/OIzFb+AUTf98ugCZd/fGu5leRc29srb" +
            "Ft60Zu3Bxf+uvWudvqdBCpkuRk92vWDzss/F7f+HZ60wwTUc/Ibxg6Eb9lIckSXP" +
            "Iu1NGZYYtix3xcjL6xdRLLk/Xz7Auy/uijss7NUVfJDCFViwGW0aYK072sGc7qmN" +
            "UGhCD/KJ/cWgCQK6SvnOwAVBs5WiMQ+ipycWsyy6WONFpvgGQJ4VOoCdTAKP4Z61" +
            "fM0px0PEESySh53/ohHOdHSgJVJBHSlwLcSi6dnAnKD6nvTkMJz0+0FJ6Yo/QVfx" +
            "ftSfSWx/WLs26rtYBAOw/c2pvFtjHT/pdJBAdXenj/CGI0BE0IeY9wSECJ1Bo6tp" +
            "IvKQtti1bFmqbfM9rdCKaz8DOevPTuItOCvYaFskhVhMcCOvjkYX4rH8hvfrAS6L" +
            "TudHE+PtuybzZXpwKmRh1SJnn6higB3M60zOZkoJBoJBgtuilA07rZzFJVEYBCCu" +
            "ITtW3vYVNO8vxVkcJOU6IseWQsvILyvB9VAFZ6Rbc8tA3IQpdrz//DbN0mdL5F/i" +
            "ZKdBTnkp/vPzPw==" +
            "-----END CERTIFICATE-----";

    public static final String SAMPLE_JWT = "Bearer eyJ4NXQiOiJOVGRtWmpNNFpEazNOalkwWXpjNU1tWm1PR" +
            "Gd3TVRFM01XWXdOREU1TVdSbFpEZzROemM0WkEiLCJraWQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQTNNV0k" +
            "yTkRBelpHUXpOR00wWkdSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZ19SUzI1NiIsImFsZyI6IlJTMjU2In0.eyJzdWIiO" +
            "iJhZG1pbkB3c28yLmNvbUBjYXJib24uc3VwZXIiLCJhdXQiOiJBUFBMSUNBVElPTiIsImF1ZCI6IlBTREdCLU9CLVVua25vd24wMDE" +
            "1ODAwMDAxSFFRclpBQVgiLCJuYmYiOjE2NTA5NTQ5NzUsImF6cCI6IlBTREdCLU9CLVVua25vd24wMDE1ODAwMDAxSFFRclpBQVgiL" +
            "CJzY29wZSI6ImFjY291bnRzIiwiaXNzIjoiaHR0cHM6XC9cL2xvY2FsaG9zdDo5NDQ2XC9vYXV0aDJcL3Rva2VuIiwiY25mIjp7Ing" +
            "1dCNTMjU2IjoieWJlcWFJbTUwMElNUHkxOVZrUGZIUlRKTnQ5cXZfUXQ1am1IZHQtYkptYyJ9LCJleHAiOjE2NTA5NTg1NzUsImlhd" +
            "CI6MTY1MDk1NDk3NSwianRpIjoiYzU4ZDEyMTEtNzE5MC00ZmU0LWI1MDktYTY2YmQyZDM0ZGJlIn0.AudZ4ojlzR3BsQpjD6JbNbk" +
            "_wNa5kgfLTlZ8ZByJyKmw_UDOEOoQIeCPe_TQz-i7JEF9hmBPrLw7FGVYseyskM_amq8nQyFwonEmrPShLPPAqGjSyNmL3IZoQM-TJ" +
            "n3sLS-l23DfYFZfjRgMYETTd2nPtfe4q_lNzmv4r2gsYsvpMcgZ7hzPp5X5SO6X3iSDmD3BGvNoE1V0hpkZGkte-uo6rM97z-icwlm" +
            "h7dM7l9IvVAwmqxmZG1YL200kgD4bctwfry";

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

    public static synchronized java.security.cert.X509Certificate getExpiredSelfCertificate()
            throws OpenBankingException {
        if (expiredSelfCertificate == null) {
            expiredSelfCertificate = CertificateUtils.parseCertificate(EXPIRED_SELF_CERT);
        }
        return expiredSelfCertificate;
    }

    public static synchronized java.security.cert.X509Certificate getTestSigningCertificate()
            throws OpenBankingException {
        if (testEidasCertificate == null) {
            testEidasCertificate = CertificateUtils.parseCertificate(TEST_SIGNATURE_CERT);
        }
        return testEidasCertificate;
    }

    public static synchronized java.security.cert.X509Certificate getTestClientCertificateIssuer()
            throws OpenBankingException {
        if (testClientCertificateIssuer == null) {
            testClientCertificateIssuer = CertificateUtils.parseCertificate(TEST_CLIENT_CERT_ISSUER);
        }
        return testClientCertificateIssuer;
    }

    public static String getPayloadFromJWT(String jwtString) {
        return jwtString.split("\\.")[1];
    }

    public static JSONObject decodeBase64(String payload) throws UnsupportedEncodingException {
        return new JSONObject(new String(Base64.getDecoder().decode(payload), String.valueOf(StandardCharsets.UTF_8)));
    }
}
