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
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.enums.ScaApproachEnum;
import com.wso2.openbanking.berlin.common.models.ScaApproach;
import com.wso2.openbanking.berlin.common.models.ScaMethod;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
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
    private final List<ScaMethod> currentScaMethods = new ArrayList<>();
    private final ScaMethod redirectScaMethod = new ScaMethod();
    private final ScaMethod decoupledScaMethod = new ScaMethod();

    @BeforeClass
    public void initClass() {

        redirectScaApproach = new ScaApproach();
        redirectScaApproach.setApproach(ScaApproachEnum.REDIRECT);
        redirectScaApproach.setDefault(true);

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

        PowerMockito.mockStatic(CommonConfigParser.class);
        PowerMockito.when(CommonConfigParser.getInstance()).thenReturn(commonConfigParserMock);

        Mockito.doReturn("https://localhost:8243/.well-known/openid-configuration")
                .when(commonConfigParserMock).getOauthMetadataEndpoint();
        Mockito.doReturn("v1")
                .when(commonConfigParserMock).getApiVersion(ConsentTypeEnum.ACCOUNTS.toString());
        Mockito.doReturn("v1")
                .when(commonConfigParserMock).getApiVersion(ConsentTypeEnum.PAYMENTS.toString());
        Mockito.doReturn("v2")
                .when(commonConfigParserMock).getApiVersion(ConsentTypeEnum.FUNDS_CONFIRMATION.toString());
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testAccountsInitiationLinksWithImplicitRedirect() {
        currentScaMethods.add(redirectScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(false, redirectScaApproach,
                currentScaMethods, "consents", "1234", "5678", ConsentTypeEnum.ACCOUNTS.toString());

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        JSONObject selfLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SELF);
        Assert.assertEquals(selfLinkObject.get(ConsentExtensionConstants.HREF), "/v1/consents/1234");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        JSONObject statusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.STATUS);
        Assert.assertEquals(statusLinkObject.get(ConsentExtensionConstants.HREF), "/v1/consents/1234/status");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SCA_OAUTH));
        JSONObject scaOAuthLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SCA_OAUTH);
        Assert.assertEquals(scaOAuthLinkObject.get(ConsentExtensionConstants.HREF),
                "https://localhost:8243/.well-known/openid-configuration");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SCA_STATUS));
        JSONObject scaStatusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SCA_STATUS);
        Assert.assertEquals(scaStatusLinkObject.get(ConsentExtensionConstants.HREF),
                "/v1/consents/1234/authorisations/5678");
    }

    @Test
    public void testPaymentsInitiationLinksWithImplicitRedirect() {
        currentScaMethods.add(redirectScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(false, redirectScaApproach,
                currentScaMethods, "payments/sepa-credit-transfers", "1234", "5678",
                ConsentTypeEnum.PAYMENTS.toString());

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        JSONObject selfLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SELF);
        Assert.assertEquals(selfLinkObject.get(ConsentExtensionConstants.HREF),
                "/v1/payments/sepa-credit-transfers/1234");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        JSONObject statusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.STATUS);
        Assert.assertEquals(statusLinkObject.get(ConsentExtensionConstants.HREF),
                "/v1/payments/sepa-credit-transfers/1234/status");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SCA_OAUTH));
        JSONObject scaOAuthLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SCA_OAUTH);
        Assert.assertEquals(scaOAuthLinkObject.get(ConsentExtensionConstants.HREF),
                "https://localhost:8243/.well-known/openid-configuration");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SCA_STATUS));
        JSONObject scaStatusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SCA_STATUS);
        Assert.assertEquals(scaStatusLinkObject.get(ConsentExtensionConstants.HREF),
                "/v1/payments/sepa-credit-transfers/1234/authorisations/5678");
    }

    @Test
    public void testFundsConfirmationInitiationLinksWithImplicitRedirect() {
        currentScaMethods.add(redirectScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(false, redirectScaApproach,
                currentScaMethods, "consents/confirmation-of-funds", "1234", "5678",
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString());

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        JSONObject selfLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SELF);
        Assert.assertEquals(selfLinkObject.get(ConsentExtensionConstants.HREF),
                "/v2/consents/confirmation-of-funds/1234");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        JSONObject statusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.STATUS);
        Assert.assertEquals(statusLinkObject.get(ConsentExtensionConstants.HREF),
                "/v2/consents/confirmation-of-funds/1234/status");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SCA_OAUTH));
        JSONObject scaOAuthLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SCA_OAUTH);
        Assert.assertEquals(scaOAuthLinkObject.get(ConsentExtensionConstants.HREF),
                "https://localhost:8243/.well-known/openid-configuration");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SCA_STATUS));
        JSONObject scaStatusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SCA_STATUS);
        Assert.assertEquals(scaStatusLinkObject.get(ConsentExtensionConstants.HREF),
                "/v2/consents/confirmation-of-funds/1234/authorisations/5678");
    }

    @Test
    public void testAccountsInitiationLinksWithExplicitRedirect() {
        currentScaMethods.add(redirectScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(true, redirectScaApproach,
                currentScaMethods, "consents", "1234", null, ConsentTypeEnum.ACCOUNTS.toString());

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        JSONObject selfLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SELF);
        Assert.assertEquals(selfLinkObject.get(ConsentExtensionConstants.HREF), "/v1/consents/1234");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        JSONObject statusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.STATUS);
        Assert.assertEquals(statusLinkObject.get(ConsentExtensionConstants.HREF), "/v1/consents/1234/status");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION));
        JSONObject startAuthWithPsuIdentificationObject = (JSONObject)
                links.get(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION);
        Assert.assertEquals(startAuthWithPsuIdentificationObject.get(ConsentExtensionConstants.HREF),
                "/v1/consents/1234/authorisations");
    }

    @Test
    public void testPaymentsInitiationLinksWithExplicitRedirect() {
        currentScaMethods.add(redirectScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(true, redirectScaApproach,
                currentScaMethods, "payments/sepa-credit-transfers", "1234", null,
                ConsentTypeEnum.PAYMENTS.toString());

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        JSONObject selfLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SELF);
        Assert.assertEquals(selfLinkObject.get(ConsentExtensionConstants.HREF),
                "/v1/payments/sepa-credit-transfers/1234");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        JSONObject statusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.STATUS);
        Assert.assertEquals(statusLinkObject.get(ConsentExtensionConstants.HREF),
                "/v1/payments/sepa-credit-transfers/1234/status");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION));
        JSONObject startAuthWithPsuIdentificationObject = (JSONObject)
                links.get(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION);
        Assert.assertEquals(startAuthWithPsuIdentificationObject.get(ConsentExtensionConstants.HREF),
                "/v1/payments/sepa-credit-transfers/1234/authorisations");
    }

    @Test
    public void testFundsConfirmationInitiationLinksWithExplicitRedirect() {
        currentScaMethods.add(redirectScaMethod);

        JSONObject links = LinksConstructor.getInitiationLinks(true, redirectScaApproach,
                currentScaMethods, "consents/confirmation-of-funds", "1234", null,
                ConsentTypeEnum.FUNDS_CONFIRMATION.toString());

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.SELF));
        JSONObject selfLinkObject = (JSONObject) links.get(ConsentExtensionConstants.SELF);
        Assert.assertEquals(selfLinkObject.get(ConsentExtensionConstants.HREF),
                "/v2/consents/confirmation-of-funds/1234");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.STATUS));
        JSONObject statusLinkObject = (JSONObject) links.get(ConsentExtensionConstants.STATUS);
        Assert.assertEquals(statusLinkObject.get(ConsentExtensionConstants.HREF),
                "/v2/consents/confirmation-of-funds/1234/status");

        Assert.assertTrue(links.containsKey(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION));
        JSONObject startAuthWithPsuIdentificationObject = (JSONObject)
                links.get(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION);
        Assert.assertEquals(startAuthWithPsuIdentificationObject.get(ConsentExtensionConstants.HREF),
                "/v2/consents/confirmation-of-funds/1234/authorisations");
    }

}
