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

package com.wso2.openbanking.berlin.consent.extensions.authorize.utils;

import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Data retrieval util class.
 */
public class DataRetrievalUtil {

    private static final Log log = LogFactory.getLog(DataRetrievalUtil.class);

    /**
     * Returns the account list string after fetching from the bank backend.
     *
     * @param shareableAccountsRetrieveUrl shareable accounts retrieval URL
     * @param parameters                   URL params
     * @param headers                      request headers
     * @return retrieved accounts
     */
    public static String getAccountsFromEndpoint(String shareableAccountsRetrieveUrl, Map<String, String> parameters,
                                                 Map<String, String> headers) {

        String retrieveUrl = "";
        if (!shareableAccountsRetrieveUrl.endsWith("/")) {
            retrieveUrl = shareableAccountsRetrieveUrl + "/";
        } else {
            retrieveUrl = shareableAccountsRetrieveUrl;
        }
        if (!parameters.isEmpty()) {
            retrieveUrl = buildRequestURL(retrieveUrl, parameters);
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Sharable accounts retrieve endpoint : %s", retrieveUrl));
        }

        BufferedReader reader = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(retrieveUrl);
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> key : headers.entrySet()) {
                    if (key.getKey() != null && key.getValue() != null) {
                        request.addHeader(key.getKey(), key.getValue());
                    }
                }
            }
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Retrieving sharable accounts failed");
                return null;
            } else {
                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8.toString()));
                String inputLine;
                StringBuffer buffer = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    buffer.append(inputLine);
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Sharable accounts endpoints returned : %s", buffer));
                }
                return buffer.toString();
            }
        } catch (IOException e) {
            log.error("Exception occurred while retrieving sharable accounts", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing buffered reader", e);
                }
            }
        }

        return null;
    }

    /**
     * Build the complete URL with query parameters sent in the map.
     *
     * @param baseURL    the base URL
     * @param parameters map of parameters
     * @return the output URL
     */
    private static String buildRequestURL(String baseURL, Map<String, String> parameters) {

        List<NameValuePair> pairs = new ArrayList<>();

        for (Map.Entry<String, String> key : parameters.entrySet()) {
            if (key.getKey() != null && key.getValue() != null) {
                pairs.add(new BasicNameValuePair(key.getKey(), key.getValue()));
            }
        }
        String queries = URLEncodedUtils.format(pairs, StandardCharsets.UTF_8.toString());
        return baseURL + "?" + queries;
    }

    /**
     * Gets account details using the initiation payload.
     *
     * @param accessObject access object
     * @param userId       user id
     * @return accounts array
     */
    public static JSONArray getAccountsFromPayload(JSONObject accessObject, String userId) {

        JSONArray accountData = new JSONArray();
        JSONArray bankOfferedAccounts = getAccountsFromEndpoint(userId);

        if (bankOfferedAccounts == null) {
            log.error("No accounts found");
            return null;
        }

        JSONArray accounts = (JSONArray) accessObject.get(AccessMethodEnum.ACCOUNTS.toString());
        JSONArray balances = (JSONArray) accessObject.get(AccessMethodEnum.BALANCES.toString());
        JSONArray transactions = (JSONArray) accessObject.get(AccessMethodEnum.TRANSACTIONS.toString());

        // Avoid collecting currency details if multi currency is not enabled
        if (!CommonConfigParser.getInstance().isMultiCurrencyEnabled()) {
            List<JSONArray> referencesList = new ArrayList<>();
            referencesList.add(accounts);
            referencesList.add(balances);
            referencesList.add(transactions);
            removeCurrencyType(referencesList);
        }

        JSONArray allAccounts = new JSONArray();

        /*
        Access methods 'balances' and 'transactions' includes 'accounts' permissions.
        The access methods are added in scenarios where the accounts are finalised with
        the access methods, not adding them for bank offered consent scenarios since the
        accounts are yet to be given access methods.

        If access method is an empty array then add bank offered accounts.
        If not empty array then add the account details from initiation payload.
         */
        if (balances != null && balances.isEmpty()) {
            JSONArray permissionArray = new JSONArray();
            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.BALANCES_PERMISSION);

            JSONArray accessMethodArray = new JSONArray();

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, bankOfferedAccounts);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.SELECT_BALANCE);

            accountData.add(object);
        } else if (balances != null) {
            JSONArray permissionArray = new JSONArray();
            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.BALANCES_PERMISSION);

            JSONArray accessMethodArray = new JSONArray();
            accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
            accessMethodArray.add(AccessMethodEnum.BALANCES.toString());

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, balances);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_BALANCE);

            accountData.add(object);
            allAccounts.addAll(balances);
        }

        if (transactions != null && transactions.isEmpty()) {
            JSONArray permissionArray = new JSONArray();
            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.TRANSACTIONS_PERMISSION);

            JSONArray accessMethodArray = new JSONArray();

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, bankOfferedAccounts);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.SELECT_TRANSACTION);

            accountData.add(object);
        } else if (transactions != null) {
            JSONArray permissionArray = new JSONArray();
            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);
            permissionArray.add(ConsentExtensionConstants.TRANSACTIONS_PERMISSION);

            JSONArray accessMethodArray = new JSONArray();
            accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());
            accessMethodArray.add(AccessMethodEnum.TRANSACTIONS.toString());

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, transactions);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_TRANSACTION);

            accountData.add(object);
            allAccounts.addAll(transactions);
        }

        if (accounts != null && accounts.isEmpty()) {
            JSONArray permissionArray = new JSONArray();
            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);

            JSONArray accessMethodArray = new JSONArray();

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, bankOfferedAccounts);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.SELECT_ACCOUNT);

            accountData.add(object);
        } else if (accounts != null) {
            JSONArray permissionArray = new JSONArray();
            permissionArray.add(ConsentExtensionConstants.ACCOUNTS_PERMISSION);

            JSONArray accessMethodArray = new JSONArray();
            accessMethodArray.add(AccessMethodEnum.ACCOUNTS.toString());

            JSONObject object = new JSONObject();
            object.put(ConsentExtensionConstants.PERMISSIONS, permissionArray);
            object.put(ConsentExtensionConstants.ACCOUNT_NUMBERS, accounts);
            object.put(ConsentExtensionConstants.ACCESS_METHODS, accessMethodArray);
            object.put(ConsentExtensionConstants.ACCOUNT_TYPE, ConsentExtensionConstants.STATIC_ACCOUNT);

            accountData.add(object);
            allAccounts.addAll(accounts);
        }

        if (allAccounts.stream().allMatch(bankOfferedAccounts::contains)) {
            return accountData;
        } else {
            log.error("Consent accounts mismatch");
            return null;
        }
    }

    /**
     * Get accounts array from endpoint.
     *
     * @param userId user id
     * @return array of accounts
     */
    public static JSONArray getAccountsFromEndpoint(String userId) {

        CommonConfigParser configParser = CommonConfigParser.getInstance();
        String accountsURL = configParser.getShareableAccountsRetrieveEndpoint();

        if (!accountsURL.endsWith("/")) {
            accountsURL += "/";
        }
        accountsURL += userId;

        if (log.isDebugEnabled()) {
            log.debug(String.format("Getting accounts details from backend endpoint %s", accountsURL));
        }
        String accountData = DataRetrievalUtil.getAccountsFromEndpoint(accountsURL, new HashMap<>(), new HashMap<>());
        if (accountData == null) {
            log.error("No account details available");
            return null;
        }

        JSONArray accountArray = new JSONArray();
        try {
            JSONObject accountJson = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(accountData);

            String accountRefType = ConsentExtensionConstants.IBAN;
            JSONArray slideContent = (JSONArray) accountJson.get("accounts");
            Iterator i = slideContent.iterator();

            while (i.hasNext()) {
                JSONObject slide = (JSONObject) i.next();
                String account = (String) slide.get(accountRefType);

                JSONObject accountObject = new JSONObject();
                accountObject.put(accountRefType, account);
                accountArray.add(accountObject);
            }

            return accountArray;
        } catch (ParseException e) {
            log.error(ErrorConstants.JSON_PARSE_ERROR, e);
            return null;
        }
    }

    /**
     * Avoid collecting currency data if multi currency is disabled.
     *
     * @param referencesList account references json arrays list
     */
    private static void removeCurrencyType(List<JSONArray> referencesList) {

        // Loops through accounts, balances, transactions json arrays
        for (JSONArray reference : referencesList) {
            for (Object referenceObject : reference) {
                JSONObject referenceObjectJson = (JSONObject) referenceObject;
                referenceObjectJson.remove(ConsentExtensionConstants.CURRENCY);
            }
        }
    }
}
