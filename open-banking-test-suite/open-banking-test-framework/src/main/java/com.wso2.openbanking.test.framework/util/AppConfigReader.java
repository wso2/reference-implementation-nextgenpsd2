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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Application Configuration Reader Class.
 */
public class AppConfigReader {
  private static final ConfigParser configParser = ConfigParser.getInstance();
  private static final Log log = LogFactory.getLog(AppConfigReader.class);
  public static Integer tppNumber;

  /**
   * Get the index of the relevant TPP from ApplicationConfigList in test-config.xml.
   *
   * @return index of the corresponding TPP.
   */
  public static Integer getTppNumber() {
    return tppNumber;
  }

  /**
   * Set the index of the corresponding TPP (Index of AppConfig tag).
   *
   * @param tppNumber
   */
  public void setTppNumber(Integer tppNumber) {
    AppConfigReader.tppNumber = tppNumber;
  }

  /**
   * Get Client Id of the given TPP.
   *
   * @return clientid
   */
  public static String getClientId() {

    if (configParser.getClientId().getClass().toString().contains("java.lang.String")) {
      String clientId = configParser.getClientId().toString();

      if (!clientId.equalsIgnoreCase("Application.ClientID")) {
        return clientId;
      } else {
        log.error("Application.ClientID property has not been configured properly.");
        return null;
      }
    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getClientId();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get Client Secret of the given TPP.
   *
   * @return clientSecret
   */
  public static String getClientSecret() {

    if (configParser.getClientSecret().getClass().toString().contains("java.lang.String")) {
      String clientSecret = configParser.getClientId().toString();

      if (!clientSecret.equalsIgnoreCase("Application.ClientSecret")) {
        return clientSecret;
      } else {
        log.error("Application.ClientSecret property has not been configured properly.");
        return null;
      }
    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getClientSecret();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get Redirect URL of the given TPP.
   *
   * @return redirectUrl
   */
  public static String getRedirectURL() {

    if (configParser.getRedirectURL().getClass().toString().contains("java.lang.String")) {
      return configParser.getRedirectURL().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getRedirectURL();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Application Keystore Location for the given TPP.
   *
   * @return applicationKeystoreLocation
   */
  public static String getApplicationKeystoreLocation() {

    if (configParser.getApplicationKeystoreLocation().getClass().toString().contains("java.lang.String")) {
      return configParser.getApplicationKeystoreLocation().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getApplicationKeystoreLocation();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Application Keystore Alias for the given TPP.
   *
   * @return applicationKeystoreAlias
   */
  public static String getApplicationKeystoreAlias() {

    if (configParser.getApplicationKeystoreAlias().getClass().toString().contains("java.lang.String")) {
      return configParser.getApplicationKeystoreAlias().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getApplicationKeystoreAlias();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Application Keystore Password for the given TPP.
   *
   * @return applicationKeystorePassword
   */
  public static String getApplicationKeystorePassword() {

    if (configParser.getApplicationKeystorePassword().getClass().toString().contains("java.lang.String")) {
      return configParser.getApplicationKeystorePassword().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getApplicationKeystorePassword();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Signing Certificate Kid for the given TPP.
   *
   * @return signingCertificateKid
   */
  public static String getSigningCertificateKid() {

    if (configParser.getSigningCertificateKid().getClass().toString().contains("java.lang.String")) {
      return configParser.getSigningCertificateKid().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getSigningCertificateKid();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the config of the MTLS Enabled Tag of the given TPP.
   *
   * @return true if mtls enabled
   */
  public static Boolean isMTLSEnabled() {

    if (configParser.isMTLSEnabled().getClass().toString().contains("java.lang.String")) {
      return Boolean.valueOf(String.valueOf(configParser.isMTLSEnabled()));

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.isMTLSEnabled();

      if (getTppNumber() == null) {
        return Boolean.valueOf(String.valueOf(listObj.get(0)));
      } else {
        return Boolean.valueOf(String.valueOf(listObj.get(tppNumber)));
      }
    }
  }

  /**
   * Get Transport Keystore Location for the given TPP.
   *
   * @return transportKeystoreLocation
   */
  public static String getTransportKeystoreLocation() {

    if (configParser.getTransportKeystoreLocation().getClass().toString().contains("java.lang.String")) {
      return configParser.getTransportKeystoreLocation().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getTransportKeystoreLocation();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Transport Keystore Password for the given TPP.
   *
   * @return transportKeystorePassword
   */
  public static String getTransportKeystorePassword() {

    if (configParser.getTransportKeystorePassword().getClass().toString().contains("java.lang.String")) {
      return configParser.getTransportKeystorePassword().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getTransportKeystorePassword();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Transport Keystore Type for the given TPP.
   *
   * @return transportKeystoreType
   */
  public static String getTransportKeystoreType() {

    if (configParser.getTransportKeystoreType().getClass().toString().contains("java.lang.String")) {
      return configParser.getTransportKeystoreType().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getTransportKeystoreType();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Transport Keystore Alias for the given TPP.
   *
   * @return transportKeystoreAlias
   */
  public static String getTransportKeystoreAlias() {

    if (configParser.getTransportKeystoreAlias().getClass().toString().contains("java.lang.String")) {
      return configParser.getTransportKeystoreAlias().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getTransportKeystoreAlias();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Application Keystore Domain for the given TPP.
   *
   * @return applicationKeystoreDomain
   */
  public static String getApplicationKeystoreDomain() {

    if (configParser.getApplicationKeystoreDomain().getClass().toString().contains("java.lang.String")) {
      return configParser.getApplicationKeystoreDomain().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getApplicationKeystoreDomain();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the SSA File Path for the given TPP.
   *
   * @return SSAFilePath
   */
  public static String getSSAFilePath() {

    if (configParser.getSSAFilePath().getClass().toString().contains("java.lang.String")) {
      return configParser.getSSAFilePath().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getSSAFilePath();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Self Signed SSA File Path.
   *
   * @return selfSignedSSAFilePath
   */
  public static String getSelfSignedSSAFilePath() {
    return configParser.getSelfSignedSSAFilePath().toString();
  }

  /**
   * Get the Software Id for the given TPP.
   *
   * @return softwareId
   */
  public static String getSoftwareId() {

    if (configParser.getSoftwareId().getClass().toString().contains("java.lang.String")) {
      return configParser.getSoftwareId().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getSoftwareId();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Dcr Redirect Uri for the given TPP.
   *
   * @return DcrRedirectUri
   */
  public static String getDcrRedirectUri() {

    if (configParser.getDcrRedirectUri().getClass().toString().contains("java.lang.String")) {
      return configParser.getDcrRedirectUri().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getDcrRedirectUri();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the Alternate Redirect Uri for the given TPP.
   *
   * @return alternateRedirectUri
   */
  public static String getAlternateRedirectUri() {

    if (configParser.getAlternateRedirectUri().getClass().toString().contains("java.lang.String")) {
      return configParser.getAlternateRedirectUri().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getAlternateRedirectUri();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }

  /**
   * Get the DCR API Version of the given TPP configs.
   *
   * @return DCRAPIVersion
   */
  public static String getDCRAPIVersion() {

    if (configParser.getDCRAPIVersion().getClass().toString().contains("java.lang.String")) {
      return configParser.getDCRAPIVersion().toString();

    } else {
      List<Object[]> listObj = (List<Object[]>) configParser.getDCRAPIVersion();

      if (getTppNumber() == null) {
        return String.valueOf(listObj.get(0));
      } else {
        return String.valueOf(listObj.get(tppNumber));
      }
    }
  }
}
