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

package org.wso2.openbanking.berlin.consent.extensions.common;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;

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

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testFromValueOfEnums() {

        ConsentStatusEnum.fromValue("");
        ConsentTypeEnum.fromValue("");
        AuthTypeEnum.fromValue("");
        AccessMethodEnum.fromValue("");
        TransactionStatusEnum.fromValue("");

        ConsentStatusEnum.fromValue("ABC");
        ConsentTypeEnum.fromValue("ABC");
        AuthTypeEnum.fromValue("ABC");
        AccessMethodEnum.fromValue("ABC");
        TransactionStatusEnum.fromValue("ABC");
    }
}
