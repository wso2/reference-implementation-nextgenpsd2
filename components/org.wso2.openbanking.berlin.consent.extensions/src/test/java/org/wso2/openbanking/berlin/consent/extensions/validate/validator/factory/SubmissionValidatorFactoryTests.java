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

package org.wso2.openbanking.berlin.consent.extensions.validate.validator.factory;

import org.junit.Test;
import org.testng.Assert;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.AccountSubmissionValidator;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.FundsConfirmationSubmissionValidator;

/**
 * This contains unit tests for SubmissionValidatorFactory class.
 */
public class SubmissionValidatorFactoryTests {

    @Test
    public void testGetSubmissionValidator() {

        SubmissionValidator submissionValidator;

        // Testing AccountSubmissionValidator instances
        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("accounts");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("accounts/{account-id}");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("accounts/{account-id}/balances");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("accounts/{account-id}/transactions");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("accounts/{account-id}/transactions/{transactionId}");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("accounts?withBalance");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("accounts/{account-id}?withBalance");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("accounts/{account-id}/transactions?withBalance");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("card-accounts");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("card-accounts/{account-id}");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("card-accounts/{account-id}/balances");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("card-accounts/{account-id}/transactions");
        Assert.assertTrue(submissionValidator instanceof AccountSubmissionValidator);

        // Testing FundsConfirmationSubmissionValidator instances
        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("funds-confirmations");
        Assert.assertTrue(submissionValidator instanceof FundsConfirmationSubmissionValidator);
    }

}
