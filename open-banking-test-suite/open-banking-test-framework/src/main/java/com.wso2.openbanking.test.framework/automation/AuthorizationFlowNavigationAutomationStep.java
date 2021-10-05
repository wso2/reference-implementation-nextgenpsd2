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

import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.TimeUnit;

/**
 * Duplicate Authorization Flow Navigation Automation.
 */
public class AuthorizationFlowNavigationAutomationStep implements BrowserAutomationStep {

  public String authorizeUrl;

  /**
   * Initialize Basic Auth Flow.
   *
   * @param authorizeUrl authorise URL.
   */
  public AuthorizationFlowNavigationAutomationStep(String authorizeUrl) {

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

    webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }
}
