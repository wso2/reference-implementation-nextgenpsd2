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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Class to handle Payment Consent data retrieval for Authorize.
 */
public class PaymentConsentRetrievalHandler implements ConsentRetrievalHandler {

    private static final Log log = LogFactory.getLog(PaymentConsentRetrievalHandler.class);

    @Override
    public JSONArray getConsentDataSet(ConsentResource consentResource) throws ConsentException {

        try {
            String receiptString = consentResource.getReceipt();
            // This can be directly created to JSONObject since the payload is validated during initiation
            JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);

            JSONArray consentDataJSON;

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
            log.error("Error while parsing retrieved consent data");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_DATA_RETRIEVE_ERROR);
        }
    }

    @Override
    public boolean validateAuthorizationStatus(ConsentResource consentResource, String authType) {

        boolean isApplicable = false;
        String consentStatus = consentResource.getCurrentStatus();

        if (StringUtils.equals(AuthTypeEnum.AUTHORISATION.toString(), authType)) {
            isApplicable = StringUtils.equals(TransactionStatusEnum.RCVD.name(), consentStatus);
        }

        if (StringUtils.equals(AuthTypeEnum.CANCELLATION.toString(), authType)) {
            isApplicable = StringUtils.equals(TransactionStatusEnum.ACTC.name(), consentStatus);
        }

        if (log.isDebugEnabled()) {
            log.debug("The consent with Id: " + consentResource.getConsentID() + "is in an applicable status("
                    + consentStatus + ") to authorize");
        }
        return isApplicable;
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentData consentData) {
        return null;
    }

    /**
     * Method to populate single payments data.
     *
     * @param receipt   Consent Receipt
     * @return a JSON array with single payments data in it
     */
    private static JSONArray populateSinglePaymentData(JSONObject receipt) {

        JSONArray singlePaymentDataArray = new JSONArray();
        JSONObject singlePaymentElement = new JSONObject();
        JSONArray consentDataJSON = new JSONArray();

        populateDebtorAccountData(receipt, singlePaymentDataArray);
        populateCommonData(receipt, singlePaymentDataArray);

        singlePaymentElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.REQUESTED_DATA_TITLE);
        singlePaymentElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, singlePaymentDataArray);
        consentDataJSON.add(singlePaymentElement);

        return consentDataJSON;
    }

    /**
     * Method to populate bulk payments data.
     *
     * @param receipt Consent Receipt
     * @return a JSON array with bulk payments data in it
     */
    private static JSONArray populateBulkPaymentData(JSONObject receipt) {

        JSONObject debtorAccountElement = new JSONObject();
        JSONArray debtorAccountArray = new JSONArray();
        JSONArray consentDataJSON = new JSONArray();

        populateDebtorAccountData(receipt, debtorAccountArray);
        debtorAccountElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.DEBTOR_ACCOUNT_TITLE);
        debtorAccountElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, debtorAccountArray);
        consentDataJSON.add(debtorAccountElement);

        JSONArray paymentsArray = (JSONArray) receipt.get(ConsentExtensionConstants.PAYMENTS);

        for (int paymentIndex = 0; paymentIndex < paymentsArray.size(); paymentIndex++) {
            JSONObject bulkPayment = (JSONObject) paymentsArray.get(paymentIndex);
            JSONObject bulkPaymentElement = new JSONObject();
            JSONArray bulkPaymentDataArray = new JSONArray();

            populateCommonData(bulkPayment, bulkPaymentDataArray);

            int paymentNumber = paymentIndex + 1;
            bulkPaymentElement.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.PAYMENT_TITLE + paymentNumber);
            bulkPaymentElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, bulkPaymentDataArray);
            consentDataJSON.add(bulkPaymentElement);
        }
        return consentDataJSON;
    }

    /**
     * Method to populate periodic payments data.
     *
     * @param receipt Consent Receipt
     * @return a JSON array with periodic payments data in it
     */
    private static JSONArray populatePeriodicPaymentData(JSONObject receipt) {

        JSONArray periodicPaymentDataArray = new JSONArray();
        JSONObject periodicPaymentElement = new JSONObject();
        JSONArray consentDataJSON = new JSONArray();

        populateCommonData(receipt, periodicPaymentDataArray);

        periodicPaymentDataArray.add(ConsentExtensionConstants.START_DATE_TITLE + " : "
                + receipt.getAsString(ConsentExtensionConstants.START_DATE));

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.END_DATE))) {
            periodicPaymentDataArray.add(ConsentExtensionConstants.END_DATE_TITLE + " : "
                    + receipt.getAsString(ConsentExtensionConstants.END_DATE));
        }

        periodicPaymentDataArray.add(ConsentExtensionConstants.FREQUENCY_TITLE + " : "
                + receipt.getAsString(ConsentExtensionConstants.FREQUENCY));

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.EXECUTION_RULE))) {
            periodicPaymentDataArray.add(ConsentExtensionConstants.EXECUTION_RULE_TITLE + " : "
                    + receipt.getAsString(ConsentExtensionConstants.EXECUTION_RULE));
        }

        periodicPaymentElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.REQUESTED_DATA_TITLE);
        periodicPaymentElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, periodicPaymentDataArray);
        consentDataJSON.add(periodicPaymentElement);

        return consentDataJSON;
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

        paymentDataArray.add(ConsentExtensionConstants.INSTRUCTED_AMOUNT_TITLE + " : "
                + instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT));
        paymentDataArray.add(ConsentExtensionConstants.CURRENCY_TITLE + " : "
                + instructedAmount.getAsString(ConsentExtensionConstants.CURRENCY));

        paymentDataArray.add(ConsentExtensionConstants.CREDITOR_NAME_TITLE + " : "
                + receipt.getAsString(ConsentExtensionConstants.CREDITOR_NAME));

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.CREDITOR_AGENT))) {
            paymentDataArray.add(ConsentExtensionConstants.CREDITOR_AGENT_TITLE + " : "
                    + receipt.getAsString(ConsentExtensionConstants.CREDITOR_AGENT));
        }

        JSONObject creditorAccount = (JSONObject) receipt.get(ConsentExtensionConstants.CREDITOR_ACCOUNT);

        // This validation is done separately for creditor account since we do not configure a specific account
        // reference type for the creditor account reference type. We can expect any type of creditor account
        // reference in the initiation payload.
        if (creditorAccount.containsKey(ConsentExtensionConstants.IBAN)
                && StringUtils.isNotBlank(creditorAccount.getAsString(ConsentExtensionConstants.IBAN))) {
            paymentDataArray.add(String.format(ConsentExtensionConstants.CREDITOR_REFERENCE_TITLE,
                    ConsentExtensionConstants.IBAN) + " : " + creditorAccount.get(ConsentExtensionConstants.IBAN));
        } else if (creditorAccount.containsKey(ConsentExtensionConstants.BBAN)
                && StringUtils.isNotBlank(creditorAccount.getAsString(ConsentExtensionConstants.BBAN))) {
            paymentDataArray.add(String.format(ConsentExtensionConstants.CREDITOR_REFERENCE_TITLE,
                    ConsentExtensionConstants.BBAN) + " : " + creditorAccount.get(ConsentExtensionConstants.BBAN));
        } else if (creditorAccount.containsKey(ConsentExtensionConstants.PAN)
                && StringUtils.isNotBlank(creditorAccount.getAsString(ConsentExtensionConstants.PAN))) {
            paymentDataArray.add(String.format(ConsentExtensionConstants.CREDITOR_REFERENCE_TITLE,
                    ConsentExtensionConstants.PAN) + " : " + creditorAccount.get(ConsentExtensionConstants.PAN));
        } else if (creditorAccount.containsKey(ConsentExtensionConstants.MASKED_PAN)
                && StringUtils.isNotBlank(creditorAccount.getAsString(ConsentExtensionConstants.MASKED_PAN))) {
            paymentDataArray.add(String.format(ConsentExtensionConstants.CREDITOR_REFERENCE_TITLE,
                    ConsentExtensionConstants.MASKED_PAN) + " : "
                    + creditorAccount.get(ConsentExtensionConstants.MASKED_PAN));
        } else if (creditorAccount.containsKey(ConsentExtensionConstants.MSISDN)
                && StringUtils.isNotBlank(creditorAccount.getAsString(ConsentExtensionConstants.MSISDN))) {
            paymentDataArray.add(String.format(ConsentExtensionConstants.CREDITOR_REFERENCE_TITLE,
                    ConsentExtensionConstants.MSISDN) + " : " + creditorAccount.get(ConsentExtensionConstants.MSISDN));
        }

        if (creditorAccount.containsKey(ConsentExtensionConstants.CURRENCY)
                && StringUtils.isNotBlank(creditorAccount.getAsString(ConsentExtensionConstants.CURRENCY))) {
            paymentDataArray.add(ConsentExtensionConstants.CURRENCY_TITLE + " : "
                    + creditorAccount.get(ConsentExtensionConstants.CURRENCY));
        }

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED))) {
            paymentDataArray.add(ConsentExtensionConstants.REMITTANCE_INFORMATION_UNSTRUCTURED_TITLE + " : "
                    + receipt.getAsString(ConsentExtensionConstants.REMITTANCE_INFO_UNSTRUCTURED));
        }

        if (StringUtils.isNotBlank(receipt.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION))) {
            paymentDataArray.add(ConsentExtensionConstants.END_TO_END_IDENTIFICATION_TITLE + " : "
                    + receipt.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION));
        }
    }

    /**
     * Method to populate debtor account data for payments data.
     *
     * @param receipt Consent Receipt
     * @param paymentDataArray Consent Data JSON
     * @return a JSON array with debtor account data in it
     */
    private static void populateDebtorAccountData(JSONObject receipt, JSONArray paymentDataArray) {

        String debtorAccountReference;
        JSONObject debtorAccountElement = (JSONObject) receipt.get(ConsentExtensionConstants.DEBTOR_ACCOUNT);
        String configuredAccountReference = CommonConfigParser.getInstance().getAccountReferenceType();

        if (StringUtils.equals(ConsentExtensionConstants.IBAN, configuredAccountReference)) {
            debtorAccountReference = debtorAccountElement.getAsString(ConsentExtensionConstants.IBAN);
        } else if (StringUtils.equals(ConsentExtensionConstants.BBAN, configuredAccountReference)) {
            debtorAccountReference = debtorAccountElement.getAsString(ConsentExtensionConstants.BBAN);
        } else {
            debtorAccountReference = debtorAccountElement.getAsString(ConsentExtensionConstants.PAN);
        }

        paymentDataArray.add(String.format(ConsentExtensionConstants.DEBTOR_REFERENCE_TITLE,
                configuredAccountReference) + " : " + debtorAccountReference);
    }
}
