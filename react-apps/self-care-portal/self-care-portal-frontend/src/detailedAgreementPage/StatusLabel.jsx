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
import Badge from "react-bootstrap/Badge";
import {specConfigurations} from "../specConfigs/specConfigurations";
import "../css/StatusLabel.css";
import {isExpiredConsent} from "../services/utils";

export const StatusLabel = ({consent, consentType, infoLabel}) => {

    const [statusForLbl, setStatusForLbl] = useState("Valid");
    const [badge, setBadge] = useState("success");


    function defaultStatusLabel() {
        setBadge(infoLabel.labelBadgeVariant);
        setStatusForLbl(infoLabel.label);
    }

    useEffect(() => {
        try {
            if (isExpiredConsent(consent, consentType)) {
                setBadge("secondary");
                setStatusForLbl(specConfigurations.status.expired);
            } else {
                defaultStatusLabel();
            }
        } catch (e) {
            defaultStatusLabel();
        }
    }, [infoLabel])


    return (
        <div className="statuslbl">
            <Badge className="badge" variant={badge}>
                {statusForLbl}
            </Badge>
        </div>
    );
};
