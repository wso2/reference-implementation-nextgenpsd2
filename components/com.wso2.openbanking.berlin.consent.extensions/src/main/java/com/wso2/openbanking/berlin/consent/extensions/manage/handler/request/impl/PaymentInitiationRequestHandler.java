/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.utils.ConsentTypeEnum;
import com.wso2.openbanking.berlin.common.utils.ErrorConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionUtil;
import com.wso2.openbanking.berlin.consent.extensions.common.HeaderValidator;
import com.wso2.openbanking.berlin.consent.extensions.common.ScaStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.common.TransactionStatusEnum;
import com.wso2.openbanking.berlin.consent.extensions.manage.handler.request.RequestHandler;
import com.wso2.openbanking.berlin.consent.extensions.manage.util.PaymentConsentUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handle Payments initiation request.
 */
public class PaymentInitiationRequestHandler implements RequestHandler {

    private static final Log log = LogFactory.getLog(PaymentInitiationRequestHandler.class);

    @Override
    public void handle(ConsentManageData consentManageData) throws ConsentException {

        DetailedConsentResource createdConsent;

        CommonConfigParser configParser = CommonConfigParser.getInstance();
        ConsentCoreService consentCoreService = getConsentService();

        Map<String, String> headersMap = consentManageData.getHeaders();
        JSONObject requestPayload = (JSONObject) consentManageData.getPayload();
        String requestPath = consentManageData.getRequestPath();
        String paymentService = ConsentExtensionUtil.getServiceDifferentiatingRequestPath(requestPath);
        String paymentProduct = PaymentConsentUtil.getPaymentProduct(requestPath);
        String maxPaymentExecutionDays = configParser.getMaxFuturePaymentDays();
        String configuredAccReference = configParser.getAccountReferenceType();
        String clientId = consentManageData.getClientId();

        validateRequestHeaders(headersMap);
        validateRequestPayload(requestPayload, paymentService, configuredAccReference, maxPaymentExecutionDays);

        log.debug("Determining whether the consent request is implicit or explicit");
        boolean isExplicitAuth = HeaderValidator.isTppExplicitAuthorisationPreferred(headersMap);

        log.debug("Determining whether the TPP-Redirect-Preferred header is true or false or not present");
        Optional<Boolean> isRedirectPreferred = HeaderValidator.isTppRedirectPreferred(headersMap);

        log.debug("The consent initiation is an implicit initiation");
        if (!isRedirectPreferred.isPresent() || isRedirectPreferred.get()) {
            log.debug("SCA approach is Redirect SCA (OAuth2)");

            log.debug("Constructing consent request to be stored");
            ConsentResource consentResource = new ConsentResource(clientId, requestPayload.toJSONString(),
                    ConsentTypeEnum.PAYMENTS.toString(), TransactionStatusEnum.RCVD.name());

            String tenantEnsuredPSUId = ConsentExtensionUtil
                    .ensureSuperTenantDomain(headersMap.get(ConsentExtensionConstants.PSU_ID_HEADER));
            try {
                log.debug("Creating consent");
                createdConsent = consentCoreService.createAuthorizableConsent(consentResource, tenantEnsuredPSUId,
                        ScaStatusEnum.RECEIVED.toString(), ConsentExtensionConstants.AUTHORISATION,
                        !isExplicitAuth);
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.CONSENT_INITIATION_ERROR);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            try {
                log.debug("Storing consent attributes");
                consentCoreService.storeConsentAttributes(createdConsent.getConsentID(),
                        getConsentAttributesToPersist(paymentService, paymentProduct, headersMap));
            } catch (ConsentManagementException e) {
                log.error(ErrorConstants.CONSENT_ATTRIBUTE_INITIATION_ERROR);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            log.debug("Constructing response");
            String apiVersion = configParser.getApiVersion(consentResource.getConsentType());
            boolean isSCARequired = configParser.isScaRequired();
            boolean isTransactionFeeEnabled = configParser.isTransactionFeeEnabled();
            int transactionFee = configParser.getTransactionFee();
            String transactionFeeCurrency = configParser.getTransactionFeeCurrency();

            consentManageData.setResponsePayload(PaymentConsentUtil
                    .constructPaymentInitiationResponse(consentManageData, createdConsent, isExplicitAuth, requestPath,
                            apiVersion, isSCARequired, isTransactionFeeEnabled, transactionFee,
                            transactionFeeCurrency));
            consentManageData.setResponseStatus(ResponseStatus.CREATED);
        } else {
            log.error(ErrorConstants.DECOUPLED_FLOW_NOT_SUPPORTED);
            throw new ConsentException(ResponseStatus.FORBIDDEN, ErrorConstants.DECOUPLED_FLOW_NOT_SUPPORTED);
        }
    }

    /**
     * Validates payment consent request headers. This method can be overridden to do header validations by different
     * Berlin based specifications.
     *
     * @param headersMap headers map
     * @throws ConsentException
     */
    protected void validateRequestHeaders(Map<String, String> headersMap) throws ConsentException {

        log.debug("Validating TPP-Redirect-Preferred header according to the specification");
        /**
         * 1). If the header is true, the REDIRECT SCA Approach must be configured in open-banking.xml as the
         * supported SCA approach.
         *
         * 2). If the header is false, the DECOUPLED SCA Approach must be configured in open-banking.xml (Currencly
         * not supported by the toolkit).
         *
         * 3) If the header is not present, the TPP/PSU should decide what is the SCA Approach based on the
         * configured SCA methods by the ASPSP (Currently only one SCA method is supported to be configured in
         * open-banking.xml). Therefore SCA method choosing is not yet supported by the toolkit.
         */
        HeaderValidator.validateTppRedirectPreferredHeader(headersMap);

        log.debug("Validating PSU-IP-Address header");
        HeaderValidator.validatePsuIpAddress(headersMap);
    }

    /**
     * Validates payment consent payloads. This method can be overridden to validate payloads by different Berlin
     * based specifications.
     *
     * @param payload request payload
     * @param paymentType payment type (payment, bulk-payment, periodic-payment)
     */
    protected void validateRequestPayload(JSONObject payload, String paymentType, String configuredAccReference,
                                          String maxPaymentExecutionDays) {

        // One of the following payment types must present according to the swagger validation
        if (StringUtils.equals(ConsentExtensionConstants.PAYMENTS, paymentType)) {
            PaymentConsentUtil.validatePaymentsPayload(payload, configuredAccReference);
        } else if (StringUtils.equals(ConsentExtensionConstants.BULK_PAYMENTS, paymentType)) {
            PaymentConsentUtil.validateBulkPaymentsPayload(payload, maxPaymentExecutionDays, configuredAccReference);
        } else {
            PaymentConsentUtil.validatePeriodicPaymentsPayload(payload, configuredAccReference);
        }
    }

    /**
     * Sets necessary attributes to be persisted as consent attributes. This method can be overridden to persist
     * needed consent attributes by different Berlin based specifications.
     *
     * @param paymentService payment service
     * @param paymentProduct payment product
     * @param headersMap headers map
     * @return
     */
    protected Map<String, String> getConsentAttributesToPersist(String paymentService, String paymentProduct,
                                                                Map<String, String> headersMap) {

        Map<String, String> consentAttributesMap = new HashMap<>();

        consentAttributesMap.put(ConsentExtensionConstants.PAYMENT_SERVICE, paymentService);
        consentAttributesMap.put(ConsentExtensionConstants.PAYMENT_PRODUCT, paymentProduct);
        consentAttributesMap.put(ConsentExtensionConstants.X_REQUEST_ID_HEADER,
                headersMap.get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));

        if (headersMap.containsKey(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER)) {
            consentAttributesMap.put(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER,
                    headersMap.get(ConsentExtensionConstants.TPP_REDIRECT_PREFERRED_HEADER));
        }

        if (headersMap.containsKey(ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END)) {
            consentAttributesMap.put(ConsentExtensionConstants.TPP_EXPLICIT_AUTH_PREFERRED_HEADER,
                    headersMap.get(ConsentExtensionConstants.EXPLICIT_AUTHORISATION_PATH_END));
        }
        return consentAttributesMap;
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    ConsentCoreServiceImpl getConsentService() {

        return new ConsentCoreServiceImpl();
    }
}
