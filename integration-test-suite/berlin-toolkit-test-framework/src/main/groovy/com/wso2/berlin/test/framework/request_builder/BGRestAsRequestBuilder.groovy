/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework.request_builder

import com.wso2.berlin.test.framework.configuration.BGConfigurationService
import com.wso2.berlin.test.framework.constant.BerlinConstants
import com.wso2.berlin.test.framework.utility.BerlinTestUtil
import com.wso2.bfsi.test.framework.exception.TestFrameworkException
import com.wso2.openbanking.test.framework.utility.RestAsRequestBuilder
import io.restassured.RestAssured
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.apache.http.conn.ssl.SSLSocketFactory

/**
 * BG Class for provide Basic Rest-assured Request Objects
 */
class BGRestAsRequestBuilder extends RestAsRequestBuilder {

    private static BGConfigurationService bgConfiguration = new BGConfigurationService()

    /**
     * Get Base Request Specification.
     *
     * @return request specification.
     */
    static RequestSpecification buildRequest() throws TestFrameworkException {

        if (bgConfiguration.getAppTransportMLTSEnable()) {
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
