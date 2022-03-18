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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Retrieval_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.test.framework.TestSuite
import com.wso2.openbanking.test.framework.filters.BerlinSignatureFilter
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofRetrievalPayloads
import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Header Validation Tests on Funds Confirmations Retrieval Request.
 */
class CofRetrievalRequestHeaderValidationTests extends AbstractCofFlow {

    String resourcePath = CofConstants.COF_RETRIEVAL_PATH
    String consentPath = CofConstants.COF_CONSENT_PATH
    String initiationPayload = CofInitiationPayloads.defaultInitiationPayload
    String retrievalPayload = CofRetrievalPayloads.defaultRetrievalPayload

    void preRetrievalFlow() {

        //Do Account Initiation
        doDefaultCofInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)

        //Authorize the Consent and Extract the Code
        doCofAuthorizationFlow()
        Assert.assertNotNull(code)

        //Get User Access Token
        generateUserAccessToken()
        Assert.assertNotNull(userAccessToken)
    }

    @Test (groups = ["1.3.6"])
    void "TC0605010_Confirm funds with an access token of client credentials type"() {

        preRetrievalFlow()

        def response = BerlinRequestBuilder
                .buildBasicRequest(applicationAccessToken)
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(retrievalPayload)
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).
                toString().contains("Incorrect Access Token Type provided"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605008_Confirm funds without Authorization header"() {

        preRetrievalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertTrue (TestUtil.parseResponseBody (response, BerlinConstants.TPPMESSAGE_TEXT).
                contains("Invalid Credentials. Make sure your API invocation call has a header: " +
                        "'Authorization"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605009_Confirm funds with an invalid Authorization header"() {

        preRetrievalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer 1234")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TOKEN_INVALID)
        Assert.assertTrue (TestUtil.parseResponseBody (response, BerlinConstants.TPPMESSAGE_TEXT).
                contains("Token is not valid"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605005_Confirm funds without X-Request-ID header"() {

        preRetrievalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT)
                .contains("X-Request-ID header is missing in the request"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605006_Confirm funds with an empty X-Request-ID header"() {

        preRetrievalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, "")
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)

        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "X-Request-ID header is missing in the request")
    }

    @Test (groups = ["1.3.6"])
    void "TC0605007_Confirm funds with an invalid X-Request-ID header"() {

        preRetrievalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, "1234")
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)

        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "Input string \"1234\" is not a valid UUID")
    }

    @Test (groups = ["1.3.6"])
    void "TC0605004_Confirm funds without a consent id header"() {

        preRetrievalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString().
                contains("Missing mandatory header: Consent-ID"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0605003_Confirm funds with an invalid consent id header"() {

        preRetrievalFlow()

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, "1234")
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.CONSENT_UNKNOWN)
    }

    @Test (groups = ["1.3.6"])
    void "TC0605002_Confirm funds with an empty consent id header"() {

        preRetrievalFlow()

        //Make Account Retrieval Request with the deleted Consent
        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, "")
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "Parameter 'Consent-ID' is required but is missing.")
    }

    @Test (groups = ["1.3.6"])
    void "TC0605011_Confirm funds when associate consent is not in authorized state"() {

        preRetrievalFlow()

        //Delete Consent
        deleteCofConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)

        //Check Status
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, CofConstants.CONSENT_STATUS_TERMINATED_BY_TPP)

        //Make Account Retrieval Request with the deleted Consent

        def response = TestSuite.buildRequest()
                .contentType(ContentType.JSON)
                .header(BerlinConstants.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinConstants.Date, getCurrentDate())
                .header(TestConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${userAccessToken}")
                .filter(new BerlinSignatureFilter())
                .header(BerlinConstants.CONSENT_ID_HEADER, consentId)
                .body(retrievalPayload)
                .baseUri(ConfigParser.getInstance().getBaseURL())
                .post(resourcePath)

        Assert.assertEquals(response.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_CODE).toString(),
                BerlinConstants.CONSENT_UNKNOWN)
        Assert.assertEquals(TestUtil.parseResponseBody(response, BerlinConstants.TPPMESSAGE_TEXT).toString(),
                "Consent is not in a valid state")
    }
}
