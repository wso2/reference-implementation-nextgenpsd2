/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.berlin.test.framework.utility;

import com.wso2.berlin.test.framework.configuration.AppConfigReader;
import com.wso2.berlin.test.framework.configuration.ConfigParser;
import com.wso2.bfsi.test.framework.exception.TestFrameworkException;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


/**
 * Creates an SSL socket factory for MTLS requests.
 */
public class SslSocketFactoryCreator {

    /**
     * Create SSL socket factory.
     *
     * @return an SSLSocketFactory implementation
     * @throws TestFrameworkException when an error occurs while loading the keystore and truststore
     */
    public SSLSocketFactory create() throws TestFrameworkException {
        try (FileInputStream keystoreLocation =
                     new FileInputStream(new File(AppConfigReader.getTransportKeystoreLocation()));
             FileInputStream truststoreLocation =
                     new FileInputStream(new File(ConfigParser.getInstance()
                             .getTransportTruststoreLocation()))) {

            KeyStore keyStore = KeyStore.getInstance(AppConfigReader.getTransportKeystoreType());
            keyStore.load(keystoreLocation, AppConfigReader.getTransportKeystorePassword().toCharArray());
            KeyStore trustStore = KeyStore.getInstance(ConfigParser.getInstance()
                    .getTransportTruststoreType());
            trustStore.load(truststoreLocation, ConfigParser.getInstance()
                    .getTransportTruststorePassword().toCharArray());

            // Manually create a new socketfactory and pass in the required values.
            return new SSLSocketFactory(keyStore, AppConfigReader.
                    getTransportKeystorePassword(), trustStore);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException
                 | KeyManagementException | UnrecoverableKeyException | IOException e) {
            throw new TestFrameworkException("Unable to load the transport keystore and truststore", e);
        }
    }
}

