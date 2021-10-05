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
