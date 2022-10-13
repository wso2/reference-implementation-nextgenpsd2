/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.utils;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Contains util methods need for authorization process.
 */
public class ConsentAuthUtil {

    private static final Log log = LogFactory.getLog(ConsentAuthUtil.class);

    /**
     * Returns the consent Id sent in the scope string of authorization request.
     *
     * @param scopeString the scope string of the request
     * @return the consent Id
     */
    public static String getConsentId(String scopeString) {

        String[] scopeArray = null;
        if (scopeString != null) {
            // The scopes string is split by a space here because the scopes are sent as space separated string
            scopeArray = scopeString.split(" ");
        }

        if (scopeArray != null) {
            for (String parameter : scopeArray) {
                if (StringUtils.startsWithIgnoreCase(parameter, "ais:") ||
                        StringUtils.startsWithIgnoreCase(parameter, "pis:") ||
                        StringUtils.startsWithIgnoreCase(parameter, "piis:")) {
                    if (parameter.split(CommonConstants.DELIMITER).length == 2) {
                        return parameter.split(CommonConstants.DELIMITER)[1];
                    } else {
                        return StringUtils.EMPTY;
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Validates the consent Id with provided consent type in scope string. If not valid, an error is sent to the
     * redirect URI of the request.
     *
     * @param consentType the consent type
     * @param scopeString the scope string sent in request
     * @param redirectUri the redirect URI of the request
     * @param state       the state of the request
     * @throws ConsentException thrown if a validation failure happen
     */
    public static void validateConsentTypeWithId(String consentType, String scopeString, URI redirectUri,
                                                 String state)
            throws ConsentException {

        log.debug("Validating whether the provided consent Id matches with the scope type");

        if (StringUtils.equals(ConsentTypeEnum.ACCOUNTS.toString(), consentType)
                && !StringUtils.contains(scopeString, CommonConstants.AIS_SCOPE)) {
            log.error(ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_SCOPE,
                            ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH, redirectUri, state));
        }

        if ((StringUtils.equals(ConsentTypeEnum.PAYMENTS.toString(), consentType)
                || StringUtils.equals(ConsentTypeEnum.BULK_PAYMENTS.toString(), consentType)
                || StringUtils.equals(ConsentTypeEnum.PERIODIC_PAYMENTS.toString(), consentType))
                && !StringUtils.contains(scopeString, CommonConstants.PIS_SCOPE)) {
            log.error(ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_SCOPE,
                            ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH, redirectUri, state));
        }

        if (StringUtils.equals(ConsentTypeEnum.FUNDS_CONFIRMATION.toString(), consentType)
                && !StringUtils.contains(scopeString, CommonConstants.PIIS_SCOPE)) {
            log.error(ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_SCOPE,
                            ErrorConstants.CONSENT_ID_AND_SCOPE_MISMATCH, redirectUri, state));
        }
    }

    /**
     * Method to construct TPP errors which needs to send as a redirect.
     *
     * @param state state parameter of the request
     * @return the constructed error JSON object
     */
    public static JSONObject constructRedirectErrorJson(AuthErrorCode authErrorCode,
                                                        String errorDescription, URI redirectUri, String state) {

        JSONObject errorObject = new JSONObject();
        errorObject.appendField("error", authErrorCode.toString());
        errorObject.appendField("error_description", errorDescription);
        errorObject.appendField("redirect_uri", redirectUri.toString());
        errorObject.appendField("state", state);
        return errorObject;
    }

    /**
     * Retrieves all the accounts for a particular account number in multi-currency scenarios.
     *
     * @param accountRefObject single account json object
     * @param accountArray     all the accounts json array
     * @return accounts array
     */
    public static JSONArray getFilteredAccountsForAccountNumber(JSONObject accountRefObject, JSONArray accountArray) {

        String accountRefType = ConsentExtensionUtil.getAccountReferenceType(accountRefObject);
        String accountNumber = accountRefObject.getAsString(accountRefType);
        JSONArray filteredAccountRefObjects = new JSONArray();

        // Filtering the accounts with the same account number
        for (Object object : accountArray) {
            JSONObject accountObject = (JSONObject) object;
            if (StringUtils.equals(accountObject.getAsString(accountRefType), accountNumber)) {
                filteredAccountRefObjects.add(accountObject);
            }
        }

        return filteredAccountRefObjects;
    }

    /**
     * This method checks whether all the authorization resources are authorized. There are two scenarios.
     *
     * 1. There can be only one authorization resource for a consent.
     *
     * In this scenario, this method will return true if the current authorization resource get the approval to update
     * to psuAuthenticated status. After the payment submission is successfully done, the BerlinConsentPersistStep will
     * do the real update of the authorization resource status. Otherwise, false will be returned to indicate that the
     * authorization resource of this consent is not in psuAuthenticated state. Therefore, the payment
     * submission/cancellation will not happen.
     *
     * 2. In multi level scenario, there can be multiple authorization resources per consent.
     *
     * In this scenario, this method will return true if all other authorization resources except current one are in
     * psuAuthenticates status. After the payment submission is successfully done, the BerlinConsentPersistStep will
     * do the real updating of the authorization resource status. Otherwise, false will be returned to indicate that
     * all the authorization resources related to the current consent are not in psuAuthenticated status. Therefore,
     * the payment submission/cancellation will not happen.
     *
     * @param consentCoreService consent core service
     * @param currentAuthResource the current authorization resource
     * @return true or false according to the aforementioned scenarios
     */
    public static boolean areAllOtherAuthResourcesValid(ConsentCoreService consentCoreService,
                                                        String consentId, AuthorizationResource currentAuthResource)
            throws ConsentManagementException {

        List<AuthorizationResource> authorizationResourcesOfCurrentConsent = consentCoreService
                .searchAuthorizations(consentId)
                .stream()
                .filter(authorisation -> StringUtils.equals(currentAuthResource.getAuthorizationType()
                        , authorisation.getAuthorizationType()))
                .collect(Collectors.toList());

        //Remove current authorization resource from the list
        authorizationResourcesOfCurrentConsent.removeIf(resource
                -> (StringUtils.equals(resource.getAuthorizationID(),
                currentAuthResource.getAuthorizationID())));

        if (authorizationResourcesOfCurrentConsent.isEmpty()) {
            return true;
        } else {
            return authorizationResourcesOfCurrentConsent.stream().allMatch(authorisation
                    -> StringUtils.equals(authorisation.getAuthorizationStatus(),
                    ScaStatusEnum.PSU_AUTHENTICATED.toString()));
        }
    }

    /**
     * This method contains the http client implementation to send the POST request to submit the payment to the bank.
     * The parameter "submissionType" determines whether the payment resource is submitted for the real payment to
     * happen or for the payment cancellation.
     *
     * @param paymentId ID of the payment to submit/cancel
     * @param paymentData payment data to be submitted to the backend
     * @param submissionType the submission type (payment submission or cancellation)
     * @return true if submission is a success, false otherwise
     * @throws OpenBankingException thrown if an error occurs when retrieving the http client
     * @throws IOException thrown if an error occurs executing the request
     */
    @Generated(message = "Excluding from coverage since this involves an external http call")
    public static boolean isPaymentResourceSubmitted(String paymentId, String paymentData, String submissionType)
            throws OpenBankingException, IOException {

        String paymentBackendURL = CommonConfigParser.getInstance().getPaymentsBackendURL();

        if (StringUtils.isBlank(paymentBackendURL)) {
            log.error("Payment backend URL is not configured");
            return false;
        }

        CloseableHttpClient client = HTTPClientUtils.getHttpsClient();
        HttpPost request = new HttpPost(paymentBackendURL + "/" + submissionType + "/" + paymentId);
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        StringEntity stringEntity = new StringEntity(paymentData);
        request.setEntity(stringEntity);
        HttpResponse response = client.execute(request);
        return (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_ACCEPTED);
    }
}
