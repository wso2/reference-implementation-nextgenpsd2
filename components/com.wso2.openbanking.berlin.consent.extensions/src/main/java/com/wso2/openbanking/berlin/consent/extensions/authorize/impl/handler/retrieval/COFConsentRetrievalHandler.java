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
 * Class to handle funds confirmations data retrieval for Authorize.
 */
public class COFConsentRetrievalHandler implements ConsentRetrievalHandler {

    @Override
    public JSONArray getConsentDataSet(ConsentResource consentResource) throws ConsentException {

        // todo: Implement for funds confirmations flow
        return null;
    }

    @Override
    public boolean validateAuthorizationStatus(ConsentResource consentResource, String authStatus) {
        return false;
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentData consentData) {
        return null;
    }
}
