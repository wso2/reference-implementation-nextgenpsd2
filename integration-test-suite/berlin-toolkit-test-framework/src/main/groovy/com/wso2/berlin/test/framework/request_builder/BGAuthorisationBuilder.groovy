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

package com.wso2.berlin.test.framework.request_builder

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.id.ClientID
import com.wso2.berlin.test.framework.configuration.BGConfigurationService

class BGAuthorisationBuilder {
    private AuthorizationRequest request
    private BGConfigurationService bgConfiguration
    private ClientID clientID
    private int tppNumber

    BGAuthorisationBuilder() {
        bgConfiguration = new BGConfigurationService()
    }

    ClientID getClientID() {

        if (clientID == null) {
            clientID = new ClientID(bgConfiguration.getAppInfoClientID(tppNumber))
        }
        return clientID
    }
}
