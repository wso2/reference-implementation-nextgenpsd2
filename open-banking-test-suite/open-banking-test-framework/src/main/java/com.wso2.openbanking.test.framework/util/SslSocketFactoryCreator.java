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

package com.wso2.openbanking.test.framework.util;

import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
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

  /**
   * Create SSL socket factory.
   * @param keystoreFilePath keystore file path.
   * @param keystorePassword keystore password.
   * @return an SSLSocketFactory implementation.
   * @throws TestFrameworkException when an error occurs while loading the keystore and truststore.
   */
  public SSLSocketFactory create(String keystoreFilePath, String keystorePassword) throws TestFrameworkException {
    try (FileInputStream keystoreLocation =
                 new FileInputStream(new File(keystoreFilePath));
         FileInputStream truststoreLocation =
                 new FileInputStream(new File(ConfigParser.getInstance()
                         .getTransportTruststoreLocation()))) {

      KeyStore keyStore = KeyStore.getInstance(AppConfigReader.getTransportKeystoreType());
      keyStore.load(keystoreLocation, keystorePassword.toCharArray());
      KeyStore trustStore = KeyStore.getInstance(ConfigParser.getInstance()
              .getTransportTruststoreType());
      trustStore.load(truststoreLocation, ConfigParser.getInstance()
              .getTransportTruststorePassword().toCharArray());

      // Manually create a new socket factory and pass in the required values.
      return new SSLSocketFactory(keyStore, keystorePassword, trustStore);
    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException
            | KeyManagementException | UnrecoverableKeyException | IOException e) {
      throw new TestFrameworkException("Unable to load the transport keystore and truststore", e);
    }
  }
}
