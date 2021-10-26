/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.test.validate.validator.factory;

import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.factory.SubmissionValidatorFactory;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.AccountSubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.CardAccountSubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.FundsConfirmationSubmissionValidator;
import org.junit.Test;
import org.testng.Assert;

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

        // Testing CardAccountSubmissionValidator instances
        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("card-accounts");
        Assert.assertTrue(submissionValidator instanceof CardAccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("card-accounts/{account-id}");
        Assert.assertTrue(submissionValidator instanceof CardAccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("card-accounts/{account-id}/balances");
        Assert.assertTrue(submissionValidator instanceof CardAccountSubmissionValidator);

        submissionValidator = SubmissionValidatorFactory
                .getSubmissionValidator("card-accounts/{account-id}/transactions");
        Assert.assertTrue(submissionValidator instanceof CardAccountSubmissionValidator);

        // Testing FundsConfirmationSubmissionValidator instances
        submissionValidator = SubmissionValidatorFactory.getSubmissionValidator("funds-confirmations");
        Assert.assertTrue(submissionValidator instanceof FundsConfirmationSubmissionValidator);
    }

}
