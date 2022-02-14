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

package com.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for error hanlding executor.
 */
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class ErrorHandlingExecutorTests {

    private OBAPIRequestContext obApiRequestContextMock;
    Map<String, String> addedHeaders = new HashMap<>();
    private ErrorHandlingExecutor errorHandlingExecutor;

    @BeforeClass
    public void initClass() {

        errorHandlingExecutor = Mockito.spy(ErrorHandlingExecutor.class);
        obApiRequestContextMock = Mockito.mock(OBAPIRequestContext.class);

        Mockito.when(obApiRequestContextMock.isError()).thenReturn(true);
        Mockito.when(obApiRequestContextMock.getAddedHeaders()).thenReturn(addedHeaders);
        Mockito.when(obApiRequestContextMock.getContextProperty(GatewayConstants.ERROR_STATUS_PROP)).thenReturn("401");
    }

    @Test
    public void testHandleRequestErrors() {

        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        errors.add(new OpenBankingExecutorError("CERTIFICATE_INVALID", "sampleTitle",
                "sampleMessage", "401"));

        Mockito.when(obApiRequestContextMock.getErrors()).thenReturn(errors);
        errorHandlingExecutor.preProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertEquals(obApiRequestContextMock.getContextProperty(GatewayConstants.ERROR_STATUS_PROP),
                "401");
    }

    @Test
    public void testHandleRequestErrorsPostProcess() {

        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        errors.add(new OpenBankingExecutorError("CERTIFICATE_INVALID", "sampleTitle",
                "sampleMessage", "401"));

        Mockito.when(obApiRequestContextMock.getErrors()).thenReturn(errors);
        errorHandlingExecutor.postProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertEquals(obApiRequestContextMock.getContextProperty(GatewayConstants.ERROR_STATUS_PROP),
                "401");
    }

    @Test
    public void testHandleRequestErrorsWithoutHttpCode() {

        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        errors.add(new OpenBankingExecutorError("CERTIFICATE_INVALID", "sampleTitle",
                "sampleMessage", null));

        Mockito.when(obApiRequestContextMock.getErrors()).thenReturn(errors);
        errorHandlingExecutor.preProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertEquals(obApiRequestContextMock.getContextProperty(GatewayConstants.ERROR_STATUS_PROP),
                "401");
    }

    @Test
    public void testHandleRequestErrorsWithOpenBankingError() {

        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        errors.add(new OpenBankingExecutorError("CERTIFICATE_INVALID", "sampleTitle",
                "sampleMessage", "200003"));

        Mockito.when(obApiRequestContextMock.getErrors()).thenReturn(errors);
        errorHandlingExecutor.preProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertEquals(obApiRequestContextMock.getContextProperty(GatewayConstants.ERROR_STATUS_PROP),
                "401");
    }

    @Test
    public void testHandleRequestErrorsWithOpenBankingErrorWithoutMessage() {

        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        errors.add(new OpenBankingExecutorError("200003", "sampleTitle",
                null, "401"));

        Mockito.when(obApiRequestContextMock.getErrors()).thenReturn(errors);
        errorHandlingExecutor.preProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertEquals(obApiRequestContextMock.getContextProperty(GatewayConstants.ERROR_STATUS_PROP),
                "401");
    }

    @Test
    public void testHandleRequestErrorsWithRoleInvalid() {

        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        errors.add(new OpenBankingExecutorError("200004", "sampleTitle",
                "sampleMessage", "401"));

        Mockito.when(obApiRequestContextMock.getErrors()).thenReturn(errors);
        errorHandlingExecutor.preProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertEquals(obApiRequestContextMock.getContextProperty(GatewayConstants.ERROR_STATUS_PROP),
                "401");
    }

    @Test
    public void testHandleRequestErrorsWithTokenInvalid() {

        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        errors.add(new OpenBankingExecutorError("200001", "sampleTitle",
                "sampleMessage", "401"));

        Mockito.when(obApiRequestContextMock.getErrors()).thenReturn(errors);
        errorHandlingExecutor.preProcessRequest(obApiRequestContextMock);
        Assert.assertNotNull(obApiRequestContextMock.getAddedHeaders());
        Assert.assertEquals(obApiRequestContextMock.getAddedHeaders().get(GatewayConstants.CONTENT_TYPE_TAG),
                GatewayConstants.JSON_CONTENT_TYPE);
        Assert.assertEquals(obApiRequestContextMock.getContextProperty(GatewayConstants.ERROR_STATUS_PROP),
                "401");
    }
}
