/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.cache.CertificateRevocationCache;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.service.CertValidationService;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import com.wso2.openbanking.berlin.common.constants.ErrorConstants;
import com.wso2.openbanking.berlin.common.models.TPPMessage;
import com.wso2.openbanking.berlin.gateway.exceptions.DigestMissingException;
import com.wso2.openbanking.berlin.gateway.exceptions.DigestValidationException;
import com.wso2.openbanking.berlin.gateway.exceptions.SignatureCertMissingException;
import com.wso2.openbanking.berlin.gateway.exceptions.SignatureMissingException;
import com.wso2.openbanking.berlin.gateway.exceptions.SignatureValidationException;
import com.wso2.openbanking.berlin.gateway.utils.GatewayUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This executor validates the message signature and digest. Also validates the signing certificate for revocation.
 */
public class SignatureValidationExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(SignatureValidationExecutor.class);

    private static final String DIGEST_HEADER = "Digest";
    private static final String PSU_ID_HEADER = "PSU-ID";
    private static final String PSU_CORPORATE_ID_HEADER = "PSU-Corporate-ID";
    private static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";
    private static final String DATE_HEADER = "Date";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER = "TPP-Signature-Certificate";

    private static final String SIGNATURE_HEADER = "Signature";
    private static final String SIGNATURE_ELEMENT = "signature";
    private static final String HEADERS_ELEMENT = "headers";
    private static final String KEY_ID_ELEMENT = "keyId";
    private static final String SN = "SN";
    private static final String CA = "CA";

    private static final String CERT_BEGIN_STRING = "-----BEGIN CERTIFICATE-----";
    private static final String CERT_END_STRING = "-----END CERTIFICATE-----";
    private static final String X509_CERT_INSTANCE_NAME = "X.509";

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        if (!obapiRequestContext.isError()) {

            Map<String, String> headersMap = obapiRequestContext.getMsgInfo().getHeaders();
            String signatureCertificateHeader = headersMap.get(TPP_SIGNATURE_CERTIFICATE_HEADER);
            String requestPayload = obapiRequestContext.getRequestPayload();

            // Validate whether the required headers are present.
            try {
                validateHeaders(headersMap);
            } catch (SignatureCertMissingException e) {
                log.error(ErrorConstants.SIGNING_CERT_MISSING, e);
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_MISSING.toString(),
                        ErrorConstants.SIGNING_CERT_MISSING);
                return;
            } catch (DigestMissingException e) {
                log.error(ErrorConstants.DIGEST_HEADER_MISSING, e);
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.SIGNATURE_INVALID.toString(),
                        ErrorConstants.DIGEST_HEADER_MISSING);
                return;
            } catch (SignatureMissingException e) {
                log.error(ErrorConstants.SIGNATURE_HEADER_MISSING, e);
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.SIGNATURE_MISSING.toString(),
                        ErrorConstants.SIGNATURE_HEADER_MISSING);
                return;
            } catch (SignatureValidationException e) {
                log.error(ErrorConstants.INVALID_SIGNATURE_HEADER, e);
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.FORMAT_ERROR.toString(),
                        ErrorConstants.X_REQUEST_ID_MISSING);
                return;
            }

            try {
                X509Certificate x509Certificate = CertificateUtils
                        .parseCertificate(signatureCertificateHeader);
                if (x509Certificate == null) {
                    log.error(ErrorConstants.CERT_PARSE_EROR);
                    GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_INVALID.toString(),
                            ErrorConstants.CERT_PARSE_EROR);
                    return;
                }

                // Expiry validation
                validateCertExpiration(x509Certificate, obapiRequestContext);

                // Revocation validation
                if (!isValidCertStatus(x509Certificate)) {
                    log.error(ErrorConstants.SIGNING_CERT_REVOKED);
                    GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_REVOKED.toString(),
                            ErrorConstants.SIGNING_CERT_REVOKED);
                    return;
                }

                // Digest validation
                String digestHeader = headersMap.get(DIGEST_HEADER);
                if (!validateDigest(digestHeader, requestPayload)) {
                    log.error(ErrorConstants.DIGEST_VALIDATION_ERROR);
                    GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.SIGNATURE_INVALID.toString(),
                            ErrorConstants.DIGEST_VALIDATION_ERROR);
                    return;
                }

                // Validate signature
                if (!validateSignature(headersMap, x509Certificate)) {
                    log.error(ErrorConstants.SIGNATURE_VERIFICATION_FAIL);
                    GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.SIGNATURE_INVALID.toString(),
                            ErrorConstants.SIGNATURE_VERIFICATION_FAIL);
                    return;
                } else {
                    log.debug("Signature validation successfully completed");
                }
            } catch (DigestValidationException e) {
                log.error(ErrorConstants.INVALID_DIGEST_HEADER, e);
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.SIGNATURE_INVALID.toString(),
                        ErrorConstants.INVALID_DIGEST_HEADER);
            } catch (OpenBankingException | CertificateException e) {
                log.error(ErrorConstants.SIGNING_CERT_INVALID, e);
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_INVALID.toString(),
                        ErrorConstants.SIGNING_CERT_INVALID);
            } catch (SignatureValidationException e) {
                log.error(ErrorConstants.INVALID_SIGNATURE_HEADER, e);
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.SIGNATURE_INVALID.toString(),
                        ErrorConstants.INVALID_SIGNATURE_HEADER);
            }
        }
    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        if (!obapiRequestContext.isError()) {

            // Retrieve transport certificate from the request
            javax.security.cert.X509Certificate[] x509Certificates = obapiRequestContext.getClientCerts();
            javax.security.cert.X509Certificate transportCert;
            Optional<java.security.cert.X509Certificate> convertedTransportCert;

            if (x509Certificates.length != 0) {
                transportCert = x509Certificates[0];
                convertedTransportCert = CertificateValidationUtils.convert(transportCert);
            } else {
                log.error("Transport certificate not found in request context");
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_MISSING.toString(),
                        "Transport certificate is missing. Cannot do organization ID validation.");
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
                            "Invalid transport certificate. Cannot do organization ID validation.");
                    return;
                }
            } catch (CertificateValidationException e) {
                log.error("Error while extracting transport certificate content", e);
                GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_INVALID.toString(),
                        "Transport certificate is invalid. Cannot do organization ID validation.");
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
                    log.error("Client ID: " + clientId + " is not matching with the organization ID: "
                            + certificateOrgId + " of transport certificate");
                    GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_INVALID.toString(),
                            "Organization ID mismatch with Client ID");
                    return;
                }
            }
            log.debug("Organization ID validation is completed");
        }
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Validate whether all the required headers are present in the request.
     *
     * @param headersMap request headers map
     * @throws SignatureMissingException thrown if the signature is missing
     * @throws DigestMissingException thrown if the digest is missing
     * @throws SignatureCertMissingException thrown if the signature certificate is missing
     * @throws SignatureValidationException thrown if the mandatory headers of the signature are not present
     */
    protected void validateHeaders(Map<String, String> headersMap)
            throws SignatureMissingException, DigestMissingException, SignatureCertMissingException,
            SignatureValidationException {

        if (StringUtils.isBlank(headersMap.get(SIGNATURE_HEADER))) {
            log.error(ErrorConstants.SIGNATURE_HEADER_MISSING);
            throw new SignatureMissingException(ErrorConstants.SIGNATURE_HEADER_MISSING);
        }
        if (StringUtils.isBlank(headersMap.get(DIGEST_HEADER))) {
            log.error(ErrorConstants.DIGEST_HEADER_MISSING);
            throw new DigestMissingException(ErrorConstants.DIGEST_HEADER_MISSING);
        }
        if (StringUtils.isBlank(headersMap.get(TPP_SIGNATURE_CERTIFICATE_HEADER))) {
            log.error(ErrorConstants.SIGNING_CERT_MISSING);
            throw new SignatureCertMissingException(ErrorConstants.SIGNING_CERT_MISSING);
        }
        if (StringUtils.isBlank(headersMap.get(X_REQUEST_ID_HEADER))) {
            log.error(ErrorConstants.X_REQUEST_ID_MISSING);
            throw new SignatureValidationException(ErrorConstants.X_REQUEST_ID_MISSING);
        }
        if (StringUtils.isBlank(headersMap.get(DATE_HEADER))) {
            log.error(ErrorConstants.DATE_HEADER_MISSING);
            throw new SignatureValidationException(ErrorConstants.DATE_HEADER_MISSING);
        }
    }

    /**
     * Validating the digest of the request.
     *
     * @param digestHeader digest header sent with the request
     * @param requestPayload the request payload
     * @return return true if the digest validation is a success, false otherwise
     * @throws DigestValidationException thrown if an error occurs while digest validation
     */
    protected boolean validateDigest(String digestHeader, String requestPayload) throws DigestValidationException {
        try {
            String[] digestAttribute = digestHeader.split("=", 2);
            if (digestAttribute.length != 2) {
                log.error(ErrorConstants.INVALID_DIGEST_HEADER);
                throw new DigestValidationException(ErrorConstants.INVALID_DIGEST_HEADER);
            }
            String digestAlgorithm = digestAttribute[0].trim();
            String digestValue = digestAttribute[1].trim();

            MessageDigest messageDigest;
            CommonConfigParser configParser = getConfigParser();
            if (configParser.getSupportedHashAlgorithms().contains(digestAlgorithm)) {
                messageDigest = MessageDigest.getInstance(digestAlgorithm);
            } else {
                log.error(ErrorConstants.INVALID_DIGEST_ALGORITHM);
                throw new DigestValidationException(ErrorConstants.INVALID_DIGEST_ALGORITHM);
            }

            byte[] digestHash;

            if (StringUtils.isBlank(requestPayload)) {
                digestHash = messageDigest.digest("{}".getBytes(StandardCharsets.UTF_8));
            } else {
                digestHash = messageDigest.digest(requestPayload.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder digestHashHex = new StringBuilder();
            for (byte b : digestHash) {
                digestHashHex.append(String.format("%02x", b));
            }
            String generatedDigest = Base64.getEncoder()
                    .encodeToString(new BigInteger(digestHashHex.toString(), 16).toByteArray());
            if (generatedDigest.equals(digestValue)) {
                log.debug("Digest validation successfully completed");
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            log.error(ErrorConstants.INVALID_DIGEST_ALGORITHM, e);
            throw new DigestValidationException(ErrorConstants.INVALID_DIGEST_ALGORITHM);
        }
        return false;
    }

    /**
     * Validate the signature of the request with the public key of the TPP certificate.
     *
     * @param requestHeaders the headers passed through the request
     * @param x509Certificate  the signature certificate passed through the TPP-Signature-Certificate header
     * @return true if the signature validation is successful, false otherwise
     * @throws SignatureValidationException when an error occurs during signature validation
     */
    protected boolean validateSignature(Map<String, String> requestHeaders,
                                      java.security.cert.X509Certificate x509Certificate)
            throws SignatureValidationException {

        try {
            // For signature validation, the order of the headers are considered.
            // A case-insensitive treeMap is initialized since the headers need to be retrieved without considering
            // case-sensitivity for generating signing string.
            Map<String, String> orderedRequestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            orderedRequestHeaders.putAll(requestHeaders);

            String signatureHeader = orderedRequestHeaders.get(SIGNATURE_HEADER);
            String[] signatureElements = signatureHeader.split("\",");
            Map<String, String> signatureMap = getSignatureMap(signatureElements);

            if ((!signatureMap.containsKey(KEY_ID_ELEMENT)) || (!signatureMap.containsKey(HEADERS_ELEMENT)) ||
                    !signatureMap.containsKey(SIGNATURE_ELEMENT)) {
                log.error("Provided signature does not contain the required headers");
                throw new SignatureValidationException("Invalid signature header. Required signature header" +
                        " elements cannot be found");
            }
            if (!validateKeyId(signatureMap.get(KEY_ID_ELEMENT).split(",", 2), x509Certificate)) {
                log.error("Signature contains an invalid keyId");
                throw new SignatureValidationException("Invalid keyId element in the Signature header");
            }

            Signature signature;
            String requestSignatureAlgorithm = x509Certificate.getSigAlgName();
            CommonConfigParser configParser = getConfigParser();
            if (configParser.getSupportedSignatureAlgorithms().contains(requestSignatureAlgorithm)) {
                signature = Signature.getInstance(requestSignatureAlgorithm);
            } else {
                log.error("Provided signature algorithm is not supported");
                throw new SignatureValidationException("Request signature algorithm " +
                        requestSignatureAlgorithm + " is not supported");
            }

            signature.initVerify(x509Certificate.getPublicKey());
            String[] headerElements;
            headerElements = signatureMap.get(HEADERS_ELEMENT).split(" ");
            if (!validateHeaderElements(headerElements, orderedRequestHeaders)) {
                log.error("Signature header contains invalid header/s");
                throw new SignatureValidationException("Invalid headers element in the Signature header");
            }

            byte[] decodedSignature = Base64.getDecoder()
                    .decode(signatureMap.get(SIGNATURE_ELEMENT).getBytes(StandardCharsets.UTF_8));
            signature.update(generateSigningString(headerElements, orderedRequestHeaders)
                    .getBytes(StandardCharsets.UTF_8));
            return signature.verify(decodedSignature);
        } catch (NoSuchAlgorithmException e) {
            log.error(ErrorConstants.INVALID_SIGNATURE_ALGORITHM, e);
            throw new SignatureValidationException(ErrorConstants.INVALID_SIGNATURE_ALGORITHM, e);
        } catch (SignatureException e) {
            log.error(ErrorConstants.SIGNATURE_VERIFICATION_FAIL, e);
            throw new SignatureValidationException(ErrorConstants.SIGNATURE_VERIFICATION_FAIL, e);
        } catch (InvalidKeyException e) {
            log.error("Invalid public key", e);
            throw new SignatureValidationException("Invalid public key", e);
        }
    }

    /**
     * Returns a map with the signature elements.
     *
     * @param signatureElements the elements of the signature of the request
     * @return the signature elements map
     */
    private Map<String, String> getSignatureMap(String[] signatureElements) {
        Map<String, String> signatureMap = new HashMap<>();
        for (String signatureElement : signatureElements) {
            String[] signatureAttributes = signatureElement.replaceAll("\"", "").split("=",
                    2);
            if (!signatureMap.containsKey(signatureAttributes[0].trim())) {
                signatureMap.put(signatureAttributes[0].trim(), signatureAttributes[1].trim());
            }
        }
        return signatureMap;
    }

    /**
     * Validate the keyId passed through the request with the signature certificate.
     *
     * @param keyIdElements a map of values passed through the keyId signature header
     * @param x509Certificate the signature certificate
     * @return true if the keyId validation is successful, false otherwise
     */
    private boolean validateKeyId(String[] keyIdElements, X509Certificate x509Certificate) {
        Map<String, String> keyIdMap = Arrays.stream(keyIdElements)
                .map(element -> element.trim().split("=", 2))
                .collect(Collectors.toMap(keyValue -> keyValue[0].trim(), keyValue -> keyValue[1].trim(), (a, b) -> b));

        if (keyIdMap.get(SN) == null || keyIdMap.get(CA) == null) {
            return false;
        }
        // validate the hexadecimal coded SN in request against the SN retrieved from the cert in the Hexadecimal coding
        if (!new BigInteger(String.valueOf(keyIdMap.get(SN)), 16).equals(x509Certificate.getSerialNumber())) {
            return false;
        }
        return keyIdMap.get(CA).equals(x509Certificate.getIssuerX500Principal().getName());
    }

    /**
     * Validate whether all the mandatory headers are included in the signature.
     *
     * @param headerElements header names that are included in the signature by the client
     * @param requestHeaders request headers
     * @return true if all the required headers are included in the signature, false otherwise
     */
    private boolean validateHeaderElements(String[] headerElements, Map<String, String> requestHeaders)
            throws SignatureValidationException {

        // According to the spec, the headers elements defined in signature header should be lowercase
        for (String headerElement : headerElements) {
            if (!StringUtils.isAllLowerCase(headerElement.replace("-", ""))) {
                log.error("Header elements present in the signature are not lowercase");
                throw new SignatureValidationException("The headers element of the signature element should " +
                        "contain lowercase headers");
            }
        }

        boolean containsDigestHeader = false;
        boolean containsRequestIDHeader = false;
        boolean containsPSUIDHeader = false;
        boolean containsPSUCorporateIDHeader = false;
        boolean containsTPPRedirectURIHeader = false;
        boolean containsDateHeader = false;

        for (String headerElement : headerElements) {
            if (headerElement.equalsIgnoreCase(DIGEST_HEADER)) {
                containsDigestHeader = true;
            } else if (headerElement.equalsIgnoreCase(PSU_ID_HEADER)) {
                containsPSUIDHeader = true;
            } else if (headerElement.equalsIgnoreCase(PSU_CORPORATE_ID_HEADER)) {
                containsPSUCorporateIDHeader = true;
            } else if (headerElement.equalsIgnoreCase(X_REQUEST_ID_HEADER)) {
                containsRequestIDHeader = true;
            } else if (headerElement.equalsIgnoreCase(TPP_REDIRECT_URI_HEADER)) {
                containsTPPRedirectURIHeader = true;
            } else if (headerElement.equalsIgnoreCase(DATE_HEADER)) {
                containsDateHeader = true;
            }
        }

        // Check whether the mandated headers present in the headers element in the Signature header
        if (!containsDigestHeader || !containsRequestIDHeader || !containsDateHeader) {
            return false;
        }

        // If any of the headers PSU-ID, PSU-Corporate-ID or TPP-Redirect-URI contains in the request, they need to
        // be also contained in the headers element of the Signature header
        if ((requestHeaders.get(PSU_ID_HEADER) != null && !containsPSUIDHeader) ||
                (requestHeaders.get(PSU_CORPORATE_ID_HEADER) != null && !containsPSUCorporateIDHeader) ||
                (requestHeaders.get(TPP_REDIRECT_URI_HEADER) != null && !containsTPPRedirectURIHeader)) {
            return false;
        }
        return true;
    }

    /**
     * Generate the signing string for signature comparison.
     *
     * @param headerElements an array of values passed through the header element in the signature
     * @param requestHeaders the headers passed through the request
     * @return the signing string of the request
     */
    private String generateSigningString(String[] headerElements, Map<String, String> requestHeaders) {

        StringBuilder signingString = new StringBuilder();
        for (String header : headerElements) {
            if (requestHeaders.get(header) == null) {
                log.error("Required header element not passed through the request header");
                break;
            }
            signingString.append(StringUtils.lowerCase(header))
                    .append(": ")
                    .append((requestHeaders).get(header))
                    .append("\n");
        }
        return signingString.substring(0, signingString.length() - 1);
    }

    /**
     * Validates the signature certificate for expiration.
     *
     * @param signatureCertificate the signature certificate
     * @param obapiRequestContext the request context
     */
    private void validateCertExpiration(X509Certificate signatureCertificate, OBAPIRequestContext obapiRequestContext) {

        if (CertificateValidationUtils.isExpired(signatureCertificate)) {
            log.error(ErrorConstants.SIGNATURE_CERTIFICATE_EXPIRED);
            GatewayUtils.handleFailure(obapiRequestContext, TPPMessage.CodeEnum.CERTIFICATE_EXPIRED.toString(),
                    ErrorConstants.SIGNATURE_CERTIFICATE_EXPIRED);
        } else {
            log.debug("TPP-Signature-Certificate expiry validation completed");
        }
    }

    /**
     * Checks the certificate validity of a given certificate. For this validation, the immediate issuer
     * of the peer certificate must be present in the trust store.
     *
     * @param signatureCertificate the signature certificate
     * @return true if certificate is in valid status, false otherwise
     * @throws CertificateEncodingException thrown if the certificate encoding is failed
     */
    @Generated(message = "Excluding since this method is already covered from other tests")
    private boolean isValidCertStatus(X509Certificate signatureCertificate)
            throws CertificateEncodingException {

        // Initializing certificate cache and cache key
        CertificateRevocationCache certificateRevocationCache = CertificateRevocationCache.getInstance();

        // Generating the certificate thumbprint to use as cache key
        String certificateValidationCacheKeyStr = DigestUtils.sha256Hex(signatureCertificate.getEncoded());
        GatewayCacheKey certificateValidationCacheKey =
                GatewayCacheKey.of(certificateValidationCacheKeyStr);

        // Executing certificate revocation process or retrieve last status from cache
        if (certificateRevocationCache.getFromCache(certificateValidationCacheKey) != null) {
            // previous result is present in cache, return result
            return certificateRevocationCache.getFromCache(certificateValidationCacheKey);
        } else {
            final boolean result = isCertRevocationSuccess(signatureCertificate);
            if (result) {
                // Adding result to cache
                certificateRevocationCache.addToCache(certificateValidationCacheKey, true);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the certificate is revoked or not.
     *
     * @param certificate signature certificate
     * @return true is the certificate is valid, false if revoked
     */
    @Generated(message = "Excluding since this method is already covered from other tests")
    private boolean isCertRevocationSuccess(X509Certificate certificate) {

        int certificateRevocationValidationRetryCount = Integer.parseInt((String) getOpenBankingConfigParser()
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_RETRY_COUNT));

        boolean isCertificateRevocationValidationEnabled = Boolean.parseBoolean((String) getOpenBankingConfigParser()
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_ENABLED));

        boolean isValid;
        // Check certificate revocation status.
        if (isCertificateRevocationValidationEnabled) {
            log.debug("Client certificate revocation validation is enabled");

            // Skip certificate revocation validation if the certificate is self-signed.
            if (certificate.getSubjectDN().getName().equals(certificate.getIssuerDN().getName())) {
                log.debug("Client certificate is self signed. Hence, excluding the certificate revocation" +
                        " validation");
                return true;
            }

            /*
             *  Skip certificate revocation validation if the certificate issuer is listed to exclude from
             *  revocation validation in open-banking.xml under
             *  CertificateManagement.RevocationValidationExcludedIssuers configuration.
             *
             *  This option can be used to skip certificate revocation validation for certificates which have been
             *  issued by a trusted locally generated CA.
             */
            List<String> revocationValidationExcludedIssuers = getConfigParser()
                    .getRevocationValidationExcludedIssuers();

            if (revocationValidationExcludedIssuers != null
                    && revocationValidationExcludedIssuers.contains(certificate.getIssuerDN().getName())) {
                log.debug("The issuer of the client certificate has been configured to exclude from " +
                        "certificate revocation validation. Hence, excluding the certificate " +
                        "revocation validation");
                return true;
            }

            // Get issuer certificate from the truststore to continue with the certificate validation.
            X509Certificate issuerCertificate;
            try {
                issuerCertificate = CertificateValidationUtils
                        .getIssuerCertificateFromTruststore(certificate);
            } catch (CertificateValidationException e) {
                log.error("Issuer certificate retrieving failed for client certificate with" +
                        " serial number " + certificate.getSerialNumber() + " issued by the CA " +
                        certificate.getIssuerDN().toString(), e);
                return false;
            }

            isValid = CertValidationService.getInstance().verify(certificate, issuerCertificate,
                    certificateRevocationValidationRetryCount);
        } else {
            isValid = true;
        }
        log.debug("Stored certificate validation status in cache");
        return isValid;
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    protected CommonConfigParser getConfigParser() {

        return CommonConfigParser.getInstance();
    }

    @Generated(message = "Excluded from coverage since this is used for testing purposes")
    protected OpenBankingConfigParser getOpenBankingConfigParser() {

        return OpenBankingConfigParser.getInstance();
    }
}
