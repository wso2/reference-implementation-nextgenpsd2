package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    public void testConsentPersistWithValidData() throws URISyntaxException, ConsentManagementException {

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
