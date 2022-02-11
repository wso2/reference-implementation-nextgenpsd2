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

import {CONFIG} from "../config";
import {withdrawLang_CDS} from "./CDS/withdrawLang_CDS";
import {withdrawLang_UK} from "./UK/withdrawLang_UK";
import {withdrawLang_BG} from "./BG/withdrawLang_BG";
import {withdrawLang_Default} from "./Default/withdrawLang_Default";

export let withdrawLang

let spec = CONFIG.SPEC;

if (spec === "Default") {
    withdrawLang = withdrawLang_Default;
}

if (spec === "CDS") {
    withdrawLang = withdrawLang_CDS;
}

if (spec === "UK") {
    withdrawLang = withdrawLang_UK;
}

if (spec === "BG") {
    withdrawLang = withdrawLang_BG;
}

