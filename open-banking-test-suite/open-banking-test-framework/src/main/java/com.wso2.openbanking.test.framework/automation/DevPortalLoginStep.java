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
