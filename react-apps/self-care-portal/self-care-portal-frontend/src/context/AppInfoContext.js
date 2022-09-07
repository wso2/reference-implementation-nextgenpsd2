/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

import React, { createContext, useContext, useState } from 'react';
import { getApplicationInfo } from '../api';
import { UserContext } from './UserContext';

export const AppInfoContext = createContext();

const AppInfoContextProvider = (props) => {
    const [contextAppInfo , setAppInfo] = useState({
        isGetRequestLoading:false,
        appInfo:[]
    });

    const {setResponseError} = useContext(UserContext)

    const setContextAppInfo = (payload)=> {
        setAppInfo((contextAppInfo)=>({
            ...contextAppInfo,
            appInfo:payload
        }))
    }

    const setContextAppInfoRequestLoadingStatus = (payload) => {
        setAppInfo((contextAppInfo)=>({
            ...contextAppInfo,
            isGetRequestLoading:payload
        }))
    }
    
    const getContextAppInfo = () => {
        setContextAppInfoRequestLoadingStatus(true);
        getApplicationInfo()
            .then((response) => setContextAppInfo(response.data))
            .catch((error) => setResponseError(error.response.data))
            .finally(()=>setContextAppInfoRequestLoadingStatus(false));
    }

    const value = {
        contextAppInfo,
        getContextAppInfo
    }

    return (
        <AppInfoContext.Provider value = {value}>
            {props.children}
        </AppInfoContext.Provider>
    );
}
 
export default AppInfoContextProvider;
