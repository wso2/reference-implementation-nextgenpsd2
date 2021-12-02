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
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle Payment Consent data persistence for Authorize.
 */
public class PaymentConsentPersistHandler implements ConsentPersistHandler {

    private ConsentCoreServiceImpl consentCoreService;
    private static final Log log = LogFactory.getLog(PaymentConsentPersistHandler.class);

    public PaymentConsentPersistHandler(ConsentCoreServiceImpl consentCoreService) {

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

        try {
            JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                    .parse(consentPersistData.getConsentData().getConsentResource().getReceipt());
            JSONObject debtorAccountElement = (JSONObject) receiptJSON.get(ConsentExtensionConstants.DEBTOR_ACCOUNT);
            String configuredAccountReference = CommonConfigParser.getInstance().getAccountReferenceType();
            String debtorAccountReference = debtorAccountElement.getAsString(configuredAccountReference);

            // Adding default permission since a payment consent doesn't have any permissions
            Map<String, ArrayList<String>> accountIdMapWithPermissions = new HashMap<>();
            ArrayList<String> permissionDefault = new ArrayList<>();
            permissionDefault.add(ConsentExtensionConstants.DEFAULT_PERMISSION);
            accountIdMapWithPermissions.put(debtorAccountReference, permissionDefault);

            ConsentPersistHandlerService consentPersistHandlerService =
                    new ConsentPersistHandlerService(consentCoreService);
            consentPersistHandlerService.persistAuthorisation(consentResource, accountIdMapWithPermissions,
                    authorisationId, userId, authStatus);
        } catch (ParseException e) {
            log.error(ErrorConstants.CONSENT_PERSIST_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.CONSENT_PERSIST_ERROR);
        }
    }

    @Override
    public Map<String, Object> populateReportingData(ConsentPersistData consentPersistData) {
        return null;
    }
}
