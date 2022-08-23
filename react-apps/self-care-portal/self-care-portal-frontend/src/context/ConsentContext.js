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