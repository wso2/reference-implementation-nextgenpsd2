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

package com.wso2.openbanking.toolkit.berlin.integration.test.accounts.common_test.Accounts_Authorization_Tests

import com.wso2.openbanking.berlin.common.utils.BerlinConstants
import com.wso2.openbanking.berlin.common.utils.BerlinOAuthAuthorization
import com.wso2.openbanking.test.framework.automation.BasicAuthAutomationStep
import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AbstractAccountsFlow
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsConstants
import com.wso2.openbanking.toolkit.berlin.integration.test.accounts.util.AccountsInitiationPayloads
import org.openqa.selenium.By
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Implicit Authorisation Flow.
 */
class ImplicitAuthorisationRequestValidationTest extends AbstractAccountsFlow {

    // Static parameters
    String consentPath = AccountsConstants.CONSENT_PATH
    String initiationPayload = AccountsInitiationPayloads.AllAccessBankOfferedConsentPayload

    @Test (groups = ["1.3.3", "1.3.6"])
    void "PSU authentication on cancelled account consent"() {

        //Account Initiation Request
        doDefaultInitiation(consentPath, initiationPayload)
        Assert.assertEquals(consentResponse.statusCode(), BerlinConstants.STATUS_CODE_201)
        Assert.assertNotNull(accountId)

        //Delete Created account consent
        deleteConsent(consentPath)
        Assert.assertEquals(consentDeleteResponse.statusCode(), BerlinConstants.STATUS_CODE_204)

        //Authorize the cancelled consent
        def authAuthorization = new BerlinOAuthAuthorization(scopes, accountId)
        new BrowserAutomation(BrowserAutomation.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authAuthorization.authoriseUrl))
                .addStep {driver, context ->
                    Assert.assertTrue(driver.findElement(By.xpath(BerlinConstants.LBL_CONSENT_PAGE_ERROR)).getText().trim()
                            .equalsIgnoreCase("Current Consent State is not valid for authorisation"))
                }
                .execute()
    }
}
