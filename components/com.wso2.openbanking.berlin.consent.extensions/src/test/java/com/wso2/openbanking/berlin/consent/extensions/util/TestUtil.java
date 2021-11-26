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
import com.wso2.openbanking.berlin.common.enums.ScaApproachEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class with util classes for tests.
 */
public class TestUtil {

    public static void assertImplicitConsentResponse(ConsentManageData paymentConsentManageData,
                                              AuthorizationResource authorizationResource, boolean isImplicit,
                                              MockHttpServletRequest mockHttpServletRequest,
                                              MockHttpServletResponse mockHttpServletResponse) {

        Assert.assertNotNull(paymentConsentManageData.getResponsePayload());
        Assert.assertTrue(paymentConsentManageData.getResponsePayload() instanceof JSONObject);

        JSONObject response = (JSONObject) paymentConsentManageData.getResponsePayload();

        Assert.assertEquals(ResponseStatus.CREATED, paymentConsentManageData.getResponseStatus());
        Assert.assertEquals(mockHttpServletResponse.getHeader(ConsentExtensionConstants.ASPSP_SCA_APPROACH).toString(),
                ScaApproachEnum.REDIRECT.toString());
        Assert.assertNotNull(mockHttpServletResponse
                .getHeader(ConsentExtensionConstants.LOCATION_PROPER_CASE_HEADER).toString());
        Assert.assertEquals(TransactionStatusEnum.RCVD.name(),
                response.get(ConsentExtensionConstants.TRANSACTION_STATUS));
        Assert.assertNotNull(response.get(ConsentExtensionConstants.PAYMENT_ID));
        Assert.assertNotNull(response.get(ConsentExtensionConstants.LINKS));

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

    public static void assertConsentRetrieval(JSONObject jsonObject) {

        Assert.assertNotNull(jsonObject.get(ConsentExtensionConstants.CONSENT_DATA));
        JSONArray consentData = (JSONArray) jsonObject.get(ConsentExtensionConstants.CONSENT_DATA);
        for (Object element : consentData) {
            JSONObject jsonElement = (JSONObject) element;
            if (jsonElement.containsKey(ConsentExtensionConstants.DATA_SIMPLE)) {
                Assert.assertNotNull(jsonElement.get(ConsentExtensionConstants.DATA_SIMPLE));
                Assert.assertNotNull(jsonElement.get(ConsentExtensionConstants.TITLE));
                Assert.assertTrue(org.codehaus.plexus.util.StringUtils
                        .contains(jsonElement.getAsString(ConsentExtensionConstants.TITLE),
                        ConsentExtensionConstants.REQUESTED_DATA_TITLE));
                JSONArray data = (JSONArray) jsonElement.get(ConsentExtensionConstants.DATA_SIMPLE);
                Assert.assertNotNull(data);
            }
        }
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

    public static ConsentResource getSamplePaymentConsentResource(String currentStatus, String consentType,
                                                                  String receipt) {

        ConsentResource consentResource = new ConsentResource();
        consentResource.setReceipt(receipt);
        consentResource.setCurrentStatus(currentStatus);
        consentResource.setConsentType(consentType);

        return consentResource;
    }

    public static ConsentResource getSamplePaymentConsentResource(String currentStatus, String consentType,
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
}
