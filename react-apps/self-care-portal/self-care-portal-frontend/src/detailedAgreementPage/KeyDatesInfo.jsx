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

import {keyDateTypes} from "../specConfigs/common";
import React from "react";
import moment from "moment";
import {getValueFromConsent} from "../services";

export const KeyDatesInfo = ({consent, infoLabels, consentType}) => {

    let keyDatesConfig = infoLabels;

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
        } else if (keyDate.type == keyDateTypes.value) {
            try {
                let valueParameterKey = keyDate.valueParameterKey;
                let valueFromConsent = getValueFromConsent(valueParameterKey, consent);

                return (
                    <>
                        <h6>{keyDate.title}</h6>
                        <p className="infoItem">{keyDate.preText} <b>{valueFromConsent}</b> {keyDate.postText}</p>
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
