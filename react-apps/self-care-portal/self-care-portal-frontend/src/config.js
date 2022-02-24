/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

export const CONFIG = {
    SERVER_URL: window.env.SERVER_URL,
    SPEC: window.env.SPEC,
    TENANT_DOMAIN:window.env.TENANT_DOMAIN,
    TOKEN_ENDPOINT: window.env.SERVER_URL + "/oauth2/token",
    AUTHORIZE_ENDPOINT: window.env.SERVER_URL + "/consentmgr/scp_oauth2_authorize",
    LOGOUT_URL: window.env.SERVER_URL + "/oidc/logout",
    REDIRECT_URI: window.env.SERVER_URL + "/consentmgr/scp_oauth2_callback",
    BACKEND_URL: window.env.SERVER_URL + "/consentmgr/scp",
    NUMBER_OF_CONSENTS: window.env.NUMBER_OF_CONSENTS,
    VERSION: window.env.VERSION
};
