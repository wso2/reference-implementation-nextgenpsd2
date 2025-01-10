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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

public class DevPortalLoginStep implements BrowserAutomationStep {

	public String devPortalLoginStep;

	/**
	 * Initialize DevPortal Login Step.
	 *
	 * @param devPortalLoginStep devPortal URL.
	 */
	public DevPortalLoginStep(String devPortalLoginStep)
	{
		this.devPortalLoginStep = devPortalLoginStep;
	}

	/**
	 * Execute automation using driver.
	 *
	 * @param webDriver driver object.
	 * @param context   automation context.
	 */
	@Override
	public void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

		webDriver.navigate().to(devPortalLoginStep);
		webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		webDriver.findElement(By.xpath(TestConstants.BTN_DEVPORTAL_SIGNIN)).click();
		WebElement username;

		//Enter User Name
		if (TestConstants.APIM_VERSION_420.equals(ConfigParser.getInstance().getAPIMVersion())) {
			username = webDriver.findElement(By.id(TestConstants.USERNAME_FIELD_ID_420));
		} else {
			username = webDriver.findElement(By.id(TestConstants.USERNAME_FIELD_ID));
		}
		username.clear();
		username.sendKeys(ConfigParser.getInstance().getTppUserName());

		WebElement password = webDriver.findElement(By.id(TestConstants.APIM_PASSWORD));
		password.clear();
		password.sendKeys(ConfigParser.getInstance().getTppPassword());

		//Click on Continue Button
		webDriver.findElement(By.xpath(TestConstants.BTN_APIM_CONTINUE)).submit();

		WebDriverWait wait = new WebDriverWait(webDriver, 30);

		if (TestConstants.APIM_VERSION_420.equals(ConfigParser.getInstance().getAPIMVersion())) {
			wait.until(ExpectedConditions.invisibilityOfElementLocated(
					By.id(TestConstants.USERNAME_FIELD_ID_420)));
		} else {
			wait.until(ExpectedConditions.invisibilityOfElementLocated(
					By.id(TestConstants.USERNAME_FIELD_ID)));
		}
	}
}
