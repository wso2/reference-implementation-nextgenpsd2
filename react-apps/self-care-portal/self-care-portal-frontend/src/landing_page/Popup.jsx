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

import React, {useContext, useEffect} from "react";
import "../css/Popup.css";
import QRCode  from "qrcode.react";
import { getAccessToken } from "../data/User";
import { DeviceRegistrationContext } from "../context/DeviceRegistrationContext";

export const QRButton = (props) => {
  const {deviceRegistrationContextData,getDeviceRegistrationContextInfo} =useContext(DeviceRegistrationContext);

  const deviceRegistrationData = deviceRegistrationContextData.deviceRegistrationData;

  useEffect(() => {
    const accessToken = getAccessToken();
    getDeviceRegistrationContextInfo(accessToken);
    const dropDownMenu = document.getElementsByClassName("dropdown-menu")[0];
    dropDownMenu.style.opacity = 0;
    dropDownMenu.style.pointerEvents = 'none';
    }, []);

  return (
    <span>
        <div className="device-registration">
          <h1 className="device-registration-header">Register device</h1>
          <p className="device-registration-content">Please scan the QR code to register your device</p>
          <div className="device-registration-code">
            <QRCode value={JSON.stringify(deviceRegistrationData)}  size="256"/>
          </div>
        </div>
    </span>
   );
};
