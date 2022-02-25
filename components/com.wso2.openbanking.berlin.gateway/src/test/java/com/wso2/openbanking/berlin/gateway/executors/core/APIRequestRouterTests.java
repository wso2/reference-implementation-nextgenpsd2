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

package com.wso2.openbanking.berlin.gateway.executors.core;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.berlin.gateway.utils.GatewayTestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for APIRequestRouter.
 */
public class APIRequestRouterTests {

    APIRequestRouter apiRequestRouter;
    OpenAPI openAPI;

    @BeforeClass
    public void beforeClass() {

        apiRequestRouter = new APIRequestRouter();
        apiRequestRouter.setExecutorMap(GatewayTestUtils.initExecutors());
        openAPI = new OpenAPI();
        openAPI.setExtensions(new HashMap<>());
    }

    @Test(priority = 1)
    public void testAccountRequestsForRouter() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        Info apiInfo = Mockito.mock(Info.class);
        openAPI.setInfo(apiInfo);
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(GatewayConstants.API_TYPE_CUSTOM_PROP, "accounts");
        contextProps.put(GatewayConstants.API_TYPE_CUSTOM_PROP, "accounts");
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(msgInfoDTO.getResource()).thenReturn("/consents");
        Assert.assertNotNull(apiRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(apiRequestRouter.getExecutorsForResponse(obapiResponseContext));
    }

    @Test(priority = 2)
    public void testSinglePaymentRequestsForRouter() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        Info apiInfo = Mockito.mock(Info.class);
        openAPI.setInfo(apiInfo);
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(GatewayConstants.API_TYPE_CUSTOM_PROP, APIRequestRouterConstants.PAYMENTS_TYPE);
        contextProps.put(GatewayConstants.API_TYPE_CUSTOM_PROP, APIRequestRouterConstants.PAYMENTS_TYPE);
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(msgInfoDTO.getResource()).thenReturn("/payments");
        Assert.assertNotNull(apiRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(apiRequestRouter.getExecutorsForResponse(obapiResponseContext));
    }

    @Test(priority = 3)
    public void testNonRegulatoryAPICall() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        Info apiInfo = Mockito.mock(Info.class);
        openAPI.setInfo(apiInfo);
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(GatewayConstants.API_TYPE_CUSTOM_PROP, APIRequestRouterConstants.PAYMENTS_TYPE);
        contextProps.put(GatewayConstants.API_TYPE_CUSTOM_PROP, APIRequestRouterConstants.PAYMENTS_TYPE);
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(msgInfoDTO.getResource()).thenReturn("/PizzaShack");
        Assert.assertEquals(apiRequestRouter.getExecutorsForRequest(obapiRequestContext).size(), 0);
        Assert.assertEquals(apiRequestRouter.getExecutorsForResponse(obapiResponseContext).size(), 0);
    }
}
