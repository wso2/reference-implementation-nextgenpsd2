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
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.DataRetrievalUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Payment account list data retrieval for Authorize.
 */
public class PaymentAccountListRetrievalHandler implements AccountListRetrievalHandler {

    private static final Log log = LogFactory.getLog(PaymentAccountListRetrievalHandler.class);

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

        JSONObject validatedAccountRefObject = getValidatedAccountRefObject(accountRefObject, userAccountsArray);

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

    private void appendAccountDataToConsentData(JSONObject validatedAccountRefObject, JSONObject consentDataJSON,
                                                String paymentType) {

        String configuredAccountRefType = CommonConfigParser.getInstance().getAccountReferenceType();
        JSONArray dataWithAccountInfo = new JSONArray();

        String accountNumber = validatedAccountRefObject.getAsString(configuredAccountRefType);
        String currency = validatedAccountRefObject.getAsString(ConsentExtensionConstants.CURRENCY);

        String accountReference = accountNumber;
        if (currency != null) {
            accountReference += String.format(" (%s)", currency);
        }

        dataWithAccountInfo.add(String.format(ConsentExtensionConstants.DEBTOR_REFERENCE_TITLE,
                configuredAccountRefType) + " : " + accountReference);
        JSONArray consentDetails = (JSONArray) consentDataJSON.get(ConsentExtensionConstants.CONSENT_DETAILS);

        if (StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), paymentType)) {
            JSONObject debtorAccountElement = new JSONObject();

            debtorAccountElement.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.DEBTOR_ACCOUNT_TITLE);
            debtorAccountElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, dataWithAccountInfo);

            JSONArray newConsentDetails = new JSONArray();
            newConsentDetails.appendElement(debtorAccountElement);
            newConsentDetails.addAll(consentDetails);

            consentDataJSON.put(ConsentExtensionConstants.CONSENT_DETAILS, newConsentDetails);
        } else {
            JSONObject consentDetail = (JSONObject) consentDetails.get(0);
            JSONArray data = (JSONArray) consentDetail.get(ConsentExtensionConstants.DATA_SIMPLE);
            dataWithAccountInfo.addAll(data);

            consentDetail.put(ConsentExtensionConstants.DATA_SIMPLE, dataWithAccountInfo);
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
        return null;
    }
}
