/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework.request_builder

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.SignedJWT
import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import com.wso2.openbanking.test.framework.keystore.OBKeyStore
import com.wso2.openbanking.test.framework.request_builder.JSONRequestGenerator
import com.wso2.openbanking.test.framework.request_builder.PayloadGenerator
import org.apache.commons.lang3.StringUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONObject
import org.testng.Reporter

import java.security.Key
import java.security.PrivateKey
import java.security.Security
import java.security.cert.Certificate
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Class for generate JWT
 */
class BGJWTGenerator {


    private BGConfigurationService bgConfiguration
    private List<String> scopesList = null // Scopes can be set before generate payload
    private String signingAlgorithm

    BGJWTGenerator() {
        bgConfiguration = new BGConfigurationService()
    }

    void setScopes(List<String> scopes) {
        scopesList = scopes
    }

    /**
     * Set signing algorithm
     * @param algorithm
     */
    void setSigningAlgorithm(String algorithm) {
        this.signingAlgorithm = algorithm
    }

    /**
     * Get signing algorithm for methods. IF signing algorithm is null, provide algorithm in configuration
     * @return
     */
    String getSigningAlgorithm() {
        if (signingAlgorithm == null) {
            signingAlgorithm = bgConfiguration.getCommonSigningAlgorithm()
        }
        return this.signingAlgorithm
    }


    /**
     * Get Signed object
     * @param claims
     * @return
     */
    String getSignedRequestObject(String claims) {
        Key signingKey
        JWSHeader header

        Certificate certificate = OBKeyStore.getApplicationCertificate()
        String thumbprint = OBKeyStore.getJwkThumbPrintForSHA1(certificate)
        header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                .keyID(thumbprint).type(JOSEObjectType.JWT).build()
        signingKey = OBKeyStore.getApplicationSigningKey()

        JWSObject jwsObject = new JWSObject(header, new Payload(claims))
        JWSSigner signer = new RSASSASigner((PrivateKey) signingKey)
        Security.addProvider(new BouncyCastleProvider())
        jwsObject.sign(signer)
        return jwsObject.serialize()
    }

    /**
     * Return JWT for application access token generation
     * @param clientId
     * @return
     * @throws TestFrameworkException
     */
    String getAppAccessTokenJwt(String clientId = null) throws TestFrameworkException {

        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopesList).addClientAsType()
                .addClientAssertion(payload).addRedirectUri().getPayload()
        return accessTokenJWT
    }
    String getClientAssertionJwt(String clientId=null) {
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        return payload
    }

    /**
     * Return JWT for user access token generation
     * @param code
     * @return
     * @throws TestFrameworkException
     */
    String getUserAccessTokenJwt(String code = "") throws TestFrameworkException {

        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer()
                .addSubject().addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()
        String payload = getSignedRequestObject(clientAssertion.toString())
        String accessTokenJWT = new PayloadGenerator().addGrantType().addCode(code).addScopes().addClientAsType()
                .addClientAssertion(payload).addRedirectUri().addClientID().getPayload()
        return accessTokenJWT
    }
}

