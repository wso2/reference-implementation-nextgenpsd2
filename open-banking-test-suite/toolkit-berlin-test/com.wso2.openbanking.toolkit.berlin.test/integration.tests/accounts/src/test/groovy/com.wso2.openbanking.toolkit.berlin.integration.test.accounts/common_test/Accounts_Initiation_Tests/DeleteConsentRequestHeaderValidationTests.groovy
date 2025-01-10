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
import com.wso2.openbanking.test.framework.util.PsuConfigReader
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Header Validation Tests on Delete Consent Request.
 */
class DeleteConsentRequestHeaderValidationTests extends AbstractAccountsFlow {

    def consentPath = AccountsConstants.CONSENT_PATH
    def initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207006_Delete Consent With Authorization Code Type Access Token"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Authorize the Consent
        doAuthorizationFlow()
        Assert.assertNotNull(code)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)

        //Delete Consent By Passing User Access Token
        def consentDeleteResponse = BerlinRequestBuilder.buildBasicRequest(userAccessToken)
                .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue (TestUtil.parseResponseBody (consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Incorrect Access Token Type provided"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207008_Delete Consent Without X-Request-ID Header"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent without X-Request-ID header
        def consentDeleteResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).
                toString().contains("X-Request-ID header is missing in the request"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207009_Delete Consent With Invalid X-Request-ID Header"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent with invalid X-Request-ID header
        def consentDeleteResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, "1234")
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals (TestUtil.parseResponseBody (consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT)
                .toString (),"Input string \"1234\" is not a valid UUID")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207010_Delete Consent With Empty X-Request-ID Header"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent with invalid X-Request-ID header
        def consentDeleteResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, "")
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "X-Request-ID header is missing in the request")
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207011_Delete Consent Without Specifying Authorization Header"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent with invalid X-Request-ID header
        def consentDeleteResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertTrue(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString()
                .contains ("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207012_Delete Consent With Invalid Authorization Header value"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent with invalid X-Request-ID header
        def consentDeleteResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_CODE)
                .toString(), BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString()
                .contains ("Token is not valid"))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0207013_Delete Consent With Empty Authorization Header value"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent with invalid X-Request-ID header
        def consentDeleteResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ")
                .filter(new BerlinSignatureFilter())
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertTrue(TestUtil.parseResponseBody(consentDeleteResponse, BerlinConstants.TPPMESSAGE_TEXT).toString()
                .contains ("Invalid Credentials. Make sure your API invocation call has a header: 'Authorization"))
    }

    @Test (groups = ["1.3.6"])
    void "OB-350_Delete the consent details with same X-Request-Id and same Consent_id"() {

        def xReqId = UUID.randomUUID().toString()

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete Consent - 1st Time
        consentDeleteResponse = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, xReqId)
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))

        //Delete Consent using X-Request-Id used in previous delete call
        Response consentDeleteResponse2 = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, xReqId)
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse2.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse2, BerlinConstants.TPPMESSAGE_CODE)
                .toString(), BerlinConstants.CONSENT_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(consentDeleteResponse2, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "The requested consent is already deleted")
    }

    @Test (groups = ["1.3.6"])
    void "OB-351_Delete the consent details with same X-Request-Id and different Consent_id"() {

        def xReqId = UUID.randomUUID().toString()

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete Consent = 1st Time
        consentDeleteResponse = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, xReqId)
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        Assert.assertNotNull(consentDeleteResponse.getHeader("X-Request-ID"))

        //Account Initiation - new Consent
        doDefaultInitiation(consentPath, initiationPayload)

        //Delete Consent using X-Request-Id used in previous delete call
        Response consentDeleteResponse2 = TestSuite.buildRequest()
          .contentType(ContentType.JSON)
          .header(BerlinConstants.X_REQUEST_ID, xReqId)
          .header(BerlinConstants.Date, getCurrentDate())
          .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
          .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
          .header(BerlinConstants.PSU_ID, "${PsuConfigReader.getPSU()}")
          .header(BerlinConstants.PSU_TYPE, "email")
          .filter(new BerlinSignatureFilter())
          .baseUri(ConfigParser.getInstance().getBaseURL())
          .delete("${consentPath}/${accountId}")

        Assert.assertEquals(consentDeleteResponse2.statusCode(), BerlinConstants.STATUS_CODE_204)
    }
}
