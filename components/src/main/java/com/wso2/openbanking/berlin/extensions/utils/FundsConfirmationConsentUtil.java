package com.wso2.openbanking.berlin.extensions.utils;

import com.wso2.openbanking.berlin.extensions.datamodels.TPPMessage;
import com.wso2.openbanking.berlin.extensions.exceptions.FailedValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Utility class for payment consent management
 */
public class FundsConfirmationConsentUtil {
    private static final Log log = LogFactory.getLog(FundsConfirmationConsentUtil.class);

    /**
     * Validate the requested card expiry date.
     *
     * @param cardExpiryDate requested card expiry date
     */
    public static void validateCardExpiryDate(String cardExpiryDate) throws FailedValidationException {

        LocalDate parsedCardExpiryDate = CommonConsentValidationUtil.parseDateToISO(cardExpiryDate,
                TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.CARD_EXPIRY_DATE_INVALID);

        if (parsedCardExpiryDate.isBefore(LocalDate.now(ZoneOffset.UTC))) {
            String errorMessage = String.format("The provided card expiry date %s is a past date",
                    parsedCardExpiryDate);
            log.error(errorMessage);
            throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
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
    public static void validateFundsConfirmationInitiationPayload(JSONObject payload) throws FailedValidationException {

        log.debug("Validating mandatory request body elements");
        if (!payload.has(ConsentExtensionConstants.ACCOUNT)
                || payload.opt(ConsentExtensionConstants.ACCOUNT) == null) {
            log.error(ErrorConstants.MANDATORY_ELEMENTS_MISSING);
            throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
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
}
