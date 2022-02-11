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

/**
 * Constants required for scp webapp
 */
public class Constants {

    private Constants() {
        // No public instances
    }

    // OAUTH Constants
    public static final String CLIENT_ID = "client_id";
    public static final String GRANT_TYPE = "grant_type";
    public static final String OAUTH_CODE = "code";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String OAUTH_SCOPE = "scope";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String ID_TOKEN = "id_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String EXPIRES_IN = "expires_in";

    public static final String COOKIE_BASE_NAME = "OB_SCP_";
    public static final String ACCESS_TOKEN_COOKIE_NAME = COOKIE_BASE_NAME + "AT";
    public static final String ID_TOKEN_COOKIE_NAME = COOKIE_BASE_NAME + "IT";
    public static final String REFRESH_TOKEN_COOKIE_NAME = COOKIE_BASE_NAME + "RT";
    public static final String TOKEN_VALIDITY_COOKIE_NAME = COOKIE_BASE_NAME + "VALIDITY";

    public static final String SCP_TOKEN_VALIDITY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    public static final String DEFAULT_COOKIE_PATH = "/consentmgr";
    public static final int DEFAULT_COOKIE_MAX_AGE = 3600; //(60*60) = 1h

    public static final String SERVLET_CONTEXT_CLIENT_KEY = "scpClientKey";
    public static final String SERVLET_CONTEXT_CLIENT_SECRET = "scpClientSecret";
    public static final String SERVLET_CONTEXT_IAM_BASE_URL = "identityServerBaseUrl";
    public static final String SERVLET_CONTEXT_APIM_BASE_URL = "apiManagerServerUrl";

    // Paths
    public static final String PATH_TOKEN = "/oauth2/token";
    public static final String PATH_LOGOUT = "/oidc/logout";
    public static final String PATH_CALLBACK = "/consentmgr/scp_oauth2_callback";
    public static final String PATH_AUTHORIZE = "/oauth2/authorize";
    public static final String PATH_APIM_CONSENT_SEARCH_V1 = "/ob_consent/v1/admin/search";
    public static final String PATH_APIM_CONSENT_REVOKE_V1 = "/ob_consent/v1/admin/revoke";
}
