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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

@PrepareForTest({CommonConfigParser.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class AccountsConsentPersistHandlerTest extends PowerMockTestCase {

    private static AccountsConsentPersistHandler accountsConsentPersistHandler;

    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    @Mock
    ConsentPersistData consentPersistData;

    @Mock
    ConsentResource consentResource;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);
        accountsConsentPersistHandler = new AccountsConsentPersistHandler(consentCoreServiceMock);

    }

    @BeforeMethod
    public void initMethod() {

        Map<String, String> headers = new HashMap<>();
        Map<String, String> requestHeaders = new HashMap<>();
        ConsentData consentData = new ConsentData("cf4d66c0-2900-4430-826c-b9cc7d61ddcf",
                "admin@wso2.com", "", "accounts", "", requestHeaders);
        consentData.setAuthResource(
                getSampleTestAuthorizationResource("bc4d66c0-2900-4430-826c-b9cc7d61d15c"));
        consentPersistData = new ConsentPersistData(new JSONObject(), headers, false, consentData);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
    }

    @Test
    public void testConsentPersist() throws URISyntaxException, ConsentManagementException {

        accountsConsentPersistHandler.consentPersist(consentPersistData, consentResource);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testConsentPersistWithUnApprovedConsentData() throws URISyntaxException, ConsentManagementException {

        Map<String, String> headers = new HashMap<>();
        Map<String, String> requestHeaders = new HashMap<>();
        ConsentData consentData = new ConsentData("cf4d66c0-2900-4430-826c-b9cc7d61ddcf",
                "admin@wso2.com", "", "accounts", "", requestHeaders);
        consentData.setAuthResource(
                getSampleTestAuthorizationResource("bc4d66c0-2900-4430-826c-b9cc7d61d15c"));
        consentPersistData = new ConsentPersistData(new JSONObject(), headers, true, consentData);
        accountsConsentPersistHandler.consentPersist(consentPersistData, consentResource);
    }

    private static AuthorizationResource getSampleTestAuthorizationResource(String consentID) {

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setConsentID(consentID);
        authorizationResource.setAuthorizationType("authorizationType");
        authorizationResource.setUserID("admin@wso2.com");
        authorizationResource.setAuthorizationStatus("awaitingAuthorization");

        return authorizationResource;
    }
}
