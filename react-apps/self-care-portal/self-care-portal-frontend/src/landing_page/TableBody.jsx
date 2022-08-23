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

import {ManageButton} from "../landing_page";
import {dataOrigins, dataTypes, lang} from "../specConfigs";
import moment from "moment";
import {getValueFromApplicationInfoWithFailOver, getValueFromConsentWithFailOver} from "../services/utils";
import {useContext, useEffect, useState} from "react";
import { ConsentContext } from "../context/ConsentContext";
import { AppInfoContext } from "../context/AppInfoContext";

export const TableBody = ({statusTab, consentType}) => {
    let id = 0;

    const {allContextConsents} = useContext(ConsentContext);
    const {contextAppInfo} = useContext(AppInfoContext);

    const consents = allContextConsents.consents;
    const appInfo = contextAppInfo.appInfo;
    const [filteredTab, setFilteredTab] = useState(() => {
        return lang[consentType].filter((lbl) => lbl.id === statusTab)[0];
    });

    useEffect(() => {
        setFilteredTab(lang[consentType].filter((lbl) => lbl.id === statusTab)[0])
    }, [consentType]);

    function renderRespectiveConfiguredValue(header, valueToView) {
        if (header.dataType === dataTypes.timestamp) {
            // timestamp value to view
            return <td key={id = id + 1}>{moment(new Date(valueToView * 1000)
            ).format(header.dateFormat)}</td>
        } else if (header.dataType === dataTypes.date) {
            // date value to view
            return <td key={id = id + 1}>{moment(valueToView
            ).format(header.dateFormat)}</td>
        } else {
            // raw text value to view
            return <td key={id = id + 1}>{valueToView}</td>
        }
    }

    return (
        <tbody key={id = id + 1}>
        {(
            consents.data.length === 0 ? (
                <tr id="noConsentsLbl" key={id = id + 1}>
                    <td id="lbl" colSpan={4} key={id = id + 1}>
                        No {filteredTab !== undefined && filteredTab.label} consents to display
                    </td>
                </tr>
            ) : consents.data.length > 0 ? (
                consents.data.map((consent) => (
                        <tr key={id = id + 1}>
                            {filteredTab !== undefined && filteredTab.tableHeaders.map((header) => {
                                    if (header.dataOrigin === dataOrigins.action) {
                                        return <ManageButton consentId={consent.consentId}/>

                                    } else if (header.dataOrigin === dataOrigins.consent) {

                                        let valueFromConsent = getValueFromConsentWithFailOver(header.dataParameterKey,
                                            header.failOverDataParameterKey, consent);
                                        if (valueFromConsent === "" || valueFromConsent === undefined) {
                                            return <td key={id = id + 1}/>
                                        }
                                        return renderRespectiveConfiguredValue(header, valueFromConsent);

                                    } else if (header.dataOrigin === dataOrigins.applicationInfo) {

                                        let valueFromAppInfo = getValueFromApplicationInfoWithFailOver(
                                            header.dataParameterKey, header.failOverDataParameterKey,
                                            consent.clientId, appInfo);
                                        if (valueFromAppInfo === "" || valueFromAppInfo === undefined) {
                                            return <td key={id = id + 1}/>
                                        }
                                        return renderRespectiveConfiguredValue(header, valueFromAppInfo);

                                    } else {
                                        return <td key={id = id + 1}/>
                                    }
                                }
                            )}
                        </tr>
                    )
                )
            ) : (
                <tr id="noConsentsLbl" key={id = id + 1}>
                    <td id="lbl" colSpan={4} key={id = id + 1}>
                        No consents found
                    </td>
                </tr>
            )
        )}
        </tbody>
    );
}
