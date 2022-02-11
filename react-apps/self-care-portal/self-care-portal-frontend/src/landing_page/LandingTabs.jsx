/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

import React, {useEffect, useState} from "react";
import {LandingTable} from "./LandingTable";
import "../css/LandingTabs.css";
import Tab from "react-bootstrap/Tab";
import Tabs from "react-bootstrap/Tabs";
import {lang} from "../specConfigs";
import {getConsentsForSearch, setSearchObject} from "../store/actions";
import {useDispatch, useSelector} from "react-redux";
import {PaginationTable} from "./PaginationTable";

export const LandingTabs = () => {

    const dispatch = useDispatch();
    let searchObj = useSelector(state => state.searchObject);
    const appInfo = useSelector((state) => state.appInfo.appInfo);

    const [key, setKey] = useState(searchObj.consentStatuses);
    const currentUser = useSelector(state => state.currentUser.user);

    useEffect(() => {
        let search = {
            ...searchObj,
            consentStatuses: key,
            offset: 0
        }
        dispatch(setSearchObject(search));
        dispatch(getConsentsForSearch(search, currentUser, appInfo));
    }, [key])

    return (
        <div>
            <Tabs id="status-tab" activeKey={key} onSelect={(k) => setKey(k)}>
                {lang.map(({label, id, description}) => (
                    <Tab key={id} eventKey={id} title={label}>
                        <LandingTable status={id} description={description} currentTab={{key}}/>
                    </Tab>
                ))}
            </Tabs>
            <PaginationTable
                currentTab={key}
            />
        </div>
    );
};
