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

package org.wso2.openbanking.berlin.gateway.failure;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.report.ImmutableValidationReport;
import com.atlassian.oai.validator.report.ValidationReport;
import graphql.Assert;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.gateway.utils.GatewayConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Unit tests for GatewayFailureResponseCreationMediator.
 */
@PrepareForTest({JsonUtil.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class GatewayFailureResponseCreationMediatorTests extends PowerMockTestCase {

    private final GatewayFailureResponseCreationMediator mediator = new GatewayFailureResponseCreationMediator();

    private static final String CLIENT_USER_AGENT = "User-Agent";
    private static final String HTTP_RESPONSE_STATUS_CODE = "HTTP_RESPONSE_STATUS_CODE";
    private MessageContext messageContextMock;

    @BeforeClass
    public void initClass() {

        messageContextMock = Mockito.mock(MessageContext.class);
    }

    @Test
    public void testMediate() throws Exception {

        MessageContext messageContext = getData();
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 900806);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testMediateForGeneralAuthFailure() throws Exception {

        MessageContext messageContext = getData();
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 900900);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testMediateForGeneralMethodNotAllowed() throws Exception {

        MessageContext messageContext = getData();
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 405);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testMediateForSchemaValidationError() throws Exception {

        MessageContext messageContext = getData();
        ImmutableValidationReport mockReport = Mockito.mock(ImmutableValidationReport.class);
        ValidationReport.Message mockMessage = Mockito.mock(ValidationReport.Message.class);
        ValidationReport.MessageContext mockContext = Mockito.mock(ValidationReport.MessageContext.class);
        Parameter mockParameter = Mockito.mock(Parameter.class);
        List<ValidationReport.Message> mockMessagesList = new ArrayList<>();
        mockMessagesList.add(mockMessage);
        messageContext.setProperty(GatewayConstants.SCHEMA_VALIDATION_REPORT_IDENTIFIER, mockReport);
        Mockito.when(mockReport.getMessages()).thenReturn(mockMessagesList);
        Mockito.when(mockMessage.getMessage()).thenReturn("Instance value (\"cat\") not found in enum (possible " +
                "values: [\"information\",\"booked\",\"pending\",\"both\"])");
        Mockito.when(mockMessage.getKey()).thenReturn("validation.request.parameter.schema.enum");
        Mockito.when(mockMessage.getContext()).thenReturn(Optional.of(mockContext));
        Mockito.when(mockContext.getParameter()).thenReturn(Optional.of(mockParameter));
        Mockito.when(mockParameter.getIn()).thenReturn("Query");
        Mockito.when(mockParameter.getName()).thenReturn("bookingStatus");
        messageContext.setProperty(GatewayConstants.ERROR_DETAIL,
                GatewayConstants.SCHEMA_VALIDATION_FAILURE_IDENTIFIER);
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 400);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testMediateForResourceFailureResponse() throws Exception {

        MessageContext messageContext = getData();
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 404);
        messageContext.setProperty(GatewayConstants.ERROR_DETAIL, null);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testMediateForPaymentInitiationSchemaValidationError() throws Exception {

        MessageContext messageContext = getData();
        ImmutableValidationReport mockReport = Mockito.mock(ImmutableValidationReport.class);
        ValidationReport.Message mockMessage = Mockito.mock(ValidationReport.Message.class);
        ValidationReport.MessageContext mockContext = Mockito.mock(ValidationReport.MessageContext.class);
        Parameter mockParameter = Mockito.mock(Parameter.class);
        List<ValidationReport.Message> mockMessagesList = new ArrayList<>();
        mockMessagesList.add(mockMessage);
        messageContext.setProperty(GatewayConstants.SCHEMA_VALIDATION_REPORT_IDENTIFIER, mockReport);
        Mockito.when(mockReport.getMessages()).thenReturn(mockMessagesList);
        Mockito.when(mockMessage.getMessage()).thenReturn("Instance failed to match exactly one schema " +
                "(matched 0 out of 3)");
        Mockito.when(mockMessage.getKey()).thenReturn("validation.request.body.schema.oneOf");
        Mockito.when(mockMessage.getContext()).thenReturn(Optional.of(mockContext));
        Mockito.when(mockContext.getParameter()).thenReturn(Optional.of(mockParameter));
        Mockito.when(mockParameter.getIn()).thenReturn("Query");
        Mockito.when(mockParameter.getName()).thenReturn("bookingStatus");
        Mockito.when(mockContext.getRequestPath()).thenReturn(Optional.of("/payments/sepa-credit-transfers/"));
        Mockito.when(mockContext.getRequestMethod()).thenReturn(Optional.of(Request.Method.POST));

        ArrayList<String> additionalErrorMsgList = new ArrayList<>();
        additionalErrorMsgList.add("/oneOf/0: Object instance has properties which are not allowed by the schema: " +
                "[\"dayOfExecution\",\"endDate\",\"executionRule\",\"frequency\",\"startDate\"]");
        additionalErrorMsgList.add("/oneOf/1: Instance value (\"Biweekly\") not found in enum (possible values: " +
                "[\"Daily\",\"Weekly\",\"EveryTwoWeeks\",\"Monthly\",\"EveryTwoMonths\",\"Quarterly\",\"SemiAnnual\"," +
                "\"Annual\",\"MonthlyVariable\"])");
        additionalErrorMsgList.add("/oneOf/2: Object has missing required properties ([\"payments\"])");
        Mockito.when(mockMessage.getAdditionalInfo()).thenReturn(additionalErrorMsgList);

        messageContext.setProperty(GatewayConstants.ERROR_DETAIL,
                GatewayConstants.SCHEMA_VALIDATION_FAILURE_IDENTIFIER);
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 400);

        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testMediateForPeriodicPaymentInitiationSchemaValidationError() throws Exception {

        MessageContext messageContext = getData();
        ImmutableValidationReport mockReport = Mockito.mock(ImmutableValidationReport.class);
        ValidationReport.Message mockMessage = Mockito.mock(ValidationReport.Message.class);
        ValidationReport.MessageContext mockContext = Mockito.mock(ValidationReport.MessageContext.class);
        Parameter mockParameter = Mockito.mock(Parameter.class);
        List<ValidationReport.Message> mockMessagesList = new ArrayList<>();
        mockMessagesList.add(mockMessage);
        messageContext.setProperty(GatewayConstants.SCHEMA_VALIDATION_REPORT_IDENTIFIER, mockReport);
        Mockito.when(mockReport.getMessages()).thenReturn(mockMessagesList);
        Mockito.when(mockMessage.getMessage()).thenReturn("Instance failed to match exactly one schema " +
                "(matched 0 out of 3)");
        Mockito.when(mockMessage.getKey()).thenReturn("validation.request.body.schema.oneOf");
        Mockito.when(mockMessage.getContext()).thenReturn(Optional.of(mockContext));
        Mockito.when(mockContext.getParameter()).thenReturn(Optional.of(mockParameter));
        Mockito.when(mockParameter.getIn()).thenReturn("Query");
        Mockito.when(mockParameter.getName()).thenReturn("bookingStatus");
        Mockito.when(mockContext.getRequestPath()).thenReturn(Optional.of("/periodic-payments/sepa-credit-transfers/"));
        Mockito.when(mockContext.getRequestMethod()).thenReturn(Optional.of(Request.Method.POST));

        ArrayList<String> additionalErrorMsgList = new ArrayList<>();
        additionalErrorMsgList.add("/oneOf/0: Object instance has properties which are not allowed by the schema: " +
                "[\"dayOfExecution\",\"endDate\",\"executionRule\",\"frequency\",\"startDate\"]");
        additionalErrorMsgList.add("/oneOf/1: Instance value (\"Biweekly\") not found in enum (possible values: " +
                "[\"Daily\",\"Weekly\",\"EveryTwoWeeks\",\"Monthly\",\"EveryTwoMonths\",\"Quarterly\",\"SemiAnnual\"," +
                "\"Annual\",\"MonthlyVariable\"])");
        additionalErrorMsgList.add("/oneOf/2: Object has missing required properties ([\"payments\"])");
        Mockito.when(mockMessage.getAdditionalInfo()).thenReturn(additionalErrorMsgList);

        messageContext.setProperty(GatewayConstants.ERROR_DETAIL,
                GatewayConstants.SCHEMA_VALIDATION_FAILURE_IDENTIFIER);
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 400);

        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testMediateForBulkPaymentInitiationSchemaValidationError() throws Exception {

        MessageContext messageContext = getData();
        ImmutableValidationReport mockReport = Mockito.mock(ImmutableValidationReport.class);
        ValidationReport.Message mockMessage = Mockito.mock(ValidationReport.Message.class);
        ValidationReport.MessageContext mockContext = Mockito.mock(ValidationReport.MessageContext.class);
        Parameter mockParameter = Mockito.mock(Parameter.class);
        List<ValidationReport.Message> mockMessagesList = new ArrayList<>();
        mockMessagesList.add(mockMessage);
        messageContext.setProperty(GatewayConstants.SCHEMA_VALIDATION_REPORT_IDENTIFIER, mockReport);
        Mockito.when(mockReport.getMessages()).thenReturn(mockMessagesList);
        Mockito.when(mockMessage.getMessage()).thenReturn("Instance failed to match exactly one schema " +
                "(matched 0 out of 3)");
        Mockito.when(mockMessage.getKey()).thenReturn("validation.request.body.schema.oneOf");
        Mockito.when(mockMessage.getContext()).thenReturn(Optional.of(mockContext));
        Mockito.when(mockContext.getParameter()).thenReturn(Optional.of(mockParameter));
        Mockito.when(mockParameter.getIn()).thenReturn("Query");
        Mockito.when(mockParameter.getName()).thenReturn("bookingStatus");
        Mockito.when(mockContext.getRequestPath()).thenReturn(Optional.of("/bulk-payments/sepa-credit-transfers/"));
        Mockito.when(mockContext.getRequestMethod()).thenReturn(Optional.of(Request.Method.POST));

        ArrayList<String> additionalErrorMsgList = new ArrayList<>();
        additionalErrorMsgList.add("/oneOf/0: Object instance has properties which are not allowed by the schema: " +
                "[\"dayOfExecution\",\"endDate\",\"executionRule\",\"frequency\",\"startDate\"]");
        additionalErrorMsgList.add("/oneOf/1: Instance value (\"Biweekly\") not found in enum (possible values: " +
                "[\"Daily\",\"Weekly\",\"EveryTwoWeeks\",\"Monthly\",\"EveryTwoMonths\",\"Quarterly\",\"SemiAnnual\"," +
                "\"Annual\",\"MonthlyVariable\"])");
        additionalErrorMsgList.add("/oneOf/2: Object has missing required properties ([\"payments\"])");
        Mockito.when(mockMessage.getAdditionalInfo()).thenReturn(additionalErrorMsgList);

        messageContext.setProperty(GatewayConstants.ERROR_DETAIL,
                GatewayConstants.SCHEMA_VALIDATION_FAILURE_IDENTIFIER);
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 400);

        Assert.assertTrue(mediator.mediate(messageContext));
    }

    private MessageContext getData() throws Exception {

        Map<String, Object> configs = new HashMap<>();
        configs.put("DataPublishing.Enabled", "true");
        configs.put(CLIENT_USER_AGENT, "dummyAgent");

        SynapseConfiguration synapseConfigurationMock = mock(SynapseConfiguration.class);
        SynapseEnvironment synapseEnvironmentMock = mock(SynapseEnvironment.class);
        org.apache.axis2.context.MessageContext messageContextMock =
                mock(org.apache.axis2.context.MessageContext.class);
        MessageContext messageContext = new Axis2MessageContext(messageContextMock, synapseConfigurationMock,
                synapseEnvironmentMock);

        messageContext.setProperty(HTTP_RESPONSE_STATUS_CODE, 500);
        org.apache.axis2.context.MessageContext axis2MessageContext = new org.apache.axis2.context.MessageContext();
        axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, configs);
        ((Axis2MessageContext) messageContext).setAxis2MessageContext(axis2MessageContext);

        mockStatic(JsonUtil.class);
        OMElement omElementMock = mock(OMElement.class);
        when(JsonUtil.getNewJsonPayload(Mockito.anyObject(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.anyBoolean())).thenReturn(omElementMock);
        return messageContext;
    }
}
