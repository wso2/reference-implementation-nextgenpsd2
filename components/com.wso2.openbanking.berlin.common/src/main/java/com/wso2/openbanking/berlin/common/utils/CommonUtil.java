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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common util class.
 */
public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    /**
     * To get a list of ScaMethod.
     *
     * @return a list of configured SCA methods after mapping it to the ScaMethod class.
     */
    public static List<ScaMethod> getSupportedScaMethods() {
        List<ScaMethod> supportedScaMethods = new ArrayList<>();

        for (Map<String, String> supportedScaMethod : CommonConfigParser.getInstance().getSupportedScaMethods()) {
            ScaMethod scaMethod = new ScaMethod();

            scaMethod.setAuthenticationType(supportedScaMethod.get(CommonConstants.SCA_TYPE));
            scaMethod.setVersion(supportedScaMethod.get(CommonConstants.SCA_VERSION));
            scaMethod.setAuthenticationMethodId(supportedScaMethod.get(CommonConstants.SCA_ID));
            scaMethod.setName(supportedScaMethod.get(CommonConstants.SCA_NAME));
            scaMethod.setMappedApproach(ScaApproachEnum.fromValue(supportedScaMethod
                    .get(CommonConstants.SCA_MAPPED_APPROACH)));
            scaMethod.setDescription(supportedScaMethod.get(CommonConstants.SCA_DESCRIPTION));
            scaMethod.setDefault(Boolean.parseBoolean(supportedScaMethod.get(CommonConstants.SCA_DEFAULT)));

            supportedScaMethods.add(scaMethod);
        }

        return supportedScaMethods;
    }

    /**
     * To get a list of ScaApproach.
     *
     * @return a list of configured SCA approaches after mapping it to the ScaApproach class.
     */
    public static List<ScaApproach> getSupportedScaApproaches() {
        List<ScaApproach> supportedScaApproaches = new ArrayList<>();

        for (Map<String, String> supportedScaApproach : CommonConfigParser.getInstance().getSupportedScaApproaches()) {
            ScaApproach scaApproach = new ScaApproach();

            scaApproach.setApproach(ScaApproachEnum.fromValue(supportedScaApproach.get(CommonConstants.SCA_NAME)));
            scaApproach.setDefault(Boolean.parseBoolean(supportedScaApproach.get(CommonConstants.SCA_DEFAULT)));

            supportedScaApproaches.add(scaApproach);
        }

        return supportedScaApproaches;
    }

    /**
     * Determines the current SCA approach and the SCA methods based on the parameters.
     *
     * @param isTppRedirectPreferred TPP-Redirect-Preferred header
     * @param isScaRequired          to know if SCA is required or not
     * @return returns the current SCA approach and methods
     */
    public static Map<String, Object> getScaApproachAndMethods(Boolean isTppRedirectPreferred, boolean isScaRequired) {
        List<ScaMethod> supportedScaMethods = getSupportedScaMethods();

        ScaApproach currentScaApproach = new ScaApproach();
        List<ScaMethod> currentScaMethods = new ArrayList<>();

        if (Boolean.TRUE.equals(isTppRedirectPreferred)) {
            currentScaApproach = getScaApproach(ScaApproachEnum.REDIRECT);
            if (isScaRequired) {
                currentScaMethods.add(getScaMethod(ScaApproachEnum.REDIRECT));
            }
        } else if (Boolean.FALSE.equals(isTppRedirectPreferred)) {
            currentScaApproach = getScaApproach(ScaApproachEnum.DECOUPLED);
            if (isScaRequired) {
                currentScaMethods.add(getScaMethod(ScaApproachEnum.DECOUPLED));
            }
        } else {
            // When TPP-Redirect-Preferred header is not sent
            if (isScaRequired) {
                if (supportedScaMethods.size() == 1) {
                    // If SCA is required and there is only a single supported SCA method, it becomes the selected
                    // SCA method and the mapped approach of the SCA method becomes the SCA approach
                    currentScaApproach = getScaApproach(supportedScaMethods.get(0).getMappedApproach());
                    currentScaMethods = supportedScaMethods;
                } else {
                    // If a default SCA method is configured then that becomes the SCA approach and method
                    ScaMethod defaultScaMethod = getDefaultScaMethod();

                    if (defaultScaMethod != null) {
                        currentScaApproach = getDefaultScaApproach();
                        currentScaMethods.add(defaultScaMethod);
                    } else {
                        // Approach not finalised and the TPP/PSU will be given the choice to select
                        currentScaMethods = supportedScaMethods;
                    }
                }
            } else {
                currentScaApproach = getDefaultScaApproach();
            }
        }

        Map<String, Object> scaApproachAndMethods = new HashMap<>();
        scaApproachAndMethods.put(CommonConstants.SCA_APPROACH_KEY, currentScaApproach);
        scaApproachAndMethods.put(CommonConstants.SCA_METHODS_KEY, currentScaMethods);

        return scaApproachAndMethods;
    }

    /**
     * Gets the SCA approach from the list of approaches.
     *
     * @param scaApproachEnum the SCA approach to find
     * @return found SCA approach
     */
    public static ScaApproach getScaApproach(ScaApproachEnum scaApproachEnum) {
        List<ScaApproach> scaApproaches = getSupportedScaApproaches();

        for (ScaApproach scaApproach : scaApproaches) {
            if (scaApproachEnum.equals(scaApproach.getApproach())) {
                return scaApproach;
            }
        }

        return null;
    }

    /**
     * Gets the SCA method for the specified approach from the list of methods.
     *
     * @param scaApproachEnum the SCA method to find
     * @return found SCA method
     */
    public static ScaMethod getScaMethod(ScaApproachEnum scaApproachEnum) {
        List<ScaMethod> scaMethods = getSupportedScaMethods();

        for (ScaMethod scaMethod : scaMethods) {
            if (scaApproachEnum.equals(scaMethod.getMappedApproach())) {
                return scaMethod;
            }
        }

        return null;
    }

    /**
     * Gets the configured default SCA approach.
     *
     * @return default SCA approach
     */
    public static ScaApproach getDefaultScaApproach() {
        List<ScaApproach> scaApproaches = getSupportedScaApproaches();

        for (ScaApproach scaApproach : scaApproaches) {
            if (scaApproach.isDefault()) {
                return scaApproach;
            }
        }

        return null;
    }

    /**
     * Gets the configured default SCA method.
     *
     * @return default SCA method
     */
    public static ScaMethod getDefaultScaMethod() {
        List<ScaMethod> scaMethods = getSupportedScaMethods();

        for (ScaMethod scaMethod : scaMethods) {
            if (scaMethod.isDefault()) {
                return scaMethod;
            }
        }

        return null;
    }

    /**
     * Used to convert any object to a json object.
     *
     * @param object any object that needs to be converted to JSON
     * @return JSONObject
     */
    public static JSONObject convertObjectToJson(Object object) {

        // Create object mapper
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(object);
            JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            return (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException | JsonProcessingException e) {
            log.error("Error while constructing the JSON Object", e);
        }
        return new JSONObject();
    }

}
