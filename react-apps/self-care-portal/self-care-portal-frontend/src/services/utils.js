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
            }
        })
    }

    return permissions;
}

export function generatePDF(consent, applicationName, consentStatus) {

    const pdf = new jsPDF("l", "mm", "a4");
    let content01 = "";
    let content02 = "";
    let content03 = "";
    pdf.setFontSize(11);

    const input = document.getElementsByClassName('permissionsUL');

    try {
        content01 = input[0].innerHTML.split("<li>");
        content01 = content01.join("").split("</li>").join(", ");
        content01 = content01.slice(0, -2);
    } catch (e) {
    }

    try {
        content02 = input[1].innerHTML.split("<li>");
        content02 = content02.join("").split("</li>").join(", ");
        content02 = content02.slice(0, -2);
    } catch (e) {
    }

    try {
        let debtorList = [];
        consent.consentMappingResources.map((account) =>
            account.mappingStatus === "active" ? debtorList.push(account.accountId) : <> </>
        );
        content03 = debtorList.join(",");
    } catch (e) {
    }

    pdf.text(20, 20, 'Consent infomation for consent ID: ' + consent.consentId)
    pdf.rect(15, 10, 265, 190);
    pdf.text(20, 30, "Status: " + consentStatus)
    pdf.text(20, 40, 'API Consumer Application : ' + applicationName)
    pdf.text(20, 50, 'Create date: ' + moment(new Date((consent.createdTimestamp) * 1000)).format("DD-MMM-YYYY"))
    pdf.text(20, 60, 'Expire date: ' + getExpireTimeFromConsent(consent, "DD-MMM-YYYY"));
    pdf.text(20, 70, 'Data we are sharing on: ')
    pdf.text(30, 80, 'Account name, type and balance: ')
    pdf.text(40, 90, content01)
    pdf.text(30, 100, 'Account numbers and features: ')
    pdf.text(40, 110, content02)
    pdf.text(20, 120, 'Accounts: ' + content03)
    pdf.save("consent.pdf");
}