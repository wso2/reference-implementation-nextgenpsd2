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

package com.wso2.openbanking.berlin.gateway.throttling;

import com.wso2.openbanking.accelerator.gateway.throttling.ThrottleDataPublisher;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Berlin Throttle data publisher to frequency per day add custom throttling properties based the request.
 */
public class CustomThrottleDataPublisherImpl implements ThrottleDataPublisher {

    private static final Log log = LogFactory.getLog(CustomThrottleDataPublisherImpl.class);

    public static final String VIEW_ACCOUNTS = "accounts";
    public static final String VIEW_CARD_ACCOUNTS = "card-accounts";

    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String CONSENT_ID_HEADER = "Consent-ID";

    //Key map constants
    private static final String ACCOUNT_ID = "accountId";
    private static final String CONSENT_ID = "consentId";
    private static final String CONSUMER_KEY = "consumerKey";

    private static final List<String> eligibleResources = Arrays.asList("/accounts/{account-id}", "/accounts/{account"
                    + "-id}?withBalance", "/accounts/{account-id}/balances", "/accounts/{account-id}/transactions",
            "/accounts/{account-id}/transactions?withBalance", "/accounts/{account-id}/transactions" +
                    "/{transactionId}", "/card-accounts/{account-id}", "/card-accounts/{account-id}/balances",
            "/card-accounts/{account-id}/transactions", "/card-accounts/{account-id}/transactions/{transactionId}");

    @Override
    public Map<String, Object> getCustomProperties(RequestContextDTO requestContextDTO) {

        Map<String, Object> customKeyMap = new HashMap<>();
        Map<String, String> requestHeaders = requestContextDTO.getMsgInfo().getHeaders();

        if (CommonConfigParser.getInstance().isFrequencyPerDayThrottlingEnabled()
                && StringUtils.isNotBlank(requestHeaders.get(PSU_IP_ADDRESS))) {

            String electedResource = requestContextDTO.getMsgInfo().getElectedResource();
            String resourceString = requestContextDTO.getMsgInfo().getResource();
            String clientId = requestContextDTO.getApiRequestInfo().getConsumerKey();

            if (log.isDebugEnabled()) {
                log.debug("Checking whether the " + resourceString + " is eligible for throttling");
            }

            if (isEligibleForFrequencyPerDayThrottling(electedResource)) {

                List<String> pathList = Arrays.asList(resourceString.split("/"));
                String accountID = getAccountIdFromURL(pathList);
                String consentID = requestHeaders.get(CONSENT_ID_HEADER);
                customKeyMap.put(ACCOUNT_ID, accountID);
                customKeyMap.put(CONSENT_ID, consentID);
                customKeyMap.put(CONSUMER_KEY, clientId);
            }
        }
        return customKeyMap;
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

    /**
     * Get the Account ID from the incoming URL.
     *
     * @param pathList requested URI
     * @return account id
     */
    private String getAccountIdFromURL(List<String> pathList) {

        int accountsIndex = -1;

        if (pathList.contains(VIEW_ACCOUNTS)) {
            accountsIndex = pathList.indexOf(VIEW_ACCOUNTS);
        } else if (pathList.contains(VIEW_CARD_ACCOUNTS)) {
            accountsIndex = pathList.indexOf(VIEW_CARD_ACCOUNTS);
        }
        int size = pathList.size();

        if (accountsIndex + 1 == size) {
            return "";
        } else if (pathList.get(accountsIndex + 1).equals("")) {
            return "";
        } else {
            return pathList.get(accountsIndex + 1).split("\\?")[0];
        }
    }
}
