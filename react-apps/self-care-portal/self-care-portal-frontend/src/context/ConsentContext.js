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

import React, { useState, createContext, useContext } from 'react'
import { getConsentsFromAPI, getConsentsFromAPIForSearch } from '../api';
import { consentTypes } from '../specConfigs';
import {UserContext} from "./UserContext"

export const ConsentContext = createContext();

const ConsentContextProvider = (props) => {
    const [allContextConsents,setAllContextConsents] = useState({
        isGetRequestLoading: false,
        consents: [],
        metadata: {
            total: 0,
            count: 0,
        }
    });

    const {setResponseError} = useContext(UserContext);

    const setContextConsents= (payload) => {
        setAllContextConsents((allContextConsents)=>({
            ...allContextConsents,
            consents:payload
        }))
    };

    const setContextConsentsRequestLoadingStatus = (payload) => {
        setAllContextConsents((allContextConsents)=>({
            ...allContextConsents,
            isGetRequestLoading:payload
        }))
    };

    const setContextConsentsMetadata = (payload) => {
        setAllContextConsents((allContextConsents)=>({
            ...allContextConsents,
            metadata: {
                total: payload.metadata.total,
                count: payload.metadata.count
            }
        }))
    };

    const getContextConsents = (user,consentTypes) => {
        setContextConsentsRequestLoadingStatus(true);
        getConsentsFromAPI(user,consentTypes)
            .then((response)=>{
                setContextConsents(response.data)
                setContextConsentsMetadata(response.data)
            })
            .catch((error)=>{
                setResponseError(error.response.data)
            })
            .finally(()=>setContextConsentsRequestLoadingStatus(false))
    };

    const getContextConsentsForSearch = (searchObj,user,appInfo) => {
        setContextConsentsRequestLoadingStatus(true)
        getConsentsFromAPIForSearch(searchObj,user,appInfo)
            .then((response)=>{
                setContextConsents(response.data)
                setContextConsentsMetadata(response.data)
            })
            .catch((error)=>{
                /*Log the error */
            })
            .finally(()=>setContextConsentsRequestLoadingStatus(false))   
    };

    const value = {
        allContextConsents,
        setContextConsents,
        setContextConsentsRequestLoadingStatus,
        setContextConsentsMetadata, 
        getContextConsents,
        getContextConsentsForSearch
    }

    return (
        <ConsentContext.Provider value = {value}>
            {props.children}
        </ConsentContext.Provider>
    );
}
 
export default ConsentContextProvider; 