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
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Funds Confirmations Consent data persistence for Authorize.
 */
public class FundsConfirmationsConsentPersistHandler implements ConsentPersistHandler {

    private ConsentCoreServiceImpl consentCoreService;

    public FundsConfirmationsConsentPersistHandler(ConsentCoreServiceImpl consentCoreService) {

        this.consentCoreService = consentCoreService;
    }

    @Override
    public void consentPersist(ConsentPersistData consentPersistData, ConsentResource consentResource)
            throws ConsentManagementException {

        String authorisationId = consentPersistData.getConsentData().getAuthResource().getAuthorizationID();
        boolean isApproved = consentPersistData.getApproval();
        String userId = consentPersistData.getConsentData().getUserId();

        String authStatus;
        if (isApproved) {
            authStatus = ScaStatusEnum.FINALISED.toString();
        } else {
            authStatus = ScaStatusEnum.FAILED.toString();
        }

        Map<String, Object> metaDataMap = consentPersistData.getConsentData().getMetaDataMap();
        JSONObject accountRefObject = (JSONObject) metaDataMap.get(ConsentExtensionConstants.ACCOUNT_REF_OBJECT);

        // Adding default permission since a funds confirmation consent doesn't have any permissions
        Map<String, ArrayList<String>> accountIdMapWithPermissions = new HashMap<>();
        ArrayList<String> permissionDefault = new ArrayList<>();
        permissionDefault.add(ConsentExtensionConstants.DEFAULT_PERMISSION);
        String accountReference = ConsentExtensionUtil.getAccountReferenceToPersist(accountRefObject);
        accountIdMapWithPermissions.put(accountReference, permissionDefault);

        ConsentPersistHandlerService consentPersistHandlerService =
                new ConsentPersistHandlerService(consentCoreService);
        consentPersistHandlerService.persistAuthorisation(consentResource, accountIdMapWithPermissions,
                authorisationId, userId, authStatus, isApproved);
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentPersistData consentPersistData) {
        return null;
    }
}
