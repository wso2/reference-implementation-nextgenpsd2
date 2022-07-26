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

import {specConfigurations, dataTypes, permissionBindTypes} from "../specConfigs";
import moment from "moment";
import jsPDF from "jspdf";

export function getDisplayName(appInfo, clientId) {
    try {
        let disName = appInfo.data[clientId].metadata[specConfigurations.application.displayNameAttribute];
        if (disName !== undefined && disName != "") {
            return disName;
        } else {
            disName = appInfo.data[clientId].metadata[specConfigurations.application.failOverDisplayNameAttribute];
            return disName;
        }
    } catch (e) {
        return clientId;
    }
}

export function getValueFromConsent(key, consent) {
    try {
        let value = consent;
        key.toString().split(".").map((section) => {
            value = value[section];
        })
        return value;
    } catch (e) {
        return ""
    }
}

export function getValueFromConsentWithFailOver(key, failOverKey, consent) {
    try {
        let valueFromConsent = getValueFromConsent(key, consent);
        if (valueFromConsent !== undefined && valueFromConsent != "") {
            return valueFromConsent;
        } else {
            return getValueFromConsent(failOverKey, consent);
        }
    } catch (e) {
        return ""
    }
}

export function getValueFromApplicationInfo(key, clientId, appInfo) {
    try {
        return appInfo.data[clientId].metadata[key];
    } catch (e) {
        return ""
    }
}

export function getValueFromApplicationInfoWithFailOver(key, failOverKey, clientId, appInfo) {
    try {
        let valueFromAppInfo = getValueFromApplicationInfo(key, clientId, appInfo);
        if (valueFromAppInfo !== undefined && valueFromAppInfo != "") {
            return valueFromAppInfo;
        } else {
            return getValueFromApplicationInfo(failOverKey, clientId, appInfo);
        }
    } catch (e) {
        return ""
    }
}

export function getLogoURL(appInfo, clientId) {
    try {
        return appInfo.data[clientId].metadata[specConfigurations.application.logoURLAttribute];
    } catch (e) {
        return "";
    }
}

export function getExpireTimeFromConsent(consent, format) {
    try {
        const expirationTime = getValueFromConsent
        (specConfigurations.consent.expirationTimeAttribute, consent);
        if (expirationTime === "" || expirationTime === undefined) {
            return "";
        }
        if (dataTypes.timestamp === specConfigurations.consent.expirationTimeDataType) {
            return moment(new Date(expirationTime * 1000)).format(format)
        }
        return moment(expirationTime).format(format)
    } catch (e) {
        return "";
    }
}

export function isExpiredConsent(consent, consentType) {

    // only account consents can expire.
    if (consentType !== "accounts") {
        return false;
    } else if (consent.recurringIndicator !== undefined && consent.recurringIndicator === false) {
        // once off consents does not have expiration.
        return false;
    } else if (consent.currentStatus !== "received" && consent.currentStatus !== "valid" && consent.currentStatus !== "partiallyAuthorised") {
        return false;
    }
    try {
        const currentDate = moment().format("YYYY-MM-DDTHH:mm:ss[Z]");
        let expireTimeFromConsent = getExpireTimeFromConsent(consent, "YYYY-MM-DDTHH:mm:ss[Z]");
        if (!expireTimeFromConsent) {
            return false;
        }
        return moment(currentDate)
            .isAfter(expireTimeFromConsent);
    } catch (e) {
        return false;
    }
}

export function isEligibleToRevoke(consent, consentType) {

    if (isExpiredConsent(consent, consentType)) {
        return false;
    } else if (consentType === "accounts" || consentType === "funds-confirmations") {
        return (consent.currentStatus === "received" || consent.currentStatus === "valid" ||
            consent.currentStatus === "partiallyAuthorised")
    } else if (consentType === "payments,periodic-payments,bulk-payments"
        && (consent.consentType === "periodic-payments" || consent.consentType === "bulk-payments")) {
        return (consent.currentStatus === "ACCP" || consent.currentStatus === "RCVD" ||
            consent.currentStatus === "ACTC")
    } else {
        return false;
    }
}

export function getConsentStatusLabel(consent, consentType, infoLabel) {
    return isExpiredConsent(consent, consentType) ? specConfigurations.status.expired : infoLabel.label;
}

export function getPermissionListForConsent(consent) {

    let permissions = [];
    if (specConfigurations.consent.permissionsView.permissionBindType ===
        permissionBindTypes.samePermissionSetForAllAccounts) {
        permissions = getValueFromConsent(
            specConfigurations.consent.permissionsView.permissionsAttribute, consent)
        if (permissions === "" || permissions === undefined) {
            permissions = [];
        }
    } else {
        permissions = {};
        let detailedAccountsList = getValueFromConsent("consentMappingResources", consent);
        detailedAccountsList.map((detailedAccount) => {
            if (detailedAccount.permission !== "n/a") {
                if (permissions[detailedAccount.accountId] === undefined) {
                    permissions[detailedAccount.accountId] = []
                    permissions[detailedAccount.accountId].push(detailedAccount.permission)
                } else {
                    permissions[detailedAccount.accountId].push(detailedAccount.permission)
                }
            } else {
                if (permissions[detailedAccount.accountId] === undefined) {
                    permissions[detailedAccount.accountId] = []
                    permissions[detailedAccount.accountId].push("")
                } else {
                    permissions[detailedAccount.accountId].push("")
                }
            }
        })
    }

    return permissions;
}

export function generatePDF(consent, applicationName, consentStatus) {

    let permissionListForConsent = getPermissionListForConsent(consent);
    const pdf = new jsPDF("l", "mm", "a4");
    pdf.setFontSize(11);
    pdf.text(20, 20, 'Consent infomation for consent ID: ' + consent.consentId)
    pdf.rect(15, 10, 265, 190);
    pdf.text(20, 30, "Status: " + consentStatus)
    pdf.text(20, 40, 'API Consumer Application : ' + applicationName)
    pdf.text(20, 50, 'Consent type : ' + consent.consentType)
    pdf.text(20, 60, 'Create date: ' +
        moment(new Date((consent.createdTimestamp) * 1000)).format("DD-MMM-YYYY"))

    if (consent.consentType === "accounts") {

        pdf.text(20, 70, 'Expire date: ' + getExpireTimeFromConsent(consent, "DD-MMM-YYYY"));
        let headers = [
            {'id': 'Account', 'name': "Account", 'width': 200, 'align': 'center', 'padding': 0},
            {'id': 'Permissions', 'name': "Permissions", 'width': 800, 'align': 'center', 'padding': 0},
        ]
        let tableData = [];
        for (let i = 0; i < Object.keys(permissionListForConsent).length; i += 1) {
            let permissions = [];
            permissionListForConsent[Object.keys(permissionListForConsent)[i]].map((permission) => (
                permissions.push(permission)
            ))
            let data =
                {
                    "Account": Object.keys(permissionListForConsent)[i],
                    "Permissions": permissions.join(":")
                }
            tableData.push(data);
        }
        if (tableData.length > 0) {
            pdf.text(20, 80, 'Data we are sharing on: ')
            pdf.table(40, 90, tableData, headers, {autoSize: true});
        }
    } else {
        let headers = [
            {'id': 'Account', 'name': "Account", 'width': 800, 'align': 'center', 'padding': 0}
        ]
        let tableData = [];
        for (let i = 0; i < Object.keys(permissionListForConsent).length; i += 1) {
            let data = {"Account": Object.keys(permissionListForConsent)[i]}
            tableData.push(data);
        }
        if (tableData.length > 0) {
            pdf.text(20, 70, 'Data we are sharing on: ')
            pdf.table(40, 80, tableData, headers, {autoSize: true});
        }
    }
    pdf.save("consent.pdf");
}
