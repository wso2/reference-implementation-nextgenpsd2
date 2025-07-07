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

package org.wso2.openbanking.berlin.identity.response.type.handlers;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
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
import org.wso2.openbanking.berlin.identity.response.type.constants.IdentityConstants;

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
