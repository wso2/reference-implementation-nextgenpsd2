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