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

package com.wso2.openbanking.toolkit.berlin.integration.test.cof.funds_confirmations_tests.Cof_Initiation_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.test.framework.util.TestUtil
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.AbstractCofFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.cof.util.CofInitiationPayloads
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Payload Validation Tests on Funds Confirmations Initiation Request.
 */
class CofInitiationRequestPayloadValidationTests extends AbstractCofFlow {

    @Test (groups = ["1.3.6"])
    void "TC0601008_Initiation request without Account attribute in the payload"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithoutAccountAttribute)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Object has missing required properties ([\"account\"])")
    }

    @Test (groups = ["1.3.6"])
    void "TC0601009_Initiation request without Account Reference in the payload"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithoutAccountReference)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Provided account reference is invalid or not supported")
    }

    @Test (groups = ["1.3.6"])
    void "TC0601010_Initiation request without CardNumber attribute in the payload"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithoutCardNumberAttribute)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test (groups = ["1.3.6"])
    void "TC0601011_Initiation request with a CardNumber of more than 35 characters"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithCardNumberOfMoreThan35Chars)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                " String \"1234567890123456789012345678901234567890\" is too long (length: 40, maximum allowed: 35)"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0601012_Initiation request without CardExpiryDate attribute in the payload"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithoutCardExpiryDateAttribute)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test (groups = ["1.3.6"])
    void "TC0601013_Initiation request with an invalid CardExpiryDate attribute in the payload"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithInvalidCardExpiryDate)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TIMESTAMP_INVALID)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT).contains(
                "String \"2025-12-312\" is invalid against requested date format(s) yyyy-MM-dd"))
    }

    @Test (groups = ["1.3.6"])
    void "TC0601014_Initiation request with a past CardExpiryDate attribute in the payload"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithPastCardExpiryDate)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.TIMESTAMP_INVALID)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "The provided card expiry date 2000-12-12 is a past date")
    }

    @Test (groups = ["1.3.6"])
    void "TC0601015_Initiation request without CardInformation attribute in the payload"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithoutCardInformationAttribute)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_201)
    }

    @Test (groups = ["1.3.6"])
    void "TC0601016_Initiation request with CardInformation of more than 140 characters"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithCardInfoOfMoreThan140Chars)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
    }

    @Test (groups = ["1.3.6"])
    void "TC0601017_Initiation request without RegistrationInformation attribute in the payload"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithoutRegInfoAttribute)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
    }

    @Test (groups = ["1.3.6"])
    void "TC0601018_Initiation request with RegistrationInformation of more than 140 characters"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithRegInfoOfMoreThan140Chars)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
    }

    /**
     * This testcase only supports if AccountReferenceType is configured as 'iban' or 'bban' in open-banking.xml.
     */
    @Test (groups = ["1.3.6"], enabled = true)
    void "TC0601019_Initiation request with different a account reference than configured"() {

        def consentPath = CofConstants.COF_CONSENT_PATH

        doDefaultCofInitiation(consentPath, CofInitiationPayloads.initiationPayloadWithPanAttribute)

        Assert.assertEquals(consentResponse.getStatusCode(), BerlinConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_CODE),
                BerlinConstants.FORMAT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, BerlinConstants.TPPMESSAGE_TEXT),
                "Provided account reference is invalid or not supported")
    }
}
