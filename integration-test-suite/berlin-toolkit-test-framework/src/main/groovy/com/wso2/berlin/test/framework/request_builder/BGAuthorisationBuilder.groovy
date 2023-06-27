/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
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
