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

    AUJWTGenerator() {
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
     * @throws com.wso2.bfsi.test.framework.exception.TestFrameworkException
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

    /**
     * Return signed JWT for Authorization request
     * @param scopeString
     * @param sharingDuration
     * @param sendSharingDuration
     * @param cdrArrangementId
     * @param redirect_uri
     * @param clientId
     * @return
     */
    JWT getSignedAuthRequestObject(String scopeString, Long sharingDuration, Boolean sendSharingDuration,
                                   String cdrArrangementId, String redirect_uri, String clientId) {

        def expiryDate = Instant.now().plus(1, ChronoUnit.DAYS)

        JSONObject acr = new JSONObject().put("essential", true).put("values", new ArrayList<String>() {
            {
                add("urn:cds.au:cdr:3")
            }
        })
        JSONObject userInfoString = new JSONObject().put("given_name", null).put("family_name", null)
        JSONObject claimsString = new JSONObject().put("id_token", new JSONObject().put("acr", acr)).put("userinfo", userInfoString)
        if (sharingDuration.intValue() != 0 || sendSharingDuration) {
            claimsString.put("sharing_duration", sharingDuration)
        }
        if (!StringUtils.isEmpty(cdrArrangementId)) {
            claimsString.put("cdr_arrangement_id", cdrArrangementId)
        }
        String claims = new JSONRequestGenerator()
                .addAudience()
                .addResponseType()
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addClientID(clientId)
                .addIssuer(clientId)
                .addRedirectURI(redirect_uri)
                .addScope(scopeString)
                .addState("suite")
                .addNonce()
                .addCustomJson("claims", claimsString)
                .getJsonObject().toString()

        String payload = getSignedRequestObject(claims)

        Reporter.log("Authorisation Request Object")
        Reporter.log("JWS Payload ${new Payload(claims).toString()}")

        return SignedJWT.parse(payload)
    }

}

