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

import {SET_CONSENTS, SET_CONSENTS_METADATA, SET_CONSENTS_REQUEST_LOADING_STATUS} from "../actions/action-types";
import {CONFIG} from "../../config";

/**
 * Initial reducer state.
 *
 */
const initialState = {
    isGetRequestLoading: false,
    consents: [],
    metadata: {
        total: 0,
        count: 0,
    }
};

export const consentReducer = (state = initialState, action) => {

    switch (action.type) {
        case SET_CONSENTS:
            return {
                ...state,
                consents: action.payload
            };

        case SET_CONSENTS_REQUEST_LOADING_STATUS:
            return {
                ...state,
                isGetRequestLoading: action.payload
            };

        case SET_CONSENTS_METADATA:
            return {
                ...state,
                metadata: {
                    total: action.payload.metadata.total,
                    count: action.payload.metadata.count,
                }
            };

        default:
            return state;
    }
};
