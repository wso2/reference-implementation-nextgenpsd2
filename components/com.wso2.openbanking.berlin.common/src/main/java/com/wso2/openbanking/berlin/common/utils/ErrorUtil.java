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

package com.wso2.openbanking.berlin.common.utils;

import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.common.models.TPPMessages;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
        tppMessage.setPath(path);
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
