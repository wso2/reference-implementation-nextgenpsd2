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

package org.wso2.openbanking.berlin.gateway.throttling;

import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import com.wso2.openbanking.accelerator.gateway.throttling.OBThrottlingExtensionImpl;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

@PrepareForTest({CommonConfigParser.class})
@PowerMockIgnore({"net.minidev.*", "jdk.internal.reflect.*"})
public class CustomThrottleDataPublisherImplTests extends PowerMockTestCase {

    private static final String SAMPLE_IP_ADDRESS = "192.168.1.1";
    private static final String SAMPLE_CONSENT_ID = "1234";
    private static final String SAMPLE_ACCOUNT_ID = "1111";
    public static final String SAMPLE_CLIENT_ID = "client_id";

    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String CONSENT_ID_HEADER = "Consent-ID";
    private static final String ACCOUNT_ID = "accountId";
    private static final String CONSENT_ID = "consentId";
    private static final String CONSUMER_KEY = "consumerKey";

    CommonConfigParser commonConfigParserMock;
    CustomThrottleDataPublisherImpl customThrottleDataPublisherImplMock;
    OBThrottlingExtensionImpl obThrottlingExtension;
    RequestContextDTO requestContextDTO;
    APIRequestInfoDTO apiRequestInfoDTO;
    MsgInfoDTO msgInfoDTO;

    @BeforeClass
    public void beforeClass() {

        customThrottleDataPublisherImplMock = new CustomThrottleDataPublisherImpl();
        GatewayDataHolder.getInstance().setThrottleDataPublisher(customThrottleDataPublisherImplMock);
        obThrottlingExtension = new OBThrottlingExtensionImpl();
        requestContextDTO = Mockito.mock(RequestContextDTO.class);
        apiRequestInfoDTO = Mockito.mock(APIRequestInfoDTO.class);
        msgInfoDTO = Mockito.mock(MsgInfoDTO.class);

        commonConfigParserMock = PowerMockito.mock(CommonConfigParser.class);
        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
    }

    @Test
    public void testGetCustomPropertiesWithOutCustomerIp() {

        Map<String, String> headerMap = new HashMap<>();
        
        headerMap.put(CONSENT_ID_HEADER, SAMPLE_CONSENT_ID);
        Mockito.when(requestContextDTO.getMsgInfo()).thenReturn(msgInfoDTO);
        doReturn(true).when(commonConfigParserMock).isFrequencyPerDayThrottlingEnabled();
        Mockito.when(msgInfoDTO.getHeaders()).thenReturn(headerMap);
        Mockito.when(msgInfoDTO.getElectedResource()).thenReturn("/accounts/{account-id}");
        Mockito.when(msgInfoDTO.getResource()).thenReturn("/accounts/" + SAMPLE_ACCOUNT_ID);
        Mockito.when(requestContextDTO.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        Mockito.when(apiRequestInfoDTO.getConsumerKey()).thenReturn(SAMPLE_CLIENT_ID);
        ExtensionResponseDTO extensionResponseDTO = obThrottlingExtension.preProcessRequest(requestContextDTO);

        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(ACCOUNT_ID), SAMPLE_ACCOUNT_ID);
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(CONSENT_ID), SAMPLE_CONSENT_ID);
        Assert.assertEquals(extensionResponseDTO.getCustomProperty().get(CONSUMER_KEY), SAMPLE_CLIENT_ID);
    }
}
