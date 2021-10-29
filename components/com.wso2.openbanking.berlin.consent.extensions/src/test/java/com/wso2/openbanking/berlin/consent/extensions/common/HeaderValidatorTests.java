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

package com.wso2.openbanking.berlin.consent.extensions.common;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.common.utils.ScaApproach;
import com.wso2.openbanking.berlin.common.utils.ScaApproachEnum;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This contains unit tests for HeaderValidator class.
 */
@PrepareForTest({CommonUtil.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*"})
public class HeaderValidatorTests {

    private ScaApproach redirectScaApproach;
    private ScaApproach decoupledScaApproach;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);

        redirectScaApproach = new ScaApproach();
        redirectScaApproach.setApproach(ScaApproachEnum.REDIRECT);

        decoupledScaApproach = new ScaApproach();
        decoupledScaApproach.setApproach(ScaApproachEnum.DECOUPLED);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testValidPsuIpAddress() {

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER, "192.168.100.10");

        HeaderValidator.validatePsuIpAddress(headers);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testEmptyPsuIpAddress() {

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.PSU_IP_ADDRESS_HEADER, "");

        HeaderValidator.validatePsuIpAddress(headers);
    }

    @Test
    public void testValidXRequestId() {

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER, "1b91e649-3d06-4e16-ada7-bf5af2136b44");

        HeaderValidator.validateXRequestId(headers);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testInvalidXRequestId() {

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER, "abc");

        HeaderValidator.validateXRequestId(headers);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testEmptyXRequestId() {

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER, "");

        HeaderValidator.validateXRequestId(headers);
    }

    @Test
    public void testIsTppExplicitAuthorisationPreferred() {

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER, "true");

        Assert.assertTrue(HeaderValidator.isTppExplicitAuthorisationPreferred(headers));

        headers.clear();
        headers.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER, "false");

        Assert.assertFalse(HeaderValidator.isTppExplicitAuthorisationPreferred(headers));

        headers.clear();
        headers.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER, "");

        Assert.assertFalse(HeaderValidator.isTppExplicitAuthorisationPreferred(headers));

        headers.clear();

        Assert.assertFalse(HeaderValidator.isTppExplicitAuthorisationPreferred(headers));
    }

    @Test
    public void testIsTppRedirectPreferred() {

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        Assert.assertEquals(HeaderValidator.isTppRedirectPreferred(headers), Optional.of(Boolean.TRUE));

        headers.clear();
        headers.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "false");

        Assert.assertEquals(HeaderValidator.isTppRedirectPreferred(headers), Optional.of(Boolean.FALSE));

        headers.clear();
        headers.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "");

        Assert.assertEquals(HeaderValidator.isTppRedirectPreferred(headers), Optional.of(Boolean.FALSE));

        headers.clear();

        Assert.assertEquals(HeaderValidator.isTppRedirectPreferred(headers), Optional.empty());
    }

    @Test
    public void testMandateHeaderPresent() {

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER, "1b91e649-3d06-4e16-ada7-bf5af2136b44");

        HeaderValidator.mandateHeader(headers, ConsentExtensionConstants.X_REQUEST_ID_HEADER);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testMandateHeaderMissing() {

        HeaderValidator.mandateHeader(new HashMap<>(), ConsentExtensionConstants.X_REQUEST_ID_HEADER);
    }

    @Test
    public void testValidateTppRedirectPreferredHeaderValidRedirect() {

        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.when(CommonUtil.getScaApproach(ScaApproachEnum.REDIRECT)).thenReturn(redirectScaApproach);

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        HeaderValidator.validateTppRedirectPreferredHeader(headers);
    }

    @Test
    public void testValidateTppRedirectPreferredHeaderValidDecoupled() {

        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.when(CommonUtil.getScaApproach(ScaApproachEnum.DECOUPLED)).thenReturn(decoupledScaApproach);

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "false");

        HeaderValidator.validateTppRedirectPreferredHeader(headers);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testValidateTppRedirectPreferredHeaderInvalidRedirect() {

        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.when(CommonUtil.getScaApproach(ScaApproachEnum.REDIRECT)).thenReturn(null);

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "true");

        HeaderValidator.validateTppRedirectPreferredHeader(headers);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testValidateTppRedirectPreferredHeaderInvalidDecoupled() {

        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.when(CommonUtil.getScaApproach(ScaApproachEnum.DECOUPLED)).thenReturn(null);

        Map<String, String> headers = new HashMap<>();
        headers.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER, "false");

        HeaderValidator.validateTppRedirectPreferredHeader(headers);
    }
}
