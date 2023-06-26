package com.wso2.berlin.test.framework.configuration

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

        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Application.ClientID");
    }

    /**
     * Get Base URL
     */
     String getBaseURL() {

        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Server.BaseURL"));
    }

    /**
     * Get authorization server URL
     */
     String getAuthorisationServerURL() {

        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Server.AuthorisationServerURL"));
    }

    /**
     * Get redirect URL
     */
     Object getRedirectURL() {

        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Application.RedirectURL");
    }

    /**
     * Get application key store location
     */
    static Object getApplicationKeystoreLocation() {

        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Application.KeyStore.Location");
    }

    /**
     * Get application key store alias
     */
    static Object getApplicationKeystoreAlias() {

        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Application.KeyStore.Alias");
    }

    /**
     * Get application key store password
     */
    static Object getApplicationKeystorePassword() {

        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Application.KeyStore.Password");
    }


    static Object isMTLSEnabled() {
        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Transport.MTLSEnabled");
    }

    /**
     * Get transport key store location
     */
     Object getTransportKeystoreLocation() {

        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Location");
    }

    /**
     * Get transport key store password
     */
     Object getTransportKeystorePassword() {

        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Password");
    }

    /**
     * Get transport key store type
     */
     Object getTransportKeystoreType() {

        return ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Type");
    }

    /**
     * Get transport trust store location
     */
     String getTransportTruststoreLocation() {

        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Transport.Truststore.Location"));
    }

    /**
     * Get transport trust store password
     */
     String getTransportTruststorePassword() {

        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Transport.Truststore.Password"));
    }

    /**
     * Get transport trust store type
     */
     String getTransportTruststoreType() {

        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Transport.Truststore.Type"));
    }

    /**
     * Get access token expire time
     */
     int getAccessTokenExpireTime() {

        return Integer.parseInt(String.valueOf(ConfigParser.getInstance()
                .getConfiguration().get("Common.AccessTokenExpireTime")));
    }

    /**
     * Get signing algorithm
     */
     String getSigningAlgorithm() {
        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Common.SigningAlgorithm"));
    }

    /**
     * Get Non Regulatory ClientId
     */
     String getNonRegulatoryClientId() {

        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("NonRegulatoryApplication.ClientID"));
    }

    /**
     * Get Audience Value
     */
     String getAudienceValue() {

        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("ConsentApi.AudienceValue"));
    }

    /**
     * Get Api Version
     */
     String getApiVersion() {
        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("ApiVersion"));
    }


    /**
     * Get Transport Keystore Alias.
     * @return alias
     */
     Object getTransportKeystoreAlias() {

        return String.valueOf(ConfigParser.getInstance().getConfiguration()
                .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Alias"));
    }

    /**
     * Get browser Preference
     */
     String browserPreference() {
        return String.valueOf(ConfigParser.getInstance().getConfiguration().get("BrowserAutomation.BrowserPreference"));
    }

    /**
     * Get Driver Location
     */
     String getDriverLocation() {
        return String.valueOf(ConfigParser.getInstance().getConfiguration()
                .get("BrowserAutomation.WebDriverLocation"));
    }

    /**
     * Get BrowserAutomation headless Enabled
     */
     boolean isHeadless() {

        return Boolean.parseBoolean(String.valueOf(ConfigParser.getInstance().
                getConfiguration().get("BrowserAutomation.HeadlessEnabled")));
    }
}
