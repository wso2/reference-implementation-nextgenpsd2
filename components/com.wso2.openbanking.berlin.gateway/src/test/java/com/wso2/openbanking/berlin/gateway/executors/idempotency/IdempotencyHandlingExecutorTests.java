package com.wso2.openbanking.berlin.gateway.executors.idempotency;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.gateway.executors.cache.IdempotencyCacheKey;
import com.wso2.openbanking.berlin.gateway.executors.cache.IdempotencyValidationCache;
import com.wso2.openbanking.berlin.gateway.executors.utils.GatewayConstants;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({IdempotencyValidationCache.class, IdempotencyCacheKey.class, CommonConfigParser.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class IdempotencyHandlingExecutorTests extends PowerMockTestCase {

    @Mock
    OBAPIRequestContext obapiRequestContextMock;

    @Mock
    OBAPIResponseContext obapiResponseContextMock;

    @Mock
    MsgInfoDTO msgInfoDTO;

    @Mock
    APIRequestInfoDTO apiRequestInfoDTO;

    @Mock
    IdempotencyValidationCache idempotencyValidationCache;

    @Mock
    IdempotencyCacheKey idempotencyCacheKey;

    @Mock
    CommonConfigParser commonConfigParser;

    String idempotencyKey = "a5ff9494-2a15-48f9-8ab4-05a10b91215b";

    DateTimeFormatter dtf = DateTimeFormatter.RFC_1123_DATE_TIME;
    ZonedDateTime zdt = ZonedDateTime.now();

    String createdTime = dtf.format(zdt);
    @BeforeClass
    public void initClass() {

    }

    @Test
    public void testPreProcessResponse() {

        when(obapiRequestContextMock.isError()).thenReturn(false);
        when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        when(msgInfoDTO.getResource()).thenReturn("/payments/sepa-credit-transfers");
        when(msgInfoDTO.getHttpMethod()).thenReturn("POST");

        mockStatic(CommonConfigParser.class);
        when(CommonConfigParser.getInstance()).thenReturn(commonConfigParser);
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(CommonConstants.PAYMENT_INITIATION_IDEMPOTENCY_ENABLED, "true");
        configuration.put(CommonConstants.IDEMPOTENCY_ALLOWED_TIME, "3600");
        when(commonConfigParser.getConfiguration()).thenReturn(configuration);


        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(GatewayConstants.X_IDEMPOTENCY_KEY, idempotencyKey);
        when(msgInfoDTO.getHeaders()).thenReturn(requestHeaders);

        Map<String, String> contextProps = new HashMap<>();

        when(obapiRequestContextMock.getContextProps()).thenReturn(contextProps);
        when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn("dummykey");


        String requestPayload = "{\"instructedAmount\": {\"currency\": \"EUR\"," +
                "\"amount\": \"123.50\"},\"debtorAccount\": {\"iban\": " +
                "\"DE12345678901234567890\",\"currency\" : \"USD\" },\"creditorName\": " +
                "\"Merchant123\",\"creditorAccount\": {\"iban\": \"DE98765432109876543210\"," +
                "\"currency\": \"EUD\"\n    },\n    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n}";


        when(obapiRequestContextMock.getRequestPayload()).thenReturn(requestPayload);

        mockStatic(IdempotencyValidationCache.class);
        when(IdempotencyValidationCache.getInstance()).thenReturn(idempotencyValidationCache);
        mockStatic(IdempotencyCacheKey.class);
        when(IdempotencyCacheKey.of(Mockito.anyString())).thenReturn(idempotencyCacheKey);
        Map cachedObjectMap = new HashMap();
        // previous result is present in cache, retrieving request from cache
        cachedObjectMap.put(GatewayConstants.REQUEST_CACHE_KEY, requestPayload);
        cachedObjectMap.put(GatewayConstants.CREATED_TIME_CACHE_KEY, createdTime);


        when(idempotencyValidationCache.getFromCache(idempotencyCacheKey)).thenReturn(cachedObjectMap);

        new IdempotencyHandlingExecutor().postProcessRequest(obapiRequestContextMock);
    }

    @Test
    public void testPreProcessResponseWithEmptyIdempotencyKey() {

        when(obapiRequestContextMock.isError()).thenReturn(false);
        when(obapiRequestContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        when(msgInfoDTO.getResource()).thenReturn("/payments/sepa-credit-transfers");
        when(msgInfoDTO.getHttpMethod()).thenReturn("POST");

        mockStatic(CommonConfigParser.class);
        when(CommonConfigParser.getInstance()).thenReturn(commonConfigParser);
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(CommonConstants.PAYMENT_INITIATION_IDEMPOTENCY_ENABLED, "true");
        configuration.put(CommonConstants.IDEMPOTENCY_ALLOWED_TIME, "3600");
        when(commonConfigParser.getConfiguration()).thenReturn(configuration);


        Map<String, String> requestHeaders = new HashMap<>();
        when(msgInfoDTO.getHeaders()).thenReturn(requestHeaders);

        Map<String, String> contextProps = new HashMap<>();

        when(obapiRequestContextMock.getContextProps()).thenReturn(contextProps);
        when(obapiRequestContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn("dummykey");


        String requestPayload = "{\"instructedAmount\": {\"currency\": \"EUR\"," +
                "\"amount\": \"123.50\"},\"debtorAccount\": {\"iban\": " +
                "\"DE12345678901234567890\",\"currency\" : \"USD\" },\"creditorName\": " +
                "\"Merchant123\",\"creditorAccount\": {\"iban\": \"DE98765432109876543210\"," +
                "\"currency\": \"EUD\"\n    },\n    \"remittanceInformationUnstructured\": \"Ref Number Merchant\"\n}";


        when(obapiRequestContextMock.getRequestPayload()).thenReturn(requestPayload);
        ArrayList<OpenBankingExecutorError> executorErrors = new ArrayList<>();
        OpenBankingExecutorError error = new OpenBankingExecutorError();
        error.setMessage(ErrorConstants.X_REQUEST_ID_MISSING);
        executorErrors.add(error);
        when(obapiRequestContextMock.getErrors()).thenReturn(executorErrors);

        mockStatic(IdempotencyValidationCache.class);
        when(IdempotencyValidationCache.getInstance()).thenReturn(idempotencyValidationCache);
        mockStatic(IdempotencyCacheKey.class);
        when(IdempotencyCacheKey.of(Mockito.anyString())).thenReturn(idempotencyCacheKey);
        Map cachedObjectMap = new HashMap();
        // previous result is present in cache, retrieving request from cache
        cachedObjectMap.put(GatewayConstants.REQUEST_CACHE_KEY, requestPayload);
        cachedObjectMap.put(GatewayConstants.CREATED_TIME_CACHE_KEY, createdTime);


        when(idempotencyValidationCache.getFromCache(idempotencyCacheKey)).thenReturn(cachedObjectMap);

        new IdempotencyHandlingExecutor().postProcessRequest(obapiRequestContextMock);

        Assert.assertEquals(obapiRequestContextMock.getErrors().get(0).getMessage(),
                ErrorConstants.X_REQUEST_ID_MISSING);
    }

    @Test
    public void testPostProcessResponse() {
        when(obapiResponseContextMock.isError()).thenReturn(false);
        when(obapiResponseContextMock.getMsgInfo()).thenReturn(msgInfoDTO);
        when(obapiResponseContextMock.getStatusCode()).thenReturn(200);
        when(msgInfoDTO.getResource()).thenReturn("/payments/sepa-credit-transfers");
        when(msgInfoDTO.getHttpMethod()).thenReturn("POST");

        mockStatic(CommonConfigParser.class);
        when(CommonConfigParser.getInstance()).thenReturn(commonConfigParser);
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(CommonConstants.PAYMENT_INITIATION_IDEMPOTENCY_ENABLED, "true");
        when(commonConfigParser.getConfiguration()).thenReturn(configuration);

        Map<String, String> responseHeaders = new HashMap<>();
        // request is not idempotent
        responseHeaders.put(GatewayConstants.IS_IDEMPOTENT, "false");
        responseHeaders.put(GatewayConstants.X_IDEMPOTENCY_KEY, idempotencyKey);
        responseHeaders.put(GatewayConstants.CREATED_TIME, createdTime);
        when(msgInfoDTO.getHeaders()).thenReturn(responseHeaders);

        mockStatic(IdempotencyValidationCache.class);
        when(IdempotencyValidationCache.getInstance()).thenReturn(idempotencyValidationCache);
        mockStatic(IdempotencyCacheKey.class);
        when(IdempotencyCacheKey.of(Mockito.anyString())).thenReturn(idempotencyCacheKey);

        Map<String, String> contextProps = new HashMap<>();
        String responsePayload = "{\"transactionStatus\":\"RCVD\",\"chosenScaMethod\":" +
                "[{\"name\":\"SMS OTP on Mobile\"," +
                "\"authenticationType\":\"SMS_OTP\",\"explanation\":\"SMS based one time password\"," +
                "\"authenticationMethodId\":\"sms-otp\"}],\"_links\":{\"scaStatus\":" +
                "{\"href\":\"/v1/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4/" +
                "authorisations/1d5b6e3b-2180-4b4f-bb8c-054c597cb4e3\"},\"scaOAuth\":" +
                "{\"href\":\"https://localhost:8243/.well-known/openid-configuration\"}," +
                "\"self\":{\"href\":\"/v/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4\"}," +
                "\"status\":{\"href\":\"/v1/payments/sepa-credit-transfers/beecd66c-82ae-4ac8-9c04-9bd7c886d4a4" +
                "/status\"}},\"paymentId\":\"beecd66c-82ae-4ac8-9c04-9bd7c886d4a4\"}";

        when(obapiResponseContextMock.getContextProps()).thenReturn(contextProps);
        when(obapiResponseContextMock.getApiRequestInfo()).thenReturn(apiRequestInfoDTO);
        when(apiRequestInfoDTO.getConsumerKey()).thenReturn("dummykey");
        when(obapiResponseContextMock.getResponsePayload()).thenReturn(responsePayload);

        new IdempotencyHandlingExecutor().postProcessResponse(obapiResponseContextMock);
    }
}
