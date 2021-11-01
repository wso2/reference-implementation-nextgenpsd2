/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.test.framework.util;

import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

/**
 * Configuration parser to read configurations from test-config.xml
 */
public class ConfigParser {

  private static final Object lock = new Object();
  private static Log log = LogFactory.getLog(ConfigParser.class);
  static String configFilePath;
  private static Map<String, Object> configuration = new HashMap<>();
  private static Map<String, String> consentProcessorConfig = new HashMap<>();
  private static volatile ConfigParser parser = null;
  private OMElement rootElement;

  protected ConfigParser() throws TestFrameworkException {

    buildGlobalConfiguration();
  }

  /**
   * Maintain single instance of Config parser through out the implementations.
   *
   * @return ConfigParser object
   */
  public static ConfigParser getInstance() {

    if (parser == null) {
      synchronized (lock) {
        if (parser == null) {
          try {
            parser = new ConfigParser();
          } catch (TestFrameworkException e) {
            log.error("Failed to initiate config parser", e);
            parser = null;
          }
        }
      }
    }
    return parser;
  }

  public Map<String, Object> getConfiguration() {

    return configuration;
  }

  /**
   * Build global configurations from test-config.xml.
   */
  void buildGlobalConfiguration() throws TestFrameworkException {

    InputStream inStream = null;
    StAXOMBuilder builder;

    String warningMessage = "";
    try {
      if (configFilePath != null) {
        File openBankingConfig = new File(configFilePath);
        if (openBankingConfig.exists()) {
          inStream = new FileInputStream(openBankingConfig);
        } else {
          log.warn("No file found in the specified path: " + configFilePath
                  + ". Proceeding with default location.");
        }
      } else {
        File configXML = new File(this.getClass().getClassLoader()
                .getResource("test-config.xml").getFile());
        if (configXML.exists()) {
          inStream = new FileInputStream(configXML);
        }
      }

      if (inStream == null) {
        String message = "Test Framework configuration not found. Cause - " + warningMessage;
        if (log.isDebugEnabled()) {
          log.debug(message);
        }
        throw new FileNotFoundException(message);
      }

      builder = new StAXOMBuilder(inStream);
      rootElement = builder.getDocumentElement();
      Deque<String> elementNames = new ArrayDeque<>();
      readChildElements(rootElement, elementNames);

    } catch (IOException | XMLStreamException e) {
      throw new TestFrameworkException("Error occurred while building configuration from test-config.xml", e);
    } finally {
      try {
        if (inStream != null) {
          inStream.close();
        }
      } catch (IOException e) {
        log.error("Error closing the input stream for test-config.xml", e);
      }
    }
  }

  /**
   * Read element recursively and put in the configuration map.
   *
   * @param serverConfig OM Element
   * @param elementNames Deque of element names
   */
  private void readChildElements(OMElement serverConfig, Deque<String> elementNames) {

    for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
      OMElement element = (OMElement) childElements.next();
      elementNames.push(element.getLocalName());
      if (elementHasText(element)) {
        String key = getKey(elementNames);
        Object currentObject = configuration.get(key);
        String value = replaceSystemProperty(element.getText());
        if (currentObject == null) {
          configuration.put(key, value);
        } else if (currentObject instanceof ArrayList) {
          List<String> list = (ArrayList) currentObject;
          if (!list.contains(value)) {
            list.add(value);
            configuration.put(key, list);
          }
        } else {
          if (!value.equals(currentObject)) {
            List<Object> arrayList = new ArrayList<>(2);
            arrayList.add(currentObject);
            arrayList.add(value);
            configuration.put(key, arrayList);
          }
        }
      }
      readChildElements(element, elementNames);
      elementNames.pop();
    }
  }

  private boolean elementHasText(OMElement element) {

    String text = element.getText();
    return text != null && text.trim().length() != 0;
  }

  /**
   * Converts the hierarchical element name to key.
   *
   * @param elementNames hierarchical element name
   * @return key name
   */
  private String getKey(Deque<String> elementNames) {

    StringBuilder key = new StringBuilder();
    for (Iterator itr = elementNames.descendingIterator(); itr.hasNext(); ) {
      key.append(itr.next()).append(".");
    }
    key.deleteCharAt(key.lastIndexOf("."));
    return key.toString();
  }

  private String replaceSystemProperty(String propertyName) {

    int indexOfStartingChars = -1;
    int indexOfClosingBrace;

    StringBuilder nameBuilder = new StringBuilder(propertyName);
    while (indexOfStartingChars < nameBuilder.indexOf("${")
            && (indexOfStartingChars = nameBuilder.indexOf("${")) != -1
            && (indexOfClosingBrace = nameBuilder.toString().indexOf('}')) != -1) { // Is a property used?
      String sysProp = nameBuilder.substring(indexOfStartingChars + 2, indexOfClosingBrace);
      String propValue = System.getProperty(sysProp);
      if (propValue != null) {
        nameBuilder = new StringBuilder(nameBuilder.substring(0, indexOfStartingChars) + propValue +
                nameBuilder.substring(indexOfClosingBrace + 1));
      }
    }
    propertyName = nameBuilder.toString();
    return propertyName;
  }

  public boolean isHeadless() {

    return Boolean.parseBoolean(String.valueOf(ConfigParser.getInstance().
            getConfiguration().get("BrowserAutomation.HeadlessEnabled")));
  }

  public Object getClientId() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Application.ClientID");
  }

  public Object getClientSecret() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Application.ClientSecret");
  }

  public Object getPSU() {

    return ConfigParser.getInstance().getConfiguration().get("PSUList.PSUInfo.Psu");
  }

  public Object getPSUPassword() {
    return ConfigParser.getInstance().getConfiguration().get("PSUList.PSUInfo.PsuPassword");
  }
  
  public String getAlternatePSU() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("AlternatePSU.Psu"));
  }

  public String getAlternatePSUPassword() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("AlternatePSU.PsuPassword"));
  }

  public String getBaseURL() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Server.BaseURL"));
  }

  public String getAuthorisationServerURL() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Server.AuthorisationServerURL"));
  }

  public String getXFapiFinanceId() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("UK.Common.XFapiFinanceId"));
  }

  public Object getRedirectURL() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Application.RedirectURL");
  }

  public String getPrompt() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("UK.Authorization.Prompt"));
  }

  public String getNonce() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("UK.Authorization.Nonce"));
  }

  public String getState() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("UK.Authorization.State"));
  }

  public String getDriverLocation() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration()
            .get("BrowserAutomation.WebDriverLocation"));
  }

  public Object getApplicationKeystoreLocation() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Application.KeyStore.Location");
  }

  public Object getApplicationKeystoreAlias() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Application.KeyStore.Alias");
  }

  public Object getApplicationKeystorePassword() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Application.KeyStore.Password");
  }

  public Object getSigningCertificateKid() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Application.KeyStore.SigningKid");
  }

  public Object isMTLSEnabled() {
    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Transport.MTLSEnabled");
  }

  public Object getTransportKeystoreLocation() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Location");
  }

  public Object getTransportKeystorePassword() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Password");
  }

  public Object getTransportKeystoreType() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Type");
  }

  public String getTransportTruststoreLocation() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Transport.Truststore.Location"));
  }

  public String getTransportTruststorePassword() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Transport.Truststore.Password"));
  }

  public String getTransportTruststoreType() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Transport.Truststore.Type"));
  }

  public int getAccessTokenExpireTime() {

    return Integer.parseInt(String.valueOf(ConfigParser.getInstance()
            .getConfiguration().get("Common.AccessTokenExpireTime")));
  }

  public String getTenantDomain() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Common.TenantDomain"));
  }

  public Object getApplicationKeystoreDomain() {
    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Application.KeyStore.DomainName");
  }

  public String getSigningAlgorithm() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Common.SigningAlgorithm"));
  }

  public String getReqObjSigningAlgorithm() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Common.ReqObjSigningAlgorithm"));
  }

  public String getKeyManagerAdminUsername() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Common.KeyManager.Admin.Username"));
  }

  public String getKeyManagerAdminPassword() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Common.KeyManager.Admin.Password"));
  }

  public String getPublisherAdminUsername() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("PublisherInfo.Publisher"));
  }

  public String getPublisherAdminPassword() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("PublisherInfo.PublisherPassword"));
  }

  public boolean isProvisioning() {

    return Boolean.parseBoolean(String.valueOf(ConfigParser.getInstance().
            getConfiguration().get("Provisioning.Enabled")));
  }

  public String getProvisionFilePath() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Provisioning.ProvisionFilePath"));
  }

  public String getSolutionVersion() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("SolutionVersion"));
  }

  public String getOBSpec() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("OBSpec"));
  }

  public String getNonRegulatoryClientId() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("NonRegulatoryApplication.ClientID"));
  }

  public String getNonRegulatoryClientSecret() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("NonRegulatoryApplication.ClientSecret"));
  }

  public String getNonRegulatoryRedirectURL() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("NonRegulatoryApplication.RedirectURL"));
  }

  public Object getSSAFilePath() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.DCR.SSAPath");
  }

  public Object getSelfSignedSSAFilePath() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.DCR.SelfSignedSSAPath");
  }

  public Object getSoftwareId() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.DCR.SoftwareId");
  }

  public Object getDcrRedirectUri() {

    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.DCR.RedirectUri");
  }

  public Object getAlternateRedirectUri() {
    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.DCR.AlternateRedirectUri");
  }

  public Object getDCRAPIVersion() {
    return ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.DCR.DCRAPIVersion");
  }

  public String getAudienceValue() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("ConsentApi.AudienceValue"));
  }

  public String getInternalConsentMgtApiContext() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("InternalApiContext.Consent-Mgt"));
  }

  public String getInternalMultiAuthApiContext() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("InternalApiContext.Multi-Auth"));
  }

  public String getApiVersion() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("ApiVersion"));
  }

  public String getCCPortal() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("CustomerCareInfo.CustomerCareUser"));
  }

  public String getCCPortalPassword() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("CustomerCareInfo.CustomerCareUserPassword"));
  }

  /**
   * Read TPP UserName.
   *
   * @return tpp userName
   */
  public String getTppUserName() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("TPPInfo.Tpp"));
  }

  /**
   * Read TPP Password.
   *
   * @return tpp password
   */
  public String getTppPassword() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("TPPInfo.TppPassword"));
  }

  /**
   * Read Test Artifact Location.
   * @return test artifact folder location
   */
  public String getTestArtifactLocation() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Common.TestArtifactLocation"));
  }

  /**
   * Get Transport Keystore Alias.
   * @return alias
   */
  public Object getTransportKeystoreAlias() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration()
            .get("ApplicationConfigList.AppConfig.Transport.KeyStore.Alias"));
  }

  public String browserPreference() {
    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("BrowserAutomation.BrowserPreference"));
  }

  public String getGatewayURL() {

    return String.valueOf(ConfigParser.getInstance().getConfiguration().get("Server.GatewayURL"));
  }
}
