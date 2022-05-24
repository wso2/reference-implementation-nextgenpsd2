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

import React, {useEffect, useState} from "react";
import "../css/Nav.css";
import wso2Logo from "../images/wso2Logo.png";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Container from "react-bootstrap/Col";
import userAvatar from "../images/userAvatar.png";
import Image from "react-bootstrap/Image";
import NavDropdown from "react-bootstrap/NavDropdown";
import {Link, useLocation} from "react-router-dom";
import {logout} from "../login/logout";
import {QRButton} from "../landing_page/Popup";
import Popup from 'reactjs-popup';
import Tabs from "react-bootstrap/Tabs";
import {consentTypes, lang} from "../specConfigs";
import Tab from "react-bootstrap/Tab";
import {useDispatch, useSelector} from "react-redux";
import {getConsentsForSearch, setSearchObject} from "../store/actions";

export const Nav = (user) => {

    const location = useLocation()
    const dispatch = useDispatch();
    let searchObj = useSelector(state => state.searchObject);
    const appInfo = useSelector((state) => state.appInfo.appInfo);
    const [consentTypeKey, setConsentTypeKey] = useState(searchObj.consentTypes);
    const [showConsentTypeToggle, setShowConsentTypeToggle] = useState(true);
    const currentUser = useSelector(state => state.currentUser.user);

    useEffect(() => {
        let search = {
            ...searchObj,
            dateRange: "",
            consentIDs: "",
            userIDs: "",
            clientIDs: "",
            consentTypes: consentTypeKey,
            consentStatuses: lang[consentTypeKey][0].id,
            offset: 0
        }
        dispatch(setSearchObject(search));
        dispatch(getConsentsForSearch(search, currentUser, appInfo));
    }, [consentTypeKey])

    const handleLogout = () => {
        logout(user.idToken)
    };

    const showQR = () => {
        return (<div> Show QR </div>)
    }

    useEffect(() => {
        if (location.pathname === "/consentmgr") {
            setShowConsentTypeToggle(true)
        } else {
            setShowConsentTypeToggle(false)
        }
    }, [location.pathname])
    return (
        <Container className="nv">
            <Row className="Navbar">
                <Col>
                    <Link to="/consentmgr/" id="navLinkStyle">
                        {
                            <Col className="branding">
                                <img
                                    alt="Logo"
                                    src={wso2Logo}
                                    className="d-inline-block align-top navLogoImage"
                                />
                                <span className="navAppName"> Consent Manager </span>
                            </Col>
                        }
                    </Link>
                </Col>
                <Col>
                    {showConsentTypeToggle && <Tabs activeKey={consentTypeKey} onSelect={(k) => setConsentTypeKey(k)}>
                        {consentTypes.map(({label, id}) => (
                            <Tab key={id} eventKey={id} title={label}>
                            </Tab>
                        ))}
                    </Tabs>}

                </Col>
                <Col className="NavDropdown">
                    <NavDropdown
                        id="dropdown-custom-components"
                        title={
                            <span>
                <Image
                    src={userAvatar}
                    alt="User Avatar"
                    className="navUserImage"
                    rounded
                />
                <span className="dropdown-userId">{user.email}</span>
              </span>
                        }
                    >
                        <NavDropdown.Item id="dropdown-menu-items">
                            <Popup
                                modal
                                overlayStyle={{background: "rgba(255,255,255,0.98"}}
                                closeOnDocumentClick={true}
                                trigger={showQR}
                            >
                                <QRButton/>
                            </Popup>
                        </NavDropdown.Item>
                        <NavDropdown.Item onClick={handleLogout} id="dropdown-menu-items">
                            Log out
                        </NavDropdown.Item>
                    </NavDropdown>
                </Col>
            </Row>
        </Container>
    );
};
