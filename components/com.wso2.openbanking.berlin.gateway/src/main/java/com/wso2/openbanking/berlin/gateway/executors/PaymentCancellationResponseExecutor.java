//package com.wso2.openbanking.berlin.gateway.executors;
//
//import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
//import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
//import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
//import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
//import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
//import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
//import com.wso2.openbanking.berlin.gateway.utils.GatewayConstants;
//import net.minidev.json.JSONObject;
//import net.minidev.json.parser.JSONParser;
//import net.minidev.json.parser.ParseException;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.http.HttpHeaders;
//import org.apache.http.HttpStatus;
//import org.apache.http.client.methods.HttpPut;
//import org.apache.http.impl.client.CloseableHttpClient;
//
///**
// * Executor to capture Payment DELETE response.
// */
//public class PaymentCancellationResponseExecutor implements OpenBankingGatewayExecutor {
//
//    private static final Log log = LogFactory.getLog(PaymentCancellationResponseExecutor.class);
//
//    @Override
//    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {
//
//    }
//
//    @Override
//    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {
//
//    }
//
//    @Override
//    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {
//
//    }
//
//    @Override
//    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {
//
//        if (StringUtils.equals("DELETE", obapiResponseContext.getMsgInfo().getHttpMethod())) {
//            if (StringUtils.contains(obapiResponseContext.getMsgInfo().getElectedResource(), "bulk-payments")
//                    || StringUtils.contains(obapiResponseContext.getMsgInfo().getElectedResource(),
//                    "periodic-payments")) {
//                Thread paymentCancellationStatusUpdateThread = new Thread(() -> {
//                   try {
//                       String url = CommonConfigParser.getInstance().getConsentMgtConfigs()
//                               .get(GatewayConstants.PAYMENT_DELETE_STATUS_UPDATE_URL);
//                       CloseableHttpClient client = HTTPClientUtils.getHttpsClient();
//                       HttpPut request = new HttpPut(url);
//                       request.setHeader(HttpHeaders.CONTENT_TYPE, GatewayConstants.JSON_CONTENT_TYPE);
//
//                       // Get the response code
//                       int responseStatusCode = obapiResponseContext.getStatusCode();
//                       if (HttpStatus.SC_NO_CONTENT == responseStatusCode) {
//                           // send the request to update the consent status to CANC
//                       } else if (HttpStatus.SC_ACCEPTED == responseStatusCode) {
//                           JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
//                           JSONObject paymentDeleteResponse = (JSONObject) jsonParser.parse(obapiResponseContext
//                                   .getResponsePayload());
//                           JSONObject data = new JSONObject();
//
//                       }
//
//                   } catch (OpenBankingException e) {
//                       throw new RuntimeException(e);
//                   } catch (ParseException e) {
//                       throw new RuntimeException(e);
//                   }
//                });
//                paymentCancellationStatusUpdateThread.start();
//            }
//        }
//    }
//}
