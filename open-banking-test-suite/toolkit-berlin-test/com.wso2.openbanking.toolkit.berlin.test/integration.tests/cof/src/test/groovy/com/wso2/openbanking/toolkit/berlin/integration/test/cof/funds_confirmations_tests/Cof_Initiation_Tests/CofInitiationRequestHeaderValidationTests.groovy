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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.berlin.common.utils.BerlinTestUtil
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Header Validation Tests on Funds Confirmation Initiation Request.
 */
class CofInitiationRequestHeaderValidationTests extends AbstractCofFlow {

    def config = ConfigParser.getInstance()
    String consentPath = CofConstants.COF_CONSENT_PATH
    String initiationPayload = CofInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["1.3.6"])
    void "TC0601002_Initiation request with an access token of authorization code type"() {

        //Initiation Request
        doDefaultCofInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent
        doCofAuthorizationFlow()
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

    @Test (groups = ["1.3.6"])
    void "TC0601003_Initiation Request With Access Token Not Bounding To Funds Confirmation Scope"() {

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

    @Test (groups = ["1.3.6"])
    void "TC0601004_Initiation Request without Authorization Header"() {

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

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse, BerlinConstants.TPPMESSAGE_TEXT).
                contains("Invalid Credentials. Make sure your API invocation call has a header: " +
                        "'Authorization"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0601005_Initiation Request invalid Authorization Header"() {

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
                contains("Token is not valid"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0601006_Initiation Request without X-Request-Id Header"() {

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

    @Test (groups = ["1.3.6"])
    void "TC0601007_Initiation Request with invalid X-Request-Id format"() {

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
                "Invalid X-Request-ID header. Needs to be in UUID format")
    }

    @Test (groups = ["1.3.6"])
    void "OB-1528_Initiation request with incorrect PSU_ID when TPP-ExplicitAuthorisationPreferred set to true"() {

        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .header(BerlinConstants.EXPLICIT_AUTH_PREFERRED, true)
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1687_Initiation request with same x-request-id and same payload"() {

        def xRequestId = UUID.randomUUID().toString()
        def date = getCurrentDate()

        //Consent Initiation - First Time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, date)
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
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
                .header(BerlinConstants.Date, date)
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_201)
        def consentId2 = TestUtil.parseResponseBody(consentResponse2, "consentId").toString()

        Assert.assertEquals(consentId1, consentId2)
    }

    @Test (groups = ["1.3.6"])
    void "OB-1685_Initiation request with same x-request-id and different payload"() {

        def xRequestId = UUID.randomUUID().toString()
        def date = getCurrentDate()
        def initiationPayload = CofInitiationPayloads.defaultInitiationPayload

        //Consent Initiation - First Time
        def consentResponse = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, date)
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Consent Initiation - Second Time
        def initiationPayload2 = CofInitiationPayloads.initiationPayloadWithDifferentAccountId

        def consentResponse2 = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(TestConstants.X_REQUEST_ID, xRequestId)
                .header(BerlinConstants.Date, date)
                .header(BerlinConstants.PSU_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${applicationAccessToken}")
                .header(BerlinConstants.PSU_ID, "${config.getPSU()}")
                .header(BerlinConstants.PSU_TYPE, "email")
                .filter(new BerlinSignatureFilter())
                .body(initiationPayload2)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(consentPath)

        Assert.assertEquals(consentResponse2.statusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse2, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue (TestUtil.parseResponseBody (consentResponse2, BerlinConstants.TPPMESSAGE_TEXT).
                contains ("Idempotency check failed."))
    }
}
