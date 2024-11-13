/**
 * Copyright (c) 2022-2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.cache.CertificateRevocationCache;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.gateway.exceptions.DigestMissingException;
import com.wso2.openbanking.berlin.gateway.exceptions.DigestValidationException;
import com.wso2.openbanking.berlin.gateway.exceptions.SignatureCertMissingException;
import com.wso2.openbanking.berlin.gateway.exceptions.SignatureMissingException;
import com.wso2.openbanking.berlin.gateway.exceptions.SignatureValidationException;
import com.wso2.openbanking.berlin.gateway.executors.core.APIRequestRouterConstants;
import com.wso2.openbanking.berlin.gateway.test.TestData;
import com.wso2.openbanking.berlin.gateway.utils.GatewayTestUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import static org.powermock.api.mockito.PowerMockito.doReturn;

/**
 * Unit tests for SignatureValidationExecutor.
 */
@PrepareForTest({CertificateValidationUtils.class, CertificateContentExtractor.class, CommonConfigParser.class,
        CertificateRevocationCache.class, OpenBankingConfigParser.class, CertificateValidationUtils.class})
@PowerMockIgnore({"net.minidev.*", "jdk.internal.reflect.*", "javax.security.auth.x500.*"})
public class SignatureValidationExecutorTests extends PowerMockTestCase {

    @Mock
    OBAPIRequestContext obapiRequestContextMock;

    @Mock
    X509Certificate x509Certificate;

    @Mock
    CertificateValidationUtils certificateValidationUtilsMock;

    @Mock
    CertificateContent certificateContentMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    OpenBankingConfigParser openBankingConfigParserMock;

    @Mock
    MsgInfoDTO msgInfoDTOMock;

    private X509Certificate transportCertificate;
    private static final String sampleClientId = "12345";
    private java.security.cert.X509Certificate expiredPeerCertificate;
    private java.security.cert.X509Certificate signingCertificate;
    private CertificateContent certificateContent;
    private java.security.cert.X509Certificate testPeerCertificateIssuer;

    @BeforeClass
    public void initClass() throws CertificateException, CertificateValidationException,
            java.security.cert.CertificateException, OpenBankingException {

        this.transportCertificate = GatewayTestUtils.getTestTransportCertificate();
        obapiRequestContextMock = Mockito.mock(OBAPIRequestContext.class);
        this.expiredPeerCertificate = GatewayTestUtils.getExpiredSelfCertificate();
        this.signingCertificate = GatewayTestUtils.getTestSigningCertificate();
        this.testPeerCertificateIssuer = GatewayTestUtils.getTestClientCertificateIssuer();
        this.certificateContent = Mockito.mock(CertificateContent.class);
        this.msgInfoDTOMock = Mockito.mock(MsgInfoDTO.class);
    }

    @BeforeMethod
    public void initMethod() {

        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);

        openBankingConfigParserMock = PowerMockito.mock(OpenBankingConfigParser.class);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);

        msgInfoDTOMock = Mockito.mock(MsgInfoDTO.class);
        PowerMockito.when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTOMock);
        PowerMockito.when(msgInfoDTOMock.getHeaders()).thenReturn(TestData.VALID_ACCOUNTS_REQUEST_HEADERS_MAP);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testOrganizationIDValidationWithNoCert() {

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        javax.security.cert.X509Certificate[] x509Certificates = {};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getRequestPayload()).thenReturn(TestData.VALID_ACCOUNT_INITIATION_PAYLOAD);
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testOrganizationIDValidationWithNoEmptyConvertedCert() {

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getRequestPayload()).thenReturn(TestData.VALID_ACCOUNT_INITIATION_PAYLOAD);
        certificateValidationUtilsMock = PowerMockito.mock(CertificateValidationUtils.class);
        PowerMockito.mockStatic(CertificateValidationUtils.class);

        x509Certificate = Mockito.mock(X509Certificate.class);
        javax.security.cert.X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        PowerMockito.when(CertificateValidationUtils.convert(Mockito.any())).thenReturn(Optional.empty());
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testOrganizationIDValidationWithCertWithEmptyOrgId() throws CertificateValidationException {

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getRequestPayload()).thenReturn(TestData.VALID_ACCOUNT_INITIATION_PAYLOAD);
        javax.security.cert.X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        apiRequestInfoDTOMock.setConsumerKey(sampleClientId);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        PowerMockito.mockStatic(CertificateContentExtractor.class);
        PowerMockito.when(CertificateContentExtractor.extract(Mockito.any()))
                .thenReturn(certificateContentMock);
        PowerMockito.when(certificateContentMock.getPspAuthorisationNumber()).thenReturn("");
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testOrganizationIDValidationWithCertWithNullgOrgId() throws CertificateValidationException {

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getRequestPayload()).thenReturn(TestData.VALID_ACCOUNT_INITIATION_PAYLOAD);
        javax.security.cert.X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        apiRequestInfoDTOMock.setConsumerKey(sampleClientId);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        PowerMockito.mockStatic(CertificateContentExtractor.class);
        PowerMockito.when(CertificateContentExtractor.extract(Mockito.any()))
                .thenReturn(certificateContentMock);
        PowerMockito.when(certificateContentMock.getPspAuthorisationNumber()).thenReturn(null);
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testOrganizationIDValidationWithCertWithMismatchingOrgId() throws CertificateValidationException {

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getRequestPayload()).thenReturn(TestData.VALID_ACCOUNT_INITIATION_PAYLOAD);
        javax.security.cert.X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        apiRequestInfoDTOMock.setConsumerKey(sampleClientId);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        PowerMockito.mockStatic(CertificateContentExtractor.class);
        PowerMockito.when(CertificateContentExtractor.extract(Mockito.any()))
                .thenReturn(certificateContentMock);
        PowerMockito.when(certificateContentMock.getPspAuthorisationNumber()).thenReturn("mismatchingOrgId");
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testValidateHeaders() throws SignatureCertMissingException, SignatureMissingException,
            DigestMissingException, SignatureValidationException {

        new SignatureValidationExecutor().validateHeaders(TestData.VALID_ACCOUNTS_REQUEST_HEADERS_MAP);
    }

    @Test (expectedExceptions = SignatureMissingException.class)
    public void testValidateHeadersWithoutSignatureHeader()
            throws SignatureCertMissingException, SignatureMissingException,
            DigestMissingException, SignatureValidationException {

        new SignatureValidationExecutor().validateHeaders(TestData.HEADERS_MAP_WITHOUT_SIGNATURE);
    }

    @Test (expectedExceptions = DigestMissingException.class)
    public void testValidateHeadersWithoutDigestHeader()
            throws SignatureCertMissingException, SignatureMissingException,
            DigestMissingException, SignatureValidationException {

        new SignatureValidationExecutor().validateHeaders(TestData.HEADERS_MAP_WITHOUT_DIGEST);
    }

    @Test (expectedExceptions = SignatureCertMissingException.class)
    public void testValidateHeadersWithoutTPPSignatureCertificateHeader()
            throws SignatureCertMissingException, SignatureMissingException,
            DigestMissingException, SignatureValidationException {

        new SignatureValidationExecutor().validateHeaders(TestData.HEADERS_MAP_WITHOUT_CERT_HEADER);
    }

    @Test
    public void testValidateHeadersWithoutDateHeader()
            throws SignatureCertMissingException, SignatureMissingException,
            DigestMissingException, SignatureValidationException {

        new SignatureValidationExecutor().validateHeaders(TestData.HEADERS_MAP_WITHOUT_DATE_HEADER);
    }

    @Test (expectedExceptions = SignatureValidationException.class)
    public void testValidateHeadersWithoutXRequestIDHeader()
            throws SignatureCertMissingException, SignatureMissingException,
            DigestMissingException, SignatureValidationException {

        new SignatureValidationExecutor().validateHeaders(TestData.HEADERS_MAP_WITHOUT_X_REQUEST_ID);
    }

    @Test
    public void testValidateDigestHeader() throws DigestValidationException {

        String validSampleDigest = TestData.VALID_ACCOUNTS_REQUEST_HEADERS_MAP.get("Digest");
        String validRelativePayload = TestData.VALID_ACCOUNT_INITIATION_PAYLOAD;

        doReturn(TestData.SUPPORTED_HASH_ALGORITHMS).when(commonConfigParserMock).getSupportedHashAlgorithms();
        Assert.assertTrue(new SignatureValidationExecutor().validateDigest(validSampleDigest, validRelativePayload));
    }

    @Test (expectedExceptions = DigestValidationException.class)
    public void testValidateDigestHeaderWithDigestWithInvalidLength() throws DigestValidationException {

        String validSampleDigest = "SHA-256=ANsITc6LgC2N6lZyCv+6s4X/Kl1EH9dNZq402rAarIR4=1234";
        String validRelativePayload = TestData.VALID_ACCOUNT_INITIATION_PAYLOAD;
        new SignatureValidationExecutor().validateDigest(validSampleDigest, validRelativePayload);
    }

    @Test (expectedExceptions = DigestValidationException.class)
    public void testValidateDigestHeaderForInvalidAlgorithm() throws DigestValidationException {

        String validSampleDigest = TestData.VALID_ACCOUNTS_REQUEST_HEADERS_MAP.get("Digest");
        String validRelativePayload = TestData.VALID_ACCOUNT_INITIATION_PAYLOAD;

        doReturn(TestData.UNSUPPORTED_ALGORITHMS).when(commonConfigParserMock).getSupportedHashAlgorithms();
        new SignatureValidationExecutor().validateDigest(validSampleDigest, validRelativePayload);
    }

    @Test
    public void testValidateDigestHeaderForEmptyPayload() throws DigestValidationException {

        String validSampleDigest = "SHA-256=RBNvo1WzZ4oRRq0W9+hknpT7T8If536DEMBg9hyq/4o=";
        String validRelativePayload = "";

        doReturn(TestData.SUPPORTED_HASH_ALGORITHMS).when(commonConfigParserMock).getSupportedHashAlgorithms();
        Assert.assertTrue(new SignatureValidationExecutor().validateDigest(validSampleDigest, validRelativePayload));
    }

    @Test
    public void testValidateDigestHeaderForInvalidDigest() throws DigestValidationException {

        String validSampleDigest = "SHA-256=ANsITc6LgC2N6lZyCv+6s4X/Kl1EH9dNZq402rAarIR5";
        String validRelativePayload = TestData.VALID_ACCOUNT_INITIATION_PAYLOAD;

        doReturn(TestData.SUPPORTED_HASH_ALGORITHMS).when(commonConfigParserMock).getSupportedHashAlgorithms();
        Assert.assertFalse(new SignatureValidationExecutor().validateDigest(validSampleDigest, validRelativePayload));
    }

    @Test
    public void testValidateInvalidSignature() throws SignatureValidationException, OpenBankingException {

        doReturn(TestData.SUPPORTED_SIGNATURE_ALGORITHMS).when(commonConfigParserMock)
                .getSupportedSignatureAlgorithms();
        Assert.assertFalse(new SignatureValidationExecutor()
                .validateSignature(TestData.VALID_ACCOUNTS_REQUEST_HEADERS_MAP,
                        GatewayTestUtils.getTestSigningCertificate()));
    }

    @Test (expectedExceptions = SignatureValidationException.class)
    public void testValidateSignatureWithInvalidKeyID()
            throws SignatureValidationException, OpenBankingException {

        new SignatureValidationExecutor().validateSignature(TestData.INVALID_ACCOUNTS_REQUEST_HEADERS_MAP,
                GatewayTestUtils.getTestSigningCertificate());
    }

    @Test (expectedExceptions = SignatureValidationException.class)
    public void testValidateSignatureWithInvalidAlgorithm()
            throws SignatureValidationException, OpenBankingException {

        doReturn(TestData.UNSUPPORTED_ALGORITHMS).when(commonConfigParserMock).getSupportedSignatureAlgorithms();
        new SignatureValidationExecutor().validateSignature(TestData.VALID_ACCOUNTS_REQUEST_HEADERS_MAP,
                GatewayTestUtils.getTestSigningCertificate());
    }

    @Test (expectedExceptions = SignatureValidationException.class)
    public void testValidateSignatureWithoutMandatoryHeaderInSignature()
            throws SignatureValidationException, OpenBankingException {

        doReturn(TestData.SUPPORTED_SIGNATURE_ALGORITHMS).when(commonConfigParserMock)
                .getSupportedSignatureAlgorithms();
        new SignatureValidationExecutor().validateSignature(TestData.INVALID_ACCOUNTS_REQUEST_HEADERS_MAP_2,
                GatewayTestUtils.getTestSigningCertificate());
    }

    @Test (expectedExceptions = SignatureValidationException.class)
    public void testValidateSignatureWithoutUpperCaseHeaderInSignature()
            throws SignatureValidationException, OpenBankingException {

        doReturn(TestData.SUPPORTED_SIGNATURE_ALGORITHMS).when(commonConfigParserMock)
                .getSupportedSignatureAlgorithms();
        new SignatureValidationExecutor().validateSignature(TestData.INVALID_ACCOUNTS_REQUEST_HEADERS_MAP_3,
                GatewayTestUtils.getTestSigningCertificate());
    }

    @Test
    public void testValidateCertExpiration() throws Exception {
        WhiteboxImpl.invokeMethod(new SignatureValidationExecutor(), "validateCertExpiration",
                expiredPeerCertificate, obapiRequestContextMock);
    }

    @Test
    public void testValidateCertExpirationWithValidCert() throws Exception {
        WhiteboxImpl.invokeMethod(new SignatureValidationExecutor(), "validateCertExpiration",
                signingCertificate, obapiRequestContextMock);
    }

    @Test
    public void testIsCertificateRolesValid() throws Exception {

        List<String> roles = new ArrayList<>();
        roles.add("AISP");
        roles.add("ASPSP");
        Map<String, List<String>> allowedScopes = new HashMap<>();
        allowedScopes.put("accounts", roles);
        doReturn(true).when(commonConfigParserMock).isPsd2RoleValidationEnabled();
        doReturn(allowedScopes).when(openBankingConfigParserMock).getAllowedScopes();
        Assert.assertTrue(WhiteboxImpl.invokeMethod(new SignatureValidationExecutor(),
                "isCertificateRolesValid", certificateContent));
    }

    @Test
    public void testPostProcessRequestMethodWithoutCertRevocation() throws java.security.cert.CertificateException {

        X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getRequestPayload()).thenReturn(TestData.VALID_ACCOUNT_INITIATION_PAYLOAD);

        CertificateRevocationCache mock = Mockito.mock(CertificateRevocationCache.class);
        PowerMockito.mockStatic(CertificateRevocationCache.class);
        PowerMockito.when(CertificateRevocationCache.getInstance()).thenReturn(mock);

        doReturn("3").when(openBankingConfigParserMock)
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_RETRY_COUNT);

        certificateValidationUtilsMock = PowerMockito.mock(CertificateValidationUtils.class);
        PowerMockito.mockStatic(CertificateValidationUtils.class);
        doReturn(TestData.SUPPORTED_HASH_ALGORITHMS).when(commonConfigParserMock).getSupportedHashAlgorithms();
        doReturn(TestData.SUPPORTED_SIGNATURE_ALGORITHMS).when(commonConfigParserMock)
                .getSupportedSignatureAlgorithms();
        PowerMockito.when(CertificateValidationUtils.convert(Mockito.any())).thenReturn(GatewayTestUtils
                .convertCert(transportCertificate));

        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testPostProcessRequestMethodWithCertRevocation() throws CertificateValidationException,
            java.security.cert.CertificateException {

        X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getRequestPayload()).thenReturn(TestData.VALID_ACCOUNT_INITIATION_PAYLOAD);

        CertificateRevocationCache mock = Mockito.mock(CertificateRevocationCache.class);
        PowerMockito.mockStatic(CertificateRevocationCache.class);
        PowerMockito.when(CertificateRevocationCache.getInstance()).thenReturn(mock);

        doReturn("3").when(openBankingConfigParserMock)
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_RETRY_COUNT);
        doReturn("true").when(openBankingConfigParserMock)
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_ENABLED);

        doReturn(TestData.SUPPORTED_HASH_ALGORITHMS).when(commonConfigParserMock).getSupportedHashAlgorithms();
        doReturn(TestData.SUPPORTED_SIGNATURE_ALGORITHMS).when(commonConfigParserMock)
                .getSupportedSignatureAlgorithms();

        certificateValidationUtilsMock = PowerMockito.mock(CertificateValidationUtils.class);
        PowerMockito.mockStatic(CertificateValidationUtils.class);
        PowerMockito.when(CertificateValidationUtils.getIssuerCertificateFromTruststore(
                Mockito.any(java.security.cert.X509Certificate.class))).thenReturn(testPeerCertificateIssuer);
        PowerMockito.when(CertificateValidationUtils.convert(Mockito.any())).thenReturn(GatewayTestUtils
                .convertCert(transportCertificate));

        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testPostProcessRequestMethodWithInvalidTPPSignatureCertRoles() throws CertificateValidationException,
            java.security.cert.CertificateException {

        X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getRequestPayload()).thenReturn(TestData.VALID_ACCOUNT_INITIATION_PAYLOAD);

        CertificateRevocationCache mock = Mockito.mock(CertificateRevocationCache.class);
        PowerMockito.mockStatic(CertificateRevocationCache.class);
        PowerMockito.when(CertificateRevocationCache.getInstance()).thenReturn(mock);

        doReturn("3").when(openBankingConfigParserMock)
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_RETRY_COUNT);
        doReturn("true").when(openBankingConfigParserMock)
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_ENABLED);

        doReturn(TestData.SUPPORTED_HASH_ALGORITHMS).when(commonConfigParserMock).getSupportedHashAlgorithms();
        doReturn(TestData.SUPPORTED_SIGNATURE_ALGORITHMS).when(commonConfigParserMock)
                .getSupportedSignatureAlgorithms();

        List<String> roles = new ArrayList<>();
        roles.add("PISP");
        Map<String, List<String>> allowedScopes = new HashMap<>();
        allowedScopes.put("accounts", roles);
        doReturn(true).when(commonConfigParserMock).isPsd2RoleValidationEnabled();
        doReturn(allowedScopes).when(openBankingConfigParserMock).getAllowedScopes();

        certificateValidationUtilsMock = PowerMockito.mock(CertificateValidationUtils.class);
        PowerMockito.mockStatic(CertificateValidationUtils.class);
        PowerMockito.when(CertificateValidationUtils.getIssuerCertificateFromTruststore(
                Mockito.any(java.security.cert.X509Certificate.class))).thenReturn(testPeerCertificateIssuer);
        PowerMockito.when(CertificateValidationUtils.convert(Mockito.any())).thenReturn(GatewayTestUtils
                .convertCert(transportCertificate));

        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testPostProcessRequestMethodWithoutDateHeaderForPaymentRequest() throws CertificateValidationException,
            java.security.cert.CertificateException {

        X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTOMock);
        Mockito.when(obapiRequestContextMock.getContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP))
                .thenReturn(APIRequestRouterConstants.PAYMENTS_TYPE);
        Mockito.when(apiRequestInfoDTOMock.getConsumerKey()).thenReturn("PSDGB-OB-Unknown0015800001HQQrZAAX");

        CertificateRevocationCache mock = Mockito.mock(CertificateRevocationCache.class);
        PowerMockito.mockStatic(CertificateRevocationCache.class);
        PowerMockito.when(CertificateRevocationCache.getInstance()).thenReturn(mock);

        doReturn("3").when(openBankingConfigParserMock)
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_RETRY_COUNT);
        doReturn("true").when(openBankingConfigParserMock)
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_ENABLED);

        certificateValidationUtilsMock = PowerMockito.mock(CertificateValidationUtils.class);
        PowerMockito.mockStatic(CertificateValidationUtils.class);
        PowerMockito.when(CertificateValidationUtils.getIssuerCertificateFromTruststore(
                Mockito.any(java.security.cert.X509Certificate.class))).thenReturn(testPeerCertificateIssuer);
        PowerMockito.when(CertificateValidationUtils.convert(Mockito.any())).thenReturn(GatewayTestUtils
                .convertCert(transportCertificate));
        PowerMockito.when(msgInfoDTOMock.getHeaders()).thenReturn(TestData.INVALID_PAYMENTS_REQUEST_HEADERS_MAP);

        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }
}
