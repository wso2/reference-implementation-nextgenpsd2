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
import {lang_BG, specConfigurations_BG} from "./BG/specConfigurations_BG";
import {lang_Default, specConfigurations_Default} from "./Default/specConfigurations_Default";

export let specConfigurations
export let lang

let spec = CONFIG.SPEC;

// common spec related configs
if (spec === "Default") {
    specConfigurations = specConfigurations_Default;
    lang = lang_Default;
}

if (spec === "BG") {
    specConfigurations = specConfigurations_BG;
    lang = lang_BG;
}