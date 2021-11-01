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

package com.wso2.openbanking.test.framework.ui;

import com.wso2.openbanking.test.framework.util.TestConstants;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * The Page factory class for KM Management Console login.
 */
public class KmLoginPageObject {

  /**
   * All WebElements are identified by @FindBy annotation.
   */
  WebDriver driver;

  @FindBy(xpath = TestConstants.LOGIN_LINK)
  WebElement loginLink;

  @FindBy(xpath = TestConstants.USERNAME_XPATH)
  WebElement username;

  @FindBy(xpath = TestConstants.PASSWORD_XPATH)
  WebElement password;

  @FindBy(xpath = TestConstants.LOGIN_BUTTON)
  WebElement login;

  /**
   * Initialize IAM Login Page Object.
   *
   * @param driver webDriver
   */
  public KmLoginPageObject(WebDriver driver) {

    this.driver = driver;
    //This initElements method will create all WebElements
    PageFactory.initElements(driver, this);
  }

  public void clickLogin() {
    loginLink.click();
  }

  public void setUserName(String strUserName) {
    username.sendKeys(strUserName);
  }

  public void setPassword(String strPassword) {
    password.sendKeys(strPassword);
  }

  public void submitLogin() {
    login.click();
  }


  /**
   * This POM method will be exposed in test case to APIStoreLoginPage in the application.
   *
   * @param strUserName userName
   * @param strPasword  password
   * @return
   */
  public KmLoginPageObject(String strUserName, String strPasword) {

    //Fill user name

    this.setUserName(strUserName);

    //Fill password

    this.setPassword(strPasword);
  }

  public void closeClass() {
    driver.close();
    driver.quit();
  }
}
