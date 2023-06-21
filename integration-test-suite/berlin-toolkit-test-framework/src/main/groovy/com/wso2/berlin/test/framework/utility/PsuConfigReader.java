/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.berlin.test.framework.utility;

import com.wso2.berlin.test.framework.configuration.AppConfigReader;
import com.wso2.berlin.test.framework.configuration.ConfigParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * PSU Configuration Reader Class.
 */
public class PsuConfigReader {

    private static final ConfigParser configParser = ConfigParser.getInstance();
    private static final Log log = LogFactory.getLog(AppConfigReader.class);
    public static Integer psuNumber;

    /**
     * Get the index of the relevant PSU from PSUList in test-config.xml.
     *
     * @return index of the corresponding PSU.
     */
    public static Integer getPsuNumber() {

        return psuNumber;
    }

    /**
     * Set the index of the corresponding PSU (Index of PSUInfo tag).
     * PSUInfo[0] - Common PSU
     * PSUInfo[1] - PSU with AISP role
     * PSUInfo[2] - PSU with PISP role
     * PSUInfo[3] - PSU with CBPII role
     *
     * @param psuNumber
     */
    public void setPsuNumber(Integer psuNumber) {

       PsuConfigReader.psuNumber = psuNumber;
    }

    /**
     * Get username of the the given PSU.
     *
     * @return clientid
     */
    public static String getPSU() {

        if (configParser.getPSU().getClass().toString().contains("java.lang.String")) {
            return configParser.getPSU().toString();

        } else {
            List<Object[]> listObj = (List<Object[]>) configParser.getPSU();

            if (getPsuNumber() == null) {
                return String.valueOf(listObj.get(0));
            } else {
                return String.valueOf(listObj.get(psuNumber));
            }
        }
    }
}
