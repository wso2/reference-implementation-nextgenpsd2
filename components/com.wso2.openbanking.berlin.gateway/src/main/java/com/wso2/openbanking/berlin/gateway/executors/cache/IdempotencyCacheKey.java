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

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCacheKey;

import java.io.Serializable;
import java.util.Objects;

/**
 * Cache Key for Open Banking Berlin Idempotency cache.
 */
public class IdempotencyCacheKey extends OpenBankingBaseCacheKey implements Serializable {

    private static final long serialVersionUID = 9008901334894171747L;
    public String idempotencyCacheKey;

    public IdempotencyCacheKey(String idempotencyCacheKey) {

        this.idempotencyCacheKey = idempotencyCacheKey;
    }

    public static IdempotencyCacheKey of(String idempotencyCacheKey) {

        return new IdempotencyCacheKey(idempotencyCacheKey);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdempotencyCacheKey that = (IdempotencyCacheKey) o;
        return Objects.equals(idempotencyCacheKey, that.idempotencyCacheKey);
    }

    @Override
    public int hashCode() {

        return Objects.hash(idempotencyCacheKey);
    }

}
