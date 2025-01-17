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

package com.wso2.openbanking.berlin.common.utils

import com.fasterxml.uuid.Generators
import com.wso2.openbanking.test.framework.util.ConfigParser

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
