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