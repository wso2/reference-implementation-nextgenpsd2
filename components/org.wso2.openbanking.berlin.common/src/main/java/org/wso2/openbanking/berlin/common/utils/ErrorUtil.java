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

package org.wso2.openbanking.berlin.common.utils;

import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.models.TPPMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for handling and constructing errors related to this toolkit.
 */
public class ErrorUtil {

    private static final Log log = LogFactory.getLog(ErrorUtil.class);

    /**
     * Used to construct a Berlin specific error.
     *
     * @param path path which the error might have occurred
     * @param category error category, ERROR or WARN
     * @param code error code
     * @param text error text
     * @return an error constructed as a json object
     */
    public static JSONObject constructBerlinError(String path, TPPMessage.CategoryEnum category,
                                                  TPPMessage.CodeEnum code, String text) {

        List<TPPMessage> tppMessagesList = new ArrayList();
        TPPMessages tppMessages = new TPPMessages();

        TPPMessage tppMessage = new TPPMessage();

        if (StringUtils.isNotBlank(path)) {
            tppMessage.setPath(path);
        }
        tppMessage.setCategory(category);
        tppMessage.setCode(code);
        tppMessage.setText(text);

        tppMessagesList.add(tppMessage);
        tppMessages.setTppMessages(tppMessagesList);

        return CommonUtil.convertObjectToJson(tppMessages);
    }

    /**
     * Used to construct error messages on demand. Essentially when need to maintain a list of errors to be
     * constructed later.
     *
     * @param path path which the error might have occurred
     * @param category error category, ERROR or WARN
     * @param code error code
     * @param text error text
     * @return a TPPMessage object to be stored in an error list
     */
    public static TPPMessage constructTPPMessage(String path, TPPMessage.CategoryEnum category,
                                                 TPPMessage.CodeEnum code, String text) {

        TPPMessage tppMessage = new TPPMessage();
        tppMessage.setPath(path);
        tppMessage.setCategory(category);
        tppMessage.setCode(code);
        tppMessage.setText(text);

        return tppMessage;
    }

    /**
     * Used to construct an error using a set of errors.
     *
     * @param tppErrorMessages a list of TPPMessage error objects
     * @return a set of errors constructed as a json object
     */
    public static JSONObject constructBerlinError(List<TPPMessage> tppErrorMessages) {

        TPPMessages tppMessages = new TPPMessages();
        tppMessages.setTppMessages(tppErrorMessages);

        return CommonUtil.convertObjectToJson(tppMessages);
    }
}
