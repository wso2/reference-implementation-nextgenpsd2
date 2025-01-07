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

package org.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.CommonConstants;
import org.wso2.openbanking.berlin.gateway.test.TestData;
import org.wso2.openbanking.berlin.gateway.utils.GatewayTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.security.cert.CertificateException;

/**
 * BerlinConsentEnforcementExecutor tests.
 */
@PrepareForTest({CommonConfigParser.class, OpenBankingConfigParser.class, HTTPClientUtils.class, GatewayUtils.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class BerlinConsentEnforcementExecutorTests extends PowerMockTestCase {

    @Mock
    CommonConfigParser commonConfigParserMock;

    @Mock
    OpenBankingConfigParser openBankingConfigParserMock;

    @Mock
    OBAPIRequestContext obapiRequestContextMock;

    @Mock
    OBAPIResponseContext obapiResponseContextMock;

    @Mock
    MsgInfoDTO msgInfoDTOMock;

    @BeforeClass
    public void initClass() throws CertificateException, CertificateValidationException,
            java.security.cert.CertificateException, OpenBankingException {

        MockitoAnnotations.initMocks(this);
        obapiRequestContextMock = Mockito.mock(OBAPIRequestContext.class);
        obapiResponseContextMock = Mockito.mock(OBAPIResponseContext.class);
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
    public void testPostProcessRequest() {

        String paymentId = UUID.randomUUID().toString();
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod("GET");
        msgInfoDTO.setResource("payments/bulk-payments/" + paymentId);
        PowerMockito.when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        new BerlinConsentEnforcementExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testPreProcessRequestSuccessScenario() throws OpenBankingException, IOException {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod("DELETE");
        msgInfoDTO.setElectedResource("/bulk-payments/{payment-product}/{paymentId}");
        msgInfoDTO.setResource("/bulk-payments/{payment-product}/" + UUID.randomUUID());
        PowerMockito.when(obapiResponseContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        PowerMockito.when(obapiResponseContextMock.getStatusCode()).thenReturn(204);

        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
        Map<String, String> consentMgtConfigsMap = new HashMap<>();
        consentMgtConfigsMap.put(CommonConstants.PAYMENT_CONSENT_STATUS_UPDATE_URL, "url");
        PowerMockito.when(commonConfigParserMock.getConsentMgtConfigs()).thenReturn(consentMgtConfigsMap);

        CloseableHttpClient closeableHttpClientMock = Mockito.mock(CloseableHttpClient.class);
        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClient()).thenReturn(closeableHttpClientMock);

        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(GatewayUtils.getAPIMgtConfig(Mockito.anyString())).thenReturn("username");

        CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
        Mockito.when(closeableHttpClientMock.execute(Mockito.any())).thenReturn(httpResponseMock);
        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
        InputStream inputStreamMock = new ByteArrayInputStream("response".getBytes());
        Mockito.when(httpEntityMock.getContent()).thenReturn(inputStreamMock);

        new BerlinConsentEnforcementExecutor().postProcessResponse(obapiResponseContextMock);
    }
}
