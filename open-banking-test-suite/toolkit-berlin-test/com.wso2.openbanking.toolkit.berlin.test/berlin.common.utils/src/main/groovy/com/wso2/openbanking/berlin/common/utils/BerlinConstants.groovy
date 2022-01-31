/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.common.utils

/**
 * Constants For Berlin Tests
 */
class BerlinConstants {

    public static final String X_REQUEST_ID = "X-Request-ID"
    static final String PSU_IP_ADDRESS = "PSU-IP-Address"
    public static final String Date = "Date"
    static final String PSU_ID = "PSU-ID"
    static final String PSU_TYPE = "PSU-ID-Type"
    static final String EXPLICIT_AUTH_PREFERRED = "TPP-Explicit-Authorisation-Preferred"
    static final String TPP_BRAND_LOGGING_INFORMATION = "TPP-Brand-LoggingInformation"
    static final String CONSENT_ID_HEADER = "Consent-ID"
    static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred"

    static final String PAYMENTS_SUBMIT_XPATH = """//*[@id="approve"]"""
    static final String PAYMENTS_DENY_XPATH = """//input[@value='Deny']"""
    static final String PAYMENTS_INTENT_TEXT_XPATH = "/html/body/div/div/div/div/div[2]/div/form/div/div[1]/div/h3"
    static final String ACCOUNTS_SUBMIT_XPATH = """//*[@id="approve"]"""
    static final String ACCOUNTS_DENY_XPATH = """//*[@id="oauth2_authz_confirm"]/div/div[3]/div/input[2]"""
    static final String COF_SUBMIT_XPATH = """//*[@id="approve"]"""
    static final String COF_DENY_XPATH = """//input[@value='Deny']"""
    static final String LBL_AUTH_PAGE_CLIENT_INVALID_ERROR = "//body[1]"
    static final String LBL_AUTH_PAGE_CLIENT_INVALID_ERROR_200 ="//body[1]//div[@class='ui visible negative message']"
    static final String LBL_CONSENT_PAGE_ERROR = "/html/body/div/div/div/div/div/div[3]/div/form/div/div/p"
    static final String PSU_EMAIL_ID = "mark@gold.com"

    static final String TPPMESSAGE_CODE = "tppMessages[0].code"
    static final String TPPMESSAGE_TEXT = "tppMessages[0].text"
    static final String TPPMESSAGE_PATH = "tppMessages[0].path"
    static final String FORMAT_ERROR = "FORMAT_ERROR"
    static final String PARAMETER_NOT_CONSISTENT = "PARAMETER_NOT_CONSISTENT"
    static final String CONSENT_INVALID = "CONSENT_INVALID"
    static final String CONSENT_UNKNOWN = "CONSENT_UNKNOWN"
    static final String CONSENT_STATUS = "consentStatus"
    static final String TIMESTAMP_INVALID = "TIMESTAMP_INVALID"
    static final String HEADER_BOOKINGSTATUS= "Header.bookingStatus"
    static final String QUERY_BOOKINGSTATUS = "Query.bookingStatus";
    static final String HEADER_DATEFROM = "Header.dateFrom"
    static final String QUERY_DATEFROM = "Query.dateFrom"
    static final String PRODUCT_UNKNOWN = "PRODUCT_UNKNOWN"
    static final String RESOURCE_UNKNOWN = "RESOURCE_UNKNOWN"
    static final String INVALID_STATUS_VALUE = "INVALID_STATUS_VALUE";


    static final int STATUS_CODE_204 = 204
    static final int STATUS_CODE_201 = 201
    static final int STATUS_CODE_200 = 200
    static final int STATUS_CODE_400 = 400
    static final int STATUS_CODE_401 = 401
    static final int STATUS_CODE_403 = 403
    static final int STATUS_CODE_404 = 404
    static final int STATUS_CODE_405 = 405
    static final int STATUS_CODE_406 = 406
    static final int STATUS_CODE_202 = 202

    static final enum AUTH_METHOD {
        PRIVATE_KEY_JWT
    }

    static final enum SCOPES {
        PAYMENTS(["openid", "payments"], "pis"), ACCOUNTS(["openid", "accounts"], "ais") ,
        COF(["openid", "fundsconfirmations"], "piis")

        private final List<String> scopes
        private final String consentScope

        SCOPES(List<String> scopes, String consentScope){
            this.scopes = scopes
            this.consentScope = consentScope
        }

        List<String> getScopes(){
            return scopes
        }

        String getConsentScope(String consentId){
            return "${consentScope}:${consentId}"
        }
    }

    static final String IS_VALID = "IsValid"
    static final String SCA_STATUS_PARAM = "scaStatus"
    static final String TRUSTED_BENEFICIARY_FLAG = "trustedBeneficiaryFlag"

    static final String NORMAL_ACCOUNT = "DE98765432109876543210"
    static final String MULTICURRENCY_ACCOUNT = "DE12345678901234567890"
    static final String CURRENCY1 = "USD"
    static final String CURRENCY2 = "EUR"
    static final String CURRENCY3 = "GBP"
}
