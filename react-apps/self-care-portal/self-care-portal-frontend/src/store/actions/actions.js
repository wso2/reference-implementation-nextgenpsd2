/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

import {
    SET_CONSENTS,
    SET_CONSENTS_REQUEST_LOADING_STATUS,
    SET_APPINFO,
    SET_APPINFO_REQUEST_LOADING_STATUS,
    SET_USER,
    SET_RESPONSE_ERROR, SET_SEARCH_OBJECT, SET_CONSENTS_METADATA, SET_SEARCH_ON_CLICK,
    SET_DEVICE_REGISTRATION_INFO
} from "./action-types";
import {
    getConsentsFromAPI,
    getApplicationInfo,
    getConsentsFromAPIForSearch,
    getDeviceRegistrationData
} from "../../api";

export const setConsents = (consents) => {
    return {
        payload: consents,
        type: SET_CONSENTS
    };
};

export const setConsentsMetadata = (consentsMetadata) => {
    return {
        payload: consentsMetadata,
        type: SET_CONSENTS_METADATA
    };
};

export const setAppInfo = (appInfo) => {
    return {
        payload: appInfo,
        type: SET_APPINFO
    };
};

export const getConsents = (user, consentTypes) => {
    // Received dispatch method as argument
    return (dispatch) => {
        //set loadings to true
        dispatch(setConsentsGetRequestLoadingStatus(true));

        getConsentsFromAPI(user, consentTypes)
            .then((response) => {
                dispatch(setConsents(response.data))
                dispatch(setConsentsMetadata(response.data))
            })
            .catch((error) => {
                dispatch(setResponseError(error.response.data))
            })
            .finally(() => dispatch(setConsentsGetRequestLoadingStatus(false)));
    };
};

export const getConsentsForSearch = (searchObj, user, appInfo) => {
    // Received dispatch method as argument

    return (dispatch) => {
        //set loadings to true
        dispatch(setConsentsGetRequestLoadingStatus(true));
        getConsentsFromAPIForSearch(searchObj, user, appInfo)
            .then((response) => {
                dispatch(setConsents(response.data))
                dispatch(setConsentsMetadata(response.data))
            })
            .catch((error) => {
                /* Log the error */
            })
            .finally(() => dispatch(setConsentsGetRequestLoadingStatus(false)));
    };
};

export const getAppInfo = () => {
    // Received dispatch method as argument
    return (dispatch) => {
        //set loadings to true
        dispatch(setAppInfoGetRequestLoadingStatus(true));

        getApplicationInfo()
            .then((response) => dispatch(setAppInfo(response.data)))
            .catch((error) => {
                dispatch(setResponseError(error.response.data))
            })
            .finally(() => dispatch(setAppInfoGetRequestLoadingStatus(false)));
    };
};

export const setConsentsGetRequestLoadingStatus = (isLoading) => {

    return {
        payload: isLoading,
        type: SET_CONSENTS_REQUEST_LOADING_STATUS
    };
};

export const setAppInfoGetRequestLoadingStatus = (isLoading) => {

    return {
        payload: isLoading,
        type: SET_APPINFO_REQUEST_LOADING_STATUS
    };
};

export const setUser = (user) => {
    return {
        payload: user,
        type: SET_USER
    }
};

export const setResponseError = error => {
    return {
        payload: error,
        type: SET_RESPONSE_ERROR
    }
}

export const setSearchObject = (searchObject) => {
    return {
        payload: searchObject,
        type: SET_SEARCH_OBJECT
    };
};

export const setSearchUtilState = (state) => {
    return {
        payload: state,
        type: SET_SEARCH_ON_CLICK
    };
};

export const setDeviceRegistrationInfo = (deviceRegistrationInfo) => {
    return {
        payload: deviceRegistrationInfo,
        type: SET_DEVICE_REGISTRATION_INFO
    };
};

export const getDeviceRegistrationInfo = (accessToken) => {
    // Received dispatch method as argument
    return (dispatch) => {
        getDeviceRegistrationData(accessToken)
            .then((response) => {
                dispatch(setDeviceRegistrationInfo(response.data))
            })
            .catch((error) => {
                dispatch(setResponseError(error.response.data))
            })
    };
};
