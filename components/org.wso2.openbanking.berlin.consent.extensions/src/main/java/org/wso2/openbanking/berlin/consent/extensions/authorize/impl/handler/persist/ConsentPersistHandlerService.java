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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.openbanking.berlin.consent.extensions.authorize.common.AuthorisationStateChangeHook;
import org.wso2.openbanking.berlin.consent.extensions.authorize.enums.AuthorisationAggregateStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.authorize.factory.AuthorizationHandlerFactory;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentStatusEnum;
import org.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Consent persist handler service class for common service level persist handler methods.
 */
public class ConsentPersistHandlerService {

    private ConsentCoreServiceImpl consentCoreService;
    private static final Log log = LogFactory.getLog(ConsentPersistHandlerService.class);

    public ConsentPersistHandlerService(ConsentCoreServiceImpl consentCoreService) {

        this.consentCoreService = consentCoreService;
    }

    /**
     * Method to perform authorisation persistence related functions.
     *
     * @param consentResource             current consent resource
     * @param accountIdMapWithPermissions account Ids with permissions map
     * @param authorisationId             current authorisation Id
     * @param psuId                       the logged in user
     * @param authStatus                  the auth status to be updated
     * @throws ConsentManagementException thrown if an error occur while persisting authorisation
     */
    public void persistAuthorisation(ConsentResource consentResource,
                                     Map<String, ArrayList<String>> accountIdMapWithPermissions,
                                     String authorisationId, String psuId, String authStatus, boolean isApproved)
            throws ConsentManagementException {

        AuthorizationResource currentAuthorisationResource =
                consentCoreService.getAuthorizationResource(authorisationId);

        // Removing super tenant from username if present in order to avoid storing in the DB
        String username = StringUtils.removeEndIgnoreCase(psuId, ConsentExtensionConstants.SUPER_TENANT_DOMAIN);
        String consentId = consentResource.getConsentID();
        String currentAuthorisationType = currentAuthorisationResource.getAuthorizationType();

        if (StringUtils.equals(consentResource.getConsentID(), currentAuthorisationResource.getConsentID())) {

            // Update the current authorization status before computing aggregated consent status
            consentCoreService.updateAuthorizationStatus(authorisationId, authStatus);

            /* Get all the authorisation requests for the given consent Id and auth type to compute aggregated
            consent status */
            List<AuthorizationResource> authorisationsList = consentCoreService.searchAuthorizations(consentId)
                    .stream()
                    .filter(authorisation -> StringUtils.equals(currentAuthorisationType,
                            authorisation.getAuthorizationType()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(authorisationsList)) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Computing the aggregated consent status for the consent of Id: %s",
                            consentId));
                }
                Optional<AuthorisationAggregateStatusEnum> aggregatedStatus =
                        computeAggregatedConsentStatus(authorisationsList);

                if (aggregatedStatus.isPresent()) {
                    AuthorisationStateChangeHook stateChangeHook =
                            getAuthorisationStateChangeHook(consentResource.getConsentType());

                    // Handling multiple recurring indicator for accounts
                    if (StringUtils.equals(consentResource.getConsentType(), ConsentTypeEnum.ACCOUNTS.toString())
                            && StringUtils.equals(aggregatedStatus.get().toString(),
                            AuthorisationAggregateStatusEnum.FULLY_AUTHORISED.toString())) {
                        handleMultipleRecurringConsent(consentResource, username);
                    }

                    String consentStatusToUpdate = stateChangeHook.onAuthorisationStateChange(consentId,
                            currentAuthorisationType, aggregatedStatus.get(), currentAuthorisationResource);
                    consentCoreService.bindUserAccountsToConsent(consentResource, username,
                            authorisationId, accountIdMapWithPermissions, authStatus, consentStatusToUpdate);

                    // Deactivate the account mappings created for the relevant consent ID when consent is denied
                    if (!isApproved) {
                        deactivateAccountMapping(consentResource);
                    }
                } else {
                    log.error(String.format(ErrorConstants.INVALID_PAYMENT_CONSENT_STATUS_UPDATE, consentId));
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            ErrorConstants.INVALID_PAYMENT_CONSENT_STATUS_UPDATE);
                }
            }
        } else {
            log.error(ErrorConstants.AUTH_ID_CONSENT_ID_MISMATCH);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.AUTH_ID_CONSENT_ID_MISMATCH);
        }
    }

    /**
     * Method to deactivate account mapping when user deny the consent.
     *
     * @param consentResource
     * @throws ConsentManagementException
     */
    private void deactivateAccountMapping(ConsentResource consentResource) throws ConsentManagementException {
        DetailedConsentResource consentResourceAfterUpdate = consentCoreService
                .getDetailedConsent(consentResource.getConsentID());
        ArrayList<String> mappingIdList = new ArrayList<>();
        for (ConsentMappingResource mappingResource : consentResourceAfterUpdate.getConsentMappingResources()) {
            mappingIdList.add(mappingResource.getMappingID());
        }
        consentCoreService.deactivateAccountMappings(mappingIdList);
    }

    /**
     * Expires old recurring consents based on the recurring indicator and configuration of multiple recurring
     * consent support. Also deactivating connected accounts of the consents which are expired in the process.
     *
     * @param currentConsentResource the current consent resource
     * @param loggedInUserId         the logged in user for authorization
     * @throws ConsentManagementException thrown if an error occur while using consent core service
     */
    private void handleMultipleRecurringConsent(ConsentResource currentConsentResource, String loggedInUserId)
            throws ConsentManagementException {

        // Expire old recurring consents only if the recurringIndicator is set to true and
        // EnableMultipleRecurringConsent is set to false.
        if (CommonConfigParser.getInstance().isMultipleRecurringConsentEnabled()
                || !currentConsentResource.isRecurringIndicator()) {
            return;
        }

        List<DetailedConsentResource> detailedConsentResources;

        /*
        Filtering out the consent resource where;
        it belongs to the same client as the current consent resource
        and is an account consent type having a single authorisation resource
        and belonging to the current logged in user
         */
        detailedConsentResources = consentCoreService.searchDetailedConsents(null,
                        new ArrayList<>(Collections.singletonList(currentConsentResource.getClientID())),
                        new ArrayList<>(Collections.singletonList(currentConsentResource.getConsentType())),
                        new ArrayList<>(Collections.singletonList(ConsentStatusEnum.VALID.toString())),
                        new ArrayList<>(Collections.singletonList(loggedInUserId)), null, null, null,
                        null)
                .stream()
                .filter(detailedConsentResource -> {
                    //todo: https://github.com/wso2-enterprise/financial-open-banking/issues/6525#issuecomment-974151193
                    return detailedConsentResource.getAuthorizationResources().size() == 1;
                })
                .filter(DetailedConsentResource::isRecurringIndicator)
                .collect(Collectors.toList());

        // Expiring all the previous consents except the current one
        for (DetailedConsentResource detailedConsentResource : detailedConsentResources) {
            if (!StringUtils.equals(detailedConsentResource.getConsentID(), currentConsentResource.getConsentID())) {
                consentCoreService.updateConsentStatus(detailedConsentResource.getConsentID(),
                        ConsentStatusEnum.EXPIRED.toString());

                // Deactivate relative mapping resources after expiration
                ArrayList<ConsentMappingResource> mappingResources =
                        detailedConsentResource.getConsentMappingResources();
                ArrayList<String> mappingIds = new ArrayList<>();
                for (ConsentMappingResource mappingResource : mappingResources) {
                    mappingIds.add(mappingResource.getMappingID());
                }
                if (CollectionUtils.isNotEmpty(mappingIds)) {
                    consentCoreService.deactivateAccountMappings(mappingIds);
                }
            }
        }
    }

    /**
     * Method to get the aggregated consent authorisation status based on multiple authorisation resources.
     *
     * @param authorisationList list of authorisations for the current consent. This doesn't contain the current
     *                          authorisation object
     * @return the enum of aggregated authorisation status
     */
    private Optional<AuthorisationAggregateStatusEnum> computeAggregatedConsentStatus(List<AuthorizationResource>
                                                                                              authorisationList) {

        // Has at least one authorisation failed
        boolean hasFailed = authorisationList.stream().anyMatch(authorisation -> authorisation.getAuthorizationStatus()
                .equals(ScaStatusEnum.FAILED.toString()));

        if (hasFailed) {
            return Optional.of(AuthorisationAggregateStatusEnum.REJECTED);
        }

        // Have all authorisations passed.
        boolean hasPassed = authorisationList.stream().allMatch(authorisation -> authorisation.getAuthorizationStatus()
                .equals(ScaStatusEnum.FINALISED.toString()));

        if (hasPassed) {
            return Optional.of(AuthorisationAggregateStatusEnum.FULLY_AUTHORISED);
        }

        // Has at least a single successful authorisation taken place.
        boolean partiallyPassed = authorisationList.stream()
                .anyMatch(authorisation -> authorisation.getAuthorizationStatus()
                        .equals(ScaStatusEnum.FINALISED.toString()));

        if (partiallyPassed) {
            return Optional.of(AuthorisationAggregateStatusEnum.PARTIALLY_AUTHORISED);
        }
        return Optional.empty();
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    AuthorisationStateChangeHook getAuthorisationStateChangeHook(String consentType) {

        return AuthorizationHandlerFactory.getAuthorisationStateChangeHook(consentType);
    }
}
