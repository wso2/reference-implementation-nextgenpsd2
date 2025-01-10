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
