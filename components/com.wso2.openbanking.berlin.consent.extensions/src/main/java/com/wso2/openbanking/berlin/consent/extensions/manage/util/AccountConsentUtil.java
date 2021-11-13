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

package com.wso2.openbanking.berlin.consent.extensions.manage.util;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.models.ScaApproach;
import com.wso2.openbanking.berlin.common.models.ScaMethod;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.common.utils.ErrorUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.LinksConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Contains functions used for accounts consent flow.
 */
public class AccountConsentUtil {

    private static final Log log = LogFactory.getLog(AccountConsentUtil.class);

    /**
     * Method to validate account initiation payload.
     *
     * @param payload
     */
    public static void validateAccountInitiationPayload(JSONObject payload, int configuredFreqPerDay,
                                                        boolean isValidUntilDateCapEnabled, long validUntilDays) {
        JSONObject accessObject = (JSONObject) payload.get(ConsentExtensionConstants.ACCESS);

        log.debug("Validating mandatory request body elements");
        if (accessObject == null
                || !payload.containsKey(ConsentExtensionConstants.RECURRING_INDICATOR)
                || !payload.containsKey(ConsentExtensionConstants.VALID_UNTIL)
                || !payload.containsKey(ConsentExtensionConstants.FREQUENCY_PER_DAY)
                || !payload.containsKey(ConsentExtensionConstants.COMBINED_SERVICE_INDICATOR)) {
            log.error(ErrorConstants.MANDATORY_ELEMENTS_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.MANDATORY_ELEMENTS_MISSING));
        }

        log.debug("Validating mandatory access object attributes");
        // At least one of these attributes must be there for the access object to be valid
        if (!accessObject.containsKey(ConsentExtensionConstants.ACCOUNTS)
                && !accessObject.containsKey(ConsentExtensionConstants.BALANCES)
                && !accessObject.containsKey(ConsentExtensionConstants.TRANSACTIONS)
                && !accessObject.containsKey(ConsentExtensionConstants.AVAILABLE_ACCOUNTS)
                && !accessObject.containsKey(ConsentExtensionConstants.AVAILABLE_ACCOUNTS_WITH_BALANCE)
                && !accessObject.containsKey(ConsentExtensionConstants.ALL_PSD2)) {
            log.error(ErrorConstants.ACCESS_OBJECT_MANDATORY_ELEMENTS_MISSING);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.ACCESS_OBJECT_MANDATORY_ELEMENTS_MISSING));
        }

        log.debug("Validating additionalInformation attribute");
        //  it can only be present with at least one of the major access attributes (accounts, balances, transactions)
        if (accessObject.containsKey(ConsentExtensionConstants.ADDITIONAL_INFORMATION)
                && (!accessObject.containsKey(ConsentExtensionConstants.ACCOUNTS)
                || !accessObject.containsKey(ConsentExtensionConstants.BALANCES)
                || !accessObject.containsKey(ConsentExtensionConstants.TRANSACTIONS))) {
            log.error(ErrorConstants.INVALID_USE_OF_ADDITIONAL_INFO_ATTRIBUTE);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.INVALID_USE_OF_ADDITIONAL_INFO_ATTRIBUTE));
        }

        // TODO: check for account ref type from config (account ref validation)

        log.debug("Validating account permissions");
        validateAccountAccessAttribute(accessObject);

        log.debug("Validating frequency per day and recurring indicator");
        if ((int) payload.get(ConsentExtensionConstants.FREQUENCY_PER_DAY) < 1) {
            log.error(ErrorConstants.INVALID_FREQ_PER_DAY);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.INVALID_FREQ_PER_DAY));
        }

        if (!((boolean) payload.get(ConsentExtensionConstants.RECURRING_INDICATOR))
                && (int) payload.get(ConsentExtensionConstants.FREQUENCY_PER_DAY) > 1) {
            log.error(ErrorConstants.INVALID_FREQ_PER_DAY_COUNT);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    ErrorConstants.INVALID_FREQ_PER_DAY_COUNT));
        }

        if ((boolean) payload.get(ConsentExtensionConstants.RECURRING_INDICATOR) && configuredFreqPerDay >
                (int) payload.get(ConsentExtensionConstants.FREQUENCY_PER_DAY)) {
            String errorMessageTemplate = "Frequency per day for recurring consent is lesser than the supported " +
                    "minimum value %s";
            if (log.isDebugEnabled()) {
                log.debug(String.format(errorMessageTemplate, configuredFreqPerDay));
            }
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                    String.format(errorMessageTemplate, configuredFreqPerDay)));
        }

        log.debug("Validating valid until");
        payload.replace(ConsentExtensionConstants.VALID_UNTIL, getValidatedValidUntil((String) payload
                .get(ConsentExtensionConstants.VALID_UNTIL), isValidUntilDateCapEnabled, validUntilDays));

        log.debug("Validating combined service indicator");
        // (Not supported)
        if ((Boolean) payload.get(ConsentExtensionConstants.COMBINED_SERVICE_INDICATOR)) {
            log.error(ErrorConstants.COMBINED_SERVICE_INDICATOR_NOT_SUPPORTED);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.SESSIONS_NOT_SUPPORTED,
                    ErrorConstants.COMBINED_SERVICE_INDICATOR_NOT_SUPPORTED));
        }
    }

    /**
     * Validates the access attribute by checking the combinations of sub-attributes that can be present.
     *
     * @param accessObject access attribute of the request body
     */
    public static void validateAccountAccessAttribute(JSONObject accessObject) {
        String availableAccounts = (String) accessObject.get(ConsentExtensionConstants.AVAILABLE_ACCOUNTS);
        String availableAccountsWithBalances = (String) accessObject
                .get(ConsentExtensionConstants.AVAILABLE_ACCOUNTS_WITH_BALANCE);
        String allPsd2 = (String) accessObject.get(ConsentExtensionConstants.ALL_PSD2);

        if (!accessObject.containsKey(ConsentExtensionConstants.ACCOUNTS)
                && !accessObject.containsKey(ConsentExtensionConstants.BALANCES)
                && !accessObject.containsKey(ConsentExtensionConstants.TRANSACTIONS)) {
            if (ConsentExtensionConstants.ALL_ACCOUNTS.equals(availableAccounts)
                    || ConsentExtensionConstants.ALL_ACCOUNTS_WITH_OWNER_NAME.equals(availableAccounts)) {
                if (availableAccountsWithBalances != null || allPsd2 != null) {
                    log.error("availableAccounts permission cannot be set with availableAccountsWithBalances " +
                            "or allPsd2 permissions");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                            null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.INVALID_PERMISSION));
                }
            }
            if (ConsentExtensionConstants.ALL_ACCOUNTS.equals(availableAccountsWithBalances)
                    || ConsentExtensionConstants.ALL_ACCOUNTS_WITH_OWNER_NAME.equals(availableAccountsWithBalances)) {
                if (availableAccounts != null || allPsd2 != null) {
                    log.error("availableAccountsWithBalances permission cannot be set with availableAccounts " +
                            "or allPsd2 permissions");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                            null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.INVALID_PERMISSION));
                }
            }
            if (ConsentExtensionConstants.ALL_ACCOUNTS.equals(allPsd2)
                    || ConsentExtensionConstants.ALL_ACCOUNTS_WITH_OWNER_NAME.equals(allPsd2)) {
                if (availableAccounts != null || availableAccountsWithBalances != null) {
                    log.error("allPsd2 permission cannot be set with availableAccounts or " +
                            "availableAccountsWithBalances permissions");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                            null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                            ErrorConstants.INVALID_PERMISSION));
                }
            }
        } else {

            /* According to Berlin/Israel specifications, either all access arrays should be empty, or non-empty.
             * The following logic checks for this requirement.
             */
            int numberOfProvidedAccessTypes = 0;
            int numberOfEmptyAccessMethodArrays = 0;

            if (accessObject.get(ConsentExtensionConstants.ACCOUNTS) != null) {
                numberOfProvidedAccessTypes++;
                JSONArray accounts = (JSONArray) accessObject.get(ConsentExtensionConstants.ACCOUNTS);
                if (accounts != null && accounts.size() == 0) {
                    numberOfEmptyAccessMethodArrays++;
                }
            }
            if (accessObject.get(ConsentExtensionConstants.BALANCES) != null) {
                numberOfProvidedAccessTypes++;
                JSONArray balances = (JSONArray) accessObject.get(ConsentExtensionConstants.BALANCES);
                if (balances != null && balances.size() == 0) {
                    numberOfEmptyAccessMethodArrays++;
                }
            }
            if (accessObject.get(ConsentExtensionConstants.TRANSACTIONS) != null) {
                numberOfProvidedAccessTypes++;
                JSONArray transactions = (JSONArray) accessObject.get(ConsentExtensionConstants.TRANSACTIONS);
                if (transactions != null && transactions.size() == 0) {
                    numberOfEmptyAccessMethodArrays++;
                }
            }

            if ((numberOfProvidedAccessTypes > numberOfEmptyAccessMethodArrays)
                    && numberOfEmptyAccessMethodArrays != 0) {
                // If either all arrays are not empty or not non-empty, an error is thrown.
                log.error("Either all arrays should be empty or non-empty");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                        null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.INVALID_PERMISSION));
            }
            if (availableAccounts != null || availableAccountsWithBalances != null || allPsd2 != null) {
                log.error("Special permissions availableAccounts, availableAccountsWithBalances or allPsd2 " +
                        "cannot be applied when account, balances or transaction access is specified");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                        null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.FORMAT_ERROR,
                        ErrorConstants.INVALID_PERMISSION));
            }
        }
    }

    /**
     * Validate the requested date.
     * If a maximal available date is requested, a date in far future is to be used: “9999-12-31”.
     *
     * @param validUntil requested valid until date
     * @return allowed valid until date
     */
    public static String getValidatedValidUntil(String validUntil, boolean isValidUntilDateCapEnabled,
                                                long validUntilDays) {

        LocalDate validUntilDate = ConsentExtensionUtil.parseDateToISO(validUntil, TPPMessage.CodeEnum.FORMAT_ERROR,
                ErrorConstants.VALID_UNTIL_DATE_INVALID);
        LocalDate today = LocalDate.now();
        if (validUntilDate.isBefore(today)) {
            String errorMessage = "validUntil has to be today, %s or a future date";
            log.error(String.format(errorMessage, today));
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.TIMESTAMP_INVALID,
                    String.format(errorMessage, today)));
        }
        LocalDate maximumValidUntil = LocalDate.parse(ConsentExtensionConstants.MAXIMUM_VALID_DATE);
        if (isValidUntilDateCapEnabled &&
                (validUntil.compareTo(LocalDateTime.now().
                        plusDays(validUntilDays).
                        format(DateTimeFormatter.ISO_LOCAL_DATE)) > 0)) {
            validUntil = LocalDateTime.now().plusDays(validUntilDays).
                    format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (validUntilDate.isAfter(maximumValidUntil)) {
            validUntil = ConsentExtensionConstants.MAXIMUM_VALID_DATE;
        }
        return validUntil;
    }

    /**
     * Converts a given date to a UTC timestamp.
     *
     * @param date date in string format
     * @return date/time after converting to UTC timestamp
     */
    public static String convertToUtcTimestamp(String date) {

        LocalDate localDate = ConsentExtensionUtil.parseDateToISO(date, TPPMessage.CodeEnum.FORMAT_ERROR,
                ErrorConstants.VALID_UNTIL_DATE_INVALID);
        LocalDateTime localDateTime = localDate.atStartOfDay();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(ConsentExtensionConstants.UTC));

        // Retrieve the UTC timestamp in long.
        long utcTimestamp = Instant.from(zonedDateTime).getEpochSecond();
        return Long.toString(utcTimestamp);
    }

    /**
     * Checks if consent is expired.
     *
     * @param validUntilDate valid until date
     * @param updatedTimeVal last updated tile
     * @return whether consent is expired or not
     */
    public static boolean isConsentExpired(String validUntilDate, long updatedTimeVal) {

        LocalDate expDate = ConsentExtensionUtil.parseDateToISO(validUntilDate, TPPMessage.CodeEnum.FORMAT_ERROR,
                ErrorConstants.VALID_UNTIL_DATE_INVALID);
        LocalDateTime updatedDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(updatedTimeVal),
                ZoneId.of(ConsentExtensionConstants.UTC));
        LocalDate updatedDate = updatedDateTime.toLocalDate();

        LocalDate currDate = LocalDate.now();
        LocalDate expTimeAfter90Days = updatedDate.plusDays(90);
        if (currDate.isBefore(expDate) || currDate.isEqual(expDate)) {
            return expTimeAfter90Days.isBefore(currDate);
        } else {
            return true;
        }
    }

    /**
     * Method to construct account initiation response.
     *
     * @param consentManageData   consent manage data
     * @param createdConsent      the created consent
     * @param isExplicitAuth      whether explicit authorisation or not
     * @param isRedirectPreferred whether redirect approach is preferred or not
     * @param apiVersion          the configured API version to construct the self links
     * @param isSCARequired       whether SCA is required or not as configured
     * @return the constructed initiation response
     */
    public static JSONObject constructAccountInitiationResponse(ConsentManageData consentManageData,
                                                                DetailedConsentResource createdConsent,
                                                                boolean isExplicitAuth, boolean isRedirectPreferred,
                                                                String apiVersion, boolean isSCARequired) {

        String requestPath = consentManageData.getRequestPath();
        String locationString = String.format(ConsentExtensionConstants.SELF_LINK_TEMPLATE,
                apiVersion, requestPath, createdConsent.getConsentID());
        consentManageData.setResponseHeader(ConsentExtensionConstants.LOCATION_PROPER_CASE_HEADER,
                locationString);

        Map<String, Object> scaElements = CommonUtil.getScaApproachAndMethods(isRedirectPreferred,
                isSCARequired);
        ScaApproach scaApproach = (ScaApproach) scaElements.get(CommonConstants.SCA_APPROACH_KEY);
        ArrayList<ScaMethod> scaMethods =
                (ArrayList<ScaMethod>) scaElements.get(CommonConstants.SCA_METHODS_KEY);
        consentManageData.setResponseHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH,
                scaApproach.getApproach().toString());

        JSONObject responseWithoutLinks = AccountConsentUtil.getAccountInitiationResponse(createdConsent, scaMethods);

        String authId = null;

        if (!isExplicitAuth) {
            // Always only one auth resource is created for implicit initiation
            ArrayList<AuthorizationResource> authResources = createdConsent.getAuthorizationResources();
            AuthorizationResource implicitAuthResource = authResources.get(0);
            authId = implicitAuthResource.getAuthorizationID();
        }

        JSONObject links = LinksConstructor.getInitiationLinks(isExplicitAuth, scaApproach,
                scaMethods, requestPath, createdConsent.getConsentID(), authId, ConsentTypeEnum.ACCOUNTS.toString());

        return responseWithoutLinks.appendField(ConsentExtensionConstants.LINKS, links);
    }

    /**
     * Method to get the account initiation response without links.
     *
     * @param createdConsent the created consent
     * @param scaMethods     decided SCA methods
     * @return the constructed initiation response without links
     */
    public static JSONObject getAccountInitiationResponse(DetailedConsentResource createdConsent,
                                                          ArrayList<ScaMethod> scaMethods) {

        JSONObject responseObject = new JSONObject();
        responseObject.appendField(ConsentExtensionConstants.CONSENT_STATUS, createdConsent.getCurrentStatus());
        responseObject.appendField(ConsentExtensionConstants.CONSENT_ID, createdConsent.getConsentID());

        JSONArray chosenSCAMethods = new JSONArray();
        for (ScaMethod scaMethod : scaMethods) {
            chosenSCAMethods.add(CommonUtil.convertObjectToJson(scaMethod));
        }

        if (scaMethods.size() > 1) {
            responseObject.appendField(ConsentExtensionConstants.SCA_METHODS, chosenSCAMethods);
        } else {
            responseObject.appendField(ConsentExtensionConstants.CHOSEN_SCA_METHOD, chosenSCAMethods);
        }

        return responseObject;
    }

    /**
     * Method to construct accounts consent get response.
     *
     * @param retrievedConsent consent object
     * @return the constructed account consent get response
     * @throws ParseException
     */
    public static JSONObject constructAccountConsentGetResponse(ConsentResource retrievedConsent)
            throws ParseException {

        JSONObject responseWithoutLinks = AccountConsentUtil.getAccountConsentGetResponse(retrievedConsent);
        return responseWithoutLinks.appendField(ConsentExtensionConstants.LINKS,
                LinksConstructor.getAccountConsentGetLinks());
    }

    /**
     * Method to get the account consent get response without links.
     *
     * @param retrievedConsent consent object
     * @return the constructed account consent get response without links
     * @throws ParseException
     */
    public static JSONObject getAccountConsentGetResponse(ConsentResource retrievedConsent) throws ParseException {

        JSONObject consentReceipt =
                (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(retrievedConsent.getReceipt());

        consentReceipt.appendField(ConsentExtensionConstants.CONSENT_STATUS, retrievedConsent.getCurrentStatus());

        Date currentDate = new Date(retrievedConsent.getUpdatedTime() * 1000L);
        DateFormat dateFormat = new SimpleDateFormat(ConsentExtensionConstants.DATE_FORMAT);
        String lastActionDate = dateFormat.format(currentDate);

        consentReceipt.appendField(ConsentExtensionConstants.LAST_ACTION_DATE, lastActionDate);

        return consentReceipt;
    }

}
