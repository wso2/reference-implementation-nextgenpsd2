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

package com.wso2.openbanking.berlin.consent.extensions.util;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.berlin.common.constants.CommonConstants;
import com.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.enums.ScaApproachEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.AccessMethodEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class with util classes for tests.
 */
public class TestUtil {

    public static JSONObject getSampleAccountRefObject(String accountRefIdentifier, String accountNumber,
                                                       String currency) {

        JSONObject accountRefObject = new JSONObject();
        accountRefObject.put(accountRefIdentifier, accountNumber);

        if (StringUtils.isNotBlank(currency)) {
            accountRefObject.put(ConsentExtensionConstants.CURRENCY, currency);
        }

        return accountRefObject;
    }

    public static void assertConsentResponse(ConsentManageData consentManageData,
                                             AuthorizationResource authorizationResource, boolean isImplicit,
                                             MockHttpServletRequest mockHttpServletRequest,
                                             MockHttpServletResponse mockHttpServletResponse,
                                             ConsentTypeEnum consentType) {

        Assert.assertNotNull(consentManageData.getResponsePayload());
        Assert.assertTrue(consentManageData.getResponsePayload() instanceof JSONObject);
        Assert.assertEquals(ResponseStatus.CREATED, consentManageData.getResponseStatus());

        JSONObject response = (JSONObject) consentManageData.getResponsePayload();
        Assert.assertNotNull(response.get(ConsentExtensionConstants.LINKS));

        if (ConsentTypeEnum.ACCOUNTS.equals(consentType)) {
            Assert.assertEquals(ConsentStatusEnum.RECEIVED.toString(),
                    response.get(ConsentExtensionConstants.CONSENT_STATUS));
            Assert.assertNotNull(response.get(ConsentExtensionConstants.CONSENT_ID));
        } else if (ConsentTypeEnum.PAYMENTS.equals(consentType)) {
            Assert.assertEquals(TransactionStatusEnum.RCVD.name(),
                    response.get(ConsentExtensionConstants.TRANSACTION_STATUS));
            Assert.assertNotNull(response.get(ConsentExtensionConstants.PAYMENT_ID));
        }

        Assert.assertEquals(mockHttpServletResponse.getHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH).toString(),
                ScaApproachEnum.REDIRECT.toString());
        Assert.assertNotNull(mockHttpServletResponse
                .getHeader(ConsentExtensionConstants.LOCATION_HEADER).toString());

        JSONObject linksObject = (JSONObject) response.get(ConsentExtensionConstants.LINKS);
        Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.SELF));

        if (isImplicit) {
            Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.SCA_STATUS));
            // The same authorization ID in scaLink in links object confirms the creation of the authorization resource
            // in implicit flow
            Assert.assertTrue(StringUtils.contains(linksObject.getAsString(ConsentExtensionConstants.SCA_STATUS),
                    authorizationResource.getAuthorizationID()));
        } else {
            Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.START_AUTH_WITH_PSU_IDENTIFICATION));
        }
    }

    public static void assertConsentRetrieval(JSONObject jsonObject, String consentType) {

        Assert.assertNotNull(jsonObject.get(ConsentExtensionConstants.CONSENT_DATA));
        JSONObject consentData = (JSONObject) jsonObject.get(ConsentExtensionConstants.CONSENT_DATA);
        JSONArray consentDetails = (JSONArray) consentData.get(ConsentExtensionConstants.CONSENT_DETAILS);
        for (Object element : consentDetails) {
            JSONObject jsonElement = (JSONObject) element;
            if (jsonElement.containsKey(ConsentExtensionConstants.DATA_SIMPLE)) {
                Assert.assertNotNull(jsonElement.get(ConsentExtensionConstants.DATA_SIMPLE));
                Assert.assertNotNull(jsonElement.get(ConsentExtensionConstants.TITLE));

                if (StringUtils.equals(consentType, ConsentTypeEnum.ACCOUNTS.toString())
                        || StringUtils.equals(consentType, ConsentTypeEnum.FUNDS_CONFIRMATION.toString())) {
                    Assert.assertTrue(StringUtils.contains(jsonElement.getAsString(ConsentExtensionConstants.TITLE),
                            ConsentExtensionConstants.CONSENT_DETAILS_TITLE));
                } else {
                    Assert.assertTrue(StringUtils.contains(jsonElement.getAsString(ConsentExtensionConstants.TITLE),
                            ConsentExtensionConstants.REQUESTED_DATA_TITLE));
                }
                JSONArray data = (JSONArray) jsonElement.get(ConsentExtensionConstants.DATA_SIMPLE);
                Assert.assertNotNull(data);
            }
        }

        if (StringUtils.equals(consentType, ConsentTypeEnum.ACCOUNTS.toString())) {
            Assert.assertNotNull(consentData.get(ConsentExtensionConstants.ACCESS_OBJECT));
            Assert.assertNotNull(consentData.get(ConsentExtensionConstants.PERMISSION));
        } else {
            Assert.assertNotNull(consentData.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECT));
        }
    }

    public static void assertStartAuthResponse(ConsentManageData consentManageData,
                                               AuthorizationResource authorizationResource,
                                               MockHttpServletRequest mockHttpServletRequest,
                                               MockHttpServletResponse mockHttpServletResponse) {

        Assert.assertEquals(ResponseStatus.CREATED, consentManageData.getResponseStatus());

        JSONObject response = (JSONObject) consentManageData.getResponsePayload();
        Assert.assertNotNull(response.get(ConsentExtensionConstants.LINKS));
        Assert.assertNotNull(response.get(ConsentExtensionConstants.AUTH_ID));
        Assert.assertEquals(response.get(ConsentExtensionConstants.SCA_STATUS), ScaStatusEnum.RECEIVED.toString());

        Assert.assertEquals(mockHttpServletResponse.getHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH).toString(),
                ScaApproachEnum.REDIRECT.toString());
        Assert.assertNotNull(mockHttpServletResponse
                .getHeader(ConsentExtensionConstants.LOCATION_HEADER).toString());

        JSONObject linksObject = (JSONObject) response.get(ConsentExtensionConstants.LINKS);
        Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.SCA_STATUS));
        Assert.assertNotNull(linksObject.get(ConsentExtensionConstants.SCA_OAUTH));
    }

    public static String getCurrentDate(int addDays) {

        return LocalDate.parse(LocalDate.now().plusDays(addDays).toString(), DateTimeFormatter.ISO_DATE).toString();
    }

    public static List<Map<String, String>> getSampleSupportedScaApproaches() {

        List<Map<String, String>> scaApproaches = new ArrayList<>();
        Map<String, String> scaApproach = new HashMap<>();
        scaApproach.put(CommonConstants.SCA_NAME, "REDIRECT");
        scaApproach.put(CommonConstants.SCA_DEFAULT, "true");
        scaApproaches.add(scaApproach);

        return scaApproaches;
    }

    public static List<Map<String, String>> getSampleSupportedScaMethods() {

        List<Map<String, String>> scaMethods = new ArrayList<>();
        Map<String, String> scaMethod = new HashMap<>();
        scaMethod.put(CommonConstants.SCA_TYPE, "SMS_OTP");
        scaMethod.put(CommonConstants.SCA_VERSION, "1.0");
        scaMethod.put(CommonConstants.SCA_ID, "sms-otp");
        scaMethod.put(CommonConstants.SCA_NAME, "SMS OTP on Mobile");
        scaMethod.put(CommonConstants.SCA_MAPPED_APPROACH, "REDIRECT");
        scaMethod.put(CommonConstants.SCA_DESCRIPTION, "SMS based one time password");
        scaMethod.put(CommonConstants.SCA_DEFAULT, "true");
        scaMethods.add(scaMethod);

        return scaMethods;
    }

    public static DetailedConsentResource getSampleDetailedStoredTestConsentResource(String consentId,
                                                                                     String clientId,
                                                                                     String consentType,
                                                                                     String consentStatus,
                                                                                     String authId, String authType,
                                                                                     String userId) {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(consentId);
        detailedConsentResource.setReceipt(TestPayloads.VALID_PERIODICAL_PAYMENT_PAYLOAD);
        detailedConsentResource.setClientID(clientId);
        detailedConsentResource.setConsentType(consentType);
        detailedConsentResource.setCurrentStatus(consentStatus);
        detailedConsentResource.setConsentFrequency(0);
        detailedConsentResource.setValidityPeriod(0);
        detailedConsentResource.setRecurringIndicator(false);
        detailedConsentResource.setCreatedTime(System.currentTimeMillis() / 1000);

        if (StringUtils.isNotBlank(authId)) {
            ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
            authorizationResources.add(getSampleStoredTestAuthorizationResource(consentId,
                    authType, ScaStatusEnum.PSU_AUTHENTICATED.toString(), authId, userId));
            detailedConsentResource.setAuthorizationResources(authorizationResources);
        }

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(getSampleTestConsentMappingResource(authId));
        detailedConsentResource.setConsentMappingResources(consentMappingResources);

        return detailedConsentResource;
    }

    public static AuthorizationResource getSampleStoredTestAuthorizationResource(String consentId,
                                                                                 String authorizationType,
                                                                                 String authStatus, String authId,
                                                                                 String userId) {

        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResource.setConsentID(consentId);
        authorizationResource.setAuthorizationID(authId);
        authorizationResource.setAuthorizationType(authorizationType);
        authorizationResource.setUserID(userId);
        authorizationResource.setAuthorizationStatus(authStatus);
        authorizationResource.setUpdatedTime(System.currentTimeMillis() / 1000);

        return authorizationResource;
    }

    public static ConsentMappingResource getSampleTestConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setMappingID("MappingId");
        consentMappingResource.setAuthorizationID(authorizationID);
        consentMappingResource.setAccountID("1234567");
        consentMappingResource.setPermission("samplePermission");
        consentMappingResource.setMappingStatus("active");

        return consentMappingResource;
    }

    public static ConsentMappingResource getSampleTestConsentMappingResource(String mappingId, String authorizationID,
                                                                             String accountID, String permission,
                                                                             String mappingStatus) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();

        if (StringUtils.isNotBlank(mappingId)) {
            consentMappingResource.setMappingID(mappingId);
        }

        if (StringUtils.isNotBlank(authorizationID)) {
            consentMappingResource.setAuthorizationID(authorizationID);
        }

        if (StringUtils.isNotBlank(accountID)) {
            consentMappingResource.setAccountID(accountID);
        }

        if (StringUtils.isNotBlank(permission)) {
            consentMappingResource.setPermission(permission);
        }

        if (StringUtils.isNotBlank(mappingStatus)) {
            consentMappingResource.setMappingStatus(mappingStatus);
        }

        return consentMappingResource;
    }

    public static ArrayList<ConsentMappingResource> getSampleTestConsentMappingResourcesWithPermissions(
            String authorizationID) {

        ArrayList<ConsentMappingResource> mappingResources = new ArrayList<>();

        ConsentMappingResource accountMappingResource = new ConsentMappingResource();
        accountMappingResource.setMappingID(UUID.randomUUID().toString());
        accountMappingResource.setAuthorizationID(authorizationID);
        accountMappingResource.setAccountID("DE12345678901234567890");
        accountMappingResource.setPermission(AccessMethodEnum.ACCOUNTS.toString());
        accountMappingResource.setMappingStatus("active");

        ConsentMappingResource accountWithBalanceMappingResource = new ConsentMappingResource();
        accountWithBalanceMappingResource.setMappingID(UUID.randomUUID().toString());
        accountWithBalanceMappingResource.setAuthorizationID(authorizationID);
        accountWithBalanceMappingResource.setAccountID("DE12345678901234567890");
        accountWithBalanceMappingResource.setPermission(AccessMethodEnum.BALANCES.toString());
        accountWithBalanceMappingResource.setMappingStatus("active");

        ConsentMappingResource balanceMappingResource = new ConsentMappingResource();
        balanceMappingResource.setMappingID(UUID.randomUUID().toString());
        balanceMappingResource.setAuthorizationID(authorizationID);
        balanceMappingResource.setAccountID("DE12345678901234567891");
        balanceMappingResource.setPermission(AccessMethodEnum.BALANCES.toString());
        balanceMappingResource.setMappingStatus("active");

        ConsentMappingResource transactionMappingResource = new ConsentMappingResource();
        transactionMappingResource.setMappingID(UUID.randomUUID().toString());
        transactionMappingResource.setAuthorizationID(authorizationID);
        transactionMappingResource.setAccountID("DE12345678901234567892");
        transactionMappingResource.setPermission(AccessMethodEnum.TRANSACTIONS.toString());
        transactionMappingResource.setMappingStatus("active");

        ConsentMappingResource transactionWithBalanceMappingResource = new ConsentMappingResource();
        transactionWithBalanceMappingResource.setMappingID(UUID.randomUUID().toString());
        transactionWithBalanceMappingResource.setAuthorizationID(authorizationID);
        transactionWithBalanceMappingResource.setAccountID("DE12345678901234567892");
        transactionWithBalanceMappingResource.setPermission(AccessMethodEnum.BALANCES.toString());
        transactionWithBalanceMappingResource.setMappingStatus("active");

        mappingResources.add(accountMappingResource);
        mappingResources.add(balanceMappingResource);
        mappingResources.add(transactionMappingResource);
        mappingResources.add(accountWithBalanceMappingResource);
        mappingResources.add(transactionWithBalanceMappingResource);

        return mappingResources;
    }

    public static ConsentData getSampleConsentDataObject(String userId, String spQueryParams, String scopeString,
                                                         String application, Map<String, String> requestHeaders,
                                                         boolean isRegulatory)
            throws URISyntaxException {

        String sessionDataKey = UUID.randomUUID().toString();
        ConsentData consentData = new ConsentData(sessionDataKey, userId, spQueryParams, scopeString, application,
                requestHeaders);
        consentData.setRedirectURI(new URI("https://www.google.com"));
        consentData.setState(UUID.randomUUID().toString());
        consentData.setRegulatory(isRegulatory);
        return consentData;
    }

    public static ConsentManageData getSampleConsentManageData(Map<String, String> headersMap, String path,
                                                               MockHttpServletRequest mockHttpServletRequest,
                                                               MockHttpServletResponse mockHttpServletResponse,
                                                               String clientId, String httpMethod, String payload)
            throws ParseException {

        mockHttpServletRequest.setMethod(httpMethod);
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        ConsentManageData consentManageData = new ConsentManageData(headersMap, parser.parse(payload), new HashMap(),
                path, mockHttpServletRequest, mockHttpServletResponse);
        consentManageData.setClientId(clientId);
        return consentManageData;
    }

    public static ConsentResource getSampleConsentResource(String currentStatus, String consentType,
                                                           String receipt) {

        ConsentResource consentResource = new ConsentResource();
        consentResource.setReceipt(receipt);
        consentResource.setCurrentStatus(currentStatus);
        consentResource.setConsentType(consentType);

        return consentResource;
    }

    public static ConsentResource getSampleConsentResource(String currentStatus, String consentType,
                                                           String receipt, String consentId, String clientId) {

        ConsentResource consentResource = new ConsentResource();
        consentResource.setReceipt(receipt);
        consentResource.setCurrentStatus(currentStatus);
        consentResource.setConsentType(consentType);
        consentResource.setClientID(clientId);
        consentResource.setConsentID(consentId);

        return consentResource;
    }

    public static ConsentData getSampleRegulatoryConsentDataResource(String scopeString) throws URISyntaxException {

        return getSampleConsentDataObject(TestConstants.USER_ID,
                TestConstants.SAMPLE_QUERY_PARAMS, scopeString, TestConstants.SAMPLE_APP_NAME,
                new HashMap<>(), true);
    }

    public static JSONObject getSampleFundsConfirmationConsentDataJSONObject(JSONObject accountRefObject) {

        JSONObject consentData = new JSONObject();
        JSONArray consentDetails = new JSONArray();
        JSONObject dataElement = new JSONObject();
        JSONArray consentDataArray = new JSONArray();

        consentDataArray.add("data: 1");
        consentDataArray.add("data: 2");
        consentDataArray.add("data: 3");

        dataElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONSENT_DETAILS_TITLE);
        dataElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, consentDataArray);
        consentDetails.add(dataElement);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConsentExtensionConstants.CONSENT_DETAILS, consentDetails);
        jsonObject.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, accountRefObject);

        consentData.put(ConsentExtensionConstants.CONSENT_DATA, jsonObject);
        return consentData;
    }

    public static JSONObject getSamplePaymentConsentDataJSONObject(JSONObject accountRefObject) {

        JSONObject consentData = new JSONObject();
        JSONArray consentDetails = new JSONArray();
        JSONObject dataElement = new JSONObject();
        JSONArray consentDataArray = new JSONArray();

        consentDataArray.add("data: 1");
        consentDataArray.add("data: 2");
        consentDataArray.add("data: 3");

        dataElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONSENT_DETAILS_TITLE);
        dataElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, consentDataArray);
        consentDetails.add(dataElement);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConsentExtensionConstants.CONSENT_DETAILS, consentDetails);
        jsonObject.put(ConsentExtensionConstants.ACCOUNT_REF_OBJECT, accountRefObject);

        consentData.put(ConsentExtensionConstants.CONSENT_DATA, jsonObject);
        return consentData;
    }

    public static JSONObject getSampleAccountsConsentDataJSONObject(JSONObject accessObject, String permission) {

        JSONObject consentData = new JSONObject();
        JSONArray consentDetails = new JSONArray();
        JSONObject dataElement = new JSONObject();
        JSONArray consentDataArray = new JSONArray();

        consentDataArray.add("data: 1");
        consentDataArray.add("data: 2");
        consentDataArray.add("data: 3");

        dataElement.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONSENT_DETAILS_TITLE);
        dataElement.appendField(ConsentExtensionConstants.DATA_SIMPLE, consentDataArray);
        consentDetails.add(dataElement);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ConsentExtensionConstants.CONSENT_DETAILS, consentDetails);
        jsonObject.put(ConsentExtensionConstants.ACCESS_OBJECT, accessObject);
        jsonObject.put(ConsentExtensionConstants.PERMISSION, permission);

        consentData.put(ConsentExtensionConstants.CONSENT_DATA, jsonObject);
        return consentData;
    }
}
