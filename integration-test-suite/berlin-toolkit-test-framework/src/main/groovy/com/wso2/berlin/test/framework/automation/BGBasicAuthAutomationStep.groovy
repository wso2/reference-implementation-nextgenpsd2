package com.wso2.berlin.test.framework.automation

import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.openbanking.test.framework.automation.BrowserAutomationStep
import com.wso2.openbanking.test.framework.automation.OBBrowserAutomation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait


/**
 * Basic Authentication Automation.
 */
class BGBasicAuthAutomationStep implements BrowserAutomationStep {

    public String authorizeUrl;
    private static final Log log = LogFactory.getLog(BGBasicAuthAutomationStep.class);
    private BGConfigurationService bgConfiguration


    /**
     * Initialize Basic Auth Flow.
     *
     * @param authorizeUrl authorise URL.
     */
    public BasicAuthAutomationStep(String authorizeUrl) {

        this.authorizeUrl = authorizeUrl;
    }

    /**
     * Execute automation using driver.
     *
     * @param webDriver driver object.
     * @param context   automation context.
     */
    @Override
    public void execute(RemoteWebDriver webDriver, OBBrowserAutomation.AutomationContext context) {

        webDriver.navigate().to(authorizeUrl);
        WebElement username;

        //Enter User Name
        username = webDriver.findElement(By.id(BerlinConstants.USERNAME_FIELD_ID));

        username.clear();
        username.sendKeys(bgConfiguration.getUserPSUName());

        WebElement password = webDriver.findElement(By.id(BerlinConstants.PASSWORD));
        password.clear();
        password.sendKeys(bgConfiguration.getUserPSUPWD());

        //Click on Sign In Button
        webDriver.findElement(By.xpath(BerlinConstants.AUTH_SIGNIN_XPATH)).submit();

        WebDriverWait wait = new WebDriverWait(webDriver, 30);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.id(BerlinConstants.PASSWORD)));

        //Second Factor Authentication Step
        try {
            if (webDriver.findElement(By.xpath(BerlinConstants.LBL_SMSOTP_AUTHENTICATOR)).isDisplayed()) {

                webDriver.navigate().refresh();

                String otpCode = BerlinConstants.OTP_CODE;

                webDriver.findElement(By.id(BerlinConstants.TXT_OTP_CODE)).sendKeys(otpCode);

                WebElement btnAuthenticate = wait.until(
                        ExpectedConditions.elementToBeClickable(By.xpath(BerlinConstants.BTN_AUTHENTICATE)));
                btnAuthenticate.click();

                wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath(BerlinConstants.BTN_AUTHENTICATE)));

            }
        } catch (NoSuchElementException e) {
            log.info("Second Factor Authentication Step is not configured");
        }
    }
}

