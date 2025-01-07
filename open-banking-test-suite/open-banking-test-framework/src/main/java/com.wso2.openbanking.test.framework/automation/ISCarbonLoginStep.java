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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

/**
 * IS Carbon Login Step.
 */
public class ISCarbonLoginStep implements BrowserAutomationStep {

	public String isServerUrl;

	/**
	 * Initialize IS Carbon Login Step.
	 *
	 * @param isServerUrl IS Server URL.
	 */
	public ISCarbonLoginStep(String isServerUrl) {
		this.isServerUrl = isServerUrl;
	}

	/**
	 * Execute automation using driver.
	 *
	 * @param webDriver driver object.
	 * @param context   automation context.
	 */
	@Override
	public void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

		webDriver.navigate().to(isServerUrl);
		webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		WebElement username;

		//Enter User Name
		username = webDriver.findElement(By.id(TestConstants.IS_USERNAME_ID));
		username.clear();
		username.sendKeys(PsuConfigReader.getPSU());

		WebElement password = webDriver.findElement(By.id(TestConstants.IS_PASSWORD_ID));
		password.clear();
		password.sendKeys(PsuConfigReader.getPSUPassword());

		//Click on Sign In Button
		webDriver.findElement(By.xpath(TestConstants.BTN_IS_SIGNING)).submit();

		WebDriverWait wait = new WebDriverWait(webDriver, 30);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(
						By.id(TestConstants.IS_USERNAME_ID)));
	}
}
