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

package com.wso2.openbanking.scp.webapp.servlet;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.model.SCPError;
import com.wso2.openbanking.scp.webapp.service.APIMService;
import com.wso2.openbanking.scp.webapp.service.OAuthService;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ApimReqHandlerServlet
 * <p>
 * This interrupts the requests, adds auth header, and forward requests to API Manager
 */
@WebServlet(name = "ApimReqHandlerServlet", urlPatterns = {"/scp/admin/search", "/scp/admin/revoke"})
public class ApimReqHandlerServlet extends HttpServlet {

    private static final long serialVersionUID = 7385252581004845440L;
    private static final Log LOG = LogFactory.getLog(ApimReqHandlerServlet.class);
    private final APIMService apimService = new APIMService();

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LOG.debug("New request received: " + req.getRequestURI() + "?" + req.getQueryString());
            if (apimService.isAccessTokenExpired(req)) {
                // access token is expired, refreshing access token
                Optional<String> optRefreshToken = apimService.constructRefreshTokenFromCookies(req);
                Optional<String> optAccessToken = apimService.constructAccessTokenFromCookies(req);

                if (optRefreshToken.isPresent() && optAccessToken.isPresent()) {
                    final OAuthService oAuthService = OAuthService.getInstance();
                    final String iamBaseUrl = getServletContext()
                            .getInitParameter(Constants.SERVLET_CONTEXT_IAM_BASE_URL);
                    final String clientKey = getServletContext().getInitParameter(Constants.SERVLET_CONTEXT_CLIENT_KEY);
                    final String clientSecret = getServletContext()
                            .getInitParameter(Constants.SERVLET_CONTEXT_CLIENT_SECRET);

                    net.minidev.json.JSONObject tokenBody = JWTUtils.decodeRequestJWT(optAccessToken.get(), "body");
                    final String requestedScopes = tokenBody.getAsString("scope");

                    JSONObject tokenResponse = oAuthService.sendRefreshTokenRequest(iamBaseUrl, clientKey,
                            clientSecret, optRefreshToken.get(), requestedScopes);

                    // add new tokes as cookies to response
                    oAuthService.generateCookiesFromTokens(tokenResponse, req, resp);

                    final String apimBaseUrl = getServletContext()
                            .getInitParameter(Constants.SERVLET_CONTEXT_APIM_BASE_URL);
                    HttpUriRequest request = Utils
                            .getHttpUriRequest(apimBaseUrl, req.getMethod(), req.getQueryString());

                    // generating header
                    Map<String, String> headers = new HashMap<>();
                    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getString(Constants.ACCESS_TOKEN));
                    headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

                    apimService.forwardRequest(resp, request, headers);

                } else {
                    // Invalid request, refresh token missing
                    SCPError error = new SCPError("Authentication Error!",
                            "Some values are missing from the request. Please try signing in again.");
                    LOG.error("Refresh token is missing from the request. Returning error to frontend, " + error);
                    OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
                    Utils.returnResponse(resp, HttpStatus.SC_BAD_REQUEST, new JSONObject(error));
                }
            } else {
                // access token is not expired yet
                Optional<String> optAccessToken = apimService.constructAccessTokenFromCookies(req);

                if (optAccessToken.isPresent()) {
                    final String apimBaseUrl = getServletContext()
                            .getInitParameter(Constants.SERVLET_CONTEXT_APIM_BASE_URL);
                    HttpUriRequest request = Utils
                            .getHttpUriRequest(apimBaseUrl, req.getMethod(), req.getQueryString());

                    // add existing req headers to new request
                    Map<String, String> headers = Collections.list(req.getHeaderNames())
                            .stream()
                            .filter(h -> !HttpHeaders.AUTHORIZATION.equalsIgnoreCase(h))
                            .collect(Collectors.toMap(h -> h, req::getHeader));

                    // add authorization headers to request
                    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + optAccessToken.get());
                    headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

                    apimService.forwardRequest(resp, request, headers);

                } else {
                    // invalid request, access token missing
                    SCPError error = new SCPError("Authentication Error!",
                            "Some values are invalid of the request. Please try signing in again.");
                    LOG.error("Requested access token is invalid. Returning error to frontend, " + error);
                    OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
                    Utils.returnResponse(resp, HttpStatus.SC_BAD_REQUEST, new JSONObject(error));
                }
            }
        } catch (TokenGenerationException | IOException | ParseException e) {
            LOG.error("Exception occurred while processing frontend request. Caused by, ", e);
            SCPError error = new SCPError("Request Forwarding Error!",
                    "Something went wrong during the authentication process. Please try signing in again.");
            // Consent revoke call might return error due to it's state,
            // Therefore we need to omit the removal of cookies in such scenarios.
            if (!req.getRequestURI().contains("/revoke")) {
                OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
            }
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
        } catch (SessionTimeoutException e) {
            LOG.debug("Session timeout exception occurred while processing request. Caused by, ", e);
            OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
            SCPError error = new SCPError("Session Has Expired!", "Please try signing in again.");
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
        }
    }

    @Generated(message = "Ignoring since method contains no logics")
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        doGet(req, resp);
    }

}
