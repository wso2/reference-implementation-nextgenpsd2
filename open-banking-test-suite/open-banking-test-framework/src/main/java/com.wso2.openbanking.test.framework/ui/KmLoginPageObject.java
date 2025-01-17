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
