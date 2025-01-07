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

package com.wso2.openbanking.berlin.common.utils

import com.wso2.openbanking.test.framework.automation.BrowserAutomation
import com.wso2.openbanking.test.framework.automation.BrowserAutomationStep
import org.openqa.selenium.remote.RemoteWebDriver

import java.util.concurrent.TimeUnit

/**
 * Basic Authentication Automation.
 */
class AuthAutomationSteps implements BrowserAutomationStep {

    public String authorizeUrl

    /**
     * Initialize Auth Flow.
     *
     * @param authorizeUrl authorise URL.
     */
    AuthAutomationSteps(String authorizeUrl) {

        this.authorizeUrl = authorizeUrl
    }

    /**
     * Execute automation using driver
     *
     * @param webDriver driver object.
     * @param context   automation context.
     */
    @Override
    void execute(RemoteWebDriver webDriver, BrowserAutomation.AutomationContext context) {
        webDriver.navigate().to(authorizeUrl)
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    }
}
