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
import {specConfigurations} from "../../../specConfigurations";
import {PermissionItem} from "../../../../detailedAgreementPage";
import {permissionBindTypes} from "../../../common";
import {getPermissionListForConsent} from "../../../../services/utils";

let id = 0;
export const DataSharedInfoBG = ({consent, infoLabels}) => {

    let permissions = getPermissionListForConsent(consent);
    return (
        <div className="dataSharedBody">
            <h5>{infoLabels.dataSharedLabel} <b>{consent.consentType}</b> consent.</h5>
            {specConfigurations.consent.permissionsView.permissionBindType ===
            permissionBindTypes.differentPermissionsForEachAccount ?
                (
                    Object.keys(permissions).map((account) => {
                        return <>
                            <h5>Account : {account}</h5>
                            <div className="dataClusters">
                                {permissions[account].map((permission) => (
                                    <PermissionItem permissionScope={permission} key={id = id + 1}/>
                                ))}
                            </div>
                        </>
                    })
                ) : (
                    <div className="dataClusters">
                        {permissions.map((permission) => (
                            <PermissionItem permissionScope={permission} key={id = id + 1}/>
                        ))}
                    </div>
                )
            }
        </div>
    );
};
