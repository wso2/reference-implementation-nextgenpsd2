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

package com.wso2.openbanking.test.framework.automation;

import com.wso2.openbanking.test.framework.ui.AccessServiceProviderPageObject;
import com.wso2.openbanking.test.framework.ui.KmLoginPageObject;
import com.wso2.openbanking.test.framework.util.ConfigParser;
import com.wso2.openbanking.test.framework.util.TestConstants;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.TimeUnit;

/**
 * Basic Authentication Automation.
 */
class ObKmAutomationStep implements BrowserAutomationStep {

  public String apiStoreUrl;
  private RemoteWebDriver webDriver;
  KmLoginPageObject kmLoginPageObject;
  AccessServiceProviderPageObject accessServiceProviderPageObject;
  FirefoxOptions options;
  BrowserAutomation browserAutomation;

  /**
   * Initialize Basic Auth Flow.
   *
   * @param apiStoreUrl apiStore URL.
   */
  public ObKmAutomationStep(String apiStoreUrl) {
    this.apiStoreUrl = apiStoreUrl;
  }

  /**
   * login automation using driver.
   *
   * @param webDriver driver object.
   * @param context   automation context.
   */
  @Override
  public void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

  }

  public void initializeStep() {
    browserAutomation = new BrowserAutomation(10);
    options = new FirefoxOptions();
    options.setHeadless(true);
    webDriver = new FirefoxDriver(options);
    webDriver.navigate().to(apiStoreUrl);
  }

  /*
      Calls login step UI
   */
  public void loginStep() {
    kmLoginPageObject = new KmLoginPageObject(webDriver);
    kmLoginPageObject.setUserName(ConfigParser.getInstance().getKeyManagerAdminUsername());
    kmLoginPageObject.setPassword(ConfigParser.getInstance().getKeyManagerAdminPassword());
    kmLoginPageObject.submitLogin();
  }

  //Manage the implicit grant.
  public void setGrantTypeStep() {
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject = new AccessServiceProviderPageObject(webDriver);
    accessServiceProviderPageObject.serviceProviderMenu();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.editButton();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.clickInboundMenu();
    JavascriptExecutor js = webDriver;
    js.executeScript(TestConstants.WINDOWS_SCROLL_100);
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.dropDownKey();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.editLink();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.grantTypeSelect();
    js.executeScript(TestConstants.WINDOWS_SCROLL_20);
    accessServiceProviderPageObject.updateGrants();

    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
  }

  //Manage the client credentials grant.
  public void setClientCredentilasGrantTypeStep() {
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject = new AccessServiceProviderPageObject(webDriver);
    accessServiceProviderPageObject.serviceProviderMenu();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.editButton();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.clickInboundMenu();
    JavascriptExecutor js = webDriver;
    js.executeScript(TestConstants.WINDOWS_SCROLL_100);
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.dropDownKey();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.editLink();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.clientCredentilasSelect();
    js.executeScript(TestConstants.WINDOWS_SCROLL_20);
    accessServiceProviderPageObject.updateGrants();

    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
  }

  //Manage the refresh token grant.
  public void setRefreshTokenGrantTypeStep() {
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject = new AccessServiceProviderPageObject(webDriver);
    accessServiceProviderPageObject.serviceProviderMenu();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.editButton();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.clickInboundMenu();
    JavascriptExecutor js = webDriver;
    js.executeScript(TestConstants.WINDOWS_SCROLL_100);
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.dropDownKey();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.editLink();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.refreshTokenGrantSelect();
    js.executeScript(TestConstants.WINDOWS_SCROLL_20);
    accessServiceProviderPageObject.updateGrants();

    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
  }

  //Manage the SAML2 grant.
  public void setSaml2GrantTypeStep() {
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
    accessServiceProviderPageObject = new AccessServiceProviderPageObject(webDriver);
    accessServiceProviderPageObject.serviceProviderMenu();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
    accessServiceProviderPageObject.editButton();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);

    accessServiceProviderPageObject.clickInboundMenu();
    JavascriptExecutor js = webDriver;
    js.executeScript(TestConstants.WINDOWS_SCROLL_100);
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
    accessServiceProviderPageObject.dropDownKey();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
    accessServiceProviderPageObject.editLink();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
    accessServiceProviderPageObject.saml2GrantSelect();
    js.executeScript(TestConstants.WINDOWS_SCROLL_20);
    accessServiceProviderPageObject.updateGrants();
    webDriver.manage().timeouts().implicitlyWait(TestConstants.TIMEOUT, TimeUnit.SECONDS);
  }

  public void cleanUp() {
    accessServiceProviderPageObject = new AccessServiceProviderPageObject(webDriver);
    accessServiceProviderPageObject.closeClass();
  }
}
