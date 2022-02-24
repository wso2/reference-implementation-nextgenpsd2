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
import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.model.SCPError;
import com.wso2.openbanking.scp.webapp.service.OAuthService;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuthCallbackServlet is responsible for handling oauth2 authorization callback requests.
 */
@WebServlet(name = "OAuthCallbackServlet", urlPatterns = "/scp_oauth2_callback")
public class OAuthCallbackServlet extends HttpServlet {

    private static final long serialVersionUID = -1253188744670051774L;
    private static final Log LOG = LogFactory.getLog(OAuthCallbackServlet.class);
    private static final String CODE = "code";

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        final String iamBaseUrl = getServletContext()
                .getInitParameter(Constants.SERVLET_CONTEXT_IAM_BASE_URL);
        try {
            final String code = req.getParameter(CODE);

            OAuthService oAuthService = OAuthService.getInstance();
            if (StringUtils.isEmpty(code)) {
                LOG.debug("Logout callback request received. Invalidating cookies.");
                oAuthService.removeAllCookiesFromRequest(req, resp);
            } else {
                LOG.debug("Authorization callback request received");
                final String clientKey = getServletContext().getInitParameter(Constants.SERVLET_CONTEXT_CLIENT_KEY);
                final String clientSecret = getServletContext()
                        .getInitParameter(Constants.SERVLET_CONTEXT_CLIENT_SECRET);

                JSONObject tokenResponse = oAuthService
                        .sendAccessTokenRequest(iamBaseUrl, clientKey, clientSecret, code);
                // add cookies to response
                oAuthService.generateCookiesFromTokens(tokenResponse, req, resp);
            }

            final String redirectUrl = iamBaseUrl + "/consentmgr";
            LOG.debug("Redirecting to frontend application: " + redirectUrl);
            resp.sendRedirect(redirectUrl);
        } catch (TokenGenerationException | IOException e) {
            LOG.error("Exception occurred while processing authorization callback request. Caused by, ", e);
            // sending error to frontend
            SCPError error = new SCPError("Authentication Failed!",
                    "Something went wrong during the authentication process. Please try signing in again.");
            final String errorUrlFormat = iamBaseUrl + "/consentmgr/error?message=%s&description=%s";
            Utils.sendErrorToFrontend(error, errorUrlFormat, resp);
        }
    }
}
