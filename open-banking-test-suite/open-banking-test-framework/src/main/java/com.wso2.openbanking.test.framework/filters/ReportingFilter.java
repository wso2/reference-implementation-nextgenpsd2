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

package com.wso2.openbanking.test.framework.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.internal.print.RequestPrinter;
import io.restassured.internal.print.ResponsePrinter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.testng.Reporter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Reporting filter for RestAssured.
 */
public class ReportingFilter implements Filter {

  private static final String heading = "<h3>%s</h3><";

  private final LogDetail logDetail;

  public ReportingFilter() {

    logDetail = LogDetail.ALL;
  }

  @Override
  public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res,
                         FilterContext ctx) {

    OutputStream outputStream = new ByteArrayOutputStream();
    RequestPrinter.print(req, req.getMethod(), req.getURI(), logDetail,
        new PrintStream(outputStream), true);

    Reporter.log(String.format(heading, "Request"));

    try {
      String raw = ((ByteArrayOutputStream) outputStream).toString("UTF-8");
      Reporter.log(formatOutput(raw));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported Char type");
    }

    Response response = ctx.next(req, res);

    outputStream = new ByteArrayOutputStream();
    ResponsePrinter.print(response, response.body(), new PrintStream(outputStream),
        LogDetail.ALL, true);

    Reporter.log(String.format(heading, "Response"));

    try {
      String raw = ((ByteArrayOutputStream) outputStream).toString("UTF-8");
      Reporter.log(formatOutput(raw));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported Char type");
    }

    return response;
  }

  private String formatOutput(String raw) {

    return raw
        .replaceAll(" ", "&nbsp;")
        .replaceAll("(\r\n|\n)", "<br />");
  }
}
