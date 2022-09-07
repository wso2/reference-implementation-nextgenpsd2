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

import React, {useEffect, useState, useContext} from "react";
import {LandingTable} from "./LandingTable";
import "../css/LandingTabs.css";
import Tab from "react-bootstrap/Tab";
import Tabs from "react-bootstrap/Tabs";
import {lang} from "../specConfigs";
import {PaginationTable} from "./PaginationTable";
import { SearchObjectContext } from "../context/SearchObjectContext";
import { ConsentContext } from "../context/ConsentContext";
import { AcroFormButton } from "jspdf";
import { AppInfoContext } from "../context/AppInfoContext";
import { UserContext } from "../context/UserContext";


export const LandingTabs = () => {
    const {contextSearchObject,setContextSearchObject} = useContext(SearchObjectContext);
    const {getContextConsentsForSearch} = useContext(ConsentContext);
    const {contextAppInfo} = useContext(AppInfoContext);
    const {currentContextUser} = useContext(UserContext);


    let searchObj = contextSearchObject;
    const appInfo = contextAppInfo.appInfo;


    const [key, setKey] = useState(searchObj.consentStatuses);
    const [consentTypeKey, setConsentTypeKey] = useState(searchObj.consentTypes);
    const currentUser = currentContextUser.user;

    const [filteredLang, setFilteredLang] = useState(lang[consentTypeKey]);

    useEffect(() => {
        setFilteredLang(lang[searchObj.consentTypes]);
        setKey(searchObj.consentStatuses)
        setConsentTypeKey(searchObj.consentTypes)
    }, [searchObj.consentTypes])

    useEffect(() => {
        let search = {
            ...searchObj,
            consentStatuses: key,
            offset: 0
        }
        setContextSearchObject(search);
        getContextConsentsForSearch(search,currentUser,appInfo);
    }, [key])

    return (
        <div>
            <Tabs id="status-tab" activeKey={key} onSelect={(k) => setKey(k)}>
                {filteredLang.map(({label, id, description}) => (
                    <Tab key={id} eventKey={id} title={label}>
                        <LandingTable status={id} description={description} currentTab={key}
                                      consentType={consentTypeKey}/>
                    </Tab>
                ))}
            </Tabs>
            <PaginationTable
                currentTab={key}
            />
        </div>
    );
};
