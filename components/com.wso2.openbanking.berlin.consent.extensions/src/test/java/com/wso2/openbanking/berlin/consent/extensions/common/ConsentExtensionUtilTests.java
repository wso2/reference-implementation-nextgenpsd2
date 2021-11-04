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

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for consent extension utils.
 */
public class ConsentExtensionUtilTests {

    @Test
    public void testGetServiceDifferentiatingRequestPathNullScenario() {

        Assert.assertEquals("", ConsentExtensionUtil.getServiceDifferentiatingRequestPath(null));
    }

    @Test
    public void testGetServiceDifferentiatingRequestPathCOF() {

        String cofPath = "consents/confirmation-of-funds";
        Assert.assertEquals(cofPath.split("/")[1],
                ConsentExtensionUtil.getServiceDifferentiatingRequestPath(cofPath));
    }

    @Test
    public void testGetServiceDifferentiatingRequestPathWithOnePathElement() {

        String accountsPath = "consents";
        Assert.assertEquals(accountsPath.split("/")[0],
                ConsentExtensionUtil.getServiceDifferentiatingRequestPath(accountsPath));
    }

    @Test
    public void testGetServiceDifferentiatingRequestPath() {

        String paymentsPath = "payments/sepa-credit-transfers";
        Assert.assertEquals(paymentsPath.split("/")[0],
                ConsentExtensionUtil.getServiceDifferentiatingRequestPath(paymentsPath));
    }

    @Test
    public void testEnsureSuperTenantDomain() {

        String samplePSUId = "admin@wso2.com";
        String psuIdWithSuperTenantDomain = "admin@wso2.com@carbon.super";
        String emptyPSUId = "";

        Assert.assertTrue(StringUtils.contains(ConsentExtensionUtil.appendSuperTenantDomain(samplePSUId),
                "@carbon.super"));
        Assert.assertEquals(psuIdWithSuperTenantDomain,
                ConsentExtensionUtil.appendSuperTenantDomain(psuIdWithSuperTenantDomain));
        Assert.assertNull(ConsentExtensionUtil.appendSuperTenantDomain(emptyPSUId));
    }
}
