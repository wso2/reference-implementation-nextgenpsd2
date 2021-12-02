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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.authorize.factory.AuthorizationHandlerFactory;
import com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.persist.ConsentPersistHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
