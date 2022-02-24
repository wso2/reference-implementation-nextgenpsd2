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

import {permissionDataLanguage} from "../specConfigs/permissionDataLanguage";
import React, {useEffect, useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCaretDown, faCaretUp} from "@fortawesome/free-solid-svg-icons";
import {Accordion, Card, Col} from "react-bootstrap";

export const PermissionItem = ({permissionScope}) => {
    var id = 0;
    const [showDetailedPermissions, setShowDetailedPermissions] = useState(false);
    const [filteredDataLang, setFilteredDataLang] = useState({
        scope: permissionScope,
        dataCluster: permissionScope,
        permissions: [permissionScope],
    });

    useEffect(() => {
        for (let index = 0; index < permissionDataLanguage.length; index++) {
            const element = permissionDataLanguage[index];
            if (element.scope === permissionScope) {
                setFilteredDataLang(element);
            }
        }
    }, [permissionScope]);


    const toggle = () => setShowDetailedPermissions(!showDetailedPermissions);

    //must add  conditional statements for data clusters and permissions
    // when response is adjusted to receive the customer type (business, individual)

    return (
        <>
            <Accordion>
                <Card className="clusterContainer">
                    <Accordion.Toggle
                        className="clusterRow"
                        onClick={toggle}
                        as={Card.Header}
                        eventKey="0"
                    >
                        <Col className="clusterLabel">
                            <h6>{filteredDataLang.dataCluster}</h6>
                        </Col>
                        <Col className="arrow">
                            <FontAwesomeIcon
                                className="clusToggle fa-lg"
                                id="clusterToggle"
                                icon={showDetailedPermissions ? faCaretDown : faCaretUp}
                            />
                        </Col>
                    </Accordion.Toggle>
                    <Accordion.Collapse eventKey="0">
                        <Card.Body>
                            <ul className="permissionsUL">
                                {filteredDataLang.permissions.map((permission) => (
                                    <li key={id = id + 1}>{permission}</li>
                                ))}
                            </ul>
                        </Card.Body>
                    </Accordion.Collapse>
                </Card>
            </Accordion>
        </>
    );
};
