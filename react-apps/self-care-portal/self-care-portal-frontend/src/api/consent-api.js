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

import axios from "axios";
import {CONFIG} from "../config";
import moment from "moment";
import User from "../data/User";
import Cookies from "js-cookie";
import {specConfigurations} from "../specConfigs/specConfigurations";

/**
 * Get the list of consents from the API.
 */
export const getConsentsFromAPI = (user, consentTypes) => {
    var adminUrl;
    var defaultUrl;

    var userId = (user.email.endsWith("@carbon.super") ? (user.email) : user.email + '@carbon.super');

    // Accelerator only supporting the account consents type in SCP.
    adminUrl = `http://localhost:3001?consentTypes=${consentTypes}`
    defaultUrl = `http://localhost:3001?consentTypes=${consentTypes}&userIDs=${userId}`

    var selectedUrl
    if (user.role === "customerCareOfficer") {
        selectedUrl = adminUrl;
    } else {
        selectedUrl = adminUrl
    }

    const requestConfig = {
        headers: {
            "Content-Type": "application/json",
            // "Authorization": "Bearer " + Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1),
        },
        method: "GET",
        url: `${selectedUrl}`,
    };
    return axios
        .request(requestConfig)
        .then((response) => {
            return Promise.resolve(response);
        })
        .catch((error) => {
            return Promise.reject(error);
        });
};


function getFromTimeFromSearchObject(dateRange) {
    if (dateRange.replace(/ /g, "") !== "") {
        let fromTime = dateRange.split("/")[0].replace(/ /g, "");
        return moment(fromTime, "DD-MMM-YYYY")
            .startOf("day")
            .unix();
    } else {
        return "";
    }
}

function getToTimeFromSearchObject(dateRange) {
    if (dateRange.replace(/ /g, "") !== "") {
        let toTime = dateRange.split("/")[1].replace(/ /g, "");
        return moment(toTime, "DD-MMM-YYYY")
            .endOf("day")
            .unix();
    } else {
        return "";
    }
}

function getClientIdsFromSoftwareProvider(softwareProvider, appInfo) {
    for (let clientId in appInfo.data) {
        if (appInfo.data.hasOwnProperty(clientId)) {
            let softwareClientName =
                appInfo.data[clientId].metadata[specConfigurations.application.displayNameAttribute];
            if (softwareProvider.toString().toLowerCase().trim() ===
                softwareClientName.toString().toLowerCase()) {
                return clientId;
            }
        }
    }
    return "*";
}

export const getConsentsFromAPIForSearch = (searchObj, user, appInfo) => {

    let currentUserEmail = (user.email.endsWith("@carbon.super") ? (user.email) : user.email + '@carbon.super');

    const serverURL = `http://localhost:3001`;
    let defaultUrl = `${serverURL}?`;
    let searchUrl
    let paramList = [
        "offset",
        "limit",
        "consentIDs",
        "userIDs",
        "clientIDs",
        "consentStatuses",
        "consentTypes"
    ];

    // Accelerator only supporting the account consents type in SCP.
    if (user.role === "customerCareOfficer") {
        searchUrl = defaultUrl;
    } else {
        searchUrl = defaultUrl ;
    }

    paramList.forEach(function (key, index) {
        if (searchObj.hasOwnProperty(key) && searchObj[key] !== "") {
            if (key === 'userIDs') {
                if (user.role === "customerCareOfficer") {
                    searchUrl = searchUrl + "&" + key + "=" + searchObj[key];
                } else {
                    searchUrl = searchUrl + "&" + key + "=" + currentUserEmail;
                }
            } else if (key === 'clientIDs') {
                searchUrl = searchUrl + "&" + key + "=" +
                    getClientIdsFromSoftwareProvider(searchObj[key], appInfo);
            } else {
                searchUrl = searchUrl + "&" + key + "=" + searchObj[key];
            }
        }
    });

    let fromTime = getFromTimeFromSearchObject(searchObj.dateRange);
    let toTime = getToTimeFromSearchObject(searchObj.dateRange);
    //Appending fromTime to search query
    if (fromTime !== "") {
        searchUrl = searchUrl + "&" + 'fromTime' + "=" + fromTime;
    }
    //Appending toTime to search query
    if (toTime !== "") {
        searchUrl = searchUrl + "&" + 'toTime' + "=" + toTime;
    }

    const requestConfig = {
        headers: {
            "Content-Type": "application/json",
            // "Authorization": "Bearer " + Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1),
        },
        method: "GET",
        url: `${searchUrl}`,
    };
    return axios
        .request(requestConfig)
        .then((response) => {
            // set pagination obj
            return Promise.resolve(response);
        })
        .catch((error) => {
            return Promise.reject(error);
        });
};

