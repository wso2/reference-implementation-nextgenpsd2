/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.berlin.identity.application.updater;

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
