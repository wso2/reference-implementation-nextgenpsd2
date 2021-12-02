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
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;

import java.util.Map;

/**
 * Interface to handle Consent data persistence for Authorize.
 */
public interface ConsentPersistHandler {

    /**
     * Abstract method defined to handle consent persistence based on the consent type.
     *
     * @param consentPersistData    Consent Persist Data Object
     * @param consentResource       Consent Resource Object
     * @throws ConsentManagementException
     */
    void consentPersist(ConsentPersistData consentPersistData, ConsentResource consentResource)
            throws ConsentManagementException;

    /**
     * Abstract method defined to populate data to publish for data reporting in consent persistence.
     *
     * @param consentPersistData   ConsentPersistData Object
     * @return Data map containing values to publish
     */
    Map<String, Object> populateReportingData(ConsentPersistData consentPersistData);
}
