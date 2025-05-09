package com.wso2.openbanking.berlin.extensions.utils;

import com.wso2.openbanking.berlin.extensions.model.ErrorResponse;
import com.wso2.openbanking.berlin.extensions.dataobjects.TPPMessage;
import com.wso2.openbanking.berlin.extensions.dataobjects.TPPMessages;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ErrorUtil {
    /**
     * Method to get ErrorResponse object for error
     * @param errorMessage
     * @param errorDescription
     * @return
     */
    public static ErrorResponse getErrorResponse(String errorMessage, String errorDescription) {
        return new ErrorResponse(ErrorResponse.StatusEnum.ERROR, getErrorDataObject(errorMessage, errorDescription));
    }

    /**
     * Method to construct the error data object.
     *
     * @param errorMessage Error Message
     * @param errorDescription Error Description
     * @return
     */
    public static JSONObject getErrorDataObject(String errorMessage, String errorDescription) {

        JSONObject data = new JSONObject();
        data.put("errorMessage", errorMessage);
        data.put("errorDescription", errorDescription);

        return data;
    }

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

        return CommonConsentValidationUtil.convertObjectToJson(tppMessages);
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

        return CommonConsentValidationUtil.convertObjectToJson(tppMessages);
    }
}
