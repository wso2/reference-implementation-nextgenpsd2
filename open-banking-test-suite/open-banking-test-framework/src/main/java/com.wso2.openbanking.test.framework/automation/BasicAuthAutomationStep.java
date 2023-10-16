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

import com.wso2.openbanking.test.framework.util.ConfigParser;
import com.wso2.openbanking.test.framework.util.PsuConfigReader;
import com.wso2.openbanking.test.framework.util.TestConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Basic Authentication Automation.
 */
public class BasicAuthAutomationStep implements BrowserAutomationStep {

  public String authorizeUrl;
  private static final Log log = LogFactory.getLog(BasicAuthAutomationStep.class);

  /**
   * Initialize Basic Auth Flow.
   *
   * @param authorizeUrl authorise URL.
   */
  public BasicAuthAutomationStep(String authorizeUrl) {

    this.authorizeUrl = authorizeUrl;
  }

  /**
   * Execute automation using driver.
   *
   * @param webDriver driver object.
   * @param context   automation context.
   */
  @Override
  public void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

    webDriver.navigate().to(authorizeUrl);
    WebElement username;

    //Enter User Name
    if (TestConstants.APIM_VERSION_420.equals(ConfigParser.getInstance().getAPIMVersion())) {
      username = webDriver.findElement(By.id(TestConstants.USERNAME_FIELD_ID_420));
    } else {
      username = webDriver.findElement(By.id(TestConstants.USERNAME_FIELD_ID));
    }

    username.clear();
    username.sendKeys(PsuConfigReader.getPSU());

    WebElement password = webDriver.findElement(By.id(TestConstants.PASSWORD_FIELD_ID));
    password.clear();
    password.sendKeys(PsuConfigReader.getPSUPassword());

    //Click on Sign In Button
    webDriver.findElement(By.xpath(TestConstants.AUTH_SIGNIN_XPATH)).submit();

    WebDriverWait wait = new WebDriverWait(webDriver, 30);
    wait.until(ExpectedConditions.invisibilityOfElementLocated(
        By.id(TestConstants.PASSWORD_FIELD_ID)));

    //Second Factor Authentication Step
    try {
      if (webDriver.findElement(By.xpath(TestConstants.LBL_SMSOTP_AUTHENTICATOR)).isDisplayed()) {

        webDriver.navigate().refresh();

        String otpCode = TestConstants.OTP_CODE;

        webDriver.findElement(By.id(TestConstants.TXT_OTP_CODE)).sendKeys(otpCode);

        WebElement btnAuthenticate = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath(TestConstants.BTN_AUTHENTICATE)));
        btnAuthenticate.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.xpath(TestConstants.BTN_AUTHENTICATE)));

      }
    } catch (NoSuchElementException e) {
      log.info("Second Factor Authentication Step is not configured");
    }
  }
}
