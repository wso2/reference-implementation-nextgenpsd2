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

package com.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CLass to add required throttling key parameters to the messageContext in frequency per day scenarios.
 */
public class FrequencyPerDayExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(FrequencyPerDayExecutor.class);

    private static final String CUSTOM_PROPERTY = "customProperty";
    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String CONSENT_ID_HEADER = "Consent-ID";
    //Key map constants
    private static final String ACCOUNT_ID = "accountId";
    private static final String CONSENT_ID = "consentId";

    private HashMap<String, Object> customKeyMap = new HashMap<>();

    private static final List<String> eligibleResources = Arrays.asList("/accounts/{account-id}", "/accounts/{account"
                    + "-id}?withBalance", "/accounts/{account-id}/balances", "/accounts/{account-id}/transactions",
            "/accounts" + "/{account-id}/transactions?withBalance", "/accounts/{account-id}/transactions" +
                    "/{transactionId}", "/card" + "-accounts/{account-id}", "/card-accounts/{account-id}/balances",
            "/card-accounts/{account-id" + "}/transactions", "/card-accounts/{account-id}/transactions/{transactionId"
                    + "}");

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        Map<String, String> requestHeaders = obapiRequestContext.getMsgInfo().getHeaders();

        if (/*CommonConfigParser.getInstance().isFrequencyPerDayThrottlingEnabled()
                && */StringUtils.isNotBlank(requestHeaders.get(PSU_IP_ADDRESS))) {

            String electedResource = obapiRequestContext.getMsgInfo().getElectedResource();
            String resourceString = obapiRequestContext.getMsgInfo().getResource();

            if (log.isDebugEnabled()) {
                log.debug("Checking whether the " + resourceString + " is eligible for throttling");
            }

//            if (isEligibleForFrequencyPerDayThrottling(electedResource)) {

                List<String> pathList = Arrays.asList(resourceString.split("/"));
                String accountID = "accountID";
                String consentID = "ConsentID";
                customKeyMap.put(ACCOUNT_ID, accountID);
                customKeyMap.put(CONSENT_ID, consentID);

                //setting throttling property to be utilized by the throttle handler
                obapiRequestContext.setCustomProperty(customKeyMap);
//            }
        }
    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Check if the request is eligible for frequency per day throttling.
     *
     * @param url elected API resource
     * @return true if eligible; false otherwise
     */
    private boolean isEligibleForFrequencyPerDayThrottling(String url) {

        for (String path : eligibleResources) {
            if (path.equals(url)) {
                return true;
            }
        }
        return false;
    }
}
