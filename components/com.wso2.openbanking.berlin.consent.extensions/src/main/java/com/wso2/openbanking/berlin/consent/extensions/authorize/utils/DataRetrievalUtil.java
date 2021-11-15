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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data retrieval util class
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
                        StandardCharsets.UTF_8.name()));
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
        String queries = URLEncodedUtils.format(pairs, StandardCharsets.UTF_8.name());
        return baseURL + "?" + queries;
    }
}
