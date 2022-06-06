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
import {lang, specConfigurations} from "../../../specConfigurations";
import {permissionBindTypes} from "../../../common";

let id = 0;

export const AccountsInfoUK = ({consent, consentType}) => {

    const consentStatus = consent.currentStatus;
    const debtorAccounts = consent.consentMappingResources;
    let keyDatesConfig = lang.filter((lbl) => lbl.id === consentStatus.toLowerCase())[0];
    return (
        <div className="accountsInfoBody">
            {specConfigurations.consent.permissionsView.permissionBindType ===
            permissionBindTypes.samePermissionSetForAllAccounts ? (
                <>
                    <h5>{keyDatesConfig.accountsInfoLabel}</h5>
                    {debtorAccounts.map((account) => (
                        account.mappingStatus === "active" ?
                            <li key={id = id + 1}>{account.accountId}</li>
                            :
                            <> </>
                    ))}
                    <h5> {""}</h5>
                </>
            ) : (
                <></>
            )
            }

        </div>
    );
};
