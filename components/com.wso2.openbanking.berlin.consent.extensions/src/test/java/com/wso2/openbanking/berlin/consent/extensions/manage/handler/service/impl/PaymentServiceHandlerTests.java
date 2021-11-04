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

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.service.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import org.mockito.Mock;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PaymentServiceHandlerTests extends PowerMockTestCase {

    @Mock
    ConsentManageData consentManageDataMock;

    private PaymentServiceHandler paymentServiceHandler;

    @BeforeClass
    public void initTest() {
        paymentServiceHandler = new PaymentServiceHandler();
        consentManageDataMock = mock(ConsentManageData.class);
    }

    @Test (expectedExceptions = ConsentException.class)
    public void testHandlePostWithValidConsentData() {

        String invalidRequestPath = "invalid request path";
        doReturn(invalidRequestPath).when(consentManageDataMock).getRequestPath();

        paymentServiceHandler.handlePost(consentManageDataMock);
    }
}
