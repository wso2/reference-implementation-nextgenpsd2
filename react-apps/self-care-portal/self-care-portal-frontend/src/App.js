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

import React from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import "./css/App.css";
import { Login } from "./login/login";
import { ResponseError } from "./errorPage";
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";

export const App = () => {
  return (
    <Router>
      <Switch>
        <Route path="/consentmgr" exact component={Login} />
        <Route path="/consentmgr/error" exact component={ResponseError} />
      </Switch>
    </Router>
  );
};
