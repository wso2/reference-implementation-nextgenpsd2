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
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import net.minidev.json.JSONObject;

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
            authStatus = ScaStatusEnum.PSU_AUTHENTICATED.toString();
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
