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

import React, {useContext, useEffect} from "react";
import {Nav, Footer} from "../common";
import {Body} from "../landing_page";
import {Switch, Route} from "react-router-dom";
import {DetailedAgreement, WithdrawStep1, WithdrawStep2, ProtectedWithdrawRoute} from "../detailedAgreementPage";
import {FourOhFourError} from '../errorPage'
import {BrowserRouter as Router} from "react-router-dom";
import {ResponseError} from "../errorPage";
import { ConsentContext } from "../context/ConsentContext";
import { AppInfoContext } from "../context/AppInfoContext";
import { UserContext } from "../context/UserContext";

export const Home = (user) => {
    const {allContextConsents,getContextConsents} = useContext(ConsentContext);
    const {getContextAppInfo} = useContext(AppInfoContext);
    const {currentContextUser} = useContext(UserContext);

    const consents = allContextConsents.consents;
    const error = currentContextUser.error;

    // Default consent type to view : accounts
    // We are only supporting the account consents in SCP.
    const consentTypes = "accounts";

    useEffect(() => {
        getContextConsents(user,consentTypes);
    }, [user]);

    useEffect(() => {
        if (consents.length !== 0) {
            getContextAppInfo();

        }
    }, [consents]);

    if (error) {
        // errors present, rendering error page
        return <ResponseError error={error}/>
    }

    return (
        <div className="home">
            {consents.length === 0 ? (
                <div className="loaderBackground">
                    <div className="loader"></div>
                </div>
            ) : (
                <Router>
                    <Nav {...user} />
                    <Switch>
                        <Route path="/consentmgr" exact component={Body}/>
                        <Route path="/consentmgr/:id" exact component={DetailedAgreement}/>
                        <Route path="/consentmgr/:id/withdrawal-step-1" exact component={WithdrawStep1}/>
                        <ProtectedWithdrawRoute path="/consentmgr/:id/withdrawal-step-2" exact
                                                component={WithdrawStep2}/>
                        <Route path="*">
                            <FourOhFourError/>
                        </Route>
                    </Switch>
                    <Footer/>
                </Router>
            )}
        </div>
    );
};
