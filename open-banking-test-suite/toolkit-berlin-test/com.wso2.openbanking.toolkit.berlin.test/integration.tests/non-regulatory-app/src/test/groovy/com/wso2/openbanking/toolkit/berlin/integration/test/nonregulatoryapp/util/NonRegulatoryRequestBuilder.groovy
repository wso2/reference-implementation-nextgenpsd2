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

package com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.util

import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.util.TestConstants
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification

import java.util.logging.Logger

/**
 * Request Builder Utility for Non-PSD2 APIs.
 */
class NonRegulatoryRequestBuilder {

    static log = Logger.getLogger(NonRegulatoryRequestBuilder.class.toString())

    static RequestSpecification buildBasicRequest(String userAccessToken) {

        return TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .accept("application/json")
                .header(NonRegulatoryConstants.CHARSET, "UTF-8")

    }


}
