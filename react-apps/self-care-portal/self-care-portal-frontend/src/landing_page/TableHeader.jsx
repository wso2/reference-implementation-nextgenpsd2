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

import {dataOrigins, lang} from "../specConfigs";
import {useEffect, useState} from "react";

export const TableHeader = ({statusTab, consentType}) => {

    const [filteredTab, setFilteredTab] = useState(() => {
        return lang[consentType].filter((lbl) => lbl.id === statusTab)[0];
    });

    useEffect(() => {
        setFilteredTab(lang[consentType].filter((lbl) => lbl.id === statusTab)[0]);
    }, [consentType])

    return (
        <thead>
        <tr>
            <>
                {filteredTab !== undefined && filteredTab.tableHeaders
                    .map((header) => {
                            if (header.dataOrigin === dataOrigins.action) {
                                return <th className="headerAction">{header.heading}</th>
                            } else {
                                return <th>{header.heading}</th>
                            }
                        }
                    )}
            </>
        </tr>
        </thead>
    );
};
