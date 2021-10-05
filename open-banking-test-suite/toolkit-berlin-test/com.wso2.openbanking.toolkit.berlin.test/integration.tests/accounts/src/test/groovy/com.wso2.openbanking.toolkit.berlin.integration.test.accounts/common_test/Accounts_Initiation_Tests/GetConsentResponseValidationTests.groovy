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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinRequestBuilder
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Response Validation Tests on Get Consent Request.
 */
class GetConsentResponseValidationTests extends AbstractAccountsFlow {

    def consentPath = AccountsConstants.CONSENT_PATH
    def initiationPayload = AccountsInitiationPayloads.defaultInitiationPayload

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208001_Get the Consent with Valid Consent Id"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208002_Get Consent With Empty Consent Id"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}/")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208003_Get Consent Without Consent Id Parameter"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)

        //Get Consent
        def retrievalResponse = BerlinRequestBuilder.buildBasicRequest(applicationAccessToken)
                .get("${consentPath}")
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_405)
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208004_Get Already Terminated Consent"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.getStatusCode(), BerlinConstants.STATUS_CODE_204)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_TERMINATEDBYTPP)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    @Test (groups = ["1.3.3", "1.3.6"])
    void "TC0208005_Get an Authorized Consent"() {

        //Account Initiation
        doDefaultInitiation(consentPath, initiationPayload)
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_RECEIVED)

        //Delete Consent
        doAuthorizationFlow()
        doStatusRetrieval(consentPath)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)

        //Get Consent
        doConsentRetrieval(consentPath)
        Assert.assertEquals(retrievalResponse.getStatusCode(), BerlinConstants.STATUS_CODE_200)
        Assert.assertEquals(consentStatus, AccountsConstants.CONSENT_STATUS_VALID)
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("access"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("recurringIndicator"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("validUntil"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("frequencyPerDay"))
        Assert.assertNotNull(retrievalResponse.jsonPath().getJsonObject("lastActionDate"))
        Assert.assertEquals(retrievalResponse.jsonPath().getJsonObject("lastActionDate"),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
}
