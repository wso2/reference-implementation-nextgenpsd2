/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.cache.CertificateRevocationCache;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.gateway.test.TestData;
import com.wso2.openbanking.berlin.gateway.utils.GatewayTestUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.security.cert.CertificateException;

@PrepareForTest({CertificateValidationUtils.class, CertificateContentExtractor.class, CommonConfigParser.class,
        CertificateRevocationCache.class, OpenBankingConfigParser.class, CertificateValidationUtils.class})
@PowerMockIgnore({"net.minidev.*", "jdk.internal.reflect.*", "javax.security.auth.x500.*"})
public class BerlinConsentEnforcementExecutorTests extends PowerMockTestCase {

    private OBAPIRequestContext obapiRequestContextMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    OpenBankingConfigParser openBankingConfigParserMock;

    @Mock
    MsgInfoDTO msgInfoDTOMock;

    @BeforeClass
    public void initClass() throws CertificateException, CertificateValidationException,
            java.security.cert.CertificateException, OpenBankingException {

        obapiRequestContextMock = Mockito.mock(OBAPIRequestContext.class);
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

    @Test (priority = 2)
    public void testPostProcessRequestWithValidData() throws UnsupportedEncodingException {

        String paymentId = UUID.randomUUID().toString();
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod("GET");
        msgInfoDTO.setResource("payments/bulk-payments/" + paymentId);
        Map<String, String> addedHeadersMap = new HashMap<>();
        addedHeadersMap.put("Account-Request-Information", GatewayTestUtils.SAMPLE_JWT_2);
        PowerMockito.when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTOMock);
        PowerMockito.when(obapiRequestContextMock.getContextProps()).thenReturn(new HashMap<>());
        PowerMockito.when(obapiRequestContextMock.getAddedHeaders()).thenReturn(addedHeadersMap);
        PowerMockito.when(obapiRequestContextMock.getConsentId()).thenReturn(paymentId);
        new BerlinConsentEnforcementExecutor().sendReturnPaymentResponseToClient(obapiRequestContextMock);
    }

    @Test
    public void testMethod() {

        String paymentId = UUID.randomUUID().toString();
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod("GET");
        msgInfoDTO.setResource("payments/bulk-payments/" + paymentId);
        PowerMockito.when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        new BerlinConsentEnforcementExecutor().postProcessRequest(obapiRequestContextMock);
    }
}
