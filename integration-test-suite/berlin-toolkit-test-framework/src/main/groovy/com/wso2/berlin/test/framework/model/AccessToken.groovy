/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.berlin.test.framework.model

import com.wso2.berlin.test.framework.TestSuite
import com.wso2.berlin.test.framework.configuration.ConfigParser
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import io.restassured.response.Response

class AccessToken {



    /**
     * Generate application access token when DTO is provided.
     * If the clientId is provided, it will be used to generate the access token.
     *
     * @return Response of RestAssured request
     * @throws TestFrameworkException When failed to obtain application access token
     */
     static Response getApplicationAccessToken(
            ApplicationAccessTokenDto applicationAccessTokenDto, String clientId)
            throws TestFrameworkException {

        String payload;

        if (clientId == null) {
            payload = applicationAccessTokenDto.getPayload();
        } else {
            payload = applicationAccessTokenDto.getPayload(clientId);
        }
        return TestSuite.buildRequest().contentType(BerlinConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
                .body(payload)
                .post(BerlinConstants.TOKEN_ENDPOINT);
    }


}
