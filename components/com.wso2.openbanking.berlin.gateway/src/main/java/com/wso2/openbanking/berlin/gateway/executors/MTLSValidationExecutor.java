/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.gateway.executors;

import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.gateway.utils.GatewayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Optional;
import javax.security.cert.X509Certificate;

/**
 * This class does the organization ID validation for the transport certificate.
 */
public class MTLSValidationExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(MTLSValidationExecutor.class);

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        // Retrieve transport certificate from the request
        X509Certificate[] x509Certificates = obapiRequestContext.getClientCerts();
        X509Certificate transportCert;
        Optional<java.security.cert.X509Certificate> convertedTransportCert;

        if (x509Certificates.length != 0) {
            transportCert = x509Certificates[0];
            convertedTransportCert = CertificateValidationUtils.convert(transportCert);
        } else {
            log.error("Transport certificate not found in request context");
            GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_MISSING.toString(),
                    "Couldn't do organization ID validation since transport certificate not found in" +
                            " request context");
            return;
        }

        CertificateContent content;

        try {
            // Extract certificate content
            if (convertedTransportCert.isPresent()) {
                content = CertificateContentExtractor.extract(convertedTransportCert.get());
            } else {
                log.error("Error while processing transport certificate");
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_INVALID.toString(),
                        "Couldn't do organization ID validation since an error occurred while processing " +
                                "transport certificate");
                return;
            }
        } catch (CertificateValidationException e) {
            log.error("Error while extracting transport certificate content", e);
            GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_INVALID.toString(),
                    "Couldn't do organization ID validation since an error occurred while extracting " +
                            "transport certificate");
            return;
        }

        String clientId = obapiRequestContext.getApiRequestInfo().getConsumerKey();
        String certificateOrgId = content.getPspAuthorisationNumber();

        if (StringUtils.isBlank(certificateOrgId)) {
            log.error("Unable to retrieve organization ID from transport certificate");
            GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_INVALID.toString(),
                    "An organization ID is not found in the provided certificate");
            return;
        } else {
            if (!StringUtils.equals(clientId, certificateOrgId)) {
                log.error("Client ID is not matching with the organization ID of the provided transport" +
                        " certificate");
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_INVALID.toString(),
                        "Organization ID mismatch with Client ID");
                return;
            }
        }
        log.debug("Organization ID validation is completed");
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }
}
