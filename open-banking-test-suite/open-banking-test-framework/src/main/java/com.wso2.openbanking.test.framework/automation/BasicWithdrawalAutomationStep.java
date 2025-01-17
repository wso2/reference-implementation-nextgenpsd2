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

package com.wso2.openbanking.test.framework.automation;

import com.wso2.openbanking.test.framework.util.PsuConfigReader;
import com.wso2.openbanking.test.framework.util.TestConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.TimeUnit;

/**
 * Basic Withdrawal Automation Step Class.
 */
public class BasicWithdrawalAutomationStep implements BrowserAutomationStep {

  public String consentWithdrawalUrl;

  /**
   * Initialize Basic Withdrawal Automation Step.
   *
   * @param consentWithdrawalUrl consentmgt url
   */
  public BasicWithdrawalAutomationStep(String consentWithdrawalUrl) {

    this.consentWithdrawalUrl = consentWithdrawalUrl;

  }

  /**
   * Execute automation using driver.
   *
   * @param webDriver driver object.
   * @param context   automation context.
   */
  @Override
  public void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

    webDriver.navigate().to(consentWithdrawalUrl);
    WebElement username;

    if (TestConstants.APIM_VERSION_420.equals(com.wso2.openbanking.test.framework.util.ConfigParser.getInstance()
            .getAPIMVersion())) {
      username = webDriver.findElement(By.id(TestConstants.USERNAME_FIELD_ID_420));
    } else {
      username = webDriver.findElement(By.id(TestConstants.USERNAME_FIELD_ID));
    }

    username.clear();
    username.sendKeys(PsuConfigReader.getPSU());

    WebElement password = webDriver.findElement(By.id(TestConstants.PASSWORD_FIELD_ID));
    password.clear();
    password.sendKeys(PsuConfigReader.getPSUPassword());

    webDriver.findElement(By.xpath(TestConstants.AUTH_SIGNIN_XPATH)).submit();

    webDriver.manage().timeouts().implicitlyWait(100, TimeUnit.SECONDS);
  }
}
