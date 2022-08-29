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

import React, { useState, createContext } from 'react'

export const UserContext = createContext();

const UserContextProvider = (props) => {
    const [currentContextUser,setCurrentUser] = useState({
        user:{},
        error:""
    });

    const setContextUser = (userData) => {
        setCurrentUser((currentContextUser)=>({
            ...currentContextUser,
            user:userData
        }));
    };

    const setResponseError =(error) => {
        setCurrentUser((currentContextUser)=>({
            ...currentContextUser,
            error:error
        }));
    }

    const value = {
        currentContextUser, 
        setContextUser, 
        setResponseError
    }

    return (
        <UserContext.Provider value = {value}>
            {props.children}
        </UserContext.Provider>
    );
}
 
export default UserContextProvider;