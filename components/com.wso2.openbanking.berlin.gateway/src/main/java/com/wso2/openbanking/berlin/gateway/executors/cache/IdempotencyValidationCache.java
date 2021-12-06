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

package com.wso2.openbanking.berlin.gateway.executors.cache;

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCache;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;

public class IdempotencyValidationCache extends OpenBankingBaseCache<IdempotencyCacheKey, Object> {

    private static final String cacheName = "OPEN_BANKING_BERLIN_IDEMPOTENCY_REQUEST_CACHE";

    private static IdempotencyValidationCache idempotencyValidationCache;
    private Integer accessExpiryMinutes;
    private Integer modifiedExpiryMinutes;

    public IdempotencyValidationCache () {
        super(cacheName);
        this.accessExpiryMinutes = setAccessExpiryMinutes();
        this.modifiedExpiryMinutes = setModifiedExpiryMinutes();
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return IdempotencyRequestCache object
     */
    public static synchronized IdempotencyValidationCache getInstance() {
        if (idempotencyValidationCache == null) {
            idempotencyValidationCache = new IdempotencyValidationCache();
        }
        return idempotencyValidationCache;
    }

    @Override
    public int getCacheAccessExpiryMinutes() {

        return accessExpiryMinutes;
    }

    @Override
    public int getCacheModifiedExpiryMinutes() {

        return modifiedExpiryMinutes;
    }

    private int setAccessExpiryMinutes() {

        String cacheAccessExpiry = (String) CommonConfigParser.getInstance().getConfiguration()
                .get(CommonConstants.IDEMPOTENCY_CACHE_ACCESS_EXPIRY);

        return cacheAccessExpiry == null ? 3600 : Integer.parseInt(cacheAccessExpiry);
    }

    private int setModifiedExpiryMinutes() {

        String cacheModifyExpiry = (String) CommonConfigParser.getInstance().getConfiguration()
                .get(CommonConstants.IDEMPOTENCY_CACHE_MODIFY_EXPIRY);

        return cacheModifyExpiry == null ? 3600 : Integer.parseInt(cacheModifyExpiry);
    }
}
