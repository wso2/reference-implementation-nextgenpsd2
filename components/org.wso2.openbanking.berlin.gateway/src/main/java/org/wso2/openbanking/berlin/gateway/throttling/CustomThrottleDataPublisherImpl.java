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

package org.wso2.openbanking.berlin.gateway.throttling;

import com.wso2.openbanking.accelerator.gateway.throttling.ThrottleDataPublisher;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Berlin Throttle data publisher to frequency per day add custom throttling properties based the request.
 */
public class CustomThrottleDataPublisherImpl implements ThrottleDataPublisher {

    private static final Log log = LogFactory.getLog(CustomThrottleDataPublisherImpl.class);

    private static final String VIEW_ACCOUNTS = "accounts";
    private static final String VIEW_CARD_ACCOUNTS = "card-accounts";

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

        /* Request will be throttled only if it is initiated by the TPP.
         * This is determined by the presence of PSU-IP-Address header in the retrieval request.
         * According to the specification, if the header is present, the request is initiated by the PSU. Otherwise,
         * the request is initiated by TPP.
         */
        if (CommonConfigParser.getInstance().isFrequencyPerDayThrottlingEnabled()
                && StringUtils.isBlank(requestHeaders.get(PSU_IP_ADDRESS))) {

            String electedResource = requestContextDTO.getMsgInfo().getElectedResource();
            String resourceString = requestContextDTO.getMsgInfo().getResource();
            String clientId = requestContextDTO.getApiRequestInfo().getConsumerKey();

            if (log.isDebugEnabled()) {
                log.debug("Checking whether the " + resourceString + " is eligible for throttling");
            }

            if (eligibleResources.contains(electedResource)) {

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

        if (accountsIndex + 1 == pathList.size() || pathList.get(accountsIndex + 1).equals("")) {
            return "";
        } else {
            return pathList.get(accountsIndex + 1).split("\\?")[0];
        }
    }
}
