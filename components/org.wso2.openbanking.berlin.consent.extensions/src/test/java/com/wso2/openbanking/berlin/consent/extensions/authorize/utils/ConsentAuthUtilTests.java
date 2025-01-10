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

package org.wso2.openbanking.berlin.consent.extensions.authorize.utils;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import org.wso2.openbanking.berlin.consent.extensions.util.TestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Contains tests related to ConsentAuthUtils class.
 */
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class ConsentAuthUtilTests {

    private static String consentId;
    private static String authId;
    List<AuthorizationResource> authResourcesList;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;

    @BeforeMethod
    public void initMethod() throws OpenBankingException, IOException {

        authId = UUID.randomUUID().toString();
        consentId = UUID.randomUUID().toString();
        authId = UUID.randomUUID().toString();
        authResourcesList = new ArrayList<>();
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
    }

    @Test
    public void testWithOneValidAuthResource() throws ConsentManagementException {

        AuthorizationResource authorizationResource = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource);
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        Assert.assertTrue(ConsentAuthUtil.areAllOtherAuthResourcesValid(consentCoreServiceMock, consentId,
                authorizationResource));
    }

    @Test
    public void testWithTwoValidAuthResources() throws ConsentManagementException {

        AuthorizationResource authorizationResource1 = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        AuthorizationResource authorizationResource2 = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.PSU_AUTHENTICATED.toString(), authId,
                TestConstants.USER_ID);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource1);
        authorizationResources.add(authorizationResource2);
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        Assert.assertTrue(ConsentAuthUtil.areAllOtherAuthResourcesValid(consentCoreServiceMock, consentId,
                authorizationResource1));
    }

    @Test
    public void testWithTwoResourcesWithOneInvalidResource() throws ConsentManagementException {

        AuthorizationResource authorizationResource1 = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        AuthorizationResource authorizationResource2 = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.AUTHORISATION.toString(), ScaStatusEnum.FAILED.toString(), "123",
                TestConstants.USER_ID);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource1);
        authorizationResources.add(authorizationResource2);
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        Assert.assertFalse(ConsentAuthUtil.areAllOtherAuthResourcesValid(consentCoreServiceMock, consentId,
                authorizationResource1));
    }

    @Test
    public void testWithTwoResourcesWithOneInvalidResourceForCancellation() throws ConsentManagementException {

        AuthorizationResource authorizationResource1 = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.CANCELLATION.toString(), ScaStatusEnum.RECEIVED.toString(), authId,
                TestConstants.USER_ID);

        AuthorizationResource authorizationResource2 = TestUtil.getSampleStoredTestAuthorizationResource(consentId,
                AuthTypeEnum.CANCELLATION.toString(), ScaStatusEnum.FAILED.toString(), "123",
                TestConstants.USER_ID);

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(authorizationResource1);
        authorizationResources.add(authorizationResource2);
        doReturn(authorizationResources).when(consentCoreServiceMock).searchAuthorizations(Mockito.anyString());
        Assert.assertFalse(ConsentAuthUtil.areAllOtherAuthResourcesValid(consentCoreServiceMock, consentId,
                authorizationResource1));
    }
}
