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

package org.wso2.openbanking.berlin.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.consent.extensions.authorize.factory.AuthorizationHandlerFactory;
import org.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.ConsentPersistHandler;

/**
 * Consent persist step implementation.
 */
public class BerlinConsentPersistStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(BerlinConsentPersistStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        try {
            ConsentData consentData = consentPersistData.getConsentData();
            ConsentResource consentResource;

            if (consentData.getConsentId() == null) {
                log.error(ErrorConstants.CONSENT_ID_NOT_FOUND_ERROR);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.CONSENT_ID_NOT_FOUND_ERROR);
            }

            ConsentCoreServiceImpl consentCoreService = getConsentService();

            //Retrieve consent details from database if not exists
            if (consentData.getConsentResource() == null) {
                consentResource = consentCoreService.getConsent(consentData.getConsentId(), false);
            } else {
                consentResource = consentData.getConsentResource();
            }

            if (consentData.getAuthResource() == null) {
                log.error(ErrorConstants.AUTHORISATION_RESOURCE_NOT_FOUND_ERROR);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.AUTHORISATION_RESOURCE_NOT_FOUND_ERROR);
            }

            //Bind the user and accounts with the consent
            String type = consentResource.getConsentType();
            ConsentPersistHandler consentPersistHandler = getConsentPersistHandler(type);

            consentPersistHandler.consentPersist(consentPersistData, consentResource);
        } catch (ConsentManagementException e) {
            log.error(ErrorConstants.CONSENT_DATA_RETRIEVE_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.CONSENT_DATA_RETRIEVE_ERROR);
        }
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentPersistHandler getConsentPersistHandler(String type) {

        return AuthorizationHandlerFactory.getConsentPersistHandler(type);
    }
}
