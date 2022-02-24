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

        if (APIRequestRouterConstants.API_TYPE_NON_REGULATORY.equals(obapiRequestContext.getOpenAPI().getExtensions()
                        .get(APIRequestRouterConstants.API_TYPE_CUSTOM_PROP))) {
            obapiRequestContext.addContextProperty(APIRequestRouterConstants.API_TYPE_CUSTOM_PROP,
                    APIRequestRouterConstants.API_TYPE_NON_REGULATORY);
            return EMPTY_LIST;
        } else if (StringUtils.contains(obapiRequestContext.getMsgInfo().getResource(),
                APIRequestRouterConstants.PAYMENTS_TYPE)) {
            obapiRequestContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    APIRequestRouterConstants.PAYMENTS_TYPE);
            return this.getExecutorMap().get(APIRequestRouterConstants.PAYMENTS);
        } else {
            return this.getExecutorMap().get(APIRequestRouterConstants.DEFAULT);
        }
    }

    @Override
    public List<OpenBankingGatewayExecutor> getExecutorsForResponse(OBAPIResponseContext obapiResponseContext) {

        if (obapiResponseContext.getContextProps().containsKey(APIRequestRouterConstants.API_TYPE_CUSTOM_PROP)) {
            if (APIRequestRouterConstants.API_TYPE_NON_REGULATORY.equals(obapiResponseContext.getContextProps()
                    .get(APIRequestRouterConstants.API_TYPE_CUSTOM_PROP))) {
                return EMPTY_LIST;
            } else if (StringUtils.contains(obapiResponseContext.getMsgInfo().getResource(),
                    APIRequestRouterConstants.PAYMENTS_TYPE)) {
                return this.getExecutorMap().get(APIRequestRouterConstants.PAYMENTS);
            } else {
                return this.getExecutorMap().get(APIRequestRouterConstants.DEFAULT);
            }
        }
        return EMPTY_LIST;
    }
}
