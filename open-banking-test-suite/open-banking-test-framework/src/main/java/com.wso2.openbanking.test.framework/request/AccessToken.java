/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.test.framework.request;

import com.wso2.openbanking.test.framework.TestSuite;
import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import com.wso2.openbanking.test.framework.model.ApplicationAccessTokenDto;
import com.wso2.openbanking.test.framework.model.UserAccessTokenDto;
import com.wso2.openbanking.test.framework.util.ConfigParser;
import com.wso2.openbanking.test.framework.util.TestConstants;
import io.restassured.response.Response;

/**
 * Class that contain functionality required for Access token.
 */
public class AccessToken {

    /**
     * Generate application access token with default values.
     *
     * @return Response of RestAssured request
     * @throws TestFrameworkException When failed to obtain application access token
     */
    public static Response getApplicationAccessToken() throws TestFrameworkException {

        ApplicationAccessTokenDto applicationAccessTokenDto = new ApplicationAccessTokenDto();
        return TestSuite.buildRequest().contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
            .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
            .relaxedHTTPSValidation()
            .body(applicationAccessTokenDto.getPayload())
            .post(TestConstants.TOKEN_ENDPOINT);
    }

    /**
     * Generate application access token when DTO is provided.
     *
     * @return Response of RestAssured request
     * @throws TestFrameworkException When failed to obtain application access token
     */
    public static Response getApplicationAccessToken(
        ApplicationAccessTokenDto applicationAccessTokenDto) throws TestFrameworkException {

        return getApplicationAccessToken(applicationAccessTokenDto, null);
    }

    /**
     * Generate application access token when DTO is provided.
     * If the clientId is provided, it will be used to generate the access token.
     *
     * @return Response of RestAssured request
     * @throws TestFrameworkException When failed to obtain application access token
     */
    public static Response getApplicationAccessToken(
          ApplicationAccessTokenDto applicationAccessTokenDto, String clientId)
        throws TestFrameworkException {

        String payload = applicationAccessTokenDto.getPayload(clientId);
        return TestSuite.buildRequest().contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
            .body(payload)
            .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
            .post(TestConstants.TOKEN_ENDPOINT);
    }

    /**
     * Generate application access token when payload is provided.
     * This can be used to send a custom JSON Body with
     * the request
     *
     * @return Response of RestAssured request
     * @throws TestFrameworkException When failed to obtain application access token
     */
    public static Response getApplicationAccessToken(String payload)
        throws TestFrameworkException {

        return TestSuite.buildRequest().contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
                .body(payload)
                .post(TestConstants.TOKEN_ENDPOINT);
    }

    /**
     * Generate user access token when code is provided.
     *
     * @return Response of RestAssured request
     * @throws TestFrameworkException When failed to obtain user access token
     */
    public static Response getUserAccessToken(String code) throws TestFrameworkException {

        UserAccessTokenDto userAccessTokenDto = new UserAccessTokenDto();
        userAccessTokenDto.setCode(code);
        return TestSuite.buildRequest().contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
                .body(userAccessTokenDto.getPayload())
                .post(TestConstants.TOKEN_ENDPOINT);
    }

    /**
     * Generate user access token when code and DTO is provided.
     *
     * @return Response of RestAssured request
     * @throws TestFrameworkException When failed to obtain user access token
     */
    public static Response getUserAccessToken(UserAccessTokenDto userAccessTokenDto, String code)
        throws TestFrameworkException {

        userAccessTokenDto.setCode(code);
        return TestSuite.buildRequest().contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(ConfigParser.getInstance().getAuthorisationServerURL())
                .body(userAccessTokenDto.getPayload())
                .post(TestConstants.TOKEN_ENDPOINT);
    }
}
