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
