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
 * The Page factory class for service provider grant type.
 */
public class AccessServiceProviderPageObject {

  /**
   * All WebElements are identified by @FindBy annotation.
   */
  WebDriver driver;

  @FindBy(xpath = TestConstants.SP_MENU_NAME)

  WebElement serviceProviderMenu;

  @FindBy(xpath = TestConstants.SP_EDIT_XPATH)

  WebElement spEditLink;

  @FindBy(xpath = TestConstants.INBOUND_MENU)

  WebElement inboundMenu;

  @FindBy(xpath = TestConstants.DROPDOWN_KEY)

  WebElement dropdownKey;

  @FindBy(xpath = TestConstants.EDIT_LINK)

  WebElement editLink;

  @FindBy(xpath = TestConstants.SELECT_IMPLICIT_GRANT_XPATH)

  WebElement implicitGrant;

  @FindBy(xpath = TestConstants.UPDATE_BUTTON_XPATH)

  WebElement updateGrants;

  @FindBy(xpath = TestConstants.SELECT_CLIENT_CREDENTIALS_GRANT)

  WebElement clientCredentials;

  @FindBy(xpath = TestConstants.SELECT_AUTH_CODE_GRANT)

  WebElement codeGrant;

  @FindBy(xpath = TestConstants.SELECT_REFRESH_TOKEN_GRANT)

  WebElement refreshToken;

  @FindBy(xpath = TestConstants.SELECT_SAML2_GRANT)

  WebElement saml2Grant;

  /**
   * Initialize Service Provider Page Object.
   *
   * @param driver wedDriver
   */
  public AccessServiceProviderPageObject(WebDriver driver) {
    this.driver = driver;
    //This initElements method will create all WebElements
    PageFactory.initElements(driver, this);
  }

  public void serviceProviderMenu() {
    serviceProviderMenu.click();
  }

  public void editButton() {
    spEditLink.click();
  }

  public void clickInboundMenu() {
    inboundMenu.click();
  }

  public void dropDownKey() {
    dropdownKey.click();
  }

  public void editLink() {
    editLink.click();
  }

  public void grantTypeSelect() {
    implicitGrant.click();
  }

  public void clientCredentilasSelect() {
    clientCredentials.click();
  }

  public void codeGrantSelect() {
    codeGrant.click();
  }

  public void saml2GrantSelect() {
    saml2Grant.click();
  }

  public void refreshTokenGrantSelect() {
    refreshToken.click();
  }

  public void updateGrants() {
    updateGrants.click();
  }

  public void closeClass() {
    driver.close();
  }
}
