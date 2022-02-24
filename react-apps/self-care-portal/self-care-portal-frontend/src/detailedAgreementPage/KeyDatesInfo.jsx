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

import {lang, specConfigurations} from "../specConfigs/specConfigurations";
import {keyDateTypes} from "../specConfigs/common";
import React from "react";
import moment from "moment";
import {getValueFromConsent} from "../services";
import {getExpireTimeFromConsent} from "../services/utils";

export const KeyDatesInfo = ({consent, infoLabels}) => {

    let keyDatesConfig = infoLabels;
    const consentStatus = consent.currentStatus;
    const currentDate = moment().format("YYYY-MM-DDTHH:mm:ss[Z]");
    const expirationDateTime = getExpireTimeFromConsent(consent, "YYYY-MM-DDTHH:mm:ss[Z]")
    let isExpired = (expirationDateTime !== "") ? moment(currentDate).isAfter(moment(expirationDateTime)) : false;

    if (consentStatus === specConfigurations.status.authorised && isExpired) {
        keyDatesConfig = lang.filter((lbl) => lbl.id === specConfigurations.status.expired.toLowerCase())[0];
    }

    let keyDatesMap = keyDatesConfig.keyDates.map((keyDate) => {
        if (keyDate.type == keyDateTypes.date) {
            try {
                let timestamp = getValueFromConsent(keyDate.dateParameterKey, consent);
                // Get timestamp in millis
                timestamp = getLongTimestampInMillis(timestamp);
                return (
                    <>
                        <h6>{keyDate.title}</h6>
                        <p className="infoItem">{moment(timestamp).format(keyDate.dateFormat)}</p>
                    </>
                )
            } catch (e) {
                return (
                    <>
                        <h6>{keyDate.title}</h6>
                        <p className="infoItem"></p>
                    </>
                )
            }
        } else if (keyDate.type == keyDateTypes.dateRange) {
            try {
                let timeRanges = keyDate.dateParameterKey.split(",")
                let fromTime = getValueFromConsent(timeRanges[0], consent);
                let toTime = getValueFromConsent(timeRanges[1], consent);

                // Get timestamp in millis
                fromTime = getLongTimestampInMillis(fromTime);
                toTime = getLongTimestampInMillis(toTime);

                return (
                    <>
                        <h6>{keyDate.title}</h6>
                        <p className="infoItem">{moment(fromTime).format(keyDate.dateFormat)} -
                            {moment(toTime).format(keyDate.dateFormat)}</p>
                    </>
                )
            } catch (e) {
                return (
                    <>
                        <h6>{keyDate.title}</h6>
                        <p className="infoItem"></p>
                    </>
                )
            }
        } else {
            return (
                <>
                    <h6>{keyDate.title}</h6>
                    <p className="infoItem">{keyDate.text}</p>
                </>
            )
        }
    });

    // Method to convert epoch second timestamps to epoch millis
    function getLongTimestampInMillis(timestamp) {
        if (timestamp.toString().length === 10) {
            timestamp = timestamp * 1000;
        }
        return timestamp;
    }

    return (
        <div className="keyDatesBody">
            <h5>{keyDatesConfig.keyDatesInfoLabel}</h5>
            {keyDatesMap}
        </div>
    );
};
