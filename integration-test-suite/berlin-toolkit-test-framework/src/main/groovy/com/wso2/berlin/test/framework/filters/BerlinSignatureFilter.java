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

package com.wso2.berlin.test.framework.filters;

import com.wso2.berlin.test.framework.configuration.BGConfigurationService;
import com.wso2.berlin.test.framework.constant.BerlinConstants;
import com.wso2.berlin.test.framework.utility.BerlinTestUtil;
import com.wso2.bfsi.test.framework.exception.TestFrameworkException;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Signature filter for RestAssured to include the signature to the request according to the berlin specification.
 */
public class BerlinSignatureFilter implements OrderedFilter {

    private static final String DIGEST_ALGORITHM = "SHA-256";
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static Log log = LogFactory.getLog(BerlinSignatureFilter.class);

    public BerlinSignatureFilter() {
    }

    @Override
    public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {

        if (CONTENT_TYPE_JSON.equals(req.getContentType())) {
            try {
                String digest = "";
                if (req.getBody() != null) {
                    digest = BerlinTestUtil.generateDigest(req.getBody(), DIGEST_ALGORITHM);
                } else {
                    digest = BerlinTestUtil.generateDigest("{}", DIGEST_ALGORITHM);
                }
                Header digestHeader = new Header(BerlinConstants.DIGEST, DIGEST_ALGORITHM + "=" + digest);

                KeyStore keyStore = BerlinTestUtil.getApplicationKeyStore();
                X509Certificate signatureCertificate =
                        (X509Certificate) keyStore.getCertificate(BGConfigurationService
                                .getApplicationKeystoreAlias().toString());
                if (signatureCertificate == null) {
                    throw new TestFrameworkException("Unable to read the signing certificate from the " +
                            "application keystore");
                }
                Header certificateHeader = new Header(BerlinConstants.TPP_SIGNATURE_CERTIFICATE, Base64.getEncoder()
                        .encodeToString(signatureCertificate.getEncoded()));

                Headers headers = req.getHeaders();
                Header xRequestId;
                List<Header> headerRequiredForSignature = new ArrayList<>();
                headerRequiredForSignature.add(digestHeader);

                // Validate mandatory headers.
                if (headers.hasHeaderWithName(BerlinConstants.X_REQUEST_ID)) {
                    headerRequiredForSignature.add(headers.get(BerlinConstants.X_REQUEST_ID));
                }
                if (headers.hasHeaderWithName(BerlinConstants.DATE)) {
                    headerRequiredForSignature.add(headers.get(BerlinConstants.DATE));
                }

                // Add optional headers
                if (headers.hasHeaderWithName(BerlinConstants.PSU_ID_VALUE)) {
                    headerRequiredForSignature.add(headers.get(BerlinConstants.PSU_ID_VALUE));
                }
                if (headers.hasHeaderWithName(BerlinConstants.PSU_CORPORATE_ID_HEADER)) {
                    headerRequiredForSignature.add(headers.get(BerlinConstants.PSU_CORPORATE_ID_HEADER));
                }
                if (headers.hasHeaderWithName(BerlinConstants.TPP_REDIRECT_URI_HEADER)) {
                    headerRequiredForSignature.add(headers.get(BerlinConstants.TPP_REDIRECT_URI_HEADER));
                }

                StringBuilder headerNamesRequiredForSignatureBuilder = new StringBuilder();
                for (Header header : headerRequiredForSignature) {
                    headerNamesRequiredForSignatureBuilder.append(header.getName().toLowerCase())
                            .append(" ");
                }

                String headerNamesRequiredForSignature = headerNamesRequiredForSignatureBuilder
                        .substring(0, headerNamesRequiredForSignatureBuilder.length() - 1);
                String signature = BerlinTestUtil.generateSignature(headerRequiredForSignature,
                        signatureCertificate.getSigAlgName());

                Header signatureHeader = new Header(BerlinConstants.SIGNATURE,
                        "keyId=" + "\"SN=" + signatureCertificate.getSerialNumber().toString(16) + "," +
                                "CA=" + signatureCertificate.getIssuerX500Principal().getName() + "\"," +
                                "algorithm=" + "\"rsa-sha256\", " +
                                "headers=" + "\"" + headerNamesRequiredForSignature + "\"," +
                                "signature=" + signature);

                if (log.isDebugEnabled()) {
                    log.debug("Signature header: " + signatureHeader.getValue());
                }
                req.header(digestHeader)
                        .header(signatureHeader)
                        .header(certificateHeader);
            } catch (TestFrameworkException | KeyStoreException | CertificateEncodingException e) {
                log.error("Error occurred while adding the request signature", e);
            }
        }
        Response response = ctx.next(req, res);
        return response;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

