/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.berlin.test.framework.model;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.wso2.berlin.test.framework.configuration.AppConfigReader;
import com.wso2.berlin.test.framework.configuration.ConfigParser;
import com.wso2.berlin.test.framework.constant.BerlinConstants;
import com.wso2.berlin.test.framework.utility.BerlinTestUtil;
import com.wso2.bfsi.test.framework.exception.TestFrameworkException;
import org.json.JSONObject;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;

/**
 * Model class for JWT Object of access token.
 */
public class AccessTokenJwtDto {

    private JWSHeader jwsHeader;
    private String sub;
    private String aud;
    private String iss;
    private long exp;
    private long iat;
    private String jti;

    String getSub() {

        return sub;
    }

    void setSub(String sub) {

        this.sub = sub;
    }

    String getAud() {

        return aud;
    }

    void setAud(String aud) {

        this.aud = aud;
    }

    String getIss() {

        return iss;
    }

    void setIss(String iss) {

        this.iss = iss;
    }

    long getExp() {

        return exp;
    }

    void setExp(long exp) {

        this.exp = exp;
    }

    long getIat() {

        return iat;
    }

    void setIat(long iat) {

        this.iat = iat;
    }

    String getJti() {

        return jti;
    }

    void setJti(String jti) {

        this.jti = jti;
    }

    JWSHeader getJwsHeader() {

        return jwsHeader;
    }

    void setJwsHeader(JWSHeader jwsHeader) {

        this.jwsHeader = jwsHeader;
    }

    /**
     * Method to generate a JWT token with provided attributes in the DTO.
     *
     * @return String of JWT token
     * @throws TestFrameworkException When failed to generate the JWT using the certificate
     */
    public String getJwt() throws TestFrameworkException {
        return getJwt(null);
    }

    /**
     * Method to generate a JWT token with provided attributes in the DTO.
     * If the client Id is given, it will be used as sub, iss.
     *
     * @param clientId - Client id
     * @return String of JWT token
     * @throws TestFrameworkException When failed to generate the JWT using the certificate
     */
    public String getJwt(String clientId) throws TestFrameworkException {

        KeyStore keyStore;
        try {
            keyStore = BerlinTestUtil.getApplicationKeyStore();
            long currentTimeInMilliseconds = System.currentTimeMillis();
            long currentTimeInSeconds = System.currentTimeMillis() / 1000;
            //expire time is read from configs and converted to milli seconds
            long expireTime = currentTimeInSeconds + (long)
                    (ConfigParser.getInstance().getAccessTokenExpireTime() * 1000);

            if (clientId == null) {
                if (sub == null) {
                    sub = AppConfigReader.getClientId();
                }
                if (iss == null) {
                    iss = AppConfigReader.getClientId();
                }
            } else {
                sub = clientId;
                iss = clientId;
            }
            if (aud == null) {
                aud = ConfigParser.getInstance().getAudienceValue();
            }
            if (exp == 0) {
                exp = expireTime;
            }
            iat = currentTimeInSeconds;
            if (jti == null) {
                jti = String.valueOf(currentTimeInMilliseconds);
            }

            JSONObject payload = new JSONObject();
            payload.put(BerlinConstants.ISSUER_KEY, iss);
            payload.put(BerlinConstants.SUBJECT_KEY, sub);
            payload.put(BerlinConstants.AUDIENCE_KEY, aud);
            payload.put(BerlinConstants.EXPIRE_DATE_KEY, exp);
            payload.put(BerlinConstants.ISSUED_AT_KEY, iat);
            payload.put(BerlinConstants.JTI_KEY, jti);

            Key signingKey;

            signingKey = keyStore.getKey(AppConfigReader.getApplicationKeystoreAlias(),
                    AppConfigReader.getApplicationKeystorePassword().toCharArray());

            JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);
            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                    AppConfigReader.getApplicationKeystoreAlias(),
                    new KeyStore.PasswordProtection(AppConfigReader.getApplicationKeystorePassword().toCharArray()));
            Certificate certificate = pkEntry.getCertificate();
            String thumbprint = BerlinTestUtil.getJwkThumbPrint(certificate);
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(ConfigParser.getInstance()
                    .getSigningAlgorithm())).keyID(thumbprint).build();
            JWSObject jwsObject = new JWSObject(header, new Payload(payload.toString()));
            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new TestFrameworkException("Failed to sign the object ", e);
        } catch (NoSuchAlgorithmException e) {
            throw new TestFrameworkException("Failed to identify the Algorithm ", e);
        } catch (KeyStoreException e) {
            throw new TestFrameworkException("Failed to initialize the Keystore ", e);
        } catch (UnrecoverableKeyException e) {
            throw new TestFrameworkException("Failed to recover the Key", e);
        } catch (UnrecoverableEntryException e) {
            throw new TestFrameworkException("Failed to recover the Entry", e);
        }
    }
}
