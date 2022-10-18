/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.validate.validator.factory;

import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.AccountSubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.FundsConfirmationSubmissionValidator;
import com.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.PaymentConsentValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * Factory for deciding the submission request validator.
 */
public class SubmissionValidatorFactory {

    /**
     * Method to get the Submission Validator instance.
     *
     * @param requestPath Request path of the request
     * @return SubmissionValidator
     */
    public static SubmissionValidator getSubmissionValidator(String requestPath) {

        if (requestPath == null) {
            return null;
        }

        String cleanedRequestPath = StringUtils.stripStart(requestPath, "/");
        String[] requestPathArray = cleanedRequestPath.split("/");
        String[] requestPathArrayWithQuery = cleanedRequestPath.split("\\?");

        if (ConsentExtensionConstants.ACCOUNTS_SUBMISSION_PATH_IDENTIFIER.equals(requestPathArray[0])
                || ConsentExtensionConstants.ACCOUNTS_SUBMISSION_PATH_IDENTIFIER.equals(requestPathArrayWithQuery[0])
                || ConsentExtensionConstants.CARD_ACCOUNTS_SUBMISSION_PATH_IDENTIFIER.equals(requestPathArray[0])) {
            return new AccountSubmissionValidator();
        }

        if (ConsentExtensionConstants.FUNDS_CONFIRMATIONS_SUBMISSION_PATH_IDENTIFIER.equals(requestPathArray[0])) {
            return new FundsConfirmationSubmissionValidator();
        }

        if (ConsentExtensionConstants.PAYMENTS_RETRIEVAL_PATH_IDENTIFIER.equals(requestPathArray[0])
                || ConsentExtensionConstants.BULK_PAYMENTS_RETRIEVAL_PATH_IDENTIFIER.equals(requestPathArray[0])
                || ConsentExtensionConstants.PERIODIC_PAYMENTS_RETRIEVAL_PATH_IDENTIFIER.equals(requestPathArray[0])) {
            return new PaymentConsentValidator();
        }

        return null;

    }

}
