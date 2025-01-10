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

import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.nimbusds.oauth2.sdk.id.ClientID
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.util.AppConfigReader
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.model.UserAccessTokenForClientCredentialGrantTypeDTO
import com.wso2.openbanking.toolkit.berlin.integration.test.nonregulatoryapp.model.UserAccessTokenForPasswordGrantTypeDTO
import io.restassured.RestAssured
import io.restassured.response.Response
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.annotations.BeforeClass

import java.nio.charset.Charset

/**
 * Abstract classes for Non Regulatory FLow
 */
abstract class AbstractNonRegulatoryFlow {

    OAuthAuthorization auth
    String code
    String userAccessToken
    BrowserAutomation.AutomationContext automation
    Response apiResponse
    String path = NonRegulatoryConstants.API_PATH
    WebDriverWait wait
    ConfigParser config

    String payload = """
            {
                "customerName": "string",
                "delivered": true,
                "address": "string",
                "pizzaType": "string",
                "creditCardNumber": "string",
                "quantity": 0,
                "orderId": "string"
            }
""".stripIndent()


    @BeforeClass
    void setup() {

        TestSuite.init()
        config = ConfigParser.getInstance()
    }

    void doAuthorization() {
        //Auth flow
        auth = new OAuthAuthorization(NonRegulatoryConstants.SCOPES.OPEN_ID)
        automation = new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(auth.authoriseUrl))
                .addStep { driver, context ->
                    driver.manage().window().maximize()
                    if (driver.findElement(By.id("approve"))) {
                        driver.findElement(By.id("approve")).click()
                    }
                    if (driver.findElements(By.id("consent_select_all")).size() != 0) {
                        driver.findElement(By.id("consent_select_all")).click()
                    }
                }
                .execute()

        //Get Code from URL
        code = TestUtil.getHybridCodeFromUrl(automation.currentUrl.get())

    }

    void getUserTokenFromAuthorizationCode() {

        AuthorizationCode grant = new AuthorizationCode(code)
        URI callbackUri = new URI(config.getNonRegulatoryRedirectURL())
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(grant, callbackUri)

        ClientID clientID = new ClientID(config.getNonRegulatoryClientId())

        URI tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")

        TokenRequest request = new TokenRequest(tokenEndpoint, clientID, codeGrant)

        HTTPRequest httpRequest = request.toHTTPRequest()

        def authToken = "${config.getNonRegulatoryClientId()}:${config.getNonRegulatoryClientSecret()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def response = TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(httpRequest.query)
                .post(tokenEndpoint.toString())

        HTTPResponse httpResponse = new HTTPResponse(response.statusCode())
        httpResponse.setContentType(response.contentType())
        httpResponse.setContent(response.getBody().print())

        userAccessToken = TokenResponse.parse(httpResponse).toSuccessResponse().tokens.accessToken

    }

    void getUserTokenFromPasswordGrantType() {

        def tokenDTO = new UserAccessTokenForPasswordGrantTypeDTO()
        tokenDTO.setScope(NonRegulatoryConstants.SCOPES_OPEN_ID)
        tokenDTO.setGrantType(NonRegulatoryConstants.PASSWORD_GRANT)

        URI tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")

        def authToken = "${config.getNonRegulatoryClientId()}:${config.getNonRegulatoryClientSecret()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .queryParam(TestConstants.CLIENT_ID_KEY, config.getNonRegulatoryClientId())
                .body(tokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        HTTPResponse httpResponse = new HTTPResponse(tokenResponse.statusCode())
        httpResponse.setContentType(tokenResponse.contentType())
        httpResponse.setContent(tokenResponse.getBody().print())

        userAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

    }

    void getUserTokenFromClientCredentialsGrantType() {

        def userTokenDTO = new UserAccessTokenForClientCredentialGrantTypeDTO()
        userTokenDTO.setScope(NonRegulatoryConstants.SCOPES_OPEN_ID)
        userTokenDTO.setGrantType(TestConstants.CLIENT_CREDENTIALS)

        URI tokenEndpoint = new URI("${config.getAuthorisationServerURL()}/oauth2/token")

        def authToken = "${config.getNonRegulatoryClientId()}:${config.getNonRegulatoryClientSecret()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        RestAssured.baseURI = ConfigParser.getInstance().getBaseURL()
        def tokenResponse =  TestSuite.buildBasicRequestWithoutTlsContext()
                .contentType(TestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, basicHeader)
                .body(userTokenDTO.getPayload())
                .post(tokenEndpoint.toString())

        HTTPResponse httpResponse = new HTTPResponse(tokenResponse.statusCode())
        httpResponse.setContentType(tokenResponse.contentType())
        httpResponse.setContent(tokenResponse.getBody().print())

        userAccessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

    }

    void doApiInvocation() {
        String baseURI = config.getBaseURL()

        URI apiPath = new URI(baseURI.concat(path))

        apiResponse = NonRegulatoryRequestBuilder.buildBasicRequest(userAccessToken)
                .body(payload)
                .post(apiPath.toString())
    }

    URI getAPIRequestPath() {

        String baseURI = config.getBaseURL()

        if ((AppConfigReader.getClientId()).contains("-")) {
            return new URI(baseURI.concat(NonRegulatoryConstants.BG_ACCOUNTS_PATH))
        } else {
            return new URI(baseURI.concat(NonRegulatoryConstants.UK_ACCOUNTS_PATH))
        }

    }
}
