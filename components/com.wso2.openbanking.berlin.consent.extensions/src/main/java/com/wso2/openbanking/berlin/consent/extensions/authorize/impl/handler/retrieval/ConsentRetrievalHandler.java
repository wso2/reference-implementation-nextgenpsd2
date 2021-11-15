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

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.handler.retrieval;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import net.minidev.json.JSONArray;

import java.util.Map;

/**
 * Interface to handle Consent data retrieval for Authorize.
 */
public interface ConsentRetrievalHandler {

    /**
     * Abstract method defined to retrieve the consent related data in the authorization flow to send them to the
     * consent page to get PSU consent.
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved from database
     * @return
     * @throws ConsentException
     */
    JSONArray getConsentDataSet(ConsentResource consentResource) throws ConsentException;

    /**
     * Abstract method defined to validate the authorization status.
     *
     * @param consentResource
     * @param authType
     * @return
     */
    boolean validateAuthorizationStatus(ConsentResource consentResource, String authType);

    /**
     * Abstract method defined to populate data to publish for data reporting in consent retrieval.
     *
     * @param consentData   ConsentData
     * @return Data map containing values to publish
     */
    Map<String, Object> populateReportingData(ConsentData consentData);
}
