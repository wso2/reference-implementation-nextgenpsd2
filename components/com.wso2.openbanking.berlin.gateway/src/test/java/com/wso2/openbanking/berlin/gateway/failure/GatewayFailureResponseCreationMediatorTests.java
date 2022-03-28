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

package com.wso2.openbanking.berlin.gateway.failure;

import com.wso2.openbanking.berlin.gateway.utils.GatewayConstants;
import graphql.Assert;
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

import java.util.HashMap;
import java.util.Map;

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
    public void testMediateForResourceFailureResponse() throws Exception {

        MessageContext messageContext = getData();
        messageContext.setProperty(GatewayConstants.ERROR_CODE, 404);
        messageContext.setProperty(GatewayConstants.ERROR_DETAIL, null);
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
