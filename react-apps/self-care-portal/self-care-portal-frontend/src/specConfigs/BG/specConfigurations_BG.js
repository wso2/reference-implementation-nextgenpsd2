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

import {consentTypes, dataOrigins, dataTypes, keyDateTypes, permissionBindTypes} from "../common";

export const specConfigurations_BG =
    {
        // key wordings for the relevant statuses.
        status: {
            authorised: "valid",
            expired: "Expired",
            revoked: "Revoked",
        },
        consent: {
            // if consent is in `authorised` state, `expirationTimeAttribute` parameter from consent data
            // will provide the expirationTime for UI validations.
            expirationTimeAttribute: "validityPeriod",
            expirationTimeDataType: dataTypes.timestamp,
            // permissionBindTypes status the type of permission binding to the account
            permissionsView: {
                permissionBindType: permissionBindTypes.differentPermissionsForEachAccount,
                permissionsAttribute: "",
            }
        },
        application: {
            logoURLAttribute: "logo_uri",
            displayNameAttribute: "DisplayName",
            failOverDisplayNameAttribute: "organization_id"
        }
    };

export const account_lang_BG = [
    {
        id: "valid",
        label: "Active",
        labelBadgeVariant: "success",
        isRevocableConsent: true,
        description:
            "A list of consents that are in",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "DisplayName",
                failOverDataParameterKey: "organization_id",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Expiry Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "receipt.validUntil",
                failOverDataParameterKey: "",
                dataType: dataTypes.date,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View confirmation of consent >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "You granted consent on",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "Your consent will expire on",
                type: keyDateTypes.date,
                dateParameterKey: "validityPeriod",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "How often your data will be shared",
                type: keyDateTypes.value,
                valueParameterKey: "consentFrequency",
                preText: "Daily",
                postText: "times"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "We share the following data via the",
        accreditation: {
            accreditDR: "API consumer application:"
        }
    },
    {
        id: "expired",
        label: "Expired",
        labelBadgeVariant: "secondary",
        description:
            "A list of consents that are in",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "DisplayName",
                failOverDataParameterKey: "organization_id",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Expiry Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "receipt.validUntil",
                failOverDataParameterKey: "",
                dataType: dataTypes.date,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View consent expiry confirmation >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "When you gave consent",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "When consent was expired",
                type: keyDateTypes.date,
                dateParameterKey: "validityPeriod",
                dateFormat: "DD MMM YYYY"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "We share the following data via the",
        accreditation: {
            accreditDR: "API consumer application:"
        }
    },
    {
        id: "revokedByPsu,terminatedByTpp",
        label: "Revoked",
        labelBadgeVariant: "secondary",
        description:
            "A list of consents that are in",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "DisplayName",
                failOverDataParameterKey: "organization_id",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Revoked Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "updatedTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View consent withdrawal confirmation >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "When you gave consent",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "You cancelled your consent on",
                type: keyDateTypes.date,
                dateParameterKey: "updatedTimestamp",
                dateFormat: "DD MMM YYYY"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "",
        accreditation: {
            accreditDR: "API consumer application:"
        }
    }
];

export const payments_lang_BG = [
    {
        id: "ACCP",
        label: "Active",
        labelBadgeVariant: "success",
        isRevocableConsent: true,
        description:
            "A list of consents that are in",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "DisplayName",
                failOverDataParameterKey: "organization_id",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View confirmation of consent >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "You granted consent on",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "We share the following data via the",
        accreditation: {
            accreditDR: "API consumer application:"
        }
    },
    {
        id: "REVOKED,CANC",
        label: "Revoked",
        labelBadgeVariant: "secondary",
        description:
            "A list of consents that are in",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "DisplayName",
                failOverDataParameterKey: "organization_id",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Revoked Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "updatedTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View confirmation of consent >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "You granted consent on",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "You cancelled your consent on",
                type: keyDateTypes.date,
                dateParameterKey: "updatedTimestamp",
                dateFormat: "DD MMM YYYY"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "",
        accreditation: {
            accreditDR: "API consumer application:"
        }
    },
];

export const cof_lang_BG = [
    {
        id: "valid",
        label: "Active",
        labelBadgeVariant: "success",
        isRevocableConsent: true,
        description:
            "A list of consents that are in",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "DisplayName",
                failOverDataParameterKey: "organization_id",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View confirmation of consent >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "You granted consent on",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "We share the following data via the",
        accreditation: {
            accreditDR: "API consumer application:"
        }
    },
    {
        id: "revokedByPsu,terminatedByTpp",
        label: "Revoked",
        labelBadgeVariant: "secondary",
        description:
            "A list of consents that are in",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "DisplayName",
                failOverDataParameterKey: "organization_id",
                dataType: dataTypes.rawData
            },
            {
                heading: "Consented Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "createdTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Revoked Date",
                dataOrigin: dataOrigins.consent,
                dataParameterKey: "updatedTimestamp",
                failOverDataParameterKey: "",
                dataType: dataTypes.timestamp,
                dateFormat: "DD MMM YYYY"
            },
            {
                heading: "Action",
                dataOrigin: dataOrigins.action,
                dataParameterKey: "",
                failOverDataParameterKey: "",
                dataType: dataTypes.rawData
            },
        ],
        profile: {
            confirmation: "View consent withdrawal confirmation >"
        },
        keyDatesInfoLabel: "Key Dates",
        keyDates: [
            {
                title: "When you gave consent",
                type: keyDateTypes.date,
                dateParameterKey: "createdTimestamp",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "You cancelled your consent on",
                type: keyDateTypes.date,
                dateParameterKey: "updatedTimestamp",
                dateFormat: "DD MMM YYYY"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "",
        accreditation: {
            accreditDR: "API consumer application:"
        }
    }
];

export const lang_BG = {
    [consentTypes[0].id]: account_lang_BG,
    [consentTypes[1].id]: payments_lang_BG,
    [consentTypes[2].id]: cof_lang_BG
}