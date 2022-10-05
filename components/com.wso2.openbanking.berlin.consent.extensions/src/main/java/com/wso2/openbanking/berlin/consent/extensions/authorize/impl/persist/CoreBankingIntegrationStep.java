/**
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.berlin.consent.extensions.authorize.impl.persist;

import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;

/**
 * Extension to implement core banking integration logic after the consent authorization.
 * Refer the documentation https://ob.docs.wso2.com/en/latest/develop/consent-management-authorize/#persist for more
 * info on persist step implementation.
 */
public class CoreBankingIntegrationStep implements ConsentPersistStep {

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

    }
}
