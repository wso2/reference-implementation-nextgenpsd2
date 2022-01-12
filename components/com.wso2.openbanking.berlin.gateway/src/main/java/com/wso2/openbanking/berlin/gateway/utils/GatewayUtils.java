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

package com.wso2.openbanking.berlin.gateway.utils;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;

import java.util.ArrayList;

/**
 * This class contains the util methods related to the gateway module.
 */
public class GatewayUtils {

    public static void handleFailure(OBAPIRequestContext obapiRequestContext, String code, String message) {

        obapiRequestContext.setError(true);
        ArrayList<OpenBankingExecutorError> executorErrors = new ArrayList<>();
        OpenBankingExecutorError openBankingExecutorError = new OpenBankingExecutorError();
        openBankingExecutorError.setCode(code);
        openBankingExecutorError.setMessage(message);
        executorErrors.add(openBankingExecutorError);
        obapiRequestContext.setErrors(executorErrors);
    }
}
