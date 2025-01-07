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
