/*
Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework.keystore

import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import com.wso2.openbanking.test.framework.keystore.OBKeyStore

import java.security.Key
import java.security.KeyStore
import java.security.cert.Certificate

/**
 * Class for provide keystore functions for AU Layer
 */
class BGKeyStore extends OBKeyStore{

    private static BGConfigurationService bgConfiguration = new BGConfigurationService()

    /**
     * Get Mock-CDR register application Keystore
     * @return
     * @throws TestFrameworkException
     */
    static KeyStore getApplicationKeyStore() throws TestFrameworkException {
        return getKeyStore(bgConfiguration.getAppKeyStoreLocation(),bgConfiguration.getAppKeyStorePWD());
    }

    /**
     * Get Mock-CDR register application Keystore Certificate
     * @return
     * @throws TestFrameworkException
     */
    static Certificate getCertificateFromKeyStore() throws TestFrameworkException {
        KeyStore keyStore = getKeyStore(bgConfiguration.getAppKeyStoreLocation(),bgConfiguration.getAppKeyStorePWD())
        return getCertificate(keyStore
                ,bgConfiguration.getAppKeyStoreAlias(),bgConfiguration.getAppKeyStorePWD())
    }

    /**
     * Get Mock-CDR register Signing key
     * @return
     * @throws TestFrameworkException
     */
    static Key getSigningKey() throws TestFrameworkException {
        return getSigningKey(bgConfiguration.getAppKeyStoreLocation(),bgConfiguration.getAppKeyStorePWD()
                ,bgConfiguration.getAppKeyStoreAlias())
    }

}
