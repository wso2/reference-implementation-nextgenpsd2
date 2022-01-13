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

package com.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.berlin.gateway.utils.GatewayTestUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;

import java.util.Optional;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

/**
 * Unit tests for MTLSValidationExecutor.
 */
@PrepareForTest({CertificateValidationUtils.class, CertificateContentExtractor.class})
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

    private X509Certificate transportCertificate;
    private static final String sampleClientId = "12345";

    @BeforeClass
    public void initClass() throws CertificateException {

        this.transportCertificate = GatewayTestUtils.getTestTransportCertificate();
        obapiRequestContextMock = Mockito.mock(OBAPIRequestContext.class);
    }

    @Test
    public void testOrganizationIDValidationWithMatchingClientId() {

        X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setConsumerKey("PSDGB-OB-Unknown0015800001HQQrZAAX");
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testOrganizationIDValidationWithNoCert() {

        javax.security.cert.X509Certificate[] x509Certificates = {};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testOrganizationIDValidationWithNoEmptyConvertedCert() {

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

        javax.security.cert.X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setConsumerKey(sampleClientId);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        PowerMockito.mockStatic(CertificateContentExtractor.class);
        PowerMockito.when(CertificateContentExtractor.extract(Mockito.any()))
                .thenReturn(certificateContentMock);
        PowerMockito.when(certificateContentMock.getPspAuthorisationNumber()).thenReturn("");
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testOrganizationIDValidationWithCertWithNullgOrgId() throws CertificateValidationException {

        javax.security.cert.X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setConsumerKey(sampleClientId);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        PowerMockito.mockStatic(CertificateContentExtractor.class);
        PowerMockito.when(CertificateContentExtractor.extract(Mockito.any()))
                .thenReturn(certificateContentMock);
        PowerMockito.when(certificateContentMock.getPspAuthorisationNumber()).thenReturn(null);
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testOrganizationIDValidationWithCertWithMismatchingOrgId() throws CertificateValidationException {

        javax.security.cert.X509Certificate[] x509Certificates = {transportCertificate};
        Mockito.when(obapiRequestContextMock.getClientCerts()).thenReturn(x509Certificates);
        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setConsumerKey(sampleClientId);
        Mockito.when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        PowerMockito.mockStatic(CertificateContentExtractor.class);
        PowerMockito.when(CertificateContentExtractor.extract(Mockito.any()))
                .thenReturn(certificateContentMock);
        PowerMockito.when(certificateContentMock.getPspAuthorisationNumber()).thenReturn("mismatchingOrgId");
        new SignatureValidationExecutor().postProcessRequest(obapiRequestContextMock);
    }
}
