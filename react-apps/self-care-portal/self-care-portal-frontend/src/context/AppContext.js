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

import React from 'react'
import AppInfoContextProvider from './AppInfoContext';
import ConsentContextProvider from './ConsentContext';
import SearchObjectContextProvider from './SearchObjectContext';
import UserContextProvider from './UserContext';
import DeviceRegistrationContextProvider from './DeviceRegistrationContext';

//Higher wrapper provider component should be placed after lower wrapper provider components.

const contextProviderArray = [
    DeviceRegistrationContextProvider,
    AppInfoContextProvider,
    ConsentContextProvider,
    SearchObjectContextProvider,
    UserContextProvider
]

const AppContextProvider = (props) => {
    return ( 
        contextProviderArray.reduce((Children,Provider)=>{
            return(
                <Provider>{Children}</Provider>
            );
        },props.children)
     );
}
 
export default AppContextProvider;
