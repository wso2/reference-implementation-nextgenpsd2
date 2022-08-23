import React, { createContext, useState } from 'react'
import { CONFIG } from '../config';
import { specConfigurations } from '../specConfigs';

export const SearchObjectContext = createContext();

const SearchObjectContextProvider = (props) => {
    const [contextSearchObject,setContextSearchObject] = useState({
        limit: JSON.parse(window.localStorage.getItem("postsPerPage")) || CONFIG.NUMBER_OF_CONSENTS,
        offset: 0,
        dateRange: "",
        consentIDs: "",
        userIDs: "",
        clientIDs: "",
        consentStatuses: specConfigurations.status.authorised,
        consentTypes: "accounts", // Accelerator only supporting the account consents type in SCP.
        hideAdvanceSearchOptions: true
    });
    const [contextSearchUtilState,setContextSearchUtilState] = useState({
        searchOnClick:true
    });

    const value = {
        contextSearchObject,
        setContextSearchObject ,
        contextSearchUtilState,
        setContextSearchUtilState
    }
    
    return (
        <SearchObjectContext.Provider value = {value} > 
            {props.children}
        </SearchObjectContext.Provider>
    );
}
 
export default SearchObjectContextProvider;