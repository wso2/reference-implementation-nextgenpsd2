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

import {dataOrigins, dataTypes, keyDateTypes, permissionBindTypes} from "../common";

export const specConfigurations_BG =
    {
        // key wordings for the relevant statuses.
        status: {
            authorised: "authorised",
            expired: "Expired",
            revoked: "Revoked",
        },
        consent: {
            // if consent is in `authorised` state, `expirationTimeAttribute` parameter from consent data
            // will provide the expirationTime for UI validations.
            expirationTimeAttribute: "receipt.Data.ExpirationDateTime",
            expirationTimeDataType: dataTypes.date,
            // permissionBindTypes status the type of permission binding to the account
            permissionsView: {
                permissionBindType: permissionBindTypes.differentPermissionsForEachAccount,
                permissionsAttribute: "",
            }
        },
        application: {
            logoURLAttribute: "logo_uri",
            displayNameAttribute: "software_client_name",
            failOverDisplayNameAttribute: "software_id"
        }
    };


export const lang_BG = [
    {
        id: "authorised",
        label: "Active",
        labelBadgeVariant: "success",
        description:
            "A list of applications that have active access to your account information",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "software_client_name",
                failOverDataParameterKey: "software_id",
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
                dataParameterKey: "receipt.Data.ExpirationDateTime",
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
                dateParameterKey: "receipt.Data.ExpirationDateTime",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "Sharing period",
                type: keyDateTypes.dateRange,
                dateParameterKey: "createdTimestamp,receipt.Data.ExpirationDateTime",
                dateFormat: "DD MMM YYYY"
            },
            {
                title: "How often your data will be shared",
                type: keyDateTypes.text,
                dateParameterKey: "",
                dateFormat: "",
                text: "Ongoing"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "Data we are sharing",
        accreditation: {
            accreditationLabel: "Accreditation",
            accreditWebsite: "is an accredited data recipient. You can check their accreditation at",
            accreditWebsiteLinkText: "website",
            accreditWebsiteLink: "https://www.cdr.gov.au/find-a-provider",
            accreditDR: "Accredited Data Recipient:"
        }
    },
    {
        id: "expired",
        label: "Expired",
        labelBadgeVariant: "secondary",
        description:
            "A list of applications that have expired access to your account information",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "software_client_name",
                failOverDataParameterKey: "software_id",
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
                dataParameterKey: "receipt.Data.ExpirationDateTime",
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
                dateParameterKey: "receipt.Data.ExpirationDateTime",
                dateFormat: "DD MMM YYYY"
            }
        ],
        accountsInfoLabel: "Accounts",
        dataSharedLabel: "Data we shared",
        accreditation: {
            accreditationLabel: "Accreditation",
            accreditWebsite: "is an accredited data recipient. You can check their accreditation at",
            accreditWebsiteLinkText: "website",
            accreditWebsiteLink: "https://www.cdr.gov.au/find-a-provider",
            accreditDR: "Accredited Data Recipient:"
        }
    },
    {
        id: "revoked",
        label: "Withdrawn",
        labelBadgeVariant: "secondary",
        description:
            "A list of applications of which consent to access your information was withdrawn",
        tableHeaders: [
            {
                heading: "Service Provider",
                dataOrigin: dataOrigins.applicationInfo,
                dataParameterKey: "software_client_name",
                failOverDataParameterKey: "software_id",
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
                heading: "Withdrawn Date",
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
        dataSharedLabel: "Data we shared",
        accreditation: {
            accreditationLabel: "Accreditation",
            accreditWebsite: "is an accredited data recipient. You can check their accreditation at",
            accreditWebsiteLinkText: "website",
            accreditWebsiteLink: "https://www.cdr.gov.au/find-a-provider",
            accreditDR: "Accredited Data Recipient:"
        }
    }
];
