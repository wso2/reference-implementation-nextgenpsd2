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
import base64url from "base64url";
import wso2Logo from "../images/wso2Logo.png";

export const ResponseError = (error = {}) => {
  let message = error.message;
  let description = error.description;

  if (!description) {
    // Reads the URL and retrieves the error params.
    const url = new URL(window.location.href);

    message = url.searchParams.get("message");
    description = url.searchParams.get("description");

    if (message && description) {
      message = base64url.decode(message);
      description = base64url.decode(description);
    } else {
      message = "Redirecting Failed!";
      description =
        "Something went wrong during the authentication process. Please try signing in again.";
    }
  }

  return (
    <div className="container">
      <div className="row justify-content-md-center top-auto">
        <div className="col col-md-6">
          <img
            className="mx-auto d-block navLogoImage"
            alt="WSO2 logo"
            src={wso2Logo}
          />
          <div className="border p-5">
            <div className="alert alert-danger m-0" role="alert">
              <h4 className="alert-heading">{message}</h4>
              <p>{description}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
