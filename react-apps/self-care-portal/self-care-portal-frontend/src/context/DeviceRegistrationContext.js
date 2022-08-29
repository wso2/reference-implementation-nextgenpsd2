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
import { getDeviceRegistrationData } from '../api';
import { UserContext } from './UserContext';

export const DeviceRegistrationContext = createContext();

const DeviceRegistrationContextProvider = (props) => {
    const [deviceRegistrationContextData,setDeviceRegistrationContextData] = useState({
        deviceRegistrationData: ""
    });

    const {setResponseError} = useContext(UserContext)
    

    const setDeviceRegistrationContextInfo = (payload)=>{
        setDeviceRegistrationContextData((prevState)=>({
            ...prevState,
            appInfo:payload
        }))
    }

    const getDeviceRegistrationContextInfo = (accessToken) => {
        getDeviceRegistrationData(accessToken)
            .then((response)=>{
                setDeviceRegistrationContextInfo(response.data)
            })
            .catch((error) => {
                setResponseError(error.response.data)
            })
    }

    const value = {
        deviceRegistrationContextData,
        getDeviceRegistrationContextInfo
    }
    return (
        <DeviceRegistrationContext.Provider value = {value}>
            {props.children}
        </DeviceRegistrationContext.Provider>
    );
}
 
export default DeviceRegistrationContextProvider;