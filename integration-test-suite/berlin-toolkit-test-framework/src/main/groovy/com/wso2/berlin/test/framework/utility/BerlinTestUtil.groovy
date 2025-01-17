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

package com.wso2.berlin.test.framework.utility

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.Base64URL
import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import com.wso2.openbanking.test.framework.utility.OBTestUtil
import io.restassured.response.Response
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.http.conn.ssl.SSLSocketFactory

import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Class to contain utility classes used for Test Framework.
 */
 class BerlinTestUtil extends OBTestUtil {

     private static BGConfigurationService bgConfiguration = new BGConfigurationService()
     private static final Log log = LogFactory.getLog(BerlinTestUtil.class);
    private static SSLSocketFactory sslSocketFactory;

    /**
     * Utility method to Stringify a list of String.
     *
     * @param params    List of Strings
     * @param delimiter delimiter between params
     * @return Final String with all the values
     */
     static String getParamListAsString(List<String> params, char delimiter) {

        String result = "";
        for (String param : params) {
            result = result.concat(param + delimiter);
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * Method to extract the SHA-1 JWK thumbprint from certificates.
     *
     * @param certificate x509 certificate
     * @return String thumbprint
     * @throws TestFrameworkException When failed to extract thumbprint
     */
     static String getJwkThumbPrint(Certificate certificate) throws TestFrameworkException {

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bais = new ByteArrayInputStream(certificate.getEncoded());
            X509Certificate x509 = (X509Certificate) cf.generateCertificate(bais);
            Base64URL jwkThumbprint = RSAKey.parse(x509).computeThumbprint("SHA-1");
            return jwkThumbprint.toString();
        } catch (CertificateException | JOSEException e) {
            throw new TestFrameworkException("Error occurred while generating SHA-1 JWK thumbprint", e);
        }

    }

    /**
     * Method to hexify array of bytes.
     *
     * @param bytes Required byte[]
     * @return hexified String
     */
     static String hexify(byte[] bytes) {

        if (bytes == null) {
            String errorMsg = "Invalid byte array: 'NULL'";
            throw new IllegalArgumentException(errorMsg);
        } else {
            char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6',
                    '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            StringBuilder buf = new StringBuilder(bytes.length * 2);

            for (byte aByte : bytes) {
                buf.append(hexDigits[(aByte & 240) >> 4]);
                buf.append(hexDigits[aByte & 15]);
            }
            return buf.toString();
        }
    }

    /**
     * Method to obtain application keystore from the configured location.
     *
     * @return Keystore object
     * @throws TestFrameworkException When failed to find the keystore of required type
     */
     static KeyStore getApplicationKeyStore() throws TestFrameworkException {

        try (InputStream inputStream = new FileInputStream(bgConfiguration.
                getAppKeyStoreLocation().toString())) {

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, bgConfiguration.getAppKeyStorePWD().toCharArray());
            return keyStore;
        } catch (IOException e) {
            throw new TestFrameworkException("Failed to load Keystore file from the location", e);
        } catch (CertificateException e) {
            throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
        } catch (NoSuchAlgorithmException e) {
            throw new TestFrameworkException("Failed to identify the Algorithm ", e);
        } catch (KeyStoreException e) {
            throw new TestFrameworkException("Failed to initialize the Keystore ", e);
        }

    }


    /**
     * Method to process a JSON Object and return a preferred value.
     *
     * @param response JSON Response
     * @param jsonPath Path of Required value
     * @return Value of requested key
     */
     static String parseResponseBody(Response response, String jsonPath) {

        return response.jsonPath().getString(jsonPath);
    }

    /**
     *
     * @param codeURL URL which the code is extracted from
     * @return extracted code
     */
    static String getCodeFromURL(String codeURL) {

        if (codeURL.contains("#")) {
            return codeURL.split("#")[1].split("&")[1].substring(18)
        } else {
            return codeURL.split("\\?")[1].split("&")[0].substring(5)
        }
    }

     /**
      * Get ISO_8601 Standard date time
      * Eg: 2019-09-30T04:44:05.271Z
      *
      * @param addDays Add particular number of days to the datetime now
      * @return String value of the date time
      */
     static String getDateAndTime(int addDays){

         return LocalDateTime.now().plusDays(addDays).format(DateTimeFormatter.ISO_LOCAL_DATE)
     }
}
