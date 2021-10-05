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

import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Reporter;

/**
 * Screenshot automation step.
 */
public class ScreenshotAutomationStep implements BrowserAutomationStep {

  private static final String TITLE_TEMPLATE = "<h3>%s</h3>";
  private static final String IMAGE_TEMPLATE = "<img src=\"data:image/png;base64, %s\">";
  private String title = "Screenshot";

  /**
   * Initialize Screenshot Automation Step.
   */
  public ScreenshotAutomationStep() {

  }

  /**
   * Initialize Screenshot Automation Step with title.
   *
   * @param title title of the image
   */
  public ScreenshotAutomationStep(String title) {

    this.title = title;
  }

  /**
   * Execute automation using driver.
   *
   * @param webDriver driver object.
   * @param context   automation context.
   */
  @Override
  public void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

    // take screenshot
    String base64 = webDriver.getScreenshotAs(OutputType.BASE64);
    Reporter.log(String.format(TITLE_TEMPLATE, title));
    Reporter.log(String.format(IMAGE_TEMPLATE, base64));
  }
}
