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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.authorize.common.AuthorisationStateChangeHook;
import com.wso2.openbanking.berlin.consent.extensions.authorize.enums.AuthorisationAggregateStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.authorize.factory.AuthorizationHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Consent persist handler service class for common service level persist handler methods.
 */
class ConsentPersistHandlerService {

    private static final Log log = LogFactory.getLog(ConsentPersistHandlerService.class);
    private static final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();

    /**
     * Method to perform authorisation persistence related functions.
     *
     * @param consentResource             current consent resource
     * @param accountIdMapWithPermissions account Ids with permissions map
     * @param authorisationId             current authorisation Id
     * @param psuId                       the logged in user
     * @param authStatus                  the auth status to be updated
     * @throws ConsentException thrown if an error occur while persisting authorisation
     */
    public static void persistAuthorisation(ConsentResource consentResource,
                                            Map<String, ArrayList<String>> accountIdMapWithPermissions,
                                            String authorisationId, String psuId, String authStatus)
            throws ConsentManagementException {

        AuthorizationResource currentAuthorisationResource =
                consentCoreService.getAuthorizationResource(authorisationId);

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
                    AuthorisationStateChangeHook stateChangeHook = AuthorizationHandlerFactory
                            .getAuthorisationStateChangeHook(consentResource.getConsentType());
                    String consentStatusToUpdate = stateChangeHook.onAuthorisationStateChange(consentId,
                            currentAuthorisationType, aggregatedStatus.get(), currentAuthorisationResource);
                    consentCoreService.bindUserAccountsToConsent(consentResource,
                            ConsentExtensionUtil.appendSuperTenantDomain(psuId), authorisationId,
                            accountIdMapWithPermissions, authStatus, consentStatusToUpdate);
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
     * Method to get the aggregated consent authorisation status based on multiple authorisation resources.
     *
     * @param authorisationList list of authorisations for the current consent. This doesn't contain the current
     *                          authorisation object
     * @return the enum of aggregated authorisation status
     */
    private static Optional<AuthorisationAggregateStatusEnum> computeAggregatedConsentStatus(List<AuthorizationResource>
                                                                                                 authorisationList) {

        // Has at least one authorisation failed
        boolean hasFailed = authorisationList.stream().anyMatch(authorisation -> authorisation.getAuthorizationStatus()
                .equals(ScaStatusEnum.FAILED.toString()));

        if (hasFailed) {
            return Optional.of(AuthorisationAggregateStatusEnum.REJECTED);
        }

        // Have all authorisations passed.
        boolean hasPassed = authorisationList.stream().allMatch(authorisation -> authorisation.getAuthorizationStatus()
                .equals(ScaStatusEnum.PSU_AUTHENTICATED.toString()));

        if (hasPassed) {
            return Optional.of(AuthorisationAggregateStatusEnum.FULLY_AUTHORISED);
        }

        // Has at least a single successful authorisation taken place.
        boolean partiallyPassed = authorisationList.stream()
                .anyMatch(authorisation -> authorisation.getAuthorizationStatus()
                        .equals(ScaStatusEnum.PSU_AUTHENTICATED.toString()));

        if (partiallyPassed) {
            return Optional.of(AuthorisationAggregateStatusEnum.PARTIALLY_AUTHORISED);
        }
        return Optional.empty();
    }

}
