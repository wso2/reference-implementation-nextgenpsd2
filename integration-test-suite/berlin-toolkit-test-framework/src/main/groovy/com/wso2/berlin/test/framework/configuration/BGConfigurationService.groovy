/*
Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework.configuration

import com.wso2.openbanking.test.framework.configuration.OBConfigParser
import com.wso2.openbanking.test.framework.configuration.OBConfigurationService

/**
 * Class for provide configuration data to the BG layers and BG tests
 * This class provide OB configuration and BG configuration.
 */
class BGConfigurationService extends OBConfigurationService {


    /**
     * Get application key store alias
     */
    static Object getApplicationKeystoreAlias() {

        return OBConfigParser.getInstance().getConfigurationMap()
                .get("ApplicationConfigList.AppConfig.Application.KeyStore.Alias");
    }

}
