/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.gateway.executors.core;

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

        if (StringUtils.contains(resource, APIRequestRouterConstants.PAYMENTS_TYPE)) {
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
