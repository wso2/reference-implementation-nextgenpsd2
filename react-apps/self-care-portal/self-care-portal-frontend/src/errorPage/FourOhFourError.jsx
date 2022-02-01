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

import Container from "react-bootstrap/Col";

export const FourOhFourError = () => {
  return (
    <Container
      className="fourOhFour"
      style={{ padding: "1rem 1rem", height: "84.5vh" }}
    >
      <h5 style={{ margin: "1rem 1rem", color: "rgba(8, 18, 71, 1)" }}>
        Error 404 : Page not found
      </h5>
    </Container>
  );
};
