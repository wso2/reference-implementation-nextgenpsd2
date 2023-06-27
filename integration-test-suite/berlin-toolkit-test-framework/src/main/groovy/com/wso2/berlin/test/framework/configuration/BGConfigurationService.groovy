package com.wso2.berlin.test.framework.configuration

import com.wso2.openbanking.test.framework.configuration.OBConfigParser
import com.wso2.openbanking.test.framework.configuration.OBConfigurationService

/**
 * Class for provide configuration data to the BG layers and BG tests
 * This class provide OB configuration and BG configuration.
 */
class BGConfigurationService extends OBConfigurationService {
    /**
     * Get Client ID
     */
     Object getClientId() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Application.ClientID");
    }

    /**
     * Get Base URL
     */
    static String getBaseURL() {

        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap().get("Server.BaseURL"));
    }

    /**
     * Get authorization server URL
     */
    static String getAuthorisationServerURL() {

        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap().get("Server.AuthorisationServerURL"));
    }

    /**
     * Get redirect URL
     */
    static Object getRedirectURL() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Application.RedirectURL");
    }

    /**
     * Get application key store location
     */
    static Object getApplicationKeystoreLocation() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Application.KeyStore.Location");
    }

    /**
     * Get application key store alias
     */
    static Object getApplicationKeystoreAlias() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Application.KeyStore.Alias");
    }

    /**
     * Get application key store password
     */
    static Object getApplicationKeystorePassword() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Application.KeyStore.Password");
    }


    static Object isMTLSEnabled() {
        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Transport.MTLSEnabled");
    }

    /**
     * Get transport key store location
     */
     Object getTransportKeystoreLocation() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Location");
    }

    /**
     * Get transport key store password
     */
     Object getTransportKeystorePassword() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Password");
    }

    /**
     * Get transport key store type
     */
     Object getTransportKeystoreType() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Type");
    }

    /**
     * Get transport trust store location
     */
     String getTransportTruststoreLocation() {

        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap().get("Transport.Truststore.Location"));
    }

    /**
     * Get transport trust store password
     */
     String getTransportTruststorePassword() {

        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap().get("Transport.Truststore.Password"));
    }

    /**
     * Get transport trust store type
     */
     String getTransportTruststoreType() {

        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap().get("Transport.Truststore.Type"));
    }

    /**
     * Get access token expire time
     */
     int getAccessTokenExpireTime() {

        return Integer.parseInt(String.valueOf(OBConfigParser.getInstance().getConfigurationMap()
                .get("Common.AccessTokenExpireTime")));
    }

    /**
     * Get signing algorithm
     */
     String getSigningAlgorithm() {
        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap().get("Common.SigningAlgorithm"));
    }

    /**
     * Get Non Regulatory ClientId
     */
     String getNonRegulatoryClientId() {

        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap()
                .get("NonRegulatoryApplication.ClientID"));
    }

    /**
     * Get Audience Value
     */
     String getAudienceValue() {

        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap().get("ConsentApi.AudienceValue"));
    }

    /**
     * Get Api Version
     */
     String getApiVersion() {
        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap().get("ApiVersion"));
    }


    /**
     * Get Transport Keystore Alias.
     * @return alias
     */
     Object getTransportKeystoreAlias() {

        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Alias"));
    }

    /**
     * Get browser Preference
     */
     String browserPreference() {
        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap()
                .get("BrowserAutomation.BrowserPreference"));
    }

    /**
     * Get Driver Location
     */
     String getDriverLocation() {
        return String.valueOf(OBConfigParser.getInstance().getConfigurationMap()
                .get("BrowserAutomation.WebDriverLocation"));
    }

    /**
     * Get BrowserAutomation headless Enabled
     */
     boolean isHeadless() {

        return Boolean.parseBoolean(String.valueOf(OBConfigParser.getInstance().getConfigurationMap()
                .get("BrowserAutomation.HeadlessEnabled")));
    }
    
}
