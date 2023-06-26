package com.wso2.berlin.test.framework.request_builder

import com.nimbusds.oauth2.sdk.AuthorizationRequest
import com.nimbusds.oauth2.sdk.id.ClientID
import com.wso2.berlin.test.framework.configuration.BGConfigurationService

class BGAuthorisationBuilder {
    private AuthorizationRequest request
    private BGConfigurationService auConfiguration
    private ClientID clientID
    private int tppNumber

    BGAuthorisationBuilder() {
        auConfiguration = new BGConfigurationService()
    }

    ClientID getClientID() {

        if (clientID == null) {
            clientID = new ClientID(auConfiguration.getAppInfoClientID(tppNumber))
        }
        return clientID
    }
}
