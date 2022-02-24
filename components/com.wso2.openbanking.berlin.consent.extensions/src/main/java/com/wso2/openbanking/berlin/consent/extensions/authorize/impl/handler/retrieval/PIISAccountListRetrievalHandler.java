/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.DataRetrievalUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Funds confirmation account list data retrieval for Authorize.
 */
public class PIISAccountListRetrievalHandler implements AccountListRetrievalHandler {

    private static final Log log = LogFactory.getLog(PIISAccountListRetrievalHandler.class);

    @Override
    public JSONObject getAccountData(ConsentData consentData, JSONObject consentDataJSON) throws ConsentException {

        JSONObject accountRefObject = (JSONObject) consentDataJSON.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECT);

        String payableAccountsEndpoint = CommonConfigParser.getInstance().getPayableAccountsRetrieveEndpoint();
        JSONArray userAccountsArray = DataRetrievalUtil.getAccountsFromEndpoint(consentData.getUserId(),
                payableAccountsEndpoint, new HashMap<>(), new HashMap<>());

        if (userAccountsArray == null || userAccountsArray.isEmpty()) {
            log.error(ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_REQUEST,
                            ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER, consentData.getRedirectURI(),
                            consentData.getState()));
        }

        JSONObject validatedAccountRefObject;
        if (accountRefObject.containsKey(ConsentExtensionConstants.MASKED_PAN)) {
            // Skipping validation for maskedPan based account reference types and this needs to be validated
            // from the bank back end since there might be scenarios where there are 2 similar maskedPans
            // for a single user therefore we are not sure which account to validate it against
            // Eg: 123456xxxxxx1234, 123456xxxxxx1234 -> Both these maskedPans can belong to the same user
            validatedAccountRefObject = accountRefObject;
        } else {
            validatedAccountRefObject = getValidatedAccountRefObject(accountRefObject, userAccountsArray);
        }

        if (validatedAccountRefObject == null) {
            log.error(ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_REQUEST,
                            ErrorConstants.ACCOUNTS_NOT_FOUND_FOR_USER, consentData.getRedirectURI(),
                            consentData.getState()));
        }

        // Appending account data to the consent data to display in the consent page
        appendAccountDataToConsentData(validatedAccountRefObject, consentDataJSON);

        JSONObject accountDataJSON = new JSONObject();
        accountDataJSON.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS,
                new JSONArray().appendElement(validatedAccountRefObject));

        return accountDataJSON;
    }

    @Override
    public void appendAccountDetailsToMetadata(Map<String, Object> metaDataMap, JSONObject accountDataJSON) {

        JSONArray accountRefObjects = (JSONArray) accountDataJSON.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECTS);
        metaDataMap.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, accountRefObjects.get(0));
    }

    private void appendAccountDataToConsentData(JSONObject validatedAccountRefObject, JSONObject consentDataJSON) {

        String accountRefType = ConsentExtensionUtil.getAccountReferenceType(validatedAccountRefObject);

        String accountNumber = validatedAccountRefObject.getAsString(accountRefType);
        String currency = validatedAccountRefObject.getAsString(ConsentExtensionConstants.CURRENCY);

        String accountReference = String.format("%s %s", accountRefType, accountNumber);
        if (currency != null) {
            accountReference += String.format(" (%s)", currency);
        }

        JSONArray consentDetails = (JSONArray) consentDataJSON.get(ConsentExtensionConstants.CONSENT_DETAILS);
        JSONObject consentDetail = (JSONObject) consentDetails.get(0);
        JSONArray data = (JSONArray) consentDetail.get(ConsentExtensionConstants.DATA_SIMPLE);
        data.add(0, ConsentExtensionConstants.ACCOUNT_REFERENCE_TITLE + ": " + accountReference);

        consentDetail.put(ConsentExtensionConstants.DATA_SIMPLE, data);
        consentDataJSON.put(ConsentExtensionConstants.CONSENT_DETAILS, new JSONArray().appendElement(consentDetail));
    }

    /**
     * Returns the validated account reference object after
     * considering funds confirmation service related multi-currency validations.
     *
     * @param accountRefObject account reference object from initiation payload
     * @param accountArray accounts array retrieved from bank backend
     * @return validated account ref object
     */
    private JSONObject getValidatedAccountRefObject(JSONObject accountRefObject, JSONArray accountArray) {

        JSONArray filteredAccountRefObjects = ConsentAuthUtil.getFilteredAccountsForAccountNumber(accountRefObject,
                accountArray);

        if (filteredAccountRefObjects.size() > 1) {
            // Multi currency account
            if (!accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY)) {
                for (Object object : filteredAccountRefObjects) {
                    JSONObject accountObject = (JSONObject) object;
                    if (Boolean.parseBoolean(accountObject.getAsString(ConsentExtensionConstants.IS_DEFAULT))) {
                        // Returning default currency account when it is a multi-currency account
                        // and the currency is not provided
                        return accountObject;
                    }
                }
            } else {
                for (Object object : filteredAccountRefObjects) {
                    JSONObject accountObject = (JSONObject) object;
                    if (accountObject.getAsString(ConsentExtensionConstants.CURRENCY)
                            .equalsIgnoreCase(accountRefObject.getAsString(ConsentExtensionConstants.CURRENCY))) {
                        return accountObject;
                    }
                }
            }
        } else if (filteredAccountRefObjects.size() == 1) {
            if (!accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY)) {
                return accountRefObject;
            }
            return null;
        }
        return null;
    }
}
