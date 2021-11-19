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

package com.wso2.openbanking.berlin.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestPayloads;
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
