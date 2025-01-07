/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Confirm sharing data
function approvedBerlinConsent() {
    document.getElementById('consent').value = true;
    validateBerlinFrm();
}

// Submit data sharing from
function validateBerlinFrm() {
    if (document.getElementById('type').value == "accounts") {

        var checkedAccounts = $('[name="checkedAccountsAccountRefs"]').length;
        var checkedBalances = $('[name="checkedBalancesAccountRefs"]').length;
        var checkedTransactions = $('[name="checkedTransactionsAccountRefs"]').length;
        var isAccountsChecked = $('input[name="checkedAccountsAccountRefs"]:checkbox').filter(':checked').length > 0;
        var isBalancesChecked = $('input[name="checkedBalancesAccountRefs"]:checkbox').filter(':checked').length > 0;
        var isTransactionsChecked = $('input[name="checkedTransactionsAccountRefs"]:checkbox').filter(':checked').length > 0;

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

    if (document.getElementById('type').value === "payments"
    || document.getElementById('type').value === "bulk-payments"
    || document.getElementById('type').value === "periodic-payments"
    || document.getElementById('type').value === "funds-confirmations") {
        document.getElementById("oauth2_authz_confirm").submit();
    }
}
