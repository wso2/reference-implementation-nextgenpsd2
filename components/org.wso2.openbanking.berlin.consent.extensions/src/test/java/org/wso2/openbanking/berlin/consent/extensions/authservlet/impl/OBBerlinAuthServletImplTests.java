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

package org.wso2.openbanking.berlin.consent.extensions.authservlet.impl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class OBBerlinAuthServletImplTests {

    private static OBBerlinAuthServletImpl obBerlinAuthServlet;
    private static MockHttpServletRequest mockHttpServletRequest;
    private static ResourceBundle resourceBundleMock;

    @BeforeClass
    public void initClass() {

        obBerlinAuthServlet = new OBBerlinAuthServletImpl();
        mockHttpServletRequest = new MockHttpServletRequest();
        resourceBundleMock = Mockito.mock(ResourceBundle.class);
    }

    @Test
    public void testUpdateRequestAttributeForPayments() {

        JSONArray consentDetails = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        JSONObject requestedData1 = new JSONObject();
        requestedData1.put(ConsentExtensionConstants.TITLE, ConsentExtensionConstants.DEBTOR_ACCOUNT_TITLE);

        JSONArray dataSimpleArray = new JSONArray();
        dataSimpleArray.put(0, "sample_data");
        requestedData1.put(ConsentExtensionConstants.DATA_SIMPLE, dataSimpleArray);

        consentDetails.put(0, requestedData1);

        JSONObject consentData = new JSONObject();
        consentData.put(ConsentExtensionConstants.CONSENT_DETAILS, consentDetails);
        consentData.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, new JSONObject().put("iban", "123"));

        jsonObject.put(ConsentExtensionConstants.CONSENT_DATA, consentData);
        jsonObject.put(ConsentExtensionConstants.TYPE, ConsentTypeEnum.PAYMENTS.toString());
        jsonObject.put(ConsentExtensionConstants.AUTH_TYPE, AuthTypeEnum.AUTHORISATION.toString());

        Map<String, Object> attributesMap =  obBerlinAuthServlet.updateRequestAttribute(mockHttpServletRequest,
                jsonObject, resourceBundleMock);

        Assert.assertNotNull(attributesMap);
        Assert.assertNotNull(attributesMap.get(ConsentExtensionConstants.DATA_REQUESTED));
        Map<String, List<String>> dataRequestedMap = (Map<String, List<String>>) attributesMap
                .get(ConsentExtensionConstants.DATA_REQUESTED);
        Assert.assertTrue(dataRequestedMap.containsKey(ConsentExtensionConstants.DEBTOR_ACCOUNT_TITLE));
        Assert.assertNotNull(dataRequestedMap.get(ConsentExtensionConstants.DEBTOR_ACCOUNT_TITLE));
        ArrayList<String> arrayList =
                (ArrayList<String>) dataRequestedMap.get(ConsentExtensionConstants.DEBTOR_ACCOUNT_TITLE);
        Assert.assertEquals(arrayList.get(0), "sample_data");


    }
}
