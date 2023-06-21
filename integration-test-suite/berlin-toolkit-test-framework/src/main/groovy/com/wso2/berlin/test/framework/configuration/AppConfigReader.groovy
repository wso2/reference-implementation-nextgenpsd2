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

package com.wso2.berlin.test.framework.configuration

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Application Configuration Reader Class.
 */
class AppConfigReader {
    private static final ConfigParser configParser = ConfigParser.getInstance();
    private static final Log log = LogFactory.getLog(AppConfigReader.class);
    public static Integer tppNumber;

    /**
     * Get the index of the relevant TPP from ApplicationConfigList in test-config.xml.
     *
     * @return index of the corresponding TPP.
     */
     static Integer getTppNumber() {
        return tppNumber;
    }

    /**
     * Set the index of the corresponding TPP (Index of AppConfig tag).
     *
     * @param tppNumber
     */
     void setTppNumber(Integer tppNumber) {
        AppConfigReader.tppNumber = tppNumber;
    }

    /**
     * Get Client Id of the given TPP.
     *
     * @return clientid
     */
     static String getClientId() {

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
     * Get Redirect URL of the given TPP.
     *
     * @return redirectUrl
     */
     static String getRedirectURL() {

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
     static String getApplicationKeystoreLocation() {

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
     static String getApplicationKeystoreAlias() {

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
     static String getApplicationKeystorePassword() {

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
     * Get the config of the MTLS Enabled Tag of the given TPP.
     *
     * @return true if mtls enabled
     */
     static Boolean isMTLSEnabled() {

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
     static String getTransportKeystoreLocation() {

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
     static String getTransportKeystorePassword() {

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
     static String getTransportKeystoreType() {

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
     static String getTransportKeystoreAlias() {

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
}
