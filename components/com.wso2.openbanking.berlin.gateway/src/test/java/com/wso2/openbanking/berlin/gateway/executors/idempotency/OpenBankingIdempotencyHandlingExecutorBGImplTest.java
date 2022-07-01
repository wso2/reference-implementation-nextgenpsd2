package com.wso2.openbanking.berlin.gateway.executors.idempotency;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCacheConstants;
import com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedMember;
import com.wso2.openbanking.accelerator.gateway.cache.OpenBankingIdempotencyCacheKey;
import com.wso2.openbanking.accelerator.gateway.cache.OpenBankingIdempotencyValidationCache;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.IdempotencyConstants;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({CommonConfigParser.class, OpenBankingConfigParser.class})
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.management.*"})
public class OpenBankingIdempotencyHandlingExecutorBGImplTest extends PowerMockTestCase {

    @Mock
    OBAPIRequestContext obapiRequestContextMock;

    @Mock
    OBAPIResponseContext obapiResponseContextMock;

    @Mock
    MsgInfoDTO msgInfoDTO;

    @Mock
    APIRequestInfoDTO apiRequestInfoDTO;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    String sampleIdempotencyKey = "a5ff9494-2a15-48f9-8ab4-05a10b91215b";
    String sampleConsumerKey = "dummykey";
    String sampleElectedResource = "/sampleElectedResource/1234";
    String sampleResponsePayload = "{\"transactionStatus\":\"RCVD\",\"chosenScaMethod\":" +
            "[{\"name\":\"SMS OTP on Mobile\"," +
            "\"authenticationType\":\"SMS_OTP\",\"explanation\":\"SMS based one time password\"," +
            "\"authenticationMethodId\":\"sms-otp\"}],\"_links\":{\"scaStatus\":" +
            "{\"href\":\"/v1/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4/" +
            "authorisations/1d5b6e3b-2180-4b4f-bb8c-054c597cb4e3\"},\"scaOAuth\":" +
            "{\"href\":\"https://localhost:8243/.well-known/openid-configuration\"}," +
            "\"self\":{\"href\":\"/v/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4\"}," +
            "\"status\":{\"href\":\"/v1/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4" +
            "/status\"}},\"paymentId\":\"beecd66c-82ae-4ac8-9c04-9bd7c886d4a4\"}";

    DateTimeFormatter dtf = DateTimeFormatter.RFC_1123_DATE_TIME;
    ZonedDateTime zdt = ZonedDateTime.now();
    String sampleCreatedTime = dtf.format(zdt);

    String idempotencyCacheKeyHeader = "x-Idempotency-Key";

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);
    }

    @Test(priority = 1)
    public void testPostProcessResponse() {

        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(getDistributedCachingMockConfigurations());
        configuration.putAll(getIdempotencyMockConfigurations());
        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configuration);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        // Mocking response payload
        when(obapiResponseContextMock.getResponsePayload()).thenReturn(sampleResponsePayload);

        // Mocking consumer key
        when(obapiResponseContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn(sampleConsumerKey);

        // Mocking context props
        Map<String, String> contextProps = new HashMap<>();
        contextProps.put(com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.REQUEST_CACHE_KEY,
                sampleResponsePayload);
        contextProps.put(com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.IDEMPOTENCY_KEY_CACHE_KEY,
                sampleIdempotencyKey);
        when(obapiResponseContextMock.getContextProps()).thenReturn(contextProps);

        // Mocking response headers
        when(obapiResponseContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(idempotencyCacheKeyHeader, sampleIdempotencyKey);
        responseHeaders.put(com.wso2.openbanking.berlin.gateway.executors.utils.GatewayConstants.CREATED_TIME,
                sampleCreatedTime);
        when(msgInfoDTO.getHeaders()).thenReturn(responseHeaders);

        // Mocking elected resource
        when(msgInfoDTO.getElectedResource()).thenReturn(sampleElectedResource);

        OpenBankingIdempotencyHandlingExecutorBGImpl openBankingIdempotencyHandlingExecutorBG =
                new OpenBankingIdempotencyHandlingExecutorBGImpl();
        openBankingIdempotencyHandlingExecutorBG.postProcessResponse(obapiResponseContextMock);

        String cacheKey = sampleConsumerKey + "_" + sampleElectedResource + "_" + sampleIdempotencyKey;
        HashMap<String, String> expectedFromCache = new HashMap<>();
        expectedFromCache.put(com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.REQUEST_CACHE_KEY,
                sampleResponsePayload);
        expectedFromCache.put(com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.RESPONSE_CACHE_KEY,
                sampleResponsePayload);
        OffsetDateTime time = OffsetDateTime.parse(sampleCreatedTime, DateTimeFormatter.RFC_1123_DATE_TIME);
        expectedFromCache.put(GatewayConstants.CREATED_TIME_CACHE_KEY,
                time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        HashMap<String, String> fromCache = OpenBankingIdempotencyValidationCache.getInstance()
                .getFromCache(OpenBankingIdempotencyCacheKey.of(cacheKey));

        Assert.assertEquals(fromCache, expectedFromCache);
    }

    @Test(priority = 2)
    public void testPostProcessRequest() {

        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(getDistributedCachingMockConfigurations());
        configuration.putAll(getIdempotencyMockConfigurations());
        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configuration);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        when(obapiRequestContextMock.getRequestPayload()).thenReturn(sampleResponsePayload);
        // Mocking request headers
        when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(idempotencyCacheKeyHeader, sampleIdempotencyKey);
        when(msgInfoDTO.getHeaders()).thenReturn(requestHeaders);

        // Mocking elected resource
        when(msgInfoDTO.getElectedResource()).thenReturn(sampleElectedResource);

        // Mocking consumer key
        when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn(sampleConsumerKey);

        OpenBankingIdempotencyHandlingExecutorBGImpl openBankingIdempotencyHandlingExecutorBG =
                new OpenBankingIdempotencyHandlingExecutorBGImpl();
        openBankingIdempotencyHandlingExecutorBG.postProcessRequest(obapiRequestContextMock);

    }

    @Test(priority = 3)
    public void testPostProcessRequestWithInvalidIdempotency() {

        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(getDistributedCachingMockConfigurations());
        configuration.putAll(getIdempotencyMockConfigurations());
        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configuration);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        when(obapiRequestContextMock.getRequestPayload()).thenReturn(sampleResponsePayload);

        // Mocking request headers
        when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(idempotencyCacheKeyHeader, "");
        when(msgInfoDTO.getHeaders()).thenReturn(requestHeaders);

        // Mocking elected resource
        when(msgInfoDTO.getElectedResource()).thenReturn(sampleElectedResource);

        // Mocking consumer key
        when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn(sampleConsumerKey);

        OpenBankingIdempotencyHandlingExecutorBGImpl openBankingIdempotencyHandlingExecutorBG =
                new OpenBankingIdempotencyHandlingExecutorBGImpl();
        openBankingIdempotencyHandlingExecutorBG.postProcessRequest(obapiRequestContextMock);

    }

    @Test(priority = 3)
    public void testPostProcessRequestWithContextError() {

        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(getDistributedCachingMockConfigurations());
        configuration.putAll(getIdempotencyMockConfigurations());
        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configuration);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        when(obapiRequestContextMock.getRequestPayload()).thenReturn(sampleResponsePayload);
        when(obapiRequestContextMock.isError()).thenReturn(true);

        OpenBankingIdempotencyHandlingExecutorBGImpl openBankingIdempotencyHandlingExecutorBG =
                new OpenBankingIdempotencyHandlingExecutorBGImpl();
        openBankingIdempotencyHandlingExecutorBG.postProcessRequest(obapiRequestContextMock);

    }

    @Test(priority = 4)
    public void testPostProcessRequestWithPayload() {

        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(getDistributedCachingMockConfigurations());
        configuration.putAll(getIdempotencyMockConfigurations());
        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configuration);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        when(obapiRequestContextMock.getRequestPayload()).thenThrow(new RuntimeException());
        when(obapiRequestContextMock.isError()).thenReturn(true);

        OpenBankingIdempotencyHandlingExecutorBGImpl openBankingIdempotencyHandlingExecutorBG =
                new OpenBankingIdempotencyHandlingExecutorBGImpl();
        openBankingIdempotencyHandlingExecutorBG.postProcessRequest(obapiRequestContextMock);

    }

    @Test(priority = 1)
    public void testPostProcessResponseForContextErrors() {

        mockStatic(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);

        Map<String, Object> configuration = new HashMap<>();
        configuration.putAll(getDistributedCachingMockConfigurations());
        configuration.putAll(getIdempotencyMockConfigurations());
        Mockito.when(openBankingConfigParser.getConfiguration()).thenReturn(configuration);

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance())
                .thenReturn(openBankingConfigParser);

        // Mocking response payload
        when(obapiResponseContextMock.getResponsePayload()).thenReturn(sampleResponsePayload);

        // Mocking consumer key
        when(obapiResponseContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn(sampleConsumerKey);

        // Mocking context props
        Map<String, String> contextProps = new HashMap<>();
        contextProps.put(com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.REQUEST_CACHE_KEY,
                sampleResponsePayload);
        contextProps.put(com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.IDEMPOTENCY_KEY_CACHE_KEY,
                sampleIdempotencyKey);
        when(obapiResponseContextMock.getContextProps()).thenReturn(contextProps);

        // Mocking response headers
        when(obapiResponseContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(com.wso2.openbanking.berlin.gateway.executors.utils.GatewayConstants.IS_IDEMPOTENT,
                "false");
        responseHeaders.put(com.wso2.openbanking.berlin.gateway.executors.utils.GatewayConstants.CREATED_TIME,
                sampleCreatedTime);
        when(msgInfoDTO.getHeaders()).thenReturn(responseHeaders);

        // Mocking elected resource
        when(msgInfoDTO.getElectedResource()).thenReturn(sampleElectedResource);

        OpenBankingIdempotencyHandlingExecutorBGImpl openBankingIdempotencyHandlingExecutorBG =
                new OpenBankingIdempotencyHandlingExecutorBGImpl();
        openBankingIdempotencyHandlingExecutorBG.postProcessResponse(obapiResponseContextMock);

        String cacheKey = sampleConsumerKey + "_" + sampleElectedResource + "_" + sampleIdempotencyKey;
        HashMap<String, String> expectedFromCache = new HashMap<>();
        expectedFromCache.put(com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.REQUEST_CACHE_KEY,
                sampleResponsePayload);
        expectedFromCache.put(com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.RESPONSE_CACHE_KEY,
                sampleResponsePayload);
        OffsetDateTime time = OffsetDateTime.parse(sampleCreatedTime, DateTimeFormatter.RFC_1123_DATE_TIME);
        expectedFromCache.put(GatewayConstants.CREATED_TIME_CACHE_KEY,
                time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        HashMap<String, String> fromCache = OpenBankingIdempotencyValidationCache.getInstance()
                .getFromCache(OpenBankingIdempotencyCacheKey.of(cacheKey));

        Assert.assertEquals(fromCache, expectedFromCache);
    }

    private Map<String, Object> getDistributedCachingMockConfigurations() {

        Map<String, Object> configuration = new HashMap<>();

        configuration.put(OpenBankingDistributedCacheConstants.ENABLED, "true");
        configuration.put(OpenBankingDistributedCacheConstants.HOST_NAME, "localhost");
        configuration.put(OpenBankingDistributedCacheConstants.PORT, "5721");
        configuration.put(OpenBankingDistributedCacheConstants.DISCOVERY_MECHANISM, "Multicast");
        configuration.put(OpenBankingDistributedCacheConstants.MULTICAST_GROUP, "224.2.2.3");
        configuration.put(OpenBankingDistributedCacheConstants.MULTICAST_PORT, "54321");
        ArrayList<String> interfaces = new ArrayList<>();
        interfaces.add("192.168.1.100-110");
        configuration.put(OpenBankingDistributedCacheConstants.TRUSTED_INTERFACES, interfaces);
        configuration.put(OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MAX_HEARTBEAT, "600");
        configuration.put(OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MAX_MASTER_CONFIRMATION, "900");
        configuration.put(OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MERGE_FIRST_RUN_DELAY, "60");
        configuration.put(OpenBankingDistributedCacheConstants.HAZELCAST_PROPERTY_MERGE_NEXT_RUN_DELAY, "30");
        configuration.put(OpenBankingDistributedCacheConstants.PROPERTY_LOGGING_TYPE, "none");

        return configuration;
    }

    private Map<String, Object> getIdempotencyMockConfigurations() {

        Map<String, Object> configuration = new HashMap<>();
        configuration.put(IdempotencyConstants.IDEMPOTENCY_IS_ENABLED, "true");
        configuration.put(IdempotencyConstants.IDEMPOTENCY_CACHE_TIME_TO_LIVE, "1440");
        configuration.put(IdempotencyConstants.IDEMPOTENCY_KEY_HEADER, idempotencyCacheKeyHeader);
        configuration.put(IdempotencyConstants.IDEMPOTENCY_ALLOWED_TIME, "24");

        return configuration;
    }

    @AfterClass
    public void after() {

        OpenBankingDistributedMember.of().shutdown();
    }
}
