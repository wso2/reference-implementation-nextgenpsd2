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

package com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.Non_Regulatory_Common_Tests

import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util.AbstractNonRegulatoryFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util.NonRegulatoryConstants
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Non-Regulatory Flow with Authorization Code Grant Type.
 */
class EndToEndFlowWithAuthorizationCodeGrantTypeTest extends AbstractNonRegulatoryFlow {

    @Test
    void "Non-Regulatory with Authorization Code Grant Type: Auth flow"() {
        doAuthorization()
        Assert.assertNotNull(code)
    }

    @Test(dependsOnMethods = "Non-Regulatory with Authorization Code Grant Type: Auth flow")
    void "Non-Regulatory with Authorization Code Grant Type: Token Generation"() {
        getUserTokenFromAuthorizationCode()
        Assert.assertNotNull(userAccessToken)

    }

    @Test(dependsOnMethods = "Non-Regulatory with Authorization Code Grant Type: Token Generation")
    void "TC1801011_Non-Regulatory with Authorization Code Grant Type: API Invocation"() {
        doApiInvocation()
        Assert.assertEquals(apiResponse.statusCode(), NonRegulatoryConstants.STATUS_CODE_201)

    }

}
