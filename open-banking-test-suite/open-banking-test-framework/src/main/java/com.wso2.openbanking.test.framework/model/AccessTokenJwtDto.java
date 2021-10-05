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

package com.wso2.openbanking.test.framework.model;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import com.wso2.openbanking.test.framework.util.AppConfigReader;
import com.wso2.openbanking.test.framework.util.ConfigParser;
import com.wso2.openbanking.test.framework.util.TestConstants;
import com.wso2.openbanking.test.framework.util.TestUtil;
import org.json.JSONObject;

import java.security.*;
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

  public String getSub() {

    return sub;
  }

  public void setSub(String sub) {

    this.sub = sub;
  }

  public String getAud() {

    return aud;
  }

  public void setAud(String aud) {

    this.aud = aud;
  }

  public String getIss() {

    return iss;
  }

  public void setIss(String iss) {

    this.iss = iss;
  }

  public long getExp() {

    return exp;
  }

  public void setExp(long exp) {

    this.exp = exp;
  }

  public long getIat() {

    return iat;
  }

  public void setIat(long iat) {

    this.iat = iat;
  }

  public String getJti() {

    return jti;
  }

  public void setJti(String jti) {

    this.jti = jti;
  }

  public JWSHeader getJwsHeader() {

    return jwsHeader;
  }

  public void setJwsHeader(JWSHeader jwsHeader) {

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
      keyStore = TestUtil.getApplicationKeyStore();
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
      payload.put(TestConstants.ISSUER_KEY, iss);
      payload.put(TestConstants.SUBJECT_KEY, sub);
      payload.put(TestConstants.AUDIENCE_KEY, aud);
      payload.put(TestConstants.EXPIRE_DATE_KEY, exp);
      payload.put(TestConstants.ISSUED_AT_KEY, iat);
      payload.put(TestConstants.JTI_KEY, jti);

      Key signingKey;

      signingKey = keyStore.getKey(AppConfigReader.getApplicationKeystoreAlias(),
              AppConfigReader.getApplicationKeystorePassword().toCharArray());

      JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);
      KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
              AppConfigReader.getApplicationKeystoreAlias(),
          new KeyStore.PasswordProtection(AppConfigReader.getApplicationKeystorePassword().toCharArray()));
      Certificate certificate = pkEntry.getCertificate();
      String thumbprint = TestUtil.getJwkThumbPrint(certificate);
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

  /**
   * Method to generate a JWT token with provided issuer and audience attributes in the DTO.
   * If the issuer is given, it will be used as sub, iss.
   * If the audience is given, it will be used as aud.
   *
   * @param issuer   - issuer
   * @param audience - audience
   * @return jwt
   * @throws TestFrameworkException exception
   */
  public String getJwt(String issuer, String audience) throws TestFrameworkException {

    KeyStore keyStore;
    try {
      keyStore = TestUtil.getApplicationKeyStore();
      long currentTimeInSeconds = System.currentTimeMillis() / 1000;
      //expire time is read from configs and converted to milli seconds
      long expireTime = currentTimeInSeconds + (long)
          (ConfigParser.getInstance().getAccessTokenExpireTime() * 1000);

      if (issuer == null) {
        if (sub == null) {
          sub = AppConfigReader.getClientId();
        }
        if (iss == null) {
          iss = AppConfigReader.getClientId();
        }
      } else {
        sub = issuer;
        iss = issuer;
      }
      if (audience == null) {
        aud = ConfigParser.getInstance().getAudienceValue();
      } else {
        aud = audience;
      }
      if (exp == 0) {
        exp = expireTime;
      }
      iat = currentTimeInSeconds;
      long currentTimeInMilliseconds = System.currentTimeMillis();
      jti = String.valueOf(currentTimeInMilliseconds);

      JSONObject payload = new JSONObject();
      payload.put(TestConstants.ISSUER_KEY, iss);
      payload.put(TestConstants.SUBJECT_KEY, sub);
      payload.put(TestConstants.AUDIENCE_KEY, aud);
      payload.put(TestConstants.EXPIRE_DATE_KEY, exp);
      payload.put(TestConstants.ISSUED_AT_KEY, iat);
      payload.put(TestConstants.JTI_KEY, jti);

      Key signingKey;

      signingKey = keyStore.getKey(AppConfigReader.getApplicationKeystoreAlias(),
              AppConfigReader.getApplicationKeystorePassword().toCharArray());

      JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);
      KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
              AppConfigReader.getApplicationKeystoreAlias(),
          new KeyStore.PasswordProtection(AppConfigReader.getApplicationKeystorePassword().toCharArray()));
      Certificate certificate = pkEntry.getCertificate();
      String thumbprint = TestUtil.getJwkThumbPrint(certificate);
      JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(ConfigParser.getInstance()
          .getSigningAlgorithm()))
          .keyID(thumbprint).type(JOSEObjectType.JWT).build();
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

  public String getJwt(String issuer, long expireTime) {

      long currentTimeInSeconds = System.currentTimeMillis() / 1000;

      if (issuer == null) {
        if (sub == null) {
          sub = AppConfigReader.getClientId();
        }
        if (iss == null) {
          iss = AppConfigReader.getClientId();
        }
      } else {
        sub = issuer;
        iss = issuer;
      }
      if (aud == null) {
        aud = ConfigParser.getInstance().getAudienceValue();
      }
      if (expireTime == 0) {
        exp = currentTimeInSeconds + (long)
                (ConfigParser.getInstance().getAccessTokenExpireTime() * 1000);
      } else {
        exp = expireTime;
      }

      iat = currentTimeInSeconds;
      long currentTimeInMilliseconds = System.currentTimeMillis();
      jti = String.valueOf(currentTimeInMilliseconds);

      JSONObject payload = new JSONObject();
      payload.put(TestConstants.ISSUER_KEY, iss);
      payload.put(TestConstants.SUBJECT_KEY, sub);
      payload.put(TestConstants.AUDIENCE_KEY, aud);
      payload.put(TestConstants.EXPIRE_DATE_KEY, exp);
      payload.put(TestConstants.ISSUED_AT_KEY, iat);
      payload.put(TestConstants.JTI_KEY, jti);

      return payload.toString();
  }
}
