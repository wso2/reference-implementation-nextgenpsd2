/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.utils;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.consent.extensions.common.AuthTypeEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.util.TestConstants;
import com.wso2.openbanking.berlin.consent.extensions.util.TestUtil;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
