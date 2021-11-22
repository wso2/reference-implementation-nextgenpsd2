/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.common.utils

import com.fasterxml.uuid.Generators
import com.wso2.openbanking.test.framework.util.ConfigParser
import com.wso2.openbanking.test.framework.util.TestConstants

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Util class for Berlin testing.
 */
class BerlinTestUtil {

    static String solutionVersion = ConfigParser.getInstance().getSolutionVersion()

    /**
     *
     * @param codeURL URL which the code is extracted from
     * @return extracted code
     */
    static String getCodeFromURL(String codeURL) {

        if (codeURL.contains("#")) {
            return codeURL.split("#")[1].split("&")[1].substring(18)
        } else {
            return codeURL.split("\\?")[1].split("&")[0].substring(5)
        }
    }

    /**
     * Get ISO_8601 Standard date time
     * Eg: 2019-09-30T04:44:05.271Z
     *
     * @param addDays Add particular number of days to the datetime now
     * @return String value of the date time
     */
    static String getDateAndTime(int addDays){

        return LocalDateTime.now().plusDays(addDays).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    static String generateUUID(){
        UUID uuid = Generators.timeBasedGenerator().generate()
        return uuid.toString()
    }

    /**
     * Get the Error Description From the authorisation redirect url
     * @param reAuthUrl
     * @return errorDescription
     */
    static String getAuthFlowError(String authUrl) {

        def errorDescription = URLDecoder.decode(authUrl.split("&")[0].split("=")[1]
                .toString(), "UTF8")
        return errorDescription
    }
}
