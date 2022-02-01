/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 *  language governing the permissions and limitations under this license,
 *  please see the license as well as any agreement you’ve entered into with
 *  WSO2 governing the purchase of this software and any associated services.
 *
 */

package com.wso2.openbanking.scp.webapp.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.model.SCPError;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utils
 * <p>
 * Contains utility methods used by self-care portal web application
 */
public class Utils {
    private static final Log LOG = LogFactory.getLog(Utils.class);

    private Utils() {
        // hiding constructor
    }

    public static JSONObject sendRequest(HttpUriRequest request)
            throws TokenGenerationException {

        LOG.debug("Sending request to " + request.getURI());
        String responseStr = null;
        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpResponse response = client.execute(request);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                responseStr = EntityUtils.toString(responseEntity);
            }
            if ((response.getStatusLine().getStatusCode() / 100) != 2) {
                if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    LOG.debug("Received unauthorized(401) response. body: " + responseStr);
                    throw new TokenGenerationException("Received unauthorized Response: " + responseStr);
                }
            } else {
                // received success (200 range) response
                JSONObject responseJson;
                if (StringUtils.isNotEmpty(responseStr)) {
                    responseJson = new JSONObject(responseStr);
                } else {
                    responseJson = new JSONObject();
                }
                responseJson.put("res_status_code", response.getStatusLine().getStatusCode());
                return responseJson;
            }

        } catch (IOException e) {
            LOG.error("Exception occurred while forwarding request. Caused by, ", e);
        } catch (OpenBankingException e) {
            LOG.error("Exception occurred while generating http client. Caused by, ", e);
        }
        throw new TokenGenerationException("Unexpected response received for the request. path: " +
                request.getURI() + " response:" + responseStr);
    }

    public static JSONObject sendTokenRequest(HttpPost tokenReq) throws TokenGenerationException {
        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpResponse response = client.execute(tokenReq);
            String responseStr = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // received success response
                return new JSONObject(responseStr);
            }
            LOG.error("Invalid response received for token request. Error: " + responseStr);
        } catch (IOException e) {
            LOG.error("Exception occurred while sending token request. Caused by, ", e);
        } catch (JSONException e) {
            LOG.error("Exception occurred while processing token response. Caused by, ", e);
        } catch (OpenBankingException e) {
            LOG.error("Exception occurred while generating http client. Caused by, ", e);
        }
        throw new TokenGenerationException("Invalid response received for token request");
    }

    public static HttpUriRequest getHttpUriRequest(String apimBaseUrl, String requestMethod, String queryParams) {
        switch (requestMethod) {
            case HttpDelete.METHOD_NAME:
                return new HttpDelete(apimBaseUrl + Constants.PATH_APIM_CONSENT_REVOKE_V1 + "?" + queryParams);
            default:
                return new HttpGet(apimBaseUrl + Constants.PATH_APIM_CONSENT_SEARCH_V1 + "?" + queryParams);
        }
    }

    public static Optional<Cookie> getCookieFromRequest(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst();
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static String formatDateToEncodedString(LocalDateTime localDateTime) {
        return Base64.getEncoder().encodeToString(localDateTime
                .format(DateTimeFormatter.ofPattern(Constants.SCP_TOKEN_VALIDITY_DATE_FORMAT))
                .getBytes(StandardCharsets.UTF_8));
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static LocalDateTime parseEncodedStringToDate(String dateStr) {
        String tokenCreatedTime = new String(Base64.getDecoder().decode(dateStr), StandardCharsets.UTF_8);
        return LocalDateTime.parse(tokenCreatedTime,
                DateTimeFormatter.ofPattern(Constants.SCP_TOKEN_VALIDITY_DATE_FORMAT));
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static void returnResponse(HttpServletResponse resp, int statusCode, JSONObject payload) {
        try {
            LOG.debug("Returning response to frontend. payload: " + payload.toString());
            // returning  response
            resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            resp.setStatus(statusCode);
            PrintWriter out = resp.getWriter();
            out.print(payload);
            out.flush();
        } catch (IOException e) {
            LOG.error("Exception occurred while returning response. Caused by, ", e);
        }
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static void sendErrorToFrontend(SCPError error, String errRedirectUrlFormat, HttpServletResponse resp) {
        LOG.debug("Sending error to frontend.");
        try {
            final String errorMsg = Base64.getUrlEncoder()
                    .encodeToString(error.getMessage().getBytes(StandardCharsets.UTF_8));
            final String errorDesc = Base64.getUrlEncoder()
                    .encodeToString((error.getDescription()).getBytes(StandardCharsets.UTF_8));

            final String errorUrl = String.format(errRedirectUrlFormat, errorMsg, errorDesc);
            resp.sendRedirect(errorUrl);
        } catch (IOException ex) {
            LOG.error("Exception occurred while redirecting to frontend. Caused by, ", ex);
        }
    }

}
