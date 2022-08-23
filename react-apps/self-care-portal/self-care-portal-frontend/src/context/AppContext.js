import React from 'react'
import AppInfoContextProvider from './AppInfoContext';
import ConsentContextProvider from './ConsentContext';
import SearchObjectContextProvider from './SearchObjectContext';
import UserContextProvider from './UserContext';
import DeviceRegistrationContextProvider from './DeviceRegistrationContext';

const contextProviderArray = [DeviceRegistrationContextProvider,AppInfoContextProvider,ConsentContextProvider,SearchObjectContextProvider,UserContextProvider]


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