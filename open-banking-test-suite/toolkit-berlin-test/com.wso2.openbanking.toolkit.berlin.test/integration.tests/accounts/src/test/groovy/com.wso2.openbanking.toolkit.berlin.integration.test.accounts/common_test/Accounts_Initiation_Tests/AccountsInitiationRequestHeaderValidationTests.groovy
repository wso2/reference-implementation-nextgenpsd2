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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Header Validation Tests on Account Initiation Request.
 */
class AccountsInitiationRequestHeaderValidationTests extends AbstractAccountsFlow {

    def config = ConfigParser.getInstance()
    def consentPath = AccountsConstants.CONSENT_PATH
    def initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload


    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201004_Initiation request with an access token of authorization code type"() {

        //Initiation Request
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        //Generate User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Incorrect Access Token Type provided"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201006_Initiation Request With Access Token Not Bounding To Accounts Scope"() {

        //Generate application access token bound to payment scope
        String accessToken = BerlinRequestBuilder.getApplicationToken(BerlinConstants.AUTH_METHOD.PRIVATE_KEY_JWT,
                BerlinConstants.SCOPES.PAYMENTS)
        Assert.assertNotNull(accessToken)

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${accessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_403)
    }


    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201007_Initiation Request without Authorization Header"() {

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals (consentResponse.getStatusCode (), BerlinConstants.STATUS_CODE_401)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201008_Initiation Request invalid Authorization Header"() {

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Token is not valid"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201009_Initiation Request without X-Request-Id Header"() {

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("X-Request-ID header is missing in the request"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201010_Initiation Request with invalid X-Request-Id format"() {

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, "1234")
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Input string \"1234\" is not a valid UUID")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0201011_Initiation Request with TPP-Brand-LoggingInformation header"() {

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.TPP_BRAND_LOGGING_INFORMATION, "brand")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1528_Initiation request with incorrect PSU_ID when TPP-ExplicitAuthorisationPreferred set to true"() {

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1677_Initiation request with same x-request-id and same payload"() {

        def xRequestId = UUID.randomUUID().toString()
        def date = getCurrentDate()

        //Consent Initiation - First Time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId1 = TestUtil.parseResponseBody(consentResponse, "consentId").toString()

        //Consent Initiation - Second Time
        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId2 = TestUtil.parseResponseBody(consentResponse2, "consentId").toString()

        Assert.assertEquals(consentId1, consentId2)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1678_Initiation request with same x-request-id and different payload"() {

        def xRequestId = UUID.randomUUID().toString()

        //Consent Initiation - First Time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Consent Initiation - Second Time
        def initiationPayload2 = AccountsInitiationPayloads.initiationPayloadWithoutTransactionsPermission

        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "psu@wso2.com")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload2)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse2, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse2, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Payloads are not similar. Hence this is not a valid idempotent request"))
    }

    @Test (groups = ["1.3.6"])
    void "OB-211_Initiation request with different x-request-id and same payload"() {

        //Consent Initiation - First Time
        def consentResponse = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
          .header(BerlinConstants.PSU_ID, "psu@wso2.com")
          .header(BerlinConstants.PSU_TYPE, "email")
          .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
          .filter(new BerlinSignatureFilter())
          .body(initiationPayload)
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId1 = TestUtil.parseResponseBody(consentResponse, "consentId").toString()

        //Consent Initiation - Second Time
        def consentResponse2 = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
          .header(BerlinConstants.PSU_ID, "psu@wso2.com")
          .header(BerlinConstants.PSU_TYPE, "email")
          .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
          .filter(new BerlinSignatureFilter())
          .body(initiationPayload)
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .post(consentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId2 = TestUtil.parseResponseBody(consentResponse2, "consentId").toString()

        Assert.assertNotEquals(consentId1, consentId2)
    }
}
