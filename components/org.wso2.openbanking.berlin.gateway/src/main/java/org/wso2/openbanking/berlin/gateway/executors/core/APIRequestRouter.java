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

package org.wso2.openbanking.berlin.gateway.executors.core;

import com.wso2.openbanking.accelerator.gateway.executor.core.AbstractRequestRouter;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Request router for Berlin APIs.
 */
public class APIRequestRouter extends AbstractRequestRouter {

    private static final List<OpenBankingGatewayExecutor> EMPTY_LIST = new ArrayList<>();

    @Override
    public List<OpenBankingGatewayExecutor> getExecutorsForRequest(OBAPIRequestContext obapiRequestContext) {

        String resource = obapiRequestContext.getMsgInfo().getResource();

        if (GatewayConstants.API_TYPE_CONSENT
                .equals(obapiRequestContext.getOpenAPI().getExtensions().get(GatewayConstants.API_TYPE_CUSTOM_PROP))) {
            //add support for consent management portal APIs
            obapiRequestContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    GatewayConstants.API_TYPE_CONSENT);
            return this.getExecutorMap().get("Consent");
        } else if (StringUtils.contains(resource, APIRequestRouterConstants.PAYMENTS_TYPE)) {
            obapiRequestContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    APIRequestRouterConstants.PAYMENTS_TYPE);
            return this.getExecutorMap().get(APIRequestRouterConstants.PAYMENTS);
        } else if (StringUtils.contains(resource, APIRequestRouterConstants.FUNDS_CONFIRMATIONS_TYPE)
                || StringUtils.contains(resource, APIRequestRouterConstants.CONFIRMATION_OF_FUNDS_TYPE)) {
            obapiRequestContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    APIRequestRouterConstants.FUNDS_CONFIRMATIONS_TYPE);
            return this.getExecutorMap().get(APIRequestRouterConstants.FUNDS_CONFIRMATIONS);
        } else if (StringUtils.contains(resource, APIRequestRouterConstants.ACCOUNTS_TYPE)
                || StringUtils.contains(resource, APIRequestRouterConstants.ACCOUNTS_INITIATION_TYPE)) {
            obapiRequestContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    APIRequestRouterConstants.ACCOUNTS_TYPE);
            return this.getExecutorMap().get(APIRequestRouterConstants.ACCOUNTS);
        } else {
            return EMPTY_LIST;
        }
    }

    @Override
    public List<OpenBankingGatewayExecutor> getExecutorsForResponse(OBAPIResponseContext obapiResponseContext) {

        String resource = obapiResponseContext.getMsgInfo().getResource();

        if (StringUtils.contains(resource, APIRequestRouterConstants.PAYMENTS_TYPE)) {
            obapiResponseContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    APIRequestRouterConstants.PAYMENTS_TYPE);
            return this.getExecutorMap().get(APIRequestRouterConstants.PAYMENTS);
        } else if (StringUtils.contains(resource, APIRequestRouterConstants.FUNDS_CONFIRMATIONS_TYPE)
                || StringUtils.contains(resource, APIRequestRouterConstants.CONFIRMATION_OF_FUNDS_TYPE)) {
            obapiResponseContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    APIRequestRouterConstants.FUNDS_CONFIRMATIONS_TYPE);
            return this.getExecutorMap().get(APIRequestRouterConstants.FUNDS_CONFIRMATIONS);
        } else if (StringUtils.contains(resource, APIRequestRouterConstants.ACCOUNTS_TYPE)
                || StringUtils.contains(resource, APIRequestRouterConstants.ACCOUNTS_INITIATION_TYPE)) {
            obapiResponseContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    APIRequestRouterConstants.ACCOUNTS_TYPE);
            return this.getExecutorMap().get(APIRequestRouterConstants.ACCOUNTS);
        } else {
            return EMPTY_LIST;
        }
    }
}
