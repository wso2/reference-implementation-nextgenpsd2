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
 * Class to handle Account consent data retrieval for Authorize.
 */
public class AccountConsentRetrievalHandler implements ConsentRetrievalHandler {

    private static final Log log = LogFactory.getLog(AccountConsentRetrievalHandler.class);

    @Override
    public JSONObject getConsentData(ConsentResource consentResource) throws ConsentException {

        String permission = consentResource.getConsentAttributes().get(ConsentExtensionConstants.PERMISSION);

        try {
            String receiptString = consentResource.getReceipt();
            // This can be directly created to JSONObject since the payload is validated during initiation
            JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);
            return populateAccountsData(receiptJSON, permission);
        } catch (ParseException e) {
            log.error("Error while parsing retrieved consent data", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_DATA_RETRIEVE_ERROR);
        }
    }

    @Override
    public boolean validateAuthorizationStatus(ConsentResource consentResource, String authType) {

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
     * Method to populate accounts data.
     *
     * @param receipt Consent Receipt
     * @return a JSON object with accounts data in it
     */
    private static JSONObject populateAccountsData(JSONObject receipt, String permission) {

        JSONObject consentData = new JSONObject();
        JSONArray consentDataArray = new JSONArray();
        JSONObject dataElement = new JSONObject();

        // construct accounts data
        String recurringIndicator = receipt.getAsString(ConsentExtensionConstants.RECURRING_INDICATOR);
        consentDataArray.add(ConsentExtensionConstants.RECURRING_INDICATOR_TITLE + ": " + recurringIndicator);

        String validUntilDate = receipt.getAsString(ConsentExtensionConstants.VALID_UNTIL);
        consentDataArray.add(ConsentExtensionConstants.VALID_UNTIL_TITLE + ": " + validUntilDate);

        String frequencyPerDay = receipt.getAsString(ConsentExtensionConstants.FREQUENCY_PER_DAY);
        consentDataArray.add(ConsentExtensionConstants.FREQUENCY_PER_DAY_TITLE + ": " + frequencyPerDay);

        String combinedServiceIndicator = receipt.getAsString(ConsentExtensionConstants.COMBINED_SERVICE_INDICATOR);
        consentDataArray.add(ConsentExtensionConstants.COMBINED_SERVICE_INDICATOR_TITLE + ": "
                + combinedServiceIndicator);

        dataElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONSENT_DETAILS_TITLE);
        dataElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, consentDataArray);

        consentData.appendField(ConsentExtensionConstants.CONSENT_DETAILS, new JSONArray().appendElement(dataElement));

        // Access object and permission
        consentData.appendField(ConsentExtensionConstants.ACCESS_OBJECT, receipt.get(ConsentExtensionConstants.ACCESS));
        consentData.appendField(ConsentExtensionConstants.PERMISSION, permission);

        return consentData;
    }
}
