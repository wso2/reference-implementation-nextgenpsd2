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

package com.wso2.openbanking.toolkit.berlin.integration.test.payments.util

import org.testng.annotations.DataProvider

class PaymentsDataProviders {

    @DataProvider(name = "PaymentsTypes")
    Object[][] getPaymentsTypes() {

        def PaymentsTypesList = new ArrayList<Object[]>()
        PaymentsTypesList.add([PaymentsConstants.SINGLE_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.singlePaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.BULK_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.bulkPaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.PERIODIC_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.periodicPaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.SINGLE_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.singlePaymentPayloadWithAdditionalParams] as Object[])

        return PaymentsTypesList
    }

    @DataProvider(name = "SinglePayments")
    Object[][] getSinglePayments() {

        def PaymentsTypesList = new ArrayList<Object[]>()
        PaymentsTypesList.add([PaymentsConstants.SINGLE_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.singlePaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.SINGLE_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.singlePaymentPayloadWithAdditionalParams] as Object[])

        return PaymentsTypesList
    }

    @DataProvider(name = "PaymentsTypesV110")
    Object[][] getPaymentsTypesV110() {

        def PaymentsTypesList = new ArrayList<Object[]>()
        PaymentsTypesList.add([PaymentsConstants.V110_SINGLE_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.singlePaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.V110_BULK_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.v110BulkPaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.V110_PERIODIC_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS,
                                PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS,
                                PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.v110PeriodicPaymentPayload] as Object[])

        return PaymentsTypesList
    }

    @DataProvider(name = "ExplicitAuthorizationData")
    static Iterator<Object[]> getExplicitAuthorizationData() {

        Collection<Object[]> explicitAuthorizationList = new ArrayList<Object[]>()
        List<Map<String, String>> listOfParamMaps = new ArrayList<Map<String, String>>()
        Map<String, String> periodicPaymentExplicitAuthDataMap = new HashMap<String, String>()
        Map<String, String> singlePaymentExplicitAuthDataMap = new HashMap<String, String>()
        Map<String, String> bulkPaymentExplicitAuthDataMap = new HashMap<String, String>()

        periodicPaymentExplicitAuthDataMap.put("consentPath", PaymentsConstants.PERIODIC_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS)
        periodicPaymentExplicitAuthDataMap.put("initiationPayload", PaymentsInitiationPayloads.periodicPaymentPayload)
        periodicPaymentExplicitAuthDataMap.put("paymentType", "periodic")

        singlePaymentExplicitAuthDataMap.put("consentPath", PaymentsConstants.SINGLE_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS)
        singlePaymentExplicitAuthDataMap.put("initiationPayload", PaymentsInitiationPayloads.singlePaymentPayload)
        singlePaymentExplicitAuthDataMap.put("paymentType", "single")

        bulkPaymentExplicitAuthDataMap.put("consentPath", PaymentsConstants.BULK_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS)
        bulkPaymentExplicitAuthDataMap.put("initiationPayload", PaymentsInitiationPayloads.bulkPaymentPayload)
        bulkPaymentExplicitAuthDataMap.put("paymentType", "bulk")

        listOfParamMaps.add(periodicPaymentExplicitAuthDataMap)
        listOfParamMaps.add(singlePaymentExplicitAuthDataMap)
        listOfParamMaps.add(bulkPaymentExplicitAuthDataMap)

        for (Map<String, String> map : listOfParamMaps) {
            explicitAuthorizationList.add([map] as Object[])
        }
        return explicitAuthorizationList.iterator()
    }

    @DataProvider(name = "PaymentsPayloads")
    Object[][] getPaymentsPayloads() {

        def PaymentsTypesList = new ArrayList<Object[]>()
        PaymentsTypesList.add([PaymentsConstants.SINGLE_PAYMENTS_PATH,
                               PaymentsInitiationPayloads.singlePaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.BULK_PAYMENTS_PATH,
                               PaymentsInitiationPayloads.bulkPaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.PERIODIC_PAYMENTS_PATH,
                               PaymentsInitiationPayloads.periodicPaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.SINGLE_PAYMENTS_PATH,
                               PaymentsInitiationPayloads.singlePaymentPayloadWithAdditionalParams] as Object[])

        return PaymentsTypesList
    }

    @DataProvider(name = "PaymentProduct")
    Object[][] getPaymentProduct() {

        def PaymentProductList = new ArrayList<Object[]>()
        PaymentProductList.add([PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS] as Object[])
        PaymentProductList.add([PaymentsConstants.PAYMENT_PRODUCT_INSTA_SEPA_CREDIT_TRANSFERS] as Object[])
        PaymentProductList.add([PaymentsConstants.PAYMENT_PRODUCT_TARGET_2_PAYMENTS] as Object[])
        PaymentProductList.add([PaymentsConstants.PAYMENT_PRODUCT_CROSS_BORDER_CREDIT_TRANSFERS] as Object[])

        return PaymentProductList
    }

    @DataProvider(name = "CreditorName")
    Object[][] getCreditorName() {

        def creditorName = new ArrayList<Object[]>()
        creditorName.add(["Merchant"] as Object[])
        creditorName.add(["Merchant1234"] as Object[])
        creditorName.add(["Merchant@&123"] as Object[])

        return creditorName
    }

    @DataProvider(name = "AccountAttributes")
    Object[][] getAccountAttributes() {

        def accountAttributes = new ArrayList<Object[]>()
        accountAttributes.add(["iban", "DE12345678901234567890"] as Object[])
        accountAttributes.add(["bban", "5390 0754 7034"] as Object[])
        accountAttributes.add(["pan", "4685-2421-1836-5024"] as Object[])
//        accountAttributes.add(["maskedPan", "4685-2466-8495-3279"] as Object[])
//        accountAttributes.add(["msisdn", "0114479234"] as Object[])

        return accountAttributes
    }

    @DataProvider(name = "PaymentFrequency")
    Object[][] getPaymentFrequency() {

        def paymentFrequency = new ArrayList<Object[]>()
        paymentFrequency.add(["Daily"] as Object[])
        paymentFrequency.add(["Weekly"] as Object[])
        paymentFrequency.add(["EveryTwoWeeks"] as Object[])
        paymentFrequency.add(["Monthly"] as Object[])
        paymentFrequency.add(["EveryTwoMonths"] as Object[])
        paymentFrequency.add(["Quarterly"] as Object[])
        paymentFrequency.add(["SemiAnnual"] as Object[])
        paymentFrequency.add(["Annual"] as Object[])

        return paymentFrequency
    }

    @DataProvider(name = "ExecutionRule")
    Object[][] getExecutionRule() {

        def executionRule = new ArrayList<Object[]>()
        executionRule.add([PaymentsConstants.executionRuleFollowing] as Object[])
        executionRule.add([PaymentsConstants.executionRulePreceding] as Object[])

        return executionRule
    }

    @DataProvider(name = "PaymentsTypesForCancellation")
    Object[][] getPaymentsTypesForCancellation() {

        def PaymentsTypesList = new ArrayList<Object[]>()

        PaymentsTypesList.add([PaymentsConstants.BULK_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.bulkPaymentPayload] as Object[])

        PaymentsTypesList.add([PaymentsConstants.PERIODIC_PAYMENTS_PATH,
                               [PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS],
                               PaymentsInitiationPayloads.periodicPaymentPayload] as Object[])

        return PaymentsTypesList
    }

    @DataProvider(name = "ExplicitAuthCancellationData")
    static Iterator<Object[]> getExplicitAuthCancellationData() {

        Collection<Object[]> explicitAuthorizationList = new ArrayList<Object[]>()
        List<Map<String, String>> listOfParamMaps = new ArrayList<Map<String, String>>()
        Map<String, String> periodicPaymentExplicitAuthDataMap = new HashMap<String, String>()
        Map<String, String> bulkPaymentExplicitAuthDataMap = new HashMap<String, String>()

        periodicPaymentExplicitAuthDataMap.put("consentPath", PaymentsConstants.PERIODIC_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS)
        periodicPaymentExplicitAuthDataMap.put("initiationPayload", PaymentsInitiationPayloads.periodicPaymentPayload)
        periodicPaymentExplicitAuthDataMap.put("paymentType", "periodic")

        bulkPaymentExplicitAuthDataMap.put("consentPath", PaymentsConstants.BULK_PAYMENTS_PATH + "/" +
                PaymentsConstants.PAYMENT_PRODUCT_SEPA_CREDIT_TRANSFERS)
        bulkPaymentExplicitAuthDataMap.put("initiationPayload", PaymentsInitiationPayloads.bulkPaymentPayload)
        bulkPaymentExplicitAuthDataMap.put("paymentType", "bulk")

        listOfParamMaps.add(periodicPaymentExplicitAuthDataMap)
        listOfParamMaps.add(bulkPaymentExplicitAuthDataMap)

        for (Map<String, String> map : listOfParamMaps) {
            explicitAuthorizationList.add([map] as Object[])
        }
        return explicitAuthorizationList.iterator()
    }
}
