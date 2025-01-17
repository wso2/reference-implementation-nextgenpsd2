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

package org.wso2.openbanking.berlin.consent.extensions.admin.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.admin.impl.DefaultConsentAdminHandler;
import com.wso2.openbanking.accelerator.consent.extensions.admin.model.ConsentAdminData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consent admin handler default implementation.
 */
public class BGConsentAdminHandler extends DefaultConsentAdminHandler {

    private static final Log log = LogFactory.getLog(BGConsentAdminHandler.class);

    private String validateAndGetQueryParam(Map queryParams, String key) {

        if (queryParams.containsKey(key) && (((ArrayList) queryParams.get(key)).get(0) instanceof String)) {
            return (String) ((ArrayList) queryParams.get(key)).get(0);
        }
        return null;
    }

    @Override
    public void handleRevoke(ConsentAdminData consentAdminData) throws ConsentException {

        Map queryParams = consentAdminData.getQueryParams();

        String consentId = validateAndGetQueryParam(queryParams, "consentID");
        if (consentId == null) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Mandatory parameter consent ID not available");
        }
        if (log.isDebugEnabled()) {
            log.debug("Get existing consent resource for provided consent Id: " + consentId);
        }
        ConsentCoreServiceImpl coreService = getConsentService();
        try {
            DetailedConsentResource consentResource = coreService.getDetailedConsent(consentId);

            // Validate non customer-care user is revoking only their own consents
            if (consentAdminData.getQueryParams().containsKey("userID")) {
                List<AuthorizationResource> filteredIds = consentResource.getAuthorizationResources().stream()
                        .filter(authorizationResource -> consentAdminData.getQueryParams().get("userID").toString()
                                .contains(authorizationResource.getUserID()))
                        .collect(Collectors.toList());
                if (filteredIds.isEmpty()) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            "Unauthorized Request, Consent userId does not match");
                }
            }
            if (consentResource.getConsentType().equalsIgnoreCase("accounts")) {
                validateAndRevokeAccountConsent(consentResource, coreService, consentAdminData);
            } else if (consentResource.getConsentType().equalsIgnoreCase("funds-confirmations")) {
                validateAndRevokeCofConsent(consentResource, coreService, consentAdminData);
            } else {
                validateAndRevokePaymentConsent(consentResource, coreService);
            }

            consentAdminData.setResponseStatus(ResponseStatus.OK);
            consentAdminData.setResponseStatus(ResponseStatus.NO_CONTENT);
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_NOT_FOUND_ERROR, e);
            throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_UNKNOWN,
                    ErrorConstants.CONSENT_NOT_FOUND_ERROR));
        }

    }

    /**
     * Method to validate and revoke Account consents
     *
     * @param consentResource  Consent resource
     * @param coreService      consent core service instance
     * @param consentAdminData consent admin date related to revoking consent
     */
    private void validateAndRevokeAccountConsent(DetailedConsentResource consentResource,
                                                 ConsentCoreServiceImpl coreService,
                                                 ConsentAdminData consentAdminData) {

        if (StringUtils.equals(ConsentStatusEnum.REVOKED_BY_PSU.toString(), consentResource.getCurrentStatus())
                || StringUtils.equals(ConsentStatusEnum.TERMINATED_BY_TPP.toString(),
                consentResource.getCurrentStatus())) {
            log.error(ErrorConstants.CONSENT_ALREADY_DELETED);
            throw new ConsentException(ResponseStatus.UNAUTHORIZED, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.CONSENT_ALREADY_DELETED));
        }

        // Check whether the consent is already expired before deleting
        if (StringUtils.equals(ConsentStatusEnum.EXPIRED.toString(), consentResource.getCurrentStatus())) {
            log.error(ErrorConstants.CONSENT_ALREADY_EXPIRED);
            throw new ConsentException(ResponseStatus.UNAUTHORIZED, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.CONSENT_ALREADY_EXPIRED));
        }

        try {

            if (!consentAdminData.getQueryParams().containsKey("userID")) {
                coreService.revokeConsent(consentResource.getConsentID(),
                        ConsentStatusEnum.TERMINATED_BY_TPP.toString());
            } else {
                coreService.revokeConsent(consentResource.getConsentID(),
                        ConsentStatusEnum.REVOKED_BY_PSU.toString());
            }
            if (log.isDebugEnabled()) {
                log.debug("Deactivating account mappings of revoked consent: " + consentResource.getConsentID());
            }
            ArrayList<ConsentMappingResource> mappingResources = consentResource.getConsentMappingResources();
            ArrayList<String> mappingIds = new ArrayList<>();
            for (ConsentMappingResource mappingResource : mappingResources) {
                mappingIds.add(mappingResource.getMappingID());
            }
            if (CollectionUtils.isNotEmpty(mappingIds)) {
                coreService.deactivateAccountMappings(mappingIds);
            }
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_UPDATE_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_UPDATE_ERROR);
        }
    }

    /**
     * Method to validate and revoke Payment consents
     *
     * @param consentResource Consent resource
     * @param coreService     consent core service instance
     */
    private void validateAndRevokePaymentConsent(DetailedConsentResource consentResource,
                                                 ConsentCoreServiceImpl coreService) {

        // Payment cancellation not applicable for single payments
        if (StringUtils.equals(consentResource.getConsentType(), ConsentTypeEnum.PAYMENTS.toString())) {
            log.error(ErrorConstants.CANCELLATION_NOT_APPLICABLE);
            throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CANCELLATION_INVALID,
                    ErrorConstants.CANCELLATION_NOT_APPLICABLE));
        }

        if (StringUtils.equals(TransactionStatusEnum.CANC.name(), consentResource.getCurrentStatus())
                || StringUtils.equals(TransactionStatusEnum.REVOKED.name(), consentResource.getCurrentStatus())) {
            log.error(ErrorConstants.CONSENT_ALREADY_DELETED);
            throw new ConsentException(ResponseStatus.UNAUTHORIZED, ErrorUtil.constructBerlinError(null,
                    TPPMessage.CategoryEnum.ERROR, TPPMessage.CodeEnum.CONSENT_INVALID,
                    ErrorConstants.CONSENT_ALREADY_DELETED));
        }

        try {
            coreService.revokeConsent(consentResource.getConsentID(), TransactionStatusEnum.CANC.name());

            if (log.isDebugEnabled()) {
                log.debug("Deactivating account mappings of revoked payment: " + consentResource.getConsentID());
            }
            ArrayList<ConsentMappingResource> mappingResources = consentResource.getConsentMappingResources();
            ArrayList<String> mappingIds = new ArrayList<>();
            for (ConsentMappingResource mappingResource : mappingResources) {
                mappingIds.add(mappingResource.getMappingID());
            }
            if (CollectionUtils.isNotEmpty(mappingIds)) {
                coreService.deactivateAccountMappings(mappingIds);
            }
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_UPDATE_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_UPDATE_ERROR);
        }
    }

    /**
     * Method to validate and revoke CoF consents
     *
     * @param consentResource  Consent resource
     * @param coreService      consent core service instance
     * @param consentAdminData consent admin date related to revoking consent
     */
    private void validateAndRevokeCofConsent(DetailedConsentResource consentResource,
                                             ConsentCoreServiceImpl coreService, ConsentAdminData consentAdminData) {

        // Both account and cof consents have same basic checks when it comes to revocation.
        // #Ref FundsConfirmationServiceHandler
        validateAndRevokeAccountConsent(consentResource, coreService, consentAdminData);
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }

}
