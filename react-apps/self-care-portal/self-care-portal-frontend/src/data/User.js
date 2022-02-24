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
import { id } from "date-fns/locale";
import Cookies from "js-cookie";

export default class User {
  constructor() {
    let idToken = getIdToken();

    if (idToken) {
      this.isLogged = true;
      this.idToken = idToken;
      this.email = decodeIdToken(idToken).sub;
      this.role = decodeIdToken(idToken).user_role;
    } else {
      this.isLogged = false;
    }
  }
}

/**
 * Concat id_token cookies and return token
 * @returns {String|null} - If cookies found, return its value, Else null value is returned
 */
const getIdToken = () => {
  const idTokenPart1 = Cookies.get(User.CONST.OB_SCP_ID_TOKEN_P1);
  const idTokenPart2 = Cookies.get(User.CONST.OB_SCP_ID_TOKEN_P2);

  if (!idTokenPart1 || !idTokenPart2) {
    return null;
  }
  return idTokenPart1 + idTokenPart2;
};

export const getAccessToken = () => {
  const accessTokenPart1 = Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P1);
  const accessTokenPart2 = Cookies.get(User.CONST.OB_SCP_ACC_TOKEN_P2);

  if (!accessTokenPart1 || !accessTokenPart2) {
    return null;
  }
  return accessTokenPart1 + accessTokenPart2;
};

export function decodeIdToken(token) {
  return JSON.parse(atob(token.split(".")[1]));
}

User.CONST = {
  OB_SCP_ACC_TOKEN_P1: "OB_SCP_AT_P1",
  OB_SCP_ACC_TOKEN_P2: "OB_SCP_AT_P2",
  OB_SCP_ID_TOKEN_P1: "OB_SCP_IT_P1",
  OB_SCP_ID_TOKEN_P2: "OB_SCP_IT_P2",
  OB_SCP_REF_TOKEN_P1: "OB_SCP_RT_P1",
  OB_SCP_REF_TOKEN_P2: "OB_SCP_RT_P2",
};
