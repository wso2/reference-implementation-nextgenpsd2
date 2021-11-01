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

package com.wso2.openbanking.test.framework;

import com.wso2.openbanking.test.framework.exception.TestFrameworkException;
import com.wso2.openbanking.test.framework.util.AppConfigReader;
import com.wso2.openbanking.test.framework.util.TestConstants;
import com.wso2.openbanking.test.framework.util.TestUtil;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;


/**
 * Test Suite Initializer.
 */
public class TestSuite {

  private static final Log log = LogFactory.getLog(TestSuite.class);

  /**
   * Initialize Test Framework.
   */
  public static void init() {

    List<Filter> filterList = new ArrayList<>();
    filterList.add(new RequestLoggingFilter(System.out));
    filterList.add(new ResponseLoggingFilter(System.out));
    Security.addProvider(new BouncyCastleProvider());

    RestAssured.filters(filterList);
  }

  /**
   * Get Base Request Specification.
   *
   * @return request specification.
   */
  public static RequestSpecification buildRequest() throws TestFrameworkException {

    if (AppConfigReader.isMTLSEnabled()) {
      RestAssuredConfig config = null;
      SSLSocketFactory sslSocketFactory = TestUtil.getSslSocketFactory();
      if (sslSocketFactory != null) {
        config = RestAssuredConfig.newConfig().sslConfig(RestAssured.config()
            .getSSLConfig()
            .sslSocketFactory(TestUtil.getSslSocketFactory()));
      } else {
        throw new TestFrameworkException("Unable to retrieve the SSL socket factory");
      }
      return RestAssured.given()
          .config(config.encoderConfig(EncoderConfig.encoderConfig()
              .encodeContentTypeAs(TestConstants.CONTENT_TYPE_APPLICATION_JWT, ContentType.TEXT)))
          .urlEncodingEnabled(true);
    } else {
      // Use relaxed HTTPS validation if MTLS is disabled.
      return RestAssured.given()
          .relaxedHTTPSValidation()
          .urlEncodingEnabled(true);
    }
  }

  /**
   * Get Base Request specification without MTLS.
   *
   * @return request specification.
   */
  public static RequestSpecification buildBasicRequestWithoutTlsContext() {
    return RestAssured.given()
        .relaxedHTTPSValidation()
        .urlEncodingEnabled(true);
  }

  /**
   * Get Base Request Specification with defined keystore.
   * @param keystoreLocation keystore file path.
   * @param keystorePassword keystore password.
   * @return request specification.
   * @throws TestFrameworkException exception.
   */
  public static RequestSpecification buildRequest(String keystoreLocation, String keystorePassword)
          throws TestFrameworkException {

      RestAssuredConfig config = null;
      SSLSocketFactory sslSocketFactory = TestUtil.getSslSocketFactory(keystoreLocation, keystorePassword);
      if (sslSocketFactory != null) {
        config = RestAssuredConfig.newConfig().sslConfig(RestAssured.config()
                .getSSLConfig()
                .sslSocketFactory(TestUtil.getSslSocketFactory(keystoreLocation, keystorePassword)));
      } else {
        throw new TestFrameworkException("Unable to retrieve the SSL socket factory");
      }
      return RestAssured.given()
              .config(config.encoderConfig(EncoderConfig.encoderConfig()
                      .encodeContentTypeAs(TestConstants.CONTENT_TYPE_APPLICATION_JWT, ContentType.TEXT)))
              .urlEncodingEnabled(true);
  }
}
