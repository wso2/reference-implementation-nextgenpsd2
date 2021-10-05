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
import com.wso2.openbanking.test.framework.util.TestConstants;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Browser Automation for Authorisation.
 */
public class BrowserAutomation {

  private RemoteWebDriver driver;
  private final LinkedHashSet<BrowserAutomationStep> automationSteps = new LinkedHashSet<>();
  private final AutomationContext context = new AutomationContext();
  public static final int DEFAULT_DELAY = 5;

  /**
   * Initialize Automation Context.
   */
  public class AutomationContext {

    public Optional<String> currentUrl = Optional.empty();
    public int timeoutSeconds;
  }

  /**
   * Initialize automation harness.
   *
   * @param stepDelaySeconds delay between steps.
   */
  public BrowserAutomation(int stepDelaySeconds) {

    switch (ConfigParser.getInstance().browserPreference()) {
      case TestConstants.CHROME:
        System.setProperty(TestConstants.CHROME_DRIVER_NAME, ConfigParser.getInstance().getDriverLocation());
        ChromeOptions chromeOptions = new ChromeOptions();
        if (ConfigParser.getInstance().isHeadless()) {
          chromeOptions.addArguments(TestConstants.HEADLESS_TAG, "--window-size=1920,1200");
        }
        driver = new ChromeDriver(chromeOptions);
        context.timeoutSeconds = stepDelaySeconds;
        break;
      default:
        FirefoxBinary firefoxBinary = new FirefoxBinary();
        if (ConfigParser.getInstance().isHeadless()) {
          firefoxBinary.addCommandLineOptions(TestConstants.HEADLESS_TAG);
        }
        System.setProperty(TestConstants.FIREFOX_DRIVER_NAME, ConfigParser.getInstance().getDriverLocation());
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setBinary(firefoxBinary);
        driver = new FirefoxDriver(firefoxOptions);
        context.timeoutSeconds = stepDelaySeconds;
    }
  }

  /**
   * Initialize automation harness.
   * Use this constructor if need to use the same session for a consecutive
   * authorisation
   *
   * @param stepDelaySeconds      delay between steps.
   * @param useSameBrowserSession value to indicate whether to use the same drive object
   *                              instead of creating a new one
   */
  public BrowserAutomation(int stepDelaySeconds, boolean useSameBrowserSession) {

    switch (ConfigParser.getInstance().browserPreference()) {
      case TestConstants.CHROME :
        System.setProperty(TestConstants.CHROME_DRIVER_NAME, ConfigParser.getInstance().getDriverLocation());
        ChromeOptions chromeOptions = new ChromeOptions();
        if (ConfigParser.getInstance().isHeadless()) {
          chromeOptions.addArguments(TestConstants.HEADLESS_TAG, "--window-size=1920,1200");
        }
        if (!useSameBrowserSession) {
          driver = new ChromeDriver(chromeOptions);
        }
        context.timeoutSeconds = stepDelaySeconds;
        break;
      default:
        FirefoxBinary firefoxBinary = new FirefoxBinary();
        if (ConfigParser.getInstance().isHeadless()) {
          firefoxBinary.addCommandLineOptions(TestConstants.HEADLESS_TAG);
        }
        System.setProperty(TestConstants.FIREFOX_DRIVER_NAME, ConfigParser.getInstance().getDriverLocation());
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setBinary(firefoxBinary);
        if (!useSameBrowserSession) {
          driver = new FirefoxDriver(firefoxOptions);
        }
        context.timeoutSeconds = stepDelaySeconds;
    }
  }

  /**
   * Add automation step.
   *
   * @param automationStep automation step.
   * @return self.
   */
  public BrowserAutomation addStep(BrowserAutomationStep automationStep) {

    automationSteps.add(automationStep);
    return this;
  }

  /**
   * Execute Automation Steps.
   */
  public AutomationContext execute() {

    driver.manage().timeouts().implicitlyWait(context.timeoutSeconds, TimeUnit.SECONDS);

    try {
      for (BrowserAutomationStep automationStep : automationSteps) {
        automationStep.execute(driver, context);
        context.currentUrl = Optional.ofNullable(driver.getCurrentUrl());
      }
    } catch (Exception e) {
      new ScreenshotAutomationStep("Point of error").execute(driver, context);
      throw new RuntimeException(e);
    } finally {
      driver.quit();
    }
    return context;
  }

  /**
   * Execute Automation Steps.
   *
   * @param closeSession boolean value to indicate whether the session should be closed
   */
  public AutomationContext execute(boolean closeSession) {

    driver.manage().timeouts().implicitlyWait(context.timeoutSeconds, TimeUnit.SECONDS);

    try {
      for (BrowserAutomationStep automationStep : automationSteps) {
        automationStep.execute(driver, context);
        context.currentUrl = Optional.ofNullable(driver.getCurrentUrl());
      }
    } catch (Exception e) {
      new ScreenshotAutomationStep("Point of error").execute(driver, context);
      throw new RuntimeException(e);
    }
    if (closeSession) {
      driver.quit();
    }
    return context;
  }

  /**
   * Get Automation Context.
   *
   * @return automation context.
   */
  public AutomationContext getContext() {
    return context;
  }
}
