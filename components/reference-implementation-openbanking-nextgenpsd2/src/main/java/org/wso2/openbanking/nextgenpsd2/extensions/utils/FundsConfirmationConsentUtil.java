/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.nextgenpsd2.extensions.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.constants.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ValidationFailureException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ScaMethod;
import org.wso2.openbanking.nextgenpsd2.extensions.model.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.model.generated.StoredDetailedConsentResourceData;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;

/**
 * Utility class for payment consent management.
 */
public class FundsConfirmationConsentUtil {
    private static final Log log = LogFactory.getLog(FundsConfirmationConsentUtil.class);

    /**
     * Validate the requested card expiry date.
     *
     * @param cardExpiryDate requested card expiry date
     */
    public static void validateCardExpiryDate(String cardExpiryDate) throws ValidationFailureException {

        LocalDate parsedCardExpiryDate = CommonConsentValidationUtil.parseDateToISO(cardExpiryDate,
                TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.CARD_EXPIRY_DATE_INVALID);

        if (parsedCardExpiryDate.isBefore(LocalDate.now(ZoneOffset.UTC))) {
            String errorMessage = String.format("The provided card expiry date %s is a past date",
                    parsedCardExpiryDate);
            log.error(errorMessage);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(
                            null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.TIMESTAMP_INVALID,
                            errorMessage));
        }
    }

    /**
     * Method to validate funds confirmation initiation payload.
     *
     * @param payload
     */
    public static void validateFundsConfirmationInitiationPayload(JSONObject payload) throws
            ValidationFailureException {

        log.debug("Validating mandatory request body elements");
        if (!payload.has(ConsentExtensionConstants.ACCOUNT)
                || payload.opt(ConsentExtensionConstants.ACCOUNT) == null) {
            log.error(ErrorConstants.MANDATORY_ELEMENTS_MISSING);
            throw new ValidationFailureException(ValidationFailureException.ErrorCode.BAD_REQUEST,
                    ErrorUtil.constructBerlinError(null,
                            TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.MANDATORY_ELEMENTS_MISSING));
        }

        if (payload.has(ConsentExtensionConstants.CARD_EXPIRY_DATE)) {
            log.debug("Validating card expiry date");
            validateCardExpiryDate(payload.getString(ConsentExtensionConstants.CARD_EXPIRY_DATE));
        }

        JSONObject accountObject = payload.optJSONObject(ConsentExtensionConstants.ACCOUNT);

        log.debug("Validating account reference object");
        CommonConsentValidationUtil.validateAccountRefObject(accountObject);
    }

    /**
     * Method to get the funds confirmation initiation response without links.
     *
     * @param createdConsent
     * @param scaMethods
     * @param payload
     */
    public static void appendPaymentInitiationResponseToPayload(StoredDetailedConsentResourceData createdConsent,
                                                                ArrayList<ScaMethod> scaMethods, JSONObject payload) {
        payload.put(ConsentExtensionConstants.CONSENT_STATUS, createdConsent.getStatus());
        payload.put(ConsentExtensionConstants.CONSENT_ID, createdConsent.getId());

        JSONArray chosenSCAMethods = new JSONArray();
        for (ScaMethod scaMethod : scaMethods) {
            chosenSCAMethods.put(CommonConsentValidationUtil.convertObjectToJson(scaMethod));
        }

        if (scaMethods.size() > 1) {
            payload.put(ConsentExtensionConstants.SCA_METHODS, chosenSCAMethods);
        } else {
            payload.put(ConsentExtensionConstants.CHOSEN_SCA_METHOD, chosenSCAMethods.get(0));
        }
    }
}
