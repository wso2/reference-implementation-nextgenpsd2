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
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;

import java.util.Map;

/**
 * Class to handle funds confirmations data retrieval for Authorize.
 */
public class FundsConfirmationConsentRetrievalHandler implements ConsentRetrievalHandler {

    private static final Log log = LogFactory.getLog(FundsConfirmationConsentRetrievalHandler.class);

    @Override
    public JSONObject getConsentData(ConsentResource consentResource) throws ConsentException {

        try {
            String receiptString = consentResource.getReceipt();
            // This can be directly created to JSONObject since the payload is validated during initiation
            JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);
            return populateFundsConfirmationData(receiptJSON);
        } catch (ParseException e) {
            log.error("Error while parsing retrieved consent data", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_DATA_RETRIEVE_ERROR);
        }
    }

    @Override
    public boolean validateAuthorizationStatus(ConsentResource consentResource, String authStatus) {

        String consentStatus = consentResource.getCurrentStatus();
        boolean isApplicable = StringUtils.equals(ConsentStatusEnum.RECEIVED.toString(), consentStatus)
                || StringUtils.equals(ConsentStatusEnum.PARTIALLY_AUTHORISED.toString(), consentStatus);

        if (log.isDebugEnabled()) {
            log.debug(String.format("The consent with Id: %s is in %s status. It is %s to authorize",
                    consentResource.getConsentID(), consentStatus, isApplicable ? "applicable" : "not applicable"));
        }
        return isApplicable;
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentData consentData) {
        return null;
    }

    /**
     * Method to populate funds confirmation data.
     *
     * @param receipt Consent Receipt
     * @return a JSON object with funds confirmation data in it
     */
    private static JSONObject populateFundsConfirmationData(JSONObject receipt) {

        JSONObject consentData = new JSONObject();
        JSONArray consentDataArray = new JSONArray();
        JSONObject dataElement = new JSONObject();

        // construct funds confirmation data
        if (receipt.containsKey(ConsentExtensionConstants.CARD_NUMBER)
                && receipt.getAsString(ConsentExtensionConstants.CARD_NUMBER) != null) {
            String cardNumber = receipt.getAsString(ConsentExtensionConstants.CARD_NUMBER);
            consentDataArray.add(ConsentExtensionConstants.CARD_NUMBER_TITLE + ": " + cardNumber);
        }

        if (receipt.containsKey(ConsentExtensionConstants.CARD_EXPIRY_DATE)
                && receipt.getAsString(ConsentExtensionConstants.CARD_EXPIRY_DATE) != null) {
            String cardExpiryDate = receipt.getAsString(ConsentExtensionConstants.CARD_EXPIRY_DATE);
            consentDataArray.add(ConsentExtensionConstants.CARD_EXPIRY_DATE_TITLE + ": " + cardExpiryDate);
        }

        if (receipt.containsKey(ConsentExtensionConstants.CARD_INFORMATION)
                && receipt.getAsString(ConsentExtensionConstants.CARD_INFORMATION) != null) {
            String cardInformation = receipt.getAsString(ConsentExtensionConstants.CARD_INFORMATION);
            consentDataArray.add(ConsentExtensionConstants.CARD_INFORMATION_TITLE + ": " + cardInformation);
        }

        dataElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONSENT_DETAILS_TITLE);
        dataElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, consentDataArray);

        consentData.appendField(ConsentExtensionConstants.CONSENT_DETAILS, new JSONArray().appendElement(dataElement));

        // Account ref object
        consentData.appendField(ConsentExtensionConstants.ACCOUNT_REF_OBJECT,
                receipt.get(ConsentExtensionConstants.ACCOUNT));

        return consentData;
    }
}
