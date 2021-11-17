/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 *
 */

// Confirm sharing data
function approvedBerlinConsent() {
    document.getElementById('consent').value = true;
    validateBerlinFrm();
}

// Submit data sharing from
function validateBerlinFrm() {
    if (document.getElementById('type').value == "accounts") {

        var checkedAccounts = $('[name="checkedAccounts"]').length;
        var checkedBalances = $('[name="checkedBalances"]').length;
        var checkedTransactions = $('[name="checkedTransactions"]').length;
        var isAccountsChecked = $('input[name="checkedAccounts"]:checkbox').filter(':checked').length > 0;
        var isBalancesChecked = $('input[name="checkedBalances"]:checkbox').filter(':checked').length > 0;
        var isTransactionsChecked = $('input[name="checkedTransactions"]:checkbox').filter(':checked').length > 0;

        // Checking if any of the checkable elements are present and if at least one is checked
        // if they are present in the dom
        if ((checkedAccounts && !isAccountsChecked) || (checkedBalances && !isBalancesChecked)
            || (checkedTransactions && !isTransactionsChecked)) {
            $(".acc-err").show();
            return false;
        } else {
            document.getElementById("oauth2_authz_confirm").submit();
        }
    }

    if (document.getElementById('type').value === "payments" || document.getElementById('type').value ===
    "bulk-payments" || document.getElementById('type').value === "periodic-payments") {
        document.getElementById("oauth2_authz_confirm").submit();
    }
}
