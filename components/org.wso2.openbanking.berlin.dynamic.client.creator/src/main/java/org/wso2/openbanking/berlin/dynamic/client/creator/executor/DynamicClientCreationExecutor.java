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

package org.wso2.openbanking.berlin.dynamic.client.creator.executor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.dynamic.client.creator.internal.DynamicClientCreationDataHolder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.security.cert.CertificateEncodingException;
import javax.ws.rs.HttpMethod;

/**
 * Executor to create a dynamic client in the consent creation flow.
 */
public class DynamicClientCreationExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(DynamicClientCreationExecutor.class);
    private static final String clientIdParam = "client_id";
    private static final String clientSecretParam = "client_secret";
    private static final String clientName = "client_name";
    private static final String grantTypes = "grant_types";
    private static final String redirectUris = "redirect_uris";
    private static final String authCode = "authorization_code";
    private static final String extParamClientId = "ext_param_client_id";
    private static final String userName = "userName";
    private static final String applicationIdParam = "applicationId";
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    private static final byte[] lineSeparator = System.lineSeparator().getBytes(StandardCharsets.UTF_8);
    private static final String consents = "consents";
    private static final String clientIdHeader = "x-wso2-clientid";
    private static final String tppRoleHeader = "x-wso2-tpp-roles";
    private static final String regulatoryPropHeader = "x-wso2-regulatory";
    private static final String spUpdatePropHeader = "x-wso2-updateSp";
    private static final String certificateHeader = "x-wso2-tpp-certificate";
    private static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
    private static final String accessToken = "access_token";
    private static final String applicationId = "application-id";

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        // check whether executor need to be engaged
        if (!(HttpMethod.POST.equals(obapiRequestContext.getMsgInfo().getHttpMethod())
                && obapiRequestContext.getMsgInfo().getElectedResource().contains(consents))) {
            return;
        }
        Map<String, Object> urlMap = DynamicClientCreationDataHolder.getInstance().getUrlMap();
        String basicAuthHeader = GatewayUtils.getBasicAuthHeader(urlMap.get(userName).toString(),
                String.valueOf((char[]) urlMap.get(GatewayConstants.PASSWORD)));
        CertificateContent content = extractCertificateContent(obapiRequestContext);
        if (content == null) {
            log.error("Certificate not found in the request");
            handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                    OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
            return;
        }
        String certificateOrgId = content.getPspAuthorisationNumber();
        List<String> pspRoles = content.getPspRoles();
        Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
        addedHeaders.put(clientIdHeader, certificateOrgId);

        try {
            // If an application exists for the org id, return.
            // Else create an application for the org id with the MTLS certificate and redirect URI.
            // At this executor level, it is assumed that the MTLS certificate is valid.
            JsonElement spApps = callGet(urlMap.get(GatewayConstants.IAM_DCR_URL).toString(), basicAuthHeader,
                    clientName, certificateOrgId);
            if (spApps != null && spApps.getAsJsonObject().get(clientIdParam).getAsString().equals(certificateOrgId)) {
                log.debug("Application already exists for the organization ID");
                // Since this is a no Auth request, consent mgt module will not have the client id.
                // Hence, passing the client id in the header.
                obapiRequestContext.setAddedHeaders(addedHeaders);
                return;
            }

            String isDCREndpointURL = urlMap.get(GatewayConstants.IAM_DCR_URL).toString();
            Map<String, List<String>> regulatoryAPIs = DynamicClientCreationDataHolder.getInstance()
                    .getOpenBankingConfigurationService().getAllowedAPIs();
            String redirectUri = obapiRequestContext.getMsgInfo().getHeaders().get(TPP_REDIRECT_URI_HEADER);
            if (redirectUri == null) {
                log.error("Redirect URI is not found in the request headers");
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                        "Redirect URI is not found in the request headers");
                return;
            }

            addedHeaders.put(tppRoleHeader, String.join(", ", pspRoles));
            addedHeaders.put(regulatoryPropHeader, String.valueOf(true));
            addedHeaders.put(spUpdatePropHeader, String.valueOf(true));
            obapiRequestContext.setAddedHeaders(addedHeaders);

            JSONObject registerData = new JSONObject();
            registerData.put(clientName, certificateOrgId);
            registerData.put(extParamClientId, certificateOrgId);
            registerData.put(grantTypes, Arrays.asList(authCode));
            registerData.put(redirectUris, Arrays.asList(redirectUri));
            JsonElement spAppRegResponse = callPost(isDCREndpointURL, registerData.toString(), basicAuthHeader);

            if (spAppRegResponse == null) {
                log.error("Error occurred while creating IAM application SP application");
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                        OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }

            String clientSecret = spAppRegResponse.getAsJsonObject().get(clientSecretParam).getAsString();
            //call IS DCR endpoint to create application for obtaining a token to invoke devportal REST APIs
            JsonElement registrationResponse = createServiceProvider(basicAuthHeader, certificateOrgId);
            if (registrationResponse == null) {
                log.error("Error while creating AM app for invoking APIM rest apis");
                //delete service provider
                callDelete(isDCREndpointURL.concat("/").concat(certificateOrgId), basicAuthHeader);
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                        OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }

            //call token endpoint to retrieve a token for invoking the devportal REST apis
            String amRestAPIInvokeClientId = registrationResponse.getAsJsonObject()
                    .get(clientIdParam).getAsString();

            String authHeaderForTokenRequest = GatewayUtils.getBasicAuthHeader(registrationResponse.getAsJsonObject()
                            .get(clientIdParam).getAsString(),
                    registrationResponse.getAsJsonObject().get(clientSecretParam).getAsString());

            JsonElement tokenResponse = getToken(authHeaderForTokenRequest,
                    urlMap.get(GatewayConstants.TOKEN_URL).toString(), amRestAPIInvokeClientId);

            if (tokenResponse == null || tokenResponse.getAsJsonObject().get(accessToken) == null) {
                log.error("Error while creating tokens");
                //delete service provider
                callDelete(isDCREndpointURL.concat("/").concat(certificateOrgId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(isDCREndpointURL.concat("/").concat(amRestAPIInvokeClientId), basicAuthHeader);
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                        OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }
            String token = tokenResponse.getAsJsonObject().get(accessToken).getAsString();

            //create am application
            JsonObject amAppCreatePayload = getAppCreatePayload(certificateOrgId);
            JsonElement amApplicationCreateResponse =
                    callPost(urlMap.get(GatewayConstants.APP_CREATE_URL).toString(),
                            amAppCreatePayload.toString(), GatewayConstants.BEARER_TAG.concat(token));

            if (amApplicationCreateResponse == null) {
                log.error("Error while creating AM app");
                //delete service provider
                callDelete(isDCREndpointURL.concat("/").concat(certificateOrgId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                        .concat(amRestAPIInvokeClientId), basicAuthHeader);
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                        OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }
            String keyMapURL = urlMap.get(GatewayConstants.KEY_MAP_URL).toString()
                    .replace(applicationId, amApplicationCreateResponse.getAsJsonObject()
                            .get(applicationIdParam).getAsString());
            String keyManagerName = DynamicClientCreationDataHolder.getInstance().getOpenBankingConfigurationService()
                    .getConfigurations().get(OpenBankingConstants.OB_KM_NAME).toString();

            //map keys to am application
            JsonObject keyMapPayload = getKeyMapPayload(certificateOrgId, clientSecret, "PRODUCTION", keyManagerName);

            JsonElement amKeyMapResponse = callPost(keyMapURL, keyMapPayload.toString(),
                    GatewayConstants.BEARER_TAG.concat(token));
            if (amKeyMapResponse == null) {
                log.error("Error while mapping keys to AM app");
                //delete service provider
                callDelete(isDCREndpointURL.concat("/").concat(certificateOrgId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(isDCREndpointURL.concat("/").concat(amRestAPIInvokeClientId), basicAuthHeader);
                //delete AM application
                callDelete(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()
                        .concat("/").concat(amApplicationCreateResponse.getAsJsonObject()
                                .get(applicationIdParam).getAsString()), GatewayConstants.BEARER_TAG.concat(token));
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                        OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }
            //get list of published APIs
            JsonElement publishedAPIsResponse = callGet(urlMap.get(GatewayConstants.API_RETRIEVE_URL).toString(),
                    GatewayConstants.BEARER_TAG.concat(token), "", "");
            if (publishedAPIsResponse == null) {
                log.error("Error while retrieving published APIs");
                //delete service provider
                callDelete(isDCREndpointURL.concat("/").concat(certificateOrgId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(isDCREndpointURL.concat("/").concat(amRestAPIInvokeClientId), basicAuthHeader);
                //delete AM application
                callDelete(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()
                        .concat("/").concat(amApplicationCreateResponse.getAsJsonObject()
                                .get(applicationIdParam).getAsString()), GatewayConstants.BEARER_TAG.concat(token));
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                        OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }

            List<String> apiIDList = new ArrayList<>();
            if (regulatoryAPIs != null && !regulatoryAPIs.isEmpty()) {
                apiIDList = filterRegulatorAPIs(regulatoryAPIs, publishedAPIsResponse.getAsJsonObject()
                        .get("list").getAsJsonArray());
            } else {
                log.warn("No regulatory APIs configured. Application will be subscribed to all published APIs");
                //subscribe to all APIs if there are no configured regulatory APIs
                for (JsonElement apiInfo : publishedAPIsResponse.getAsJsonObject().get("list").getAsJsonArray()) {
                    apiIDList.add(apiInfo.getAsJsonObject().get("id").getAsString());
                }
            }
            //subscribe to apis
            JsonArray subscribeAPIsPayload = getAPISubscriptionPayload(amApplicationCreateResponse
                    .getAsJsonObject().get(applicationIdParam).getAsString(), apiIDList);
            JsonElement subscribeAPIsResponse = callPost(urlMap.get(GatewayConstants.API_SUBSCRIBE_URL).toString(),
                    subscribeAPIsPayload.toString(), "Bearer ".concat(token));
            if (subscribeAPIsResponse == null) {
                log.error("Error while subscribing to APIs");
                //delete service provider
                callDelete(isDCREndpointURL.concat("/").concat(certificateOrgId), basicAuthHeader);
                //delete SP created for calling dev portal REST APIs
                callDelete(isDCREndpointURL.concat("/").concat(amRestAPIInvokeClientId), basicAuthHeader);
                //delete AM application
                callDelete(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()
                        .concat("/").concat(amApplicationCreateResponse.getAsJsonObject()
                                .get(applicationIdParam).getAsString()), GatewayConstants.BEARER_TAG.concat(token));
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                        OpenBankingErrorCodes.REGISTRATION_INTERNAL_ERROR);
                return;
            }

            //delete IAM application used to invoke am rest endpoints
            if (!callDelete(urlMap.get(GatewayConstants.IAM_DCR_URL).toString().concat("/")
                    .concat(amRestAPIInvokeClientId), basicAuthHeader)) {
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(),
                        OpenBankingErrorCodes.REGISTATION_DELETE_ERROR);
            }
        } catch (IOException | OpenBankingException | URISyntaxException e) {
            log.error("Error occurred while creating application", e);
            handleFailure(obapiRequestContext, TPPMessage.CodeEnum.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    private CertificateContent extractCertificateContent(OBAPIRequestContext obapiRequestContext) {

        CertificateContent content = null;
        // Retrieve transport certificate from the request
        javax.security.cert.X509Certificate[] x509Certificates = obapiRequestContext.getClientCerts();
        javax.security.cert.X509Certificate transportCert;
        Optional<X509Certificate> convertedTransportCert;

        if (x509Certificates.length != 0) {
            transportCert = x509Certificates[0];
            Map<String, String> addedHeaders = obapiRequestContext.getAddedHeaders();
            convertedTransportCert = CertificateValidationUtils.convert(transportCert);

            Base64.Encoder encoder = Base64.getMimeEncoder(64, lineSeparator);
            try {
                String pemCert = BEGIN_CERT
                        .concat(new String(encoder.encode(obapiRequestContext.getClientCerts()[0].getEncoded())))
                        .concat(END_CERT);
                addedHeaders.put(certificateHeader, pemCert);
                obapiRequestContext.setAddedHeaders(addedHeaders);
            } catch (CertificateEncodingException e) {
                log.error("Error while extracting pem certificate", e);
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                        "Transport certificate is invalid. Cannot proceed with organization ID validation.");
            }
        } else {
            log.error("Transport certificate not found in request context");
            handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                    "Transport certificate is missing. Cannot proceed with organization ID validation.");
            return null;
        }

        try {
            // Extract certificate content
            if (convertedTransportCert.isPresent()) {
                content = CertificateContentExtractor.extract(convertedTransportCert.get());
            } else {
                log.error("Error while processing transport certificate");
                handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                        "Invalid transport certificate. Cannot proceed with organization ID validation.");
            }
        } catch (CertificateValidationException e) {
            log.error("Error while extracting transport certificate content", e);
            handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                    "Transport certificate is invalid. Cannot proceed with organization ID validation.");
        }
        return content;
    }

    protected JsonElement createServiceProvider(String basicAuthHeader, String softwareId)
            throws IOException, OpenBankingException {

        JsonObject dcrPayload = getIAMDCRPayload(softwareId);
        return callPost(DynamicClientCreationDataHolder.getInstance().getUrlMap()
                        .get(GatewayConstants.IAM_DCR_URL).toString(),
                dcrPayload.toString(), basicAuthHeader);
    }

    private JsonObject getIAMDCRPayload(String uniqueId) {

        JsonObject jsonObject = new JsonObject();
        JsonElement jsonElement = new JsonArray();
        /* Concatenating the unique id (software id/client id) to the rest api invoking SP name to avoid
             issues in parallel requests
         */
        String restApiInvokerName = "AM_RESTAPI_INVOKER_".concat(uniqueId);
        ((JsonArray) jsonElement).add("client_credentials");
        jsonObject.addProperty("client_name", restApiInvokerName);
        jsonObject.add("grant_types", jsonElement);
        return jsonObject;
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    @Generated(message = "Excluding from test coverage since it is an HTTP call")
    protected JsonElement callPost(String endpoint, String payload, String authenticationHeader)
            throws IOException, OpenBankingException {

        try (CloseableHttpClient httpClient = HTTPClientUtils.getHttpsClient()) {
            HttpPost httpPost = new HttpPost(endpoint);
            StringEntity entity = new StringEntity(payload);
            httpPost.setEntity(entity);
            httpPost.setHeader(GatewayConstants.ACCEPT, GatewayConstants.JSON_CONTENT_TYPE);
            httpPost.setHeader(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, authenticationHeader);
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            return getResponse(httpResponse);
        }
    }

    @Generated(message = "Excluding since it requires an Http response")
    private JsonElement getResponse(HttpResponse response) throws IOException {

        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK ||
                response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            String responseStr = EntityUtils.toString(entity);
            JsonParser parser = new JsonParser();
            return parser.parse(responseStr);

        } else {
            String error = String.format("Error while invoking rest api : %s %s",
                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            log.error(error);
            return null;
        }

    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    @Generated(message = "Excluding from test coverage since it is an HTTP call")
    protected boolean callDelete(String endpoint, String authHeader) throws OpenBankingException, IOException {

        try (CloseableHttpClient httpClient = HTTPClientUtils.getHttpsClient()) {
            HttpDelete httpDelete = new HttpDelete(endpoint);
            httpDelete.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            CloseableHttpResponse appDeletedResponse = httpClient.execute(httpDelete);
            int status = appDeletedResponse.getStatusLine().getStatusCode();
            return (status == 204 || status == 200);
        }
    }

    @Generated(message = "Excluding from test coverage since it is an HTTP call")
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    protected JsonElement getToken(String authHeader, String url, String clientId) throws IOException, JSONException,
            OpenBankingException {

        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpPost request = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            params.add(new BasicNameValuePair("scope", "apim:subscribe apim:api_key apim:app_manage " +
                    "apim:sub_manage openid"));
            //params.add(new BasicNameValuePair("client_id", clientId));
            request.setEntity(new UrlEncodedFormEntity(params));
            request.addHeader(HTTPConstants.HEADER_AUTHORIZATION, authHeader);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Obtaining access token  failed with status code: " +
                        response.getStatusLine().getStatusCode());
                return new JsonObject();
            }
            return getResponse(response);
        }
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    @Generated(message = "Excluding from test coverage since it is an HTTP call")
    protected JsonElement callGet(String endpoint, String authHeader, String queryParamKey, String paramValue)
            throws IOException, OpenBankingException, URISyntaxException {

        try (CloseableHttpClient httpClient = HTTPClientUtils.getHttpsClient()) {
            HttpGet httpGet = new HttpGet(endpoint);
            List nameValuePairs = new ArrayList();
            if (StringUtils.isNotEmpty(queryParamKey)) {
                nameValuePairs.add(new BasicNameValuePair(queryParamKey, paramValue));
                URI uri = new URIBuilder(httpGet.getURI()).addParameters(nameValuePairs).build();
                ((HttpRequestBase) httpGet).setURI(uri);
            }
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            CloseableHttpResponse restAPIResponse = httpClient.execute(httpGet);
            return getResponse(restAPIResponse);
        }
    }

    private JsonObject getAppCreatePayload(String applicationName) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", applicationName);
        jsonObject.addProperty("throttlingPolicy", "Unlimited");
        return jsonObject;

    }

    private JsonObject getKeyMapPayload(String consumerKey, String consumerSecret, String keyType,
                                        String keyManagerName) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("consumerKey", consumerKey);
        jsonObject.addProperty("consumerSecret", consumerSecret);
        jsonObject.addProperty("keyType", keyType);
        jsonObject.addProperty("keyManager", keyManagerName);
        return jsonObject;

    }

    protected List<String> filterRegulatorAPIs(Map<String, List<String>> regulatoryAPINames, JsonArray publishedAPIs) {

        List<String> filteredAPIs = new ArrayList<>();
        for (JsonElement apiInfo : publishedAPIs) {
            for (Map.Entry<String, List<String>> entry : regulatoryAPINames.entrySet()) {
                if (entry.getKey().equals(apiInfo.getAsJsonObject().get("name").getAsString())) {
                    filteredAPIs.add(apiInfo.getAsJsonObject().get("id").getAsString());
                    break;

                }
            }
        }
        return filteredAPIs;
    }

    private JsonArray getAPISubscriptionPayload(String applicationId, List<String> apiIdList) {

        JsonArray jsonArray = new JsonArray();
        for (String apiID : apiIdList) {
            JsonObject apiInfo = new JsonObject();
            apiInfo.addProperty(applicationIdParam, applicationId);
            apiInfo.addProperty("apiId", apiID);
            apiInfo.addProperty("throttlingPolicy", "Unlimited");
            jsonArray.add(apiInfo);
        }
        return jsonArray;
    }

    public static void handleFailure(OBAPIRequestContext obapiRequestContext, String code, String message) {

        obapiRequestContext.setError(true);
        ArrayList<OpenBankingExecutorError> executorErrors = new ArrayList<>();
        OpenBankingExecutorError openBankingExecutorError = new OpenBankingExecutorError();
        openBankingExecutorError.setCode(code);
        openBankingExecutorError.setMessage(message);
        executorErrors.add(openBankingExecutorError);
        obapiRequestContext.setErrors(executorErrors);
    }
}
