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

package org.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.gateway.executor.impl.consent.ConsentEnforcementExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.CommonConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.gateway.utils.GatewayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;

/**
 * Executor to capture Payment DELETE response.
 */
public class BerlinConsentEnforcementExecutor extends ConsentEnforcementExecutor {

    private static final Log log = LogFactory.getLog(BerlinConsentEnforcementExecutor.class);
    public static final String MESSAGE = "message";

    private final List<String> allApplicablePaymentRequestTypes = Arrays.asList (
        "/bulk-payments/{payment-product}/{paymentId}",
        "/payments/{payment-product}/{paymentId}",
        "/periodic-payments/{payment-product}/{paymentId}",
        "/bulk-payments/{payment-product}/{paymentId}/status",
        "/payments/{payment-product}/{paymentId}/status",
        "/periodic-payments/{payment-product}/{paymentId}/status"
    );

    private final List<String> allApplicablePaymentRequestTypesForDelete = Arrays.asList (
        "/bulk-payments/{payment-product}/{paymentId}",
        "/periodic-payments/{payment-product}/{paymentId}"
    );

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        super.postProcessRequest(obapiRequestContext);

        if (obapiRequestContext.isError() || obapiRequestContext.getConsentId() == null) {
            return;
        }

        MsgInfoDTO msgInfo = obapiRequestContext.getMsgInfo();
        // Return if the payment request is not a GET/DELETE request
        if (!(StringUtils.equals(HttpMethod.GET, msgInfo.getHttpMethod())
                || StringUtils.equals(HttpMethod.DELETE, msgInfo.getHttpMethod()))) {
            return;
        }

        // Return if the request is not a payment request
        if (!allApplicablePaymentRequestTypes.contains(msgInfo.getElectedResource())) {
            return;
        }

        try {
            sendReturnPaymentResponseToClient(obapiRequestContext);
        } catch (UnsupportedEncodingException e) {
            log.error("Error while decoding \"Account-Request-Information\" JWT");
            GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                    e.getMessage());
        }
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

        MsgInfoDTO msgInfo = obapiResponseContext.getMsgInfo();

        // Return if the bulk/periodic payment request is not a DELETE request
        if (!StringUtils.equals(HttpMethod.DELETE, msgInfo.getHttpMethod())) {
            return;
        }

        // Return if the request is not either bulk or periodic payments
        if (!allApplicablePaymentRequestTypesForDelete.contains(msgInfo.getElectedResource())) {
            return;
        }

        try {
           // Get the response code
           int responseStatusCode = obapiResponseContext.getStatusCode();
           if (HttpStatus.SC_NO_CONTENT == responseStatusCode || HttpStatus.SC_ACCEPTED == responseStatusCode) {
               // send the request to update the consent status
               updateConsentStatus(obapiResponseContext, responseStatusCode);
           }
        } catch (OpenBankingException | IOException e) {
           log.error("Error occurred while updating consent status response data", e);
        }
    }

    /**
     * This method constructs the request for updating the consent status according to the status code of the response
     * of the payments DELETE request.
     *
     * @param obapiResponseContext response context
     * @param statusCode status code of the payments DELETE response
     * @throws OpenBankingException thrown if an error occurs while initializing the HTTP client
     * @throws IOException thrown if any error occurs during sending the request
     */
    private void updateConsentStatus(OBAPIResponseContext obapiResponseContext, int statusCode)
            throws OpenBankingException, IOException  {

        // Extract the pre-validated payment ID from the request path
        List<String> resourceElements = Arrays.asList(obapiResponseContext.getMsgInfo().getResource()
                .split("/"));
        // The payment ID will always be at the end of the request path
        String paymentId = resourceElements.get(resourceElements.size() - 1);
        net.minidev.json.JSONObject json = new net.minidev.json.JSONObject();
        json.appendField(CommonConstants.CONSENT_ID, paymentId);
        json.appendField(CommonConstants.STATUS_CODE, String.valueOf(statusCode));

        String url = CommonConfigParser.getInstance().getConsentMgtConfigs()
                .get(CommonConstants.PAYMENT_CONSENT_STATUS_UPDATE_URL);
        CloseableHttpClient client = HTTPClientUtils.getHttpsClient();
        HttpPut request = new HttpPut(url);
        request.setHeader(HttpHeaders.CONTENT_TYPE, GatewayConstants.JSON_CONTENT_TYPE);

        request.setEntity(new StringEntity(json.toString()));
        request.setHeader(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
        String userName = com.wso2.openbanking.accelerator.gateway.util.GatewayUtils
                .getAPIMgtConfig(GatewayConstants.API_KEY_VALIDATOR_USERNAME);
        String password = com.wso2.openbanking.accelerator.gateway.util.GatewayUtils
                .getAPIMgtConfig(GatewayConstants.API_KEY_VALIDATOR_PASSWORD);
        request.setHeader(GatewayConstants.AUTH_HEADER, com.wso2.openbanking.accelerator
                .gateway.util.GatewayUtils.getBasicAuthHeader(userName, password));
        HttpResponse response = client.execute(request);
        InputStream in = response.getEntity().getContent();
        IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
    }


    /**
     * This method sends a return response to the client if the "Account-Request-Information" header contains the
     * field "authStatus" to indicate the client that the payment addressed is not yet completely authorized.
     *
     * @param obapiRequestContext request context
     * @throws UnsupportedEncodingException thrown if an error occurs while decoding the JWT
     */
     void sendReturnPaymentResponseToClient(OBAPIRequestContext obapiRequestContext)
            throws UnsupportedEncodingException {

        Map<String, String> contextProps = obapiRequestContext.getContextProps();
        String consentInfo = obapiRequestContext.getAddedHeaders().get("Account-Request-Information");
        JSONObject jwtClaims = com.wso2.openbanking.accelerator.gateway.util.GatewayUtils
                .decodeBase64(com.wso2.openbanking.accelerator.gateway.util.GatewayUtils
                        .getPayloadFromJWT(consentInfo));

        JSONObject consentInformation = (JSONObject) jwtClaims.get("additionalConsentInfo");

        if (!consentInformation.isNull("authStatus")
                && StringUtils.isNotBlank((CharSequence) consentInformation.get("authStatus"))) {

            JSONObject response = new JSONObject();
            response.put(CommonConstants.PAYMENT_ID, jwtClaims.get(CommonConstants.CONSENT_ID));
            response.put(MESSAGE, "This payment is not submitted yet due to partial authorization, " +
                    "please try again after authorizing the payment");
            obapiRequestContext.setModifiedPayload(response.toString());
            contextProps.put(GatewayConstants.IS_RETURN_RESPONSE, GatewayConstants.TRUE);
            contextProps.put(GatewayConstants.MODIFIED_STATUS, String.valueOf(HttpStatus.SC_OK));
            obapiRequestContext.setContextProps(contextProps);
        }
    }
}
