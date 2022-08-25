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

export const common = {
    footerContent: "WSO2 Open Banking | 2022",
    complaintHandleLinkText: "Complaint handling and resolution",
};

export const keyDateTypes = {
    date: "Date",
    dateRange: "Date Range",
    text: "Text",
    value: "Value",

}

export const permissionBindTypes = {
    // Each account is bind to different different permissions
    samePermissionSetForAllAccounts: "SamePermissionSetForAllAccounts",
    // All the accounts in the consent bind to same set of permissions
    differentPermissionsForEachAccount: "DifferentPermissionsForEachAccount"
}

export const dataOrigins = {
    // To fetch data from consent
    consent: "CONSENT",
    // To fetch data from application information
    applicationInfo: "APPLICATION_INFO",
    // For table action button
    action: "ACTION"
}

export const dataTypes = {
    // To indicate the dataType is a ISO 8601 date
    date: "DATE_ISO_8601",
    // To indicate the dataType is a raw text
    rawData: "APPLICATION_INFO",
    // To indicate the dataType is a ISO 8601 date
    timestamp: "DATE_TIMESTAMP",
}

export const consentTypes = [
    {
        id: "accounts",
        label: "Accounts"
    },
    {
        id: "payments,periodic-payments,bulk-payments",
        label: "Payments"
    },
    {
        id: "funds-confirmations",
        label: "Funds-Confirmations"
    }
]