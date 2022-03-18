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

package com.wso2.openbanking.test.framework.filters;

import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import com.wso2.openbanking.test.framework.util.AppConfigReader;
import com.wso2.openbanking.test.framework.util.TestConstants;
import com.wso2.openbanking.test.framework.util.TestUtil;
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
                    digest = TestUtil.generateDigest(req.getBody(), DIGEST_ALGORITHM);
                } else {
                    digest = TestUtil.generateDigest("{}", DIGEST_ALGORITHM);
                }
                Header digestHeader = new Header(TestConstants.DIGEST, DIGEST_ALGORITHM + "=" + digest);

                KeyStore keyStore = TestUtil.getApplicationKeyStore();
                X509Certificate signatureCertificate =
                        (X509Certificate) keyStore.getCertificate(AppConfigReader.getApplicationKeystoreAlias());
                if (signatureCertificate == null) {
                    throw new TestFrameworkException("Unable to read the signing certificate from the " +
                            "application keystore");
                }
                Header certificateHeader = new Header(TestConstants.TPP_SIGNATURE_CERTIFICATE, Base64.getEncoder()
                        .encodeToString(signatureCertificate.getEncoded()));

                Headers headers = req.getHeaders();
                Header xRequestId;
                List<Header> headerRequiredForSignature = new ArrayList<>();
                headerRequiredForSignature.add(digestHeader);

                // Validate mandatory headers.
                if (headers.hasHeaderWithName(TestConstants.X_REQUEST_ID)) {
                    headerRequiredForSignature.add(headers.get(TestConstants.X_REQUEST_ID));
                }
                if (headers.hasHeaderWithName(TestConstants.DATE)) {
                    headerRequiredForSignature.add(headers.get(TestConstants.DATE));
                }

                // Add optional headers
                if (headers.hasHeaderWithName(TestConstants.PSU_ID)) {
                    headerRequiredForSignature.add(headers.get(TestConstants.PSU_ID));
                }
                if (headers.hasHeaderWithName(TestConstants.PSU_CORPORATE_ID_HEADER)) {
                    headerRequiredForSignature.add(headers.get(TestConstants.PSU_CORPORATE_ID_HEADER));
                }
                if (headers.hasHeaderWithName(TestConstants.TPP_REDIRECT_URI_HEADER)) {
                    headerRequiredForSignature.add(headers.get(TestConstants.TPP_REDIRECT_URI_HEADER));
                }

                StringBuilder headerNamesRequiredForSignatureBuilder = new StringBuilder();
                for (Header header : headerRequiredForSignature) {
                    headerNamesRequiredForSignatureBuilder.append(header.getName().toLowerCase())
                            .append(" ");
                }

                String headerNamesRequiredForSignature = headerNamesRequiredForSignatureBuilder
                        .substring(0, headerNamesRequiredForSignatureBuilder.length() - 1);
                String signature = TestUtil.generateSignature(headerRequiredForSignature,
                        signatureCertificate.getSigAlgName());

                Header signatureHeader = new Header(TestConstants.SIGNATURE,
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
