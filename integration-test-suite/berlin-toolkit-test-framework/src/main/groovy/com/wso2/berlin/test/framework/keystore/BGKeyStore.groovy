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
     * @throws com.wso2.bfsi.test.framework.exception.TestFrameworkException
     */
    static KeyStore getMockCDRApplicationKeyStore() throws TestFrameworkException {
        return getKeyStore(bgConfiguration.getAppKeyStoreLocation(),bgConfiguration.getAppKeyStorePWD());
    }

    /**
     * Get Mock-CDR register application Keystore Certificate
     * @return
     * @throws TestFrameworkException
     */
    static Certificate getCertificateFromMockCDRKeyStore() throws TestFrameworkException {
        KeyStore keyStore = getKeyStore(bgConfiguration.getAppKeyStoreLocation(),bgConfiguration.getAppKeyStorePWD())
        return getCertificate(keyStore
                ,bgConfiguration.getAppKeyStoreAlias(),bgConfiguration.getAppKeyStorePWD())
    }

    /**
     * Get Mock-CDR register Signing key
     * @return
     * @throws TestFrameworkException
     */
    static Key getMockCDRSigningKey() throws TestFrameworkException {
        return getSigningKey(bgConfiguration.getAppKeyStoreLocation(),bgConfiguration.getAppKeyStorePWD()
                ,bgConfiguration.getAppKeyStoreAlias())
    }

}
