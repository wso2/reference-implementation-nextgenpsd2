/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

import React, { useContext, useEffect, useState } from "react";
import { Home } from "../landing_page";
import { CONFIG } from "../config";
import { UserContext } from "../context/UserContext";
import User from "../data/User";

export const Login = () => {
  const {setContextUser} = useContext(UserContext)

  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setLoggedUser] = useState({});

  useEffect(() => {
    // this object contains user details
    let user = new User();

    setLoggedUser(user);
    setIsLoggedIn(user.isLogged);
    setIsLoading(false);

    setContextUser(user);
  },[]);

  const renderLoading = () => {
    return (
      <div className="loaderBackground">
          <div className="loader"></div>  
        </div>
    );
  };

  if (isLoading) {
    // rendering loading spinner
    return renderLoading();
  } else {
    return isLoggedIn ? <Home {...user} /> : (window.location.href =`${CONFIG.AUTHORIZE_ENDPOINT}`);
  }
};
