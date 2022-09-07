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

import React, {useContext, useEffect, useState} from "react";
import {Link} from "react-router-dom";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {Profile, SharingDetails} from "../detailedAgreementPage";
import "../css/Buttons.css";
import "../css/DetailedAgreement.css";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCaretSquareLeft} from "@fortawesome/free-solid-svg-icons";
import {lang, specConfigurations} from "../specConfigs/specConfigurations";
import {getDisplayName} from "../services";
import {getLogoURL} from "../services/utils";
import { ConsentContext } from "../context/ConsentContext";
import { SearchObjectContext } from "../context/SearchObjectContext";
import { UserContext } from "../context/UserContext";
import { AppInfoContext } from "../context/AppInfoContext";

export const DetailedAgreement = ({match}) => {
    const {allContextConsents,getContextConsentsForSearch} = useContext(ConsentContext);
    const {contextSearchObject} = useContext(SearchObjectContext);
    const {currentContextUser} = useContext(UserContext);
    const {contextAppInfo} = useContext(AppInfoContext)

    let searchObj = contextSearchObject;
    const currentUser = currentContextUser.user;
    const consents = allContextConsents.consents;
    const appInfo = contextAppInfo.appInfo;

    const [consentTypeKey, setConsentTypeKey] = useState(searchObj.consentTypes);

    const [consent, setConsent] = useState(() => {
        setConsentTypeKey(searchObj.consentTypes);
        let search = {
            ...searchObj,
            limit: 1,
            offset: 0,
            dateRange: "",
            consentIDs: match.params.id,
            userIDs: "",
            clientIDs: "",
            consentStatuses: "",
            consentTypes: ""
        }
        getContextConsentsForSearch(search,currentUser,appInfo);
        const matchedConsentId = match.params.id;
        let matchedConsent = consents.data.filter(
            (consent) => consent.consentId === matchedConsentId
        );
        return matchedConsent[0];
    });

    const [applicationName, setApplicationName] = useState(() => {
        return getDisplayName(appInfo, consent.clientId);
    });
    const [logoURL, setLogoURL] = useState(() => {
        return getLogoURL(appInfo, consent.clientId);
    });
    const [infoLabel, setInfoLabel] = useState(() => {
        const labels = lang[consentTypeKey].filter((lbl) =>
            lbl.id.split(",").some(x => x.toLowerCase() === consent.currentStatus.toLowerCase()));
        return getInfoLabel(labels[0], consent);
    });

    useEffect(() => {
        const matchedConsentId = match.params.id;
        let matchedConsent = consents.data.filter(
            (consent) => consent.consentId === matchedConsentId
        );
        setConsent(matchedConsent[0]);
    }, [consents]);

    useEffect(() => {
        const labels = lang[consentTypeKey].filter((lbl) =>
            lbl.id.split(",").some(x => x.toLowerCase() === consent.currentStatus.toLowerCase()));
        setInfoLabel(getInfoLabel(labels[0], consent));
    }, [consent]);

    function getInfoLabel(currentLabel, consent) {
        // check consent's mapping statuses are active
        const authResources = consent.authorizationResources;
        if (Array.isArray(authResources) && authResources.length) {
            // consent has more than one authorization resources
            const currentAuthResources = authResources.filter(ar => ar.userId === currentUser.email);
            if (Array.isArray(currentAuthResources) && currentAuthResources.length > 0) {
                const currentAuthResource = currentAuthResources[0];
                const mappings = consent.consentMappingResources
                    .filter(mapping => mapping.authorizationId === currentAuthResource.authorizationId);
                if (mappings.every(consentMapping => consentMapping.mappingStatus === "inactive")) {
                    // every consent mapping is inactive
                    return lang[consentTypeKey].filter((lbl) => lbl.id === specConfigurations.status.revoked.toLowerCase())[0];
                }
            }
        }
        return currentLabel;
    }

    return (
        <Container fluid className="body">
            <Row>
                <Link to="/consentmgr" id="detailedPageBackBtn">
                    <FontAwesomeIcon
                        className="pageBackBtn fa-2x"
                        icon={faCaretSquareLeft}
                    />
                    {/* Back */}
                </Link>
            </Row>

            <Row id="detailRow">
                <Col sm={4} id="profileCol">
                    <Profile consent={consent} infoLabel={infoLabel} appicationName={applicationName}
                             logoURL={logoURL} consentType={consentTypeKey}/>
                </Col>
                <Col id="consentDetailCol">
                    <SharingDetails consent={consent} infoLabels={infoLabel} appicationName={applicationName}
                                    consentType={consentTypeKey}/>
                </Col>
            </Row>
        </Container>
    );
};
