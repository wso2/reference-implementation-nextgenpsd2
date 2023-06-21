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

package com.wso2.berlin.test.framework

import com.wso2.berlin.test.framework.configuration.AppConfigReader
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.berlin.test.framework.utility.BerlinTestUtil
import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.apache.http.conn.ssl.SSLSocketFactory

class TestSuite {

    /**
     * Get Base Request Specification.
     *
     * @return request specification.
     */
     static RequestSpecification buildRequest() throws TestFrameworkException {

        if (AppConfigReader.isMTLSEnabled()) {
            RestAssuredConfig config = null;
            SSLSocketFactory sslSocketFactory = BerlinTestUtil.getSslSocketFactory();
            if (sslSocketFactory != null) {
                config = RestAssuredConfig.newConfig().sslConfig(RestAssured.config()
                        .getSSLConfig()
                        .sslSocketFactory(BerlinTestUtil.getSslSocketFactory()));
            } else {
                throw new TestFrameworkException("Unable to retrieve the SSL socket factory");
            }
            return RestAssured.given()
                    .config(config.encoderConfig(EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(BerlinConstants.CONTENT_TYPE_APPLICATION_JWT, ContentType.TEXT)))
                    .urlEncodingEnabled(true);
        } else {
            // Use relaxed HTTPS validation if MTLS is disabled.
            return RestAssured.given()
                    .relaxedHTTPSValidation()
                    .urlEncodingEnabled(true);
        }
    }
}
