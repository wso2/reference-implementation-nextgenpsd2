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

package com.wso2.openbanking.berlin.consent.extensions.util;

import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.berlin.common.enums.ScaApproachEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;

/**
 * Class with util classes for tests.
 */
public class TestUtil {

    public static void assertImplicitConsentResponse(ConsentManageData paymentConsentManageData,
                                              AuthorizationResource authorizationResource, boolean isImplicit,
                                              MockHttpServletRequest mockHttpServletRequest,
                                              MockHttpServletResponse mockHttpServletResponse) {

        Assert.assertNotNull(paymentConsentManageData.getResponsePayload());
        Assert.assertTrue(paymentConsentManageData.getResponsePayload() instanceof JSONObject);

        JSONObject response = (JSONObject) paymentConsentManageData.getResponsePayload();

        Assert.assertEquals(ResponseStatus.CREATED, paymentConsentManageData.getResponseStatus());
        Assert.assertEquals(mockHttpServletResponse.getHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH).toString(),
                ScaApproachEnum.REDIRECT.toString());
        Assert.assertNotNull(mockHttpServletResponse
                .getHeader(ConsentExtensionConstants.LOCATION_PROPER_CASE_HEADER).toString());
        Assert.assertEquals(TransactionStatusEnum.RCVD.toString(),
                response.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        Assert.assertNotNull(response.get(ConsentExtensionConstants.PAYMENT_ID));
        Assert.assertNotNull(response.get(ConsentExtensionConstants.LINKS));

        JSONObject linksObject = (JSONObject) response.get(ConsentExtensionConstants.LINKS);

        Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.SELF));

        if (isImplicit) {
            Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.SCA_STATUS));
            // The same authorization ID in scaLink in links object confirms the creation of the authorization resource
            // in implicit flow
            Assert.assertTrue(StringUtils.contains(linksObject.getAsString(ConsentExtensionConstants.SCA_STATUS),
                    authorizationResource.getAuthorizationID()));
        } else {
            Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION));
        }
    }
}
