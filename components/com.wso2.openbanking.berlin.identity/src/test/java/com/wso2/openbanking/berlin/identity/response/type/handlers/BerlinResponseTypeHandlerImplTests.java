/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.identity.response.type.handlers;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.berlin.identity.response.type.constants.IdentityConstants;
import graphql.Assert;
import org.apache.commons.lang3.ArrayUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;

@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class})
public class BerlinResponseTypeHandlerImplTests extends PowerMockTestCase {

    private static Map<String, String> configMap;
    private static BerlinResponseTypeHandlerImpl berlinResponseTypeHandler;

    @Mock
    OAuthAuthzReqMessageContext authAuthzReqMessageContextMock;

    @Mock
    OAuth2AuthorizeReqDTO oAuth2AuthorizeReqDTOMock;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    @BeforeClass
    public void initClass() {

        berlinResponseTypeHandler = new BerlinResponseTypeHandlerImpl();
        authAuthzReqMessageContextMock = Mockito.mock(OAuthAuthzReqMessageContext.class);
        oAuth2AuthorizeReqDTOMock = Mockito.mock(OAuth2AuthorizeReqDTO.class);

        openBankingConfigParser = PowerMockito.mock(OpenBankingConfigParser.class);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        configMap = new HashMap<>();
        configMap.put(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME, "CONSENT_ID_");
    }

    @Test
    public void testUpdateApprovedScopes() {

        String consentId = UUID.randomUUID().toString();
        doReturn(oAuth2AuthorizeReqDTOMock).when(authAuthzReqMessageContextMock).getAuthorizationReqDTO();

        String[] scopes = {"pis:" + consentId};
        doReturn(scopes).when(oAuth2AuthorizeReqDTOMock).getScopes();
        doReturn(configMap).when(openBankingConfigParser).getConfiguration();

        String[] updatedScopes = berlinResponseTypeHandler.updateApprovedScopes(authAuthzReqMessageContextMock);
        Assert.assertTrue(ArrayUtils.contains(updatedScopes, "pis:" + consentId));
        Assert.assertTrue(ArrayUtils.contains(updatedScopes,
                configMap.get(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME) + consentId));
        Assert.assertTrue(ArrayUtils.contains(updatedScopes, IdentityConstants.PAYMENTS_SCOPE));
    }
}
