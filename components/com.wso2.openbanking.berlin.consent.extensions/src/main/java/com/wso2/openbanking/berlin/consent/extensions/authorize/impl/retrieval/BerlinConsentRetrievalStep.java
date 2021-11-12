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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.retrieval;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentRetrievalStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.AuthErrorCode;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.authorize.factory.AuthorizationHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval.ConsentRetrievalHandler;
import com.wso2.openbanking.berlin.consent.extensions.authorize.utils.ConsentAuthUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Optional;

/**
 * Class to handle payment consent data retrieval for authorize.
 */
public class BerlinConsentRetrievalStep implements ConsentRetrievalStep {

    private static final Log log = LogFactory.getLog(BerlinConsentRetrievalStep.class);
    ConsentRetrievalHandler consentRetrievalHandler;

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject) throws ConsentException {

        if (!consentData.isRegulatory()) {
            return;
        }

        ConsentCoreServiceImpl coreService = new ConsentCoreServiceImpl();

        String scopeString = consentData.getScopeString();
        String consentId = ConsentAuthUtil.getConsentId(scopeString);

        if (StringUtils.isBlank(consentId)) {
            log.error(ErrorConstants.CONSENT_ID_SCOPE_MISSING_ERROR);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_SCOPE,
                            ErrorConstants.CONSENT_ID_SCOPE_MISSING_ERROR, consentData.getRedirectURI(),
                            consentData.getState()));
        }

        consentData.setConsentId(consentId);

        try {
            ConsentResource consentResource = coreService.getConsent(consentId, false);
            ConsentAuthUtil.validateConsentTypeWithId(consentResource.getConsentType(), scopeString,
                    consentData.getRedirectURI(), consentData.getState());

            consentData.setType(consentResource.getConsentType());

            if (log.isDebugEnabled()) {
                log.debug("Retrieve applicable authorization for the consent ID: " + consentId);
            }
            List<AuthorizationResource> authorizationsList = coreService.searchAuthorizations(consentId);
            Optional<AuthorizationResource> firstUnauthorizedAuthorization =
                    authorizationsList.stream()
                            .filter(authorization -> StringUtils.equals(ScaStatusEnum.RECEIVED.toString(),
                                    authorization.getAuthorizationStatus())).findFirst();

            if (firstUnauthorizedAuthorization.isPresent()) {
                if (firstUnauthorizedAuthorization.get().getUserID() != null) {

                    if (log.isDebugEnabled()) {
                        log.debug("Validating whether the logged in user matches with the user who initiated the " +
                                "consent of Id:" + consentId);
                    }
                    String initiationPsuId = firstUnauthorizedAuthorization.get().getUserID();
                    String loggedInUserId = consentData.getUserId();
                    String loggedInUserWithSuperTenant = ConsentExtensionUtil.appendSuperTenantDomain(loggedInUserId);

                    if (!StringUtils.equals(initiationPsuId, loggedInUserWithSuperTenant)) {
                        log.error(ErrorConstants.LOGGED_IN_USER_MISMATCH);
                        jsonObject.put(ConsentExtensionConstants.IS_ERROR, ErrorConstants.LOGGED_IN_USER_MISMATCH);
                    }
                }

                boolean isApplicableStatus = validateAuthorizationStatus(consentResource,
                        firstUnauthorizedAuthorization.get().getAuthorizationType());

                if (!isApplicableStatus) {
                    log.error("The consent of Id: " + consentId + " is not in an applicable status for " +
                                "authorization");
                    jsonObject.put(ConsentExtensionConstants.IS_ERROR, "The consent is not " +
                            "in an applicable status for authorization");
                    return;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Unauthenticated authorization not found for Consent Id %s", consentId));
                }
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ConsentAuthUtil.constructRedirectErrorJson(AuthErrorCode.INVALID_REQUEST,
                                "An unauthenticated authorization is not found for this consent",
                                consentData.getRedirectURI(), consentData.getState()));
            }

            consentData.setAuthResource(firstUnauthorizedAuthorization.get());
            consentData.setConsentResource(consentResource);

            JSONArray consentDataJSON = getConsentDataSet(consentResource);
            jsonObject.appendField(ConsentExtensionConstants.CONSENT_DATA, consentDataJSON);

        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_DATA_RETRIEVE_ERROR);
        }
    }

    /**
     * Method to retrieve consent related data from the initiation payload.
     * @param consentResource
     * @return
     * @throws ConsentException
     */
    public JSONArray getConsentDataSet(ConsentResource consentResource)
            throws ConsentException {

        String type = consentResource.getConsentType();
        consentRetrievalHandler = AuthorizationHandlerFactory.getConsentRetrievalHandler(type);
        return consentRetrievalHandler.getConsentDataSet(consentResource);
    }

    /**
     * Method to retrieve consent related data from the initiation payload.
     * @param consentResource
     * @return
     */
    public boolean validateAuthorizationStatus(ConsentResource consentResource, String authType) {

        consentRetrievalHandler =
                AuthorizationHandlerFactory.getConsentRetrievalHandler(consentResource.getConsentType());
        return consentRetrievalHandler.validateAuthorizationStatus(consentResource, authType);
    }
}
