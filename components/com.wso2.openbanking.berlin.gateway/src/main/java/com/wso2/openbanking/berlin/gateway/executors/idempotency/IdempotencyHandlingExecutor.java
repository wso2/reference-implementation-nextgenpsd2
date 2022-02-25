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

package com.wso2.openbanking.berlin.gateway.executors.idempotency;

import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.utils.CommonUtil;
import com.wso2.openbanking.berlin.gateway.executors.cache.IdempotencyCacheKey;
import com.wso2.openbanking.berlin.gateway.executors.cache.IdempotencyValidationCache;
import com.wso2.openbanking.berlin.gateway.executors.utils.GatewayConstants;
import com.wso2.openbanking.berlin.gateway.utils.GatewayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;

/**
 * Idempotency handling executor.
 */
public class IdempotencyHandlingExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(IdempotencyHandlingExecutor.class);
    /**
     * Method to handle pre request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        //Return if the request contains a error or if the request is not a payment submission request
        if (obapiRequestContext.isError() || !isPaymentRequest(obapiRequestContext.getMsgInfo().getResource()) ||
                !HttpMethod.POST.equals(obapiRequestContext.getMsgInfo().getHttpMethod())) {
            return;
        }

        //Check whether idempotency handling is enabled
        if (!isIdempotencyRequired()) {
            return;
        }

        //Retrieve headers and payload
        Map<String, String> requestHeaders = obapiRequestContext.getMsgInfo().getHeaders();

        //Retrieve consumer key from headers
        String consumerKey = obapiRequestContext.getApiRequestInfo().getConsumerKey();
        //Retrieve idempotency key from headers
        String idempotencyKey = requestHeaders.get(GatewayConstants.X_IDEMPOTENCY_KEY);
        //Retrieve context properties
        Map<String, String> contextProps = obapiRequestContext.getContextProps();

        if (StringUtils.isEmpty(idempotencyKey)) {
            log.error(ErrorConstants.X_REQUEST_ID_MISSING);
            GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                    ErrorConstants.X_REQUEST_ID_MISSING);
            return;
        }

        //Construct cache keys for request and response using client Id and idempotency key
        String idempotencyCacheKey = (consumerKey.concat("_")).concat(idempotencyKey);

        IdempotencyValidationCache idempotencyValidationCache = IdempotencyValidationCache.getInstance();

        try {
            String payload;
            int httpStatus;
            payload = obapiRequestContext.getRequestPayload();
            httpStatus = HttpStatus.SC_CREATED;

            Map cachedObjectMap = getPropertiesFromCache(idempotencyValidationCache, idempotencyCacheKey);
            //Check whether the request exists in the cache
            if (!cachedObjectMap.isEmpty()) {
                log.debug("Handling idempotency through gateway");

                // previous result is present in cache, retrieving request from cache
                String cachedRequest = (String) cachedObjectMap.get(GatewayConstants.REQUEST_CACHE_KEY);
                String createdTime = (String) cachedObjectMap.get(GatewayConstants.CREATED_TIME_CACHE_KEY);
                //Check whether payload received is similar to the payload stored
                if (CommonUtil.isJSONPayloadSimilar(cachedRequest, payload)) {
                    log.debug("Payloads are similar for idempotent request");
                    //Payloads are similar, hence checking whether request came within allowed time
                    if (CommonUtil.isRequestReceivedWithinAllowedTime(createdTime)) {
                        log.debug("Idempotent request received within allowed time");
                        //Retrieving the response from cache
                        String cachedResponse = (String) cachedObjectMap.get(GatewayConstants.RESPONSE_CACHE_KEY);

                        //Setting payload as modified payload
                        log.debug("Setting cached payload as the response");
                        obapiRequestContext.setModifiedPayload(cachedResponse);

                        //Setting Context Properties to return response without executing further
                        contextProps.put(GatewayConstants.IS_RETURN_RESPONSE, GatewayConstants.TRUE);
                        contextProps.put(GatewayConstants.MODIFIED_STATUS, String.valueOf(httpStatus));
                    }
                } else {
                    //Payloads are not similar, hence returning an error
                    log.error(ErrorConstants.EXECUTOR_IDEMPOTENCY_KEY_FRAUDULENT);
                    GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                            ErrorConstants.EXECUTOR_IDEMPOTENCY_KEY_FRAUDULENT);
                }
            } else {
                log.debug("Object not found in cache. Adding the request to cache.");
                //Since request is not in cache, adding the request to the cache against the idempotency key
                contextProps.put(GatewayConstants.REQUEST_CACHE_KEY, payload);
            }
        } catch (IOException e) {
            log.error(ErrorConstants.EXECUTOR_IDEMPOTENCY_KEY_ERROR);
            GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                    ErrorConstants.EXECUTOR_IDEMPOTENCY_KEY_ERROR);
            return;
        }
        //Adding idempotency key to the context properties
        contextProps.put(GatewayConstants.IDEMPOTENCY_KEY_CACHE_KEY, idempotencyKey);
        obapiRequestContext.setContextProps(contextProps);
    }

    /**
     * Method to store properties to cache.
     *
     * @param key                 unique cache key
     * @param idempotentDetails   properties to store
     */
    protected void setPropertiesToCache(IdempotencyValidationCache idempotencyValidationCache, String key,
                                      Map idempotentDetails) {

        idempotencyValidationCache.addToCache(IdempotencyCacheKey.of(key), idempotentDetails);
    }

    /**
     * Method to retrieve context properties from cache.
     *
     * @param key unique cache key
     * @return context properties
     */
    protected Map getPropertiesFromCache(IdempotencyValidationCache idempotencyValidationCache, String key) {

        Object cachedObject = idempotencyValidationCache.getFromCache(IdempotencyCacheKey.of(key));
        return cachedObject == null ? new HashMap<>() : (Map) cachedObject;
    }

    /**
     * Method to check whether Idempotency handling is required.
     *
     * @return
     */
    protected boolean isIdempotencyRequired() {

        CommonConfigParser parser = CommonConfigParser.getInstance();
        String isIdempotencyEnabled = (String) parser.getConfiguration()
                .get(CommonConstants.PAYMENT_INITIATION_IDEMPOTENCY_ENABLED);

        return Boolean.parseBoolean(isIdempotencyEnabled);
    }

    /**
     * Method to check whether the request is a payment request.
     *
     * @param resource
     * @return
     */
    protected boolean isPaymentRequest(String resource) {

        return (resource.contains(GatewayConstants.PAYMENTS) || resource.contains(GatewayConstants.BULK_PAYMENTS) ||
                resource.contains(GatewayConstants.PERIODIC_PAYMENTS));
    }

    /**
     * Method to handle pre response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

        //Return if the request contains a error or if the request is not a payment request
        if (obapiResponseContext.isError() || !isPaymentRequest(obapiResponseContext.getMsgInfo().getResource()) ||
                !HttpMethod.POST.equals(obapiResponseContext.getMsgInfo().getHttpMethod())) {
            return;
        }

        //Check whether idempotency handling for payment submission is enabled
        if (!isIdempotencyRequired()) {
            return;
        }

        // Checking whether the request is an idempotent request and return
        // Do not cache the response for idempotent requests
        if (Boolean.parseBoolean(obapiResponseContext.getMsgInfo().getHeaders().get(GatewayConstants.IS_IDEMPOTENT))) {
            return;
        }

        // Checking whether status code is 4xx or 5xx
        if (obapiResponseContext.getStatusCode() >= 400) {
            return;
        }

        IdempotencyValidationCache idempotencyValidationCache = IdempotencyValidationCache.getInstance();

        //Retrieving payload
        String responsePayload = obapiResponseContext.getResponsePayload();
        //Retrieve idempotency key from headers
        String consumerKey = obapiResponseContext.getApiRequestInfo().getConsumerKey();
        //Retrieve context properties
        Map<String, String> contextProps = obapiResponseContext.getContextProps();

        String idempotencyKey;
        if (obapiResponseContext.getMsgInfo().getHeaders().get(GatewayConstants.X_IDEMPOTENCY_KEY) != null) {
            //Retrieve idempotency key from headers
            idempotencyKey = obapiResponseContext.getMsgInfo().getHeaders().get(GatewayConstants.X_IDEMPOTENCY_KEY);
        } else {
            //Retrieve idempotency key from context props if it does not exists as an header
            idempotencyKey = contextProps.get(GatewayConstants.IDEMPOTENCY_KEY_CACHE_KEY);
        }

        String createdTime;
        if (obapiResponseContext.getMsgInfo().getHeaders().get(GatewayConstants.CREATED_TIME) != null) {
            //Retrieve response created time from headers
            createdTime = obapiResponseContext.getMsgInfo().getHeaders().get(GatewayConstants.CREATED_TIME);
        } else {
            // Date header missing
            log.error(ErrorConstants.DATE_MISSING);
            return;
        }

        //Construct cache keys for request and response using client Id and idempotency key
        String idempotencyCacheKey = (consumerKey.concat("_")).concat(idempotencyKey);

        //Add response and created time to the cache
        Map cachedObject = getPropertiesFromCache(idempotencyValidationCache, idempotencyCacheKey);
        cachedObject.put(GatewayConstants.REQUEST_CACHE_KEY, contextProps.get(GatewayConstants.REQUEST_CACHE_KEY));
        cachedObject.put(GatewayConstants.RESPONSE_CACHE_KEY, responsePayload);
        cachedObject.put(GatewayConstants.CREATED_TIME_CACHE_KEY, createdTime);

        log.debug("Setting properties to cache");
        setPropertiesToCache(idempotencyValidationCache, idempotencyCacheKey, cachedObject);
    }
}
