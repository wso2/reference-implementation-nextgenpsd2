/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.utils;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
     * @param accountsURL shareable accounts retrieval URL
     * @param parameters  URL params
     * @param headers     request headers
     * @return retrieved accounts string from endpoint
     *
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    public static String getAccountsFromEndpoint(String accountsURL, Map<String, String> parameters,
                                                 Map<String, String> headers) {

        String retrieveUrl = "";
        if (!accountsURL.endsWith("/")) {
            retrieveUrl = accountsURL + "/";
        } else {
            retrieveUrl = accountsURL;
        }
        if (!parameters.isEmpty()) {
            retrieveUrl = buildRequestURL(retrieveUrl, parameters);
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Accounts retrieve endpoint : %s", retrieveUrl));
        }

        BufferedReader reader = null;
        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {

            /* No user input is used for constructing this "retrieverUrl". A configuration is used to get the base URL
            for this. Therefore, the threat of HTTP_PARAMETER_POLLUTION is not present. */
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
        } catch (IOException | OpenBankingException e) {
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
     * Get accounts array from endpoint.
     *
     * @param userId      user id
     * @param accountsURL accounts base URL
     * @param parameters  URL parameters
     * @param headers     request headers
     * @return array of accounts
     */
    public static JSONArray getAccountsFromEndpoint(String userId, String accountsURL, Map<String, String> parameters,
                                                    Map<String, String> headers) {

        if (!accountsURL.endsWith("/")) {
            accountsURL += "/";
        }
        accountsURL += userId;

        if (log.isDebugEnabled()) {
            log.debug(String.format("Getting accounts details from backend endpoint %s", accountsURL));
        }
        String accountData = DataRetrievalUtil.getAccountsFromEndpoint(accountsURL, parameters, headers);
        if (accountData == null) {
            log.error("No account details available");
            return null;
        }

        JSONArray accountArray = new JSONArray();
        try {
            JSONObject accountJson = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(accountData);

            JSONArray slideContent = (JSONArray) accountJson.get("accounts");
            Iterator i = slideContent.iterator();

            while (i.hasNext()) {
                JSONObject slide = (JSONObject) i.next();
                String accountRefType = ConsentExtensionUtil.getAccountReferenceType(slide);

                if (accountRefType == null) {
                    continue;
                }

                String accountId = (String) slide.get(accountRefType);

                JSONObject accountObject = new JSONObject();
                accountObject.put(accountRefType, accountId);
                accountObject.put(ConsentExtensionConstants.CURRENCY,
                        slide.get(ConsentExtensionConstants.CURRENCY));
                accountObject.put(ConsentExtensionConstants.IS_DEFAULT,
                        slide.get(ConsentExtensionConstants.IS_DEFAULT));

                accountArray.add(accountObject);
            }

            // Remove currency information from non multi-currency accounts
            return removeCurrencyInfoFromSingleCurrencyAccounts(accountArray);
        } catch (ParseException e) {
            log.error(ErrorConstants.JSON_PARSE_ERROR, e);
            return null;
        }
    }

    /**
     * Returns the accounts array provided after removing the currency information from single currency accounts.
     *
     * @param accountArray JSON array containing account info from bank back end
     * @return accounts array without currency information for single currency accounts
     */
    private static JSONArray removeCurrencyInfoFromSingleCurrencyAccounts(JSONArray accountArray) {

        for (Object object : accountArray) {
            JSONObject accountObject = (JSONObject) object;
            if (ConsentAuthUtil.getFilteredAccountsForAccountNumber(accountObject, accountArray).size() == 1) {
                accountObject.remove(ConsentExtensionConstants.CURRENCY);
            }
        }

        return accountArray;
    }
}
