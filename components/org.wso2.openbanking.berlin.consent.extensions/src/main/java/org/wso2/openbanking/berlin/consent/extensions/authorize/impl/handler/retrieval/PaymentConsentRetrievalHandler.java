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
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;

import java.util.Map;

/**
 * Class to handle Payment Consent data retrieval for Authorize.
 */
public class PaymentConsentRetrievalHandler implements ConsentRetrievalHandler {

    private static final Log log = LogFactory.getLog(PaymentConsentRetrievalHandler.class);

    @Override
    public JSONObject getConsentData(ConsentResource consentResource) throws ConsentException {

        try {
            String receiptString = consentResource.getReceipt();
            // This can be directly created to JSONObject since the payload is validated during initiation
            JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);

            JSONObject consentDataJSON;

            if (StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), consentResource.getConsentType())) {
                consentDataJSON = populateSinglePaymentData(receiptJSON);
            } else if (StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(),
                    consentResource.getConsentType())) {
                consentDataJSON = populatePeriodicPaymentData(receiptJSON);
            } else {
                consentDataJSON = populateBulkPaymentData(receiptJSON);
            }
            return consentDataJSON;
        } catch (ParseException e) {
            log.error("Error while parsing retrieved consent data", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_DATA_RETRIEVE_ERROR);
        }
    }

    @Override
    public boolean validateAuthorizationStatus(ConsentResource consentResource, String authType) {

        String consentStatus = consentResource.getCurrentStatus();
        if (log.isDebugEnabled()) {
            log.debug("Checking whether the consent with Id: " + consentResource.getConsentID() + "is in an " +
                    "applicable status to authorize");
        }

        if (StringUtils.equals(AuthTypeEnum.CANCELLATION.toString(), authType)) {
            return true;
        } else {
            return StringUtils.equals(TransactionStatusEnum.RCVD.name(), consentStatus)
                    || StringUtils.equals(TransactionStatusEnum.PATC.name(), consentStatus);
        }
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentData consentData) {
        return null;
    }

    /**
     * Method to populate single payments data.
     *
     * @param receipt   Consent Receipt
     * @return a JSON object with single payments data in it
     */
    private static JSONObject populateSinglePaymentData(JSONObject receipt) {

        JSONObject consentData = new JSONObject();
        JSONArray consentDataArray = new JSONArray();
        JSONObject dataElement = new JSONObject();

        populateCommonData(receipt, consentDataArray);

        dataElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.REQUESTED_DATA_TITLE);
        dataElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, consentDataArray);

        consentData.appendField(ConsentExtensionConstants.CONSENT_DETAILS, new JSONArray().appendElement(dataElement));

        // Debtor account ref object
        consentData.appendField(ConsentExtensionConstants.ACCOUNT_REF_OBJECT,
                receipt.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));

        return consentData;
    }

    /**
     * Method to populate bulk payments data.
     *
     * @param receipt Consent Receipt
     * @return a JSON object with bulk payments data in it
     */
    private static JSONObject populateBulkPaymentData(JSONObject receipt) {

        JSONObject consentData = new JSONObject();
        JSONArray consentDetails = new JSONArray();

        JSONArray paymentsArray = (JSONArray) receipt.get(ConsentExtensionConstants.PAYMENTS);

        for (int paymentIndex = 0; paymentIndex < paymentsArray.size(); paymentIndex++) {
            JSONObject bulkPayment = (JSONObject) paymentsArray.get(paymentIndex);
            JSONObject dataElement = new JSONObject();
            JSONArray consentDataArray = new JSONArray();

            populateCommonData(bulkPayment, consentDataArray);

            int paymentNumber = paymentIndex + 1;
            dataElement.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.PAYMENT_TITLE + paymentNumber);
            dataElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, consentDataArray);
            consentDetails.add(dataElement);
        }
        consentData.appendField(ConsentExtensionConstants.CONSENT_DETAILS, consentDetails);

        // Debtor account ref object
        consentData.appendField(ConsentExtensionConstants.ACCOUNT_REF_OBJECT,
                receipt.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));

        return consentData;
    }

    /**
     * Method to populate periodic payments data.
     *
     * @param receipt Consent Receipt
     * @return a JSON object with periodic payments data in it
     */
    private static JSONObject populatePeriodicPaymentData(JSONObject receipt) {

        JSONObject consentData = new JSONObject();
        JSONArray consentDataArray = new JSONArray();
        JSONObject dataElement = new JSONObject();

        populateCommonData(receipt, consentDataArray);

        consentDataArray.add(ConsentExtensionConstants.START_DATE_TITLE + ": "
                + receipt.getAsString(ConsentExtensionConstants.START_DATE));

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.END_DATE))) {
            consentDataArray.add(ConsentExtensionConstants.END_DATE_TITLE + ": "
                    + receipt.getAsString(ConsentExtensionConstants.END_DATE));
        }

        consentDataArray.add(ConsentExtensionConstants.FREQUENCY_TITLE + ": "
                + receipt.getAsString(ConsentExtensionConstants.FREQUENCY));

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.EXECUTION_RULE))) {
            consentDataArray.add(ConsentExtensionConstants.EXECUTION_RULE_TITLE + ": "
                    + receipt.getAsString(ConsentExtensionConstants.EXECUTION_RULE));
        }

        dataElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.REQUESTED_DATA_TITLE);
        dataElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, consentDataArray);

        consentData.appendField(ConsentExtensionConstants.CONSENT_DETAILS, new JSONArray().appendElement(dataElement));

        // Debtor account ref object
        consentData.appendField(ConsentExtensionConstants.ACCOUNT_REF_OBJECT,
                receipt.get(ConsentExtensionConstants.DEBTOR_ACCOUNT));

        return consentData;
    }

    /**
     * Method to populate common payments data.
     *
     * @param receipt Consent Receipt
     * @param paymentDataArray Consent Data JSON
     * @return a JSON array with single payments data in it
     */
    private static void populateCommonData(JSONObject receipt, JSONArray paymentDataArray) {

        JSONObject instructedAmount = (JSONObject) receipt.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);

        paymentDataArray.add(ConsentExtensionConstants.INSTRUCTED_AMOUNT_TITLE + ": "
                + instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT));
        paymentDataArray.add(ConsentExtensionConstants.INSTRUCTED_CURRENCY_TITLE + ": "
                + instructedAmount.getAsString(ConsentExtensionConstants.CURRENCY));

        paymentDataArray.add(ConsentExtensionConstants.CREDITOR_NAME_TITLE + ": "
                + receipt.getAsString(ConsentExtensionConstants.CREDITOR_NAME));

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.CREDITOR_AGENT))) {
            paymentDataArray.add(ConsentExtensionConstants.CREDITOR_AGENT_TITLE + ": "
                    + receipt.getAsString(ConsentExtensionConstants.CREDITOR_AGENT));
        }

        JSONObject creditorAccount = (JSONObject) receipt.get(ConsentExtensionConstants.CREDITOR_ACCOUNT);

        // This validation is done separately for creditor account since we do not configure a specific account
        // reference type for the creditor account reference type. We can expect any type of creditor account
        // reference in the initiation payload.
        String creditorAccRefType = ConsentExtensionUtil.getAccountReferenceType(creditorAccount);
        if (StringUtils.isNotBlank(creditorAccRefType)) {
            paymentDataArray.add(String.format(ConsentExtensionConstants.CREDITOR_REFERENCE_TITLE,
                    creditorAccRefType) + ": " + creditorAccount.get(creditorAccRefType));
        }

        if (creditorAccount.containsKey(ConsentExtensionConstants.CURRENCY)
                && StringUtils.isNotBlank(creditorAccount.getAsString(ConsentExtensionConstants.CURRENCY))) {
            paymentDataArray.add(ConsentExtensionConstants.CREDITOR_ACCOUNT_CURRENCY_TITLE + ": "
                    + creditorAccount.get(ConsentExtensionConstants.CURRENCY));
        }

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED))) {
            paymentDataArray.add(ConsentExtensionConstants.REMITTANCE_INFORMATION_UNSTRUCTURED_TITLE + ": "
                    + receipt.getAsString(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED));
        }

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION))) {
            paymentDataArray.add(ConsentExtensionConstants.END_TO_END_IDENTIFICATION_TITLE + ": "
                    + receipt.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION));
        }
    }
}
