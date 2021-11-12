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

package com.wso2.openbanking.berlin.consent.extensions.authservlet.util;

import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Util class for servlet extension that handles Berlin scenarios.
 */
public class AuthServletUtil {

    /**
     * Method to populate single payments data to be sent to consent page.
     *
     * @param httpServletRequest
     * @param dataSet
     * @return
     */
    public static Map<String, Object> populatePaymentsData(HttpServletRequest httpServletRequest, JSONObject dataSet) {

        Map<String, Object> returnMaps = new HashMap<>();

        JSONArray dataRequestedJsonArray = dataSet.getJSONArray(ConsentExtensionConstants.CONSENT_DATA);
        Map<String, List<String>> dataRequested = new LinkedHashMap<>();

        for (int requestedDataIndex = 0; requestedDataIndex < dataRequestedJsonArray.length(); requestedDataIndex++) {
            JSONObject dataObj = dataRequestedJsonArray.getJSONObject(requestedDataIndex);
            String title = dataObj.getString(ConsentExtensionConstants.TITLE);
            JSONArray dataArray = dataObj.getJSONArray(ConsentExtensionConstants.DATA_SIMPLE);
            ArrayList<String> listData = new ArrayList<>();

            for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                listData.add(dataArray.getString(dataIndex));
            }
            dataRequested.put(title, listData);
        }
        returnMaps.put(ConsentExtensionConstants.DATA_REQUESTED, dataRequested);
        httpServletRequest.setAttribute(ConsentExtensionConstants.CONSENT_TYPE,
                dataSet.getString(ConsentExtensionConstants.TYPE));

        return returnMaps;
    }

    public static Map<String, Object> populateAccountsData(HttpServletRequest httpServletRequest, JSONObject dataSet) {

        //todo: Implement for accounts flow
        return new HashMap<>();
    }

    public static Map<String, Object> populateFundsConfirmationData(HttpServletRequest httpServletRequest,
                                                                    JSONObject dataSet) {

        //todo: Implement for cof flow
        return new HashMap<>();
    }
}
