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

export const permissionDataLanguage_CDS = [
  // bank:accounts.basic:read
  {
    scope: "CDRREADACCOUNTSBASIC",
    dataCluster: "Account name, type and balance",
    permissions: ["Name of account", "Type of account", "Account balance"],
  },
  // bank:accounts.detail:read
  {
    scope: "CDRREADACCOUNTSDETAILS",
    dataCluster: "Account numbers and features",
    permissions: [
      "Account number",
      "Account mail address",
      "Interest rates",
      "Fees",
      "Discounts",
      "Account terms",
    ],
  },
  // bank:transactions:read
  {
    scope: "CDRREADTRANSACTION",
    dataCluster: "Transaction details",
    permissions: [
      "Incoming and outgoing transactions",
      "Amounts",
      "Dates",
      "Descriptions of transactions",
      "Who you have sent money to and received money from",
    ],
  },
  // bank:regular_payments:read
  {
    scope: "CDRREADPAYMENTS",
    dataCluster: "Direct debits and scheduled payments",
    permissions: ["Direct debits", "Scheduled payments"],
  },
  // bank:payees:read
  {
    scope: "CDRREADPAYEES",
    dataCluster: "Saved payees",
    permissions: ["Names and details of accounts you have saved"],
  },
  // common:customer.basic:read
  {
    scope: "READCUSTOMERDETAILSBASIC",
    dataCluster: "Name and occupation",
    permissions: ["Name", "Occupation"]
  },
  // common:customer.detail:read
  {
    scope: "READCUSTOMERDETAILS",
    dataCluster: "Contact details",
    permissions: ["Phone", "Email address", "Mail address", "Residential address"]
  },
  //3.0.0 clusters
  {
    scope: "ReadAccountsDetail",
    dataCluster: "Account numbers and features",
    permissions: [
      "Account number",
      "Account mail address",
      "Interest rates",
      "Fees",
      "Discounts",
      "Account terms",
    ],
  },

  {
    scope: "ReadBalances",
    dataCluster: "Ability to read all balance information",
    permissions: [
      "Account number",
      "Account mail address",
      "Interest rates",
      "Fees",
      "Discounts",
      "Account terms",
    ],
  },

  {
    scope: "ReadTransactionsDetail",
    dataCluster: "Ability to read transaction data elements which may hold silent party details",
    permissions: [
      "Account number",
      "Account mail address",
      "Interest rates",
      "Fees",
      "Discounts",
      "Account terms",
    ],
  },
];
