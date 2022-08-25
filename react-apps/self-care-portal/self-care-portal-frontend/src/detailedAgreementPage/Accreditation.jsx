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

import React from "react";
import Logo from "../images/Logo.png";

export const Accreditation = ({infoLabel, accreditationNumber, applicationName}) => {
    return (
        <>
            <h6>{infoLabel.accreditation.accreditationLabel}</h6>
            <p>
                {applicationName} {infoLabel.accreditation.accreditWebsite} [
                <a href={infoLabel.accreditation.accreditWebsiteLink} target="_blank" rel="noreferrer">
                    {/* add website link */}
                    {infoLabel.accreditation.accreditWebsiteLinkText}
                </a>
                ]
            </p>
            <div className="accredBox">
                <img id="Logo" src={Logo} alt="Logo"/>
                <div className="accredInfo">
                    <h6>{applicationName}</h6>
                    {/* add accredited Data Recipient's number */}
                    <p>{infoLabel.accreditation.accreditDR} {accreditationNumber}</p>
                </div>
            </div>
        </>
    );
};
