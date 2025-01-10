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

import org.apache.commons.lang3.StringUtils;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.SubmissionValidator;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.AccountSubmissionValidator;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.FundsConfirmationSubmissionValidator;
import org.wso2.openbanking.berlin.consent.extensions.validate.validator.impl.PaymentConsentValidator;

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
