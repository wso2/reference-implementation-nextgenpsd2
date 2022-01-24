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

package com.wso2.openbanking.berlin.consent.extensions.authservlet.impl;

import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
