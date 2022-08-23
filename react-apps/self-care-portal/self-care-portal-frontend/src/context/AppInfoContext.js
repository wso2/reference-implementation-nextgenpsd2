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