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
import {Accreditation, ProfileMain, StatusLabel} from "../detailedAgreementPage";
import {AccreditationBG} from "../specConfigs/BG/componants/detailedAggrementPage/AccreditationBG";

import "../css/Profile.css";
import "../css/Buttons.css";
import {CONFIG} from "../config";
import {AccreditationDefault} from "../specConfigs/Default/componants/detailedAggrementPage/AccreditationDefault";

export const Profile = ({consent, infoLabel, appicationName, logoURL, consentType}) => {

    return (
        <>
            <div className="profileBody">
                <StatusLabel
                    consent={consent}
                    consentType={consentType}
                    infoLabel={infoLabel}
                />
                <ProfileMain consent={consent} infoLabel={infoLabel} appicationName={appicationName}
                             logoURL={logoURL} consentType={consentType}/>
                <hr className="horizontalLine"/>
                <div className="infoBox">
                    {
                        CONFIG.SPEC == 'Default' ? (
                            <AccreditationDefault infoLabel={infoLabel} accreditationNumber={appicationName}
                                                  applicationName={appicationName}/>
                        ) : CONFIG.SPEC == 'BG' ? (
                            <AccreditationBG infoLabel={infoLabel} applicationName={appicationName}/>
                        ) : (
                            <Accreditation infoLabel={infoLabel} accreditationNumber={appicationName}
                                           applicationName={appicationName}/>
                        )
                    }
                </div>
                <div className="infoBox">
                    <b>Other important information</b>
                    <p>
                        There may be additional important information not shown here. Please
                        check this sharing arrangement of <b>{appicationName}’s</b> website/app.
                    </p>
                </div>
            </div>
        </>
    );
};
