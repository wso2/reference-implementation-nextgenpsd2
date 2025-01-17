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

package org.wso2.openbanking.berlin.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for Berlin Consent Manage Handler.
 */
@PrepareForTest({CommonConfigParser.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class BerlinConsentManageHandlerTests extends PowerMockTestCase {

    BerlinConsentManageHandler berlinConsentManageHandler;
    @Mock
    ConsentManageData consentManageDataMock;

    @Mock
    CommonConfigParser commonConfigParserMock;

    private static Map<String, String> headersMap;
    private JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    @BeforeClass
    public void initTest() {
        berlinConsentManageHandler = new BerlinConsentManageHandler();
        consentManageDataMock = mock(ConsentManageData.class);

        headersMap = new HashMap<>();
        headersMap.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER, UUID.randomUUID().toString());

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
    }

    @BeforeMethod
    public void initMethod() {

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandlePostWithValidConsentDataWithoutPath() throws ParseException {

        JSONObject validPayload = (JSONObject) parser.parse(TestPayloads.VALID_PAYMENTS_PAYLOAD);
        doReturn(validPayload).when(consentManageDataMock).getPayload();
        doReturn(headersMap).when(consentManageDataMock).getHeaders();

        berlinConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandlePostWithValidConsentDataWithoutPayload() throws ParseException {

        doReturn(null).when(consentManageDataMock).getPayload();
        doReturn(headersMap).when(consentManageDataMock).getHeaders();

        berlinConsentManageHandler.handlePost(consentManageDataMock);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandlePostWithInvalidPayload() throws ParseException {

        doReturn("invalid payload").when(consentManageDataMock).getPayload();
        doReturn(headersMap).when(consentManageDataMock).getHeaders();

        berlinConsentManageHandler.handlePost(consentManageDataMock);
    }
}
