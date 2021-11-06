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

package com.wso2.openbanking.test.framework.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Class to contain utility classes used for Test Framework.
 */
public class TestUtil {

  private static final String B64_CLAIM_KEY = "b64";

  private static final Log log = LogFactory.getLog(TestUtil.class);
  private static SSLSocketFactory sslSocketFactory;

  // Static initialize the SSL socket factory if MTLS is enabled.
  public static SSLSocketFactory getSslSocketFactory() {
    if (AppConfigReader.isMTLSEnabled()) {
      try {
        SslSocketFactoryCreator sslSocketFactoryCreator = new SslSocketFactoryCreator();
        sslSocketFactory = sslSocketFactoryCreator.create();

        // Skip hostname verification.
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory
                .ALLOW_ALL_HOSTNAME_VERIFIER);
      } catch (TestFrameworkException e) {
        log.error("Unable to create the SSL socket factory", e);
      }
    }
    return sslSocketFactory;
  }

  /**
   * Get request with request context.
   * @param keystoreLocation keystore file path.
   * @param keystorePassword keystore password.
   * @return sslSocketFactory.
   */
  public static SSLSocketFactory getSslSocketFactory(String keystoreLocation, String keystorePassword) {

    if (AppConfigReader.isMTLSEnabled()) {
      try {
        SslSocketFactoryCreator sslSocketFactoryCreator = new SslSocketFactoryCreator();
        sslSocketFactory = sslSocketFactoryCreator.create(keystoreLocation, keystorePassword);

        // Skip hostname verification.
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory
                .ALLOW_ALL_HOSTNAME_VERIFIER);
      } catch (TestFrameworkException e) {
        log.error("Unable to create the SSL socket factory", e);
      }
    }
    return sslSocketFactory;
  }

  /**
   * Utility method to Stringify a list of String.
   *
   * @param params    List of Strings
   * @param delimiter delimiter between params
   * @return Final String with all the values
   */
  public static String getParamListAsString(List<String> params, char delimiter) {

    String result = "";
    for (String param : params) {
      result = result.concat(param + delimiter);
    }
    return result.substring(0, result.length() - 1);
  }

  /**
   * Method to extract the thumbprint of SHA-256 type certificates.
   *
   * @param certificate SHA-256 type certificates
   * @return String thumbprint
   * @throws TestFrameworkException When failed to extract thumbprint
   */
  public static String getThumbPrint(Certificate certificate) throws TestFrameworkException {

    MessageDigest digestValue;
    try {
      digestValue = MessageDigest.getInstance("SHA-256");
      byte[] der = certificate.getEncoded();
      digestValue.update(der);
      byte[] digestInBytes = digestValue.digest();

      return hexify(digestInBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new TestFrameworkException("Failed to identify the Algorithm ", e);
    } catch (CertificateEncodingException e) {
      throw new TestFrameworkException("Failed to identify the Certificate encoding ", e);
    }

  }

  /**
   * Method to extract the SHA-1 JWK thumbprint from certificates.
   *
   * @param certificate x509 certificate
   * @return String thumbprint
   * @throws TestFrameworkException When failed to extract thumbprint
   */
  public static String getJwkThumbPrint(Certificate certificate) throws TestFrameworkException {

    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      ByteArrayInputStream bais = new ByteArrayInputStream(certificate.getEncoded());
      X509Certificate x509 = (X509Certificate) cf.generateCertificate(bais);
      Base64URL jwkThumbprint = RSAKey.parse(x509).computeThumbprint("SHA-1");
      return jwkThumbprint.toString();
    } catch (CertificateException | JOSEException e) {
      throw new TestFrameworkException("Error occurred while generating SHA-1 JWK thumbprint", e);
    }

  }

  /**
   * Generate digest for a given payload.
   *
   * @param payload   digest payload
   * @param algorithm digest alogrithm (i.e. SHA-256)
   * @return base64 encoded digest value
   * @throws TestFrameworkException exception
   */
  public static String generateDigest(String payload, String algorithm)
          throws TestFrameworkException {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
      byte[] digestHash = messageDigest.digest(payload.getBytes(StandardCharsets.UTF_8));

      if (log.isDebugEnabled()) {
        log.debug("Digest payload: " + payload);
      }
      StringBuilder digestHashHex = new StringBuilder();
      for (byte b : digestHash) {
        digestHashHex.append(String.format("%02x", b));
      }
      return Base64.getEncoder()
              .encodeToString(new BigInteger(digestHashHex.toString(), 16).toByteArray());
    } catch (NoSuchAlgorithmException e) {
      throw new TestFrameworkException("Error occurred while generating the digest", e);
    }
  }

  /**
   * Generate a signature for a given headers.
   *
   * @param headers            headers that are required to sign
   * @param signatureAlgorithm signature algorithm
   * @return signature string
   * @throws TestFrameworkException exception
   */
  public static String generateSignature(List<Header> headers,
                                         String signatureAlgorithm) throws TestFrameworkException {
    try {
      Signature rsa = Signature.getInstance(signatureAlgorithm);
      KeyStore keyStore = getApplicationKeyStore();
      PrivateKey privateKey = (PrivateKey) keyStore.getKey(AppConfigReader.getApplicationKeystoreAlias(),
              AppConfigReader.getApplicationKeystorePassword().toCharArray());
      rsa.initSign(privateKey);

      StringBuilder signatureHeader = new StringBuilder();
      for (Header header : headers) {
        signatureHeader.append(header.getName().toLowerCase())
                .append(": ")
                .append(header.getValue())
                .append("\n");
      }
      String signingPayload = signatureHeader.substring(0, signatureHeader.length() - 1);

      if (log.isDebugEnabled()) {
        log.debug("Signing payload: " + signingPayload);
      }
      rsa.update(signingPayload.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(rsa.sign());
    } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException
            | InvalidKeyException | SignatureException e) {
      throw new TestFrameworkException("Unable to generate the signature", e);
    }
  }

  /**
   * Method to hexify array of bytes.
   *
   * @param bytes Required byte[]
   * @return hexified String
   */
  private static String hexify(byte[] bytes) {

    if (bytes == null) {
      String errorMsg = "Invalid byte array: 'NULL'";
      throw new IllegalArgumentException(errorMsg);
    } else {
      char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6',
              '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
      StringBuilder buf = new StringBuilder(bytes.length * 2);

      for (byte aByte : bytes) {
        buf.append(hexDigits[(aByte & 240) >> 4]);
        buf.append(hexDigits[aByte & 15]);
      }
      return buf.toString();
    }
  }

  /**
   * Method to obtain application keystore from the configured location.
   *
   * @return Keystore object
   * @throws TestFrameworkException When failed to find the keystore of required type
   */
  public static KeyStore getApplicationKeyStore() throws TestFrameworkException {

    try (InputStream inputStream = new FileInputStream(AppConfigReader.getApplicationKeystoreLocation())) {

      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(inputStream, AppConfigReader.getApplicationKeystorePassword().toCharArray());
      return keyStore;
    } catch (IOException e) {
      throw new TestFrameworkException("Failed to load Keystore file from the location", e);
    } catch (CertificateException e) {
      throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
    } catch (NoSuchAlgorithmException e) {
      throw new TestFrameworkException("Failed to identify the Algorithm ", e);
    } catch (KeyStoreException e) {
      throw new TestFrameworkException("Failed to initialize the Keystore ", e);
    }

  }

  /**
   * Method to obtain invalid keystore.
   *
   * @return Keystore object
   * @throws TestFrameworkException When failed to find the keystore of required type
   */
  public static KeyStore getInvalidKeyStore() throws TestFrameworkException {

    try (InputStream inputStream = new FileInputStream(TestConstants.PATH_TO_INVALID_KEYSTORE)) {

      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(inputStream, TestConstants.INVALID_KEYSTORE_PASSWORD.toCharArray());
      return keyStore;
    } catch (IOException e) {
      throw new TestFrameworkException("Failed to load Keystore file from the location", e);
    } catch (CertificateException e) {
      throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
    } catch (NoSuchAlgorithmException e) {
      throw new TestFrameworkException("Failed to identify the Algorithm ", e);
    } catch (KeyStoreException e) {
      throw new TestFrameworkException("Failed to initialize the Keystore ", e);
    }

  }

  /**
   * Method to extract code value from the redirected URL.
   *
   * @param codeUrl URL
   * @return code String
   */
  public static Optional<String> getCodeFromUrl(String codeUrl) throws URISyntaxException {

    URIBuilder uriBuilder = new URIBuilder(codeUrl);

    return uriBuilder.getQueryParams()
            .stream()
            .filter(pair -> "code".equals(pair.getName()))
            .findFirst()
            .map(NameValuePair::getValue);
  }

  /**
   * Extract Authorisation code from redirect URL of hybriod flow response.
   *
   * @param codeUrl redirection url.
   * @return authorisation code.
   */
  public static String getHybridCodeFromUrl(String codeUrl) {

    return codeUrl.split("#")[1].split("&")[0].split("code")[1].substring(1);
  }

  /**
   * Method to process a JSON Object and return a preferred value.
   *
   * @param response JSON Response
   * @param jsonPath Path of Required value
   * @return Value of requested key
   */
  public static String parseResponseBody(Response response, String jsonPath) {

    return response.jsonPath().getString(jsonPath);
  }

  /**
   * Method to construct the JWS detaching the payload part from it.
   *
   * @param jws Signed JWS signature
   * @return the detached JWS
   * @throws TestFrameworkException exception
   */
  public static String constructDetachedJws(String jws) throws TestFrameworkException {

    if (StringUtils.isEmpty(jws)) {
      throw new TestFrameworkException("JWS is required for detached JWS construction");
    }

    // Split JWS by `.` deliminator
    String[] jwsParts = jws.split("\\.");

    // Redact payload and reconstruct JWS.
    if (jwsParts.length > 1) {
      jwsParts[1] = StringUtils.EMPTY;

      // Reconstruct JWS with `.` deliminator
      return String.join(".", jwsParts);
    }

    throw new TestFrameworkException("Required number of parts not "
            + "found in JWS for reconstruction");

  }

  /**
   * Generate X-JWS Signature.
   *
   * @param header      Headers from the request
   * @param requestBody Request payload
   * @return x-jws-signature
   */
  public static String generateXjwsSignature(String header, String requestBody) {

    char[] keyStorePassword = AppConfigReader.getApplicationKeystorePassword().toCharArray();
    String keyStoreName = AppConfigReader.getApplicationKeystoreAlias();

    try {

      KeyStore keyStore = TestUtil.getApplicationKeyStore();
      Key key = keyStore.getKey(keyStoreName, keyStorePassword);

      if (key instanceof RSAPrivateKey) {

        JWSHeader jwsHeader = JWSHeader.parse(header);
        Object b64ValueObject = jwsHeader.getCustomParam(B64_CLAIM_KEY);
        boolean b64Value = b64ValueObject != null ? ((Boolean) b64ValueObject) : true;

        // Create a new JsonWebSignature
        JsonWebSignature jws = new JsonWebSignature();

        // Set the payload, or signed content, on the JWS object
        jws.setPayload(requestBody);

        // Set the signature algorithm on the JWS that will integrity protect the payload
        jws.setAlgorithmHeaderValue(String.valueOf(jwsHeader.getAlgorithm()));

        // Setting headers
        jws.setKeyIdHeaderValue(jwsHeader.getKeyID());
        jws.setCriticalHeaderNames(jwsHeader.getCriticalParams().toArray(new String[0]));

        if (b64ValueObject != null) {
          jws.getHeaders().setObjectHeaderValue(B64_CLAIM_KEY, b64Value);
        }

        for (Map.Entry<String, Object> entry : jwsHeader.getCustomParams().entrySet()) {
          jws.getHeaders().setObjectHeaderValue(entry.getKey(), entry.getValue());
        }

        // Set the signing key on the JWS
        jws.setKey(key);

        // Sign the JWS and produce the detached JWS representation, which
        // is a string consisting of two dots ('.') separated base64url-encoded
        // parts in the form Header..Signature
        return jws.getDetachedContentCompactSerialization();
      }

    } catch (ParseException | KeyStoreException e) {
      log.error("Error occurred while reading the KeyStore file", e);
    } catch (NoSuchAlgorithmException | JoseException e) {
      log.error("Error occurred while signing", e);
    } catch (UnrecoverableEntryException e) {
      log.error("Error occurred while retrieving the cert key", e);
    } catch (TestFrameworkException e) {
      log.error("Error occurred while reading the certificate thumb print", e);
    }

    return " ";
  }

  /**
   * Signs the JWS with an invalid key.
   *
   * @param header      Headers from the request
   * @param requestBody Request payload
   * @return x-jws-signature
   */
  public static String generateInvalidXjwsSignature(String header, String requestBody) {

    char[] keyStorePassword = TestConstants.INVALID_KEYSTORE_PASSWORD.toCharArray();
    String keyStoreName = TestConstants.INVALID_KEYSTORE_ALIAS;

    try {
      KeyStore keyStore = TestUtil.getInvalidKeyStore();
      Key key = keyStore.getKey(keyStoreName, keyStorePassword);

      if (key instanceof RSAPrivateKey) {

        JWSHeader jwsHeader = JWSHeader.parse(header);
        Object b64ValueObject = jwsHeader.getCustomParam(B64_CLAIM_KEY);
        boolean b64Value = b64ValueObject != null ? ((Boolean) b64ValueObject) : true;

        // Create a new JsonWebSignature
        JsonWebSignature jws = new JsonWebSignature();

        // Set the payload, or signed content, on the JWS object
        jws.setPayload(requestBody);

        // Set the signature algorithm on the JWS that will integrity protect the payload
        jws.setAlgorithmHeaderValue(String.valueOf(jwsHeader.getAlgorithm()));

        // Setting headers
        jws.setKeyIdHeaderValue(jwsHeader.getKeyID());
        jws.setCriticalHeaderNames(jwsHeader.getCriticalParams().toArray(new String[0]));

        if (b64ValueObject != null) {
          jws.getHeaders().setObjectHeaderValue(B64_CLAIM_KEY, b64Value);
        }

        for (Map.Entry<String, Object> entry : jwsHeader.getCustomParams().entrySet()) {
          jws.getHeaders().setObjectHeaderValue(entry.getKey(), entry.getValue());
        }

        // Set the signing key on the JWS
        jws.setKey(key);

        // Sign the JWS and produce the detached JWS representation, which
        // is a string consisting of two dots ('.') separated base64url-encoded
        // parts in the form Header..Signature
        return jws.getDetachedContentCompactSerialization();
      }

    } catch (ParseException | KeyStoreException e) {
      log.error("Error occurred while reading the KeyStore file", e);
    } catch (NoSuchAlgorithmException | JoseException e) {
      log.error("Error occurred while signing", e);
    } catch (UnrecoverableEntryException e) {
      log.error("Error occurred while retrieving the cert key", e);
    } catch (TestFrameworkException e) {
      log.error("Error occurred while reading the certificate thumb print", e);
    }

    return " ";
  }

  /**
   * Get Mapped JWS Algorithm.
   *
   * @param algorithm configured algorithm in the test-config
   * @return Mapped JWS Algorithm for the configured string
   * @throws NoSuchAlgorithmException when algorithm configured is not supported
   */
  public static JWSAlgorithm getMappedJwsAlgorithm(String algorithm)
          throws NoSuchAlgorithmException {

    if ("RS256".equalsIgnoreCase(algorithm)) {
      return JWSAlgorithm.RS256;
    } else if ("RS384".equalsIgnoreCase(algorithm)) {
      return JWSAlgorithm.RS384;
    } else if ("RS512".equalsIgnoreCase(algorithm)) {
      return JWSAlgorithm.RS512;
    } else if ("PS256".equalsIgnoreCase(algorithm)) {
      return JWSAlgorithm.PS256;
    } else {
      log.error("signing Algorithm configured in the test-config is not supported");
      throw new NoSuchAlgorithmException("signing Algorithm configured "
              + "in the test-config is not supported");
    }
  }

  /**
   * Get the certificate from the KeyStore.
   *
   * @return The Certificate
   */
  public static Certificate getCertificateFromKeyStore() {

    try {
      KeyStore keyStore = TestUtil.getApplicationKeyStore();
      KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)
              keyStore.getEntry(AppConfigReader.getApplicationKeystoreAlias(),
                      new KeyStore.PasswordProtection(AppConfigReader.
                              getApplicationKeystorePassword().toCharArray()));
      Certificate certificate = pkEntry.getCertificate();
      return certificate;
    } catch (TestFrameworkException e) {
      log.error("Error occurred while loading the KeyStore ", e);
    } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException e) {
      log.error("Error occurred while retrieving values from KeyStore ", e);
    }
    return null;
  }

  /**
   * Returns the key id of the Application certificate.
   *
   * @return keyId
   */
  public static String getApplicationCertificateKeyId() {

    X509Certificate certificate = (X509Certificate) Objects
            .requireNonNull(getCertificateFromKeyStore());

    try {
      RSAKey rsaKey = RSAKey.parse(certificate);

      RSAKey key = new RSAKey.Builder(rsaKey)
              .keyUse(KeyUse.ENCRYPTION)
              .keyIDFromThumbprint()
              .build();
      return key.getKeyID();
    } catch (JOSEException e) {
      log.error("Error occurred while parsing Certificate ", e);
    }

    return null;
  }

  /**
   * Returns the subject DN for the application certificate.
   *
   * @return subject DN
   */
  public static String getApplicationCertificateSubjectDn() {

    X509Certificate certificate = (X509Certificate) getCertificateFromKeyStore();

    if (certificate != null && certificate.getSubjectDN() != null) {
      return certificate.getSubjectDN().getName();
    }

    return null;
  }

  /**
   * get kid from certificate in JWKs endpoint.
   *
   * @param certificate certificate
   * @return x5t
   */
  public static String getKidOfJwksCertificate(X509Certificate certificate) {

    String x5t = "";
    RSAPublicKey rsaPublicKey = (RSAPublicKey) certificate.getPublicKey();
    JWK jwk = new RSAKey.Builder(rsaPublicKey).keyID(UUID.randomUUID().toString()).build();
    try {
      x5t = jwk.computeThumbprint("SHA-1").toString();
    } catch (JOSEException e) {
      log.error("Error while computing thumbprint", e);
    }
    return x5t;
  }

  /**
   * Get PEM encoded string of application certificate.
   *
   * @return encoded pem file content
   */
  public static String getPemEncodedString() throws CertificateEncodingException {

    StringBuilder certificateBuilder = new StringBuilder();
    BASE64Encoder encoder = new BASE64Encoder();
    certificateBuilder.append(X509Factory.BEGIN_CERT);
    certificateBuilder.append(encoder.encodeBuffer(Objects
            .requireNonNull(getCertificateFromKeyStore()).getEncoded()));
    certificateBuilder.append(X509Factory.END_CERT);

    return certificateBuilder.toString().replaceAll("\n", "");
  }

  /**
   * Get decoded URL.
   *
   * @param authUrl authorisation url
   * @return encoded url
   * @throws UnsupportedEncodingException exception
   */
  public static String getDecodedUrl(String authUrl) throws UnsupportedEncodingException {

    return URLDecoder.decode(authUrl.split("&")[1].split("=")[1], "UTF8");
  }

  /**
   * Get Public Key From the Specified Keystore.
   *
   * @param keystoreLocation keystore location
   * @param keystorePassword keystore password
   * @param keystoreAlias    alias
   * @return public key
   * @throws TestFrameworkException exception
   */
  public static String getPublicKeyFromKeyStore(String keystoreLocation, String keystorePassword,
                                                String keystoreAlias) throws TestFrameworkException {

    try (InputStream inputStream = new FileInputStream(keystoreLocation)) {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(inputStream, keystorePassword.toCharArray());

      // Get certificate of public key
      Certificate cert = keyStore.getCertificate(keystoreAlias);

      // Get public key
      return Base64.getEncoder().encodeToString(cert.getPublicKey().getEncoded());
    } catch (IOException e) {
      throw new TestFrameworkException("Failed to load Keystore file from the location", e);
    } catch (CertificateException e) {
      throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
    } catch (NoSuchAlgorithmException | KeyStoreException e) {
      throw new TestFrameworkException("Error occurred while retrieving values from KeyStore ", e);
    }
  }

  /**
   * Get Public Key from Transport Keystore.
   *
   * @return public key
   * @throws TestFrameworkException exception
   */
  public static String getPublicKeyFromTransportKeyStore() throws TestFrameworkException {

    try (InputStream inputStream = new FileInputStream(
            AppConfigReader.getTransportKeystoreLocation())) {
      KeyStore keyStore = KeyStore.getInstance("JKS");

      String keystorePassword = AppConfigReader.getTransportKeystorePassword();
      keyStore.load(inputStream, keystorePassword.toCharArray());

      String keystoreAlias = AppConfigReader.getTransportKeystoreAlias();
      // Get certificate of public key
      Certificate cert = keyStore.getCertificate(keystoreAlias);

      // Get public key
      return Base64.getEncoder().encodeToString(cert.getEncoded());

    } catch (IOException e) {
      throw new TestFrameworkException("Failed to load Keystore file from the location", e);
    } catch (CertificateException e) {
      throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
    } catch (NoSuchAlgorithmException | KeyStoreException e) {
      throw new TestFrameworkException("Error occurred while retrieving values from KeyStore ", e);
    }
  }

  /**
   * Generate a sign JWT for request object.
   * @param claims claims
   * @param signingAlg signing algorithm
   * @return signed JWT
   */
  public static String getSignedRequestObject(String claims, String signingAlg) throws TestFrameworkException {

    try {
      KeyStore keyStore = getApplicationKeyStore();
      Certificate certificate = getCertificateFromKeyStore();
      JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(signingAlg)).type(JOSEObjectType.JWT).
              keyID(TestUtil.getJwkThumbPrint(certificate)).build();
      Payload payload = new Payload(claims);

      Key signingKey;
      signingKey = keyStore.getKey(AppConfigReader.getApplicationKeystoreAlias(),
              AppConfigReader.getApplicationKeystorePassword().toCharArray());
      JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);

      Security.addProvider(new BouncyCastleProvider());
      JWSObject jwsObject = new JWSObject(header, new Payload(payload.toString()));
      jwsObject.sign(signer);
      return jwsObject.serialize();

    } catch (UnrecoverableKeyException e) {
      throw new TestFrameworkException("Failed to recover the Key", e);
    } catch (NoSuchAlgorithmException e) {
      throw new TestFrameworkException("Failed to identify the Algorithm ", e);
    } catch (KeyStoreException e) {
      throw new TestFrameworkException("Failed to initialize the Keystore ", e);
    } catch (JOSEException e) {
      throw new TestFrameworkException("Failed to sign the object ", e);
    }
  }

  /**
   * Generate a sign JWT for request object with defined certificates.
   * @param claims claims
   * @param signingAlg signing algorithm
   * @param appKeystoreLocation Application Keystore Location
   * @param appKeystorePassword Application Keystore Password
   * @param appKeystoreAlias Application Keystore Alias
   * @return signed JWT
   */
  public static String getSignedRequestObjectWithDefinedCert(String claims, String signingAlg, String appKeystoreLocation,
                                                             String appKeystorePassword, String appKeystoreAlias)
          throws TestFrameworkException {
    try {
      InputStream inputStream = new FileInputStream(appKeystoreLocation);
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(inputStream, appKeystorePassword.toCharArray());

      KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(appKeystoreAlias,
              new KeyStore.PasswordProtection(appKeystorePassword.toCharArray()));
      Certificate certificate = pkEntry.getCertificate();

      JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(signingAlg)).
              keyID(TestUtil.getJwkThumbPrint(certificate)).build();
      Payload payload = new Payload(claims);

      Key signingKey;
      signingKey = keyStore.getKey(appKeystoreAlias, appKeystorePassword.toCharArray());
      JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);

      Security.addProvider(new BouncyCastleProvider());
      JWSObject jwsObject = new JWSObject(header, new Payload(payload.toString()));
      jwsObject.sign(signer);
      return jwsObject.serialize();

    } catch (IOException e) {
      throw new TestFrameworkException("Failed to load Keystore file from the location", e);
    } catch (CertificateException e) {
      throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
    } catch (NoSuchAlgorithmException e) {
      throw new TestFrameworkException("Failed to identify the Algorithm ", e);
    } catch (KeyStoreException e) {
      throw new TestFrameworkException("Failed to initialize the Keystore ", e);
    } catch (UnrecoverableEntryException e) {
      throw new TestFrameworkException("Error occurred while retrieving values from KeyStore ", e);
    } catch (JOSEException e) {
      throw new TestFrameworkException("Failed to sign the object ", e);
    }
  }

  public static Map<String, String> getJwtTokenHeader(String jwtToken) {
    String[] chunks = jwtToken.split("\\.");
    Base64.Decoder decoder = Base64.getDecoder();
    String header = new String(decoder.decode(chunks[0]));

    Map<String, String> mapHeader = new HashMap<String, String>();
    String[] partsHeader = header.replaceAll("[{}]|\"","").split(",");
    for(String part : partsHeader){
      String[] keyValue = part.split(":");
      String key = keyValue[0].trim();
      String value = keyValue[1].trim();

      mapHeader.put(key, value);
    }
    return mapHeader;
  }

  public static Map<String, String> getJwtTokenPayload(String jwtToken) {

    String[] chunks = jwtToken.split("\\.");
    Base64.Decoder decoder = Base64.getDecoder();
    String payload = new String(decoder.decode(chunks[1]));

    Map<String, String> mapPayload = new HashMap<String, String>();
    String[] partsPayload = payload.replaceAll("[{}]|\"","").split(",");
    for(String part : partsPayload){
      String[] keyValue = part.split(":");
      String key = keyValue[0].trim();
      String value = keyValue[1].trim();

      if(keyValue.length >= 3){
        value = keyValue[1].trim() + ":" + keyValue[2].trim();
      }

      mapPayload.put(key, value);
    }
    return mapPayload;
  }

  /**
   * Update XML nodes in test-config.xml file.
   *
   * @param xmlFile   path to test-config.xml file
   * @param parentNode Parent Node of the corresponding child node
   * @param childNode Child Node which need to update
   * @param value value need to be wriiten to the child node
   * @param tppNumber index of tpp
   */
  public static void writeXMLContent(String xmlFile, String parentNode, String childNode,
                                     String value, int tppNumber) {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = null;
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(xmlFile);
      NodeList parentnode = document.getElementsByTagName(parentNode);
      Element nodeElement = (Element) parentnode.item(tppNumber);
      // Update value of status tag
      Element statusTag = (Element) nodeElement.getElementsByTagName(childNode).item(0);
      statusTag.setTextContent(String.valueOf(value));

      saveXMLContent(document, xmlFile);

    } catch (ParserConfigurationException e) {
      log.error("Error while creating a new instance of a DocumentBuilder.", e);
    } catch (SAXException e) {
      log.error("Error while parsing the content of the given URI as an XML document.", e);
    } catch (IOException e) {
      log.error("Failed or interrupted I/O operations.", e);
    }

  }

  /**
   * Save the updated test-config,xml.
   *
   * @param document XML document
   * @param xmlFile  path to test-config.xml file
   */
  private static void saveXMLContent(Document document, String xmlFile) {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DOMSource domSource = new DOMSource(document);
      StreamResult streamResult = new StreamResult(xmlFile);
      transformer.transform(domSource, streamResult);

    } catch (TransformerConfigurationException e) {
      log.error("Failed to create a Transformer instance", e);
    } catch (TransformerException e) {
      log.error("Error while transforming the XML Source to a Result.", e);
    }
  }
}
