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

import com.wso2.openbanking.test.framework.util.TestConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

/**
 * Basic Authentication Automation.
 */
public class ConfigurableBasicAuthRejectStep implements BrowserAutomationStep {

    public String authorizeUrl;
    public String userName;
    public String userPassword;
    private static final Log log = LogFactory.getLog(ConfigurableBasicAuthRejectStep.class);

    /**
     * Initialize Basic Auth Flow.
     *
     * @param authorizeUrl authorise URL.
     */
    public ConfigurableBasicAuthRejectStep(String authorizeUrl, String userName, String userPassword) {

        this.authorizeUrl = authorizeUrl;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    /**
     * Execute automation using driver
     *
     * @param webDriver driver object.
     * @param context   automation context.
     */
    @Override
    public void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {

        webDriver.navigate().to(authorizeUrl);
        WebElement username;
        WebDriverWait wait = new WebDriverWait(webDriver, 30);

        username = webDriver.findElement(By.id(TestConstants.USERNAME_FIELD_ID));

        username.clear();
        username.sendKeys(userName);

        WebElement password = webDriver.findElement(By.id(TestConstants.PASSWORD_FIELD_ID));
        password.clear();
        password.sendKeys(userPassword);

        webDriver.findElement(By.xpath(TestConstants.AUTH_SIGNIN_XPATH)).submit();
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        //Second Factor Authentication Step
        try {
            if (webDriver.findElement(By.xpath(TestConstants.LBL_SMSOTP_AUTHENTICATOR)).isDisplayed()) {

                webDriver.navigate().refresh();

                String otpCode = TestConstants.OTP_CODE;

                webDriver.findElement(By.id(TestConstants.TXT_OTP_CODE)).sendKeys(otpCode);

                WebElement btnAuthenticate = wait.until(
                        ExpectedConditions.elementToBeClickable(By.xpath(TestConstants.BTN_AUTHENTICATE)));
                btnAuthenticate.click();

                webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

            }
        } catch (NoSuchElementException e) {
            log.info("Second Factor Authentication Step is not configured");
        }

        webDriver.findElement(By.xpath(TestConstants.CONSENT_DENY_XPATH)).submit();

        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }
}
