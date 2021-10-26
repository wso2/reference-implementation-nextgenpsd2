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

package com.wso2.openbanking.berlin.consent.extensions.test.common;

import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.utils.ScaApproach;
import com.wso2.openbanking.berlin.common.utils.ScaApproachEnum;
import com.wso2.openbanking.berlin.common.utils.ScaMethod;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * This contains unit tests for LinksConstructor class.
 */
@PrepareForTest({CommonConfigParser.class})
public class LinksConstructorTests {

    private ScaApproach redirectScaApproach;
    private ScaApproach decoupledScaApproach;
    private List<ScaMethod> currentScaMethods = new ArrayList<>();
    private ScaMethod redirectScaMethod = new ScaMethod();
    private ScaMethod decoupledScaMethod = new ScaMethod();

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);

        redirectScaApproach = new ScaApproach();
        redirectScaApproach.setApproach(ScaApproachEnum.REDIRECT);
        redirectScaApproach.setDefault(true);

        decoupledScaApproach = new ScaApproach();
        decoupledScaApproach.setApproach(ScaApproachEnum.REDIRECT);
        decoupledScaApproach.setDefault(true);

        redirectScaMethod.setAuthenticationType("SMS_OTP");
        redirectScaMethod.setVersion("1.0");
        redirectScaMethod.setAuthenticationMethodId("sms-otp");
        redirectScaMethod.setName("SMS OTP on Mobile");
        redirectScaMethod.setMappedApproach(ScaApproachEnum.REDIRECT);
        redirectScaMethod.setDescription("SMS based one time password");
        redirectScaMethod.setDefault(true);

        decoupledScaMethod.setAuthenticationType("PUSH_OTP");
        decoupledScaMethod.setVersion("1.0");
        decoupledScaMethod.setAuthenticationMethodId("push-otp");
        decoupledScaMethod.setName("PUSH OTP on Mobile app");
        decoupledScaMethod.setMappedApproach(ScaApproachEnum.DECOUPLED);
        decoupledScaMethod.setDescription("Mobile app notification");
        decoupledScaMethod.setDefault(false);
    }

    @BeforeMethod
    public void initMethod() {

        CommonConfigParser commonConfigParserMock = Mockito.mock(CommonConfigParser.class);
        Mockito.doReturn("https://localhost:8243/.well-known/openid-configuration")
                .when(commonConfigParserMock).getOauthMetadataEndpoint();

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testInitiationLinksWithImplicitRedirect() {
        currentScaMethods.add(redirectScaMethod);
        currentScaMethods.add(decoupledScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(false, redirectScaApproach,
                currentScaMethods, "v1/consents", "received", "received", "1234",
                "5678");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SCA_OAUTH));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SCA_STATUS));
    }

    @Test
    public void testInitiationLinksWithExplicitRedirect() {
        currentScaMethods.add(redirectScaMethod);
        currentScaMethods.add(decoupledScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(true, redirectScaApproach,
                currentScaMethods, "v1/consents", "received", null, "1234",
                null);

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION));
    }

    @Test
    public void testInitiationLinksWithImplicit() {
        currentScaMethods.add(redirectScaMethod);
        currentScaMethods.add(decoupledScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(false, new ScaApproach(),
                currentScaMethods, "v1/consents", "received", "received", "1234",
                "5678");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELECT_AUTH_METHOD));
    }

    @Test
    public void testInitiationLinksWithExplicit() {
        currentScaMethods.add(redirectScaMethod);
        currentScaMethods.add(decoupledScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(true, new ScaApproach(),
                currentScaMethods, "v1/consents", "received", "received", "1234",
                "5678");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.START_AUTH_WITH_AUTH_METHOD_SELECTION));
    }

}
