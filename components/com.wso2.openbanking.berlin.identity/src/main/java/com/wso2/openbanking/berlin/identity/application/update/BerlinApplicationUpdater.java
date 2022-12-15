/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com/). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.identity.application.updater;

import com.wso2.openbanking.accelerator.common.config.TextFileReader;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.listener.application.ApplicationUpdaterImpl;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;

import java.io.IOException;

/**
 * Berlin Specific Application Updater.
 */
public class BerlinApplicationUpdater extends ApplicationUpdaterImpl {

    public static final String NON_SCA_AUTH_SCRIPT = "non.sca.auth.script.js";
    @Override
    public void setConditionalAuthScript(boolean isRegulatoryApp, ServiceProvider serviceProvider,
                                         LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws OpenBankingException {

        if (isRegulatoryApp) {
            if (localAndOutboundAuthenticationConfig.getAuthenticationScriptConfig() == null) {
                try {
                    String authScript = TextFileReader.getInstance().readFile(NON_SCA_AUTH_SCRIPT);
                    if (StringUtils.isNotEmpty(authScript)) {
                        AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
                        scriptConfig.setContent(authScript);
                        scriptConfig.setEnabled(true);
                        localAndOutboundAuthenticationConfig.setAuthenticationScriptConfig(scriptConfig);
                    }
                } catch (IOException e) {
                    throw new OpenBankingException("Error occurred while reading file", e);
                }

            }
        }
    }
}
