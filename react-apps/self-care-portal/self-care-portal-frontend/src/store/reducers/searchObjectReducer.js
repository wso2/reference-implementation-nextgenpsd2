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


import {SET_SEARCH_OBJECT, SET_SEARCH_ON_CLICK} from "../actions/action-types";
import {CONFIG} from "../../config";
import {specConfigurations} from "../../specConfigs";

/**
 * Search object reducer
 *
 */
const initialState = {
    limit: JSON.parse(window.localStorage.getItem("postsPerPage")) || CONFIG.NUMBER_OF_CONSENTS,
    offset: 0,
    dateRange: "",
    consentIDs: "",
    userIDs: "",
    clientIDs: "",
    consentStatuses: specConfigurations.status.authorised,
    consentTypes: "accounts", // Accelerator only supporting the account consents type in SCP.
    hideAdvanceSearchOptions: true
};

export const searchObjectReducer = (state = initialState, action) => {
    switch (action.type) {
        case SET_SEARCH_OBJECT:
            return action.payload;
        default:
            return state;
    }
};

/**
 * Search on click state reducer
 *
 */
const searchUtilState = {
    searchOnClick: true
};

export const searchUtilStateReducer = (state = searchUtilState, action) => {
    switch (action.type) {
        case SET_SEARCH_ON_CLICK:
            return action.payload;
        default:
            return state;
    }
};
