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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import org.wso2.openbanking.berlin.consent.extensions.authorize.utils.DataRetrievalUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Payment account list data retrieval for Authorize.
 */
public class PISAccountListRetrievalHandler implements AccountListRetrievalHandler {

    private static final Log log = LogFactory.getLog(PISAccountListRetrievalHandler.class);

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

        String paymentType = consentData.getConsentResource().getConsentType();
        appendAccountDataToConsentData(validatedAccountRefObject, consentDataJSON, paymentType);

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

    /**
     * This method appends the validated account reference object to the consent
     * data to be displayed in the consent page.
     *
     * @param validatedAccountRefObject validated account reference object
     * @param consentDataJSON consent data json to be displayed in the consent page
     * @param paymentType type of payment
     */
    private void appendAccountDataToConsentData(JSONObject validatedAccountRefObject, JSONObject consentDataJSON,
                                                String paymentType) {

        String accountRefType = ConsentExtensionUtil.getAccountReferenceType(validatedAccountRefObject);

        String accountNumber = validatedAccountRefObject.getAsString(accountRefType);
        String currency = validatedAccountRefObject.getAsString(ConsentExtensionConstants.CURRENCY);

        String accountReference = String.format("%s %s", accountRefType, accountNumber);
        if (currency != null) {
            accountReference += String.format(" (%s)", currency);
        }

        JSONArray consentDetails = (JSONArray) consentDataJSON.get(ConsentExtensionConstants.CONSENT_DETAILS);

        if (StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), paymentType)) {
            JSONObject debtorAccountElement = new JSONObject();

            debtorAccountElement.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.DEBTOR_ACCOUNT_TITLE);

            JSONArray dataWithAccountInfo = new JSONArray();
            dataWithAccountInfo.add(String.format(ConsentExtensionConstants.DEBTOR_REFERENCE_TITLE,
                accountRefType) + ": " + accountReference);

            debtorAccountElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, dataWithAccountInfo);

            JSONArray newConsentDetails = new JSONArray();
            newConsentDetails.appendElement(debtorAccountElement);
            newConsentDetails.addAll(consentDetails);

            consentDataJSON.put(ConsentExtensionConstants.CONSENT_DETAILS, newConsentDetails);
        } else {
            JSONObject consentDetail = (JSONObject) consentDetails.get(0);
            JSONArray data = (JSONArray) consentDetail.get(ConsentExtensionConstants.DATA_SIMPLE);
            data.add(0, ConsentExtensionConstants.ACCOUNT_REFERENCE_TITLE + ": " + accountReference);

            consentDetail.put(ConsentExtensionConstants.DATA_SIMPLE, data);
            consentDataJSON.put(ConsentExtensionConstants.CONSENT_DETAILS,
                    new JSONArray().appendElement(consentDetail));
        }
    }

    /**
     * Returns the validated account reference object after
     * considering payments service related multi-currency validations.
     *
     * @param accountRefObject account reference object from initiation payload
     * @param accountArray accounts array retrieved from bank backend
     * @return validated account ref object
     */
    private JSONObject getValidatedAccountRefObject(JSONObject accountRefObject, JSONArray accountArray) {

        JSONArray filteredAccountRefObjects = ConsentAuthUtil.getFilteredAccountsForAccountNumber(accountRefObject,
                accountArray);

        if (CommonConfigParser.getInstance().isPaymentDebtorAccountCurrencyValidationEnabled()) {
            if (filteredAccountRefObjects.size() > 1) {
                // Multi currency account
                if (!accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY)) {
                    return null;
                }
                for (Object object : filteredAccountRefObjects) {
                    JSONObject accountObject = (JSONObject) object;
                    if (accountObject.getAsString(ConsentExtensionConstants.CURRENCY)
                            .equalsIgnoreCase(accountRefObject.getAsString(ConsentExtensionConstants.CURRENCY))) {
                        return accountObject;
                    }
                }
                return null;
            } else if (filteredAccountRefObjects.size() == 1) {
                if (!accountRefObject.containsKey(ConsentExtensionConstants.CURRENCY)) {
                    return accountRefObject;
                }
                return null;
            }
        } else {
            return accountRefObject;
        }
        return null;
    }
}
